/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import bobjects.SwissProtObj;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.StringUtils;

/**
 *
 * @author Alejandro
 */
public class SwissProt {

    private Transacciones transacciones;
    private static final String UNIPROT_SERVER = "http://www.uniprot.org/uniprot/?query=entry:";

    public SwissProt(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public SwissProt() {
    }

    public String loadSwissProtFromWEB(boolean debug) {
        //http://www.uniprot.org/uniprot/?query=entry:A0A0F6NZX8_FRG3V&columns=id,entry name,protein names,organism-id,lineage-id,ec,comment(PATHWAY),comment(ALTERNATIVE%20PRODUCTS),comment(TEMPERATURE%20DEPENDENCE),comment(PH%20DEPENDENCE),comment(FUNCTION)&format=tab&mail:daleabdala@hotmail.com

        ArrayList<ArrayList> allProts = transacciones.getAllDistinctPredictedSwissProt();
        for (ArrayList<String> prot : allProts) {
            String prot_id = prot.get(0).trim();
            if (transacciones.validaUniprotID(prot_id)) {
                //SI EXISTE PERO HAY QUE VER SI TIENE TODOS LOS DATOS ACC- 1 -> viene de anotación
                if (transacciones.validaUniprotAcc(prot_id)) {
                    SwissProtObj swissObj = createSwissObjectFromWeb(prot_id);
                    if (swissObj != null) {
                        transacciones.insertaQuery(swissObj.toSQLUpdateString());
                    }
                }
            } else {
                SwissProtObj swissObj = createSwissObjectFromWeb(prot_id);
                if (swissObj != null) {
                    transacciones.insertaQuery(swissObj.toSQLString());
                }

            }
        }
        return "";
    }

    public SwissProtObj createSwissObjectFromWeb(String id) {
        SwissProtObj swissObj = new SwissProtObj(id);
        //StringBuilder locationBuilder = new StringBuilder(UNIPROT_SERVER + id + "&columns=id,genes%28PREFERRED%29,protein names,organism-id,ec,comment%28PATHWAY%29,comment%28ALTERNATIVE%20PRODUCTS%29,comment%28TEMPERATURE%20DEPENDENCE%29,comment%28PH%20DEPENDENCE%29&format=tab");
        //http://www.uniprot.org/uniprot/?query=entry:A0A0F6NZX8_FRG3V&columns=id,entry name,protein names,organism-id,lineage-id,ec,comment(PATHWAY),comment(ALTERNATIVE%20PRODUCTS),comment(TEMPERATURE%20DEPENDENCE),comment(PH%20DEPENDENCE),comment(FUNCTION)&format=tab&mail:daleabdala@hotmail.com
        StringBuilder locationBuilder = new StringBuilder(UNIPROT_SERVER + id + "&columns=id,genes(PREFERRED),protein names,organism-id,ec,comment(PATHWAY),comment(ALTERNATIVE PRODUCTS),comment(TEMPERATURE DEPENDENCE),comment(PH DEPENDENCE)&format=tab");
        StringUtils sUtils = new StringUtils();
        try {
            //   String encodedURL = java.net.URLEncoder.encode(locationBuilder.toString(), "UTF-8");
            String encodedURL = locationBuilder.toString();
           // URI uri = new URI("http", null, "www.uniprot.org/uniprot/", -1, null, "query=entry:"+id+"&columns=id,entry name,protein names,organism-id,lineage-id,ec,comment(PATHWAY),comment(ALTERNATIVE PRODUCTS),comment(TEMPERATURE DEPENDENCE),comment(PH DEPENDENCE),comment(FUNCTION)&format=tab", null);
            URI uri = new URI("http", null, "www.uniprot.org/uniprot/", -1, null, "query=entry:"+id+"&columns=id,genes(PREFERRED),protein names,organism-id,ec,comment(PATHWAY),comment(ALTERNATIVE PRODUCTS),comment(TEMPERATURE DEPENDENCE),comment(PH DEPENDENCE)&format=tab", null);

            URL url = uri.toURL();
            //URL url = new URL(encodedURL);
            System.out.println("Submitting...: " + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            conn.setDoInput(true);
            conn.connect();
            int status = conn.getResponseCode();
            int intentos = 0;
            while (true && intentos < 10) {
                intentos++;
                int wait = 0;
                String header = conn.getHeaderField("Retry-After");
                if (header != null) {
                    wait = Integer.valueOf(header);
                }
                if (wait == 0) {
                    break;
                }
                System.err.println("Waiting (" + wait + ")...");
                conn.disconnect();
                Thread.sleep(wait * 1000);
                conn = (HttpURLConnection) new URL(encodedURL).openConnection();
                conn.setDoInput(true);
                conn.connect();
                status = conn.getResponseCode();
            }
            if (status == HttpURLConnection.HTTP_OK) {
                // LOG.info("Got a OK reply");
                InputStream reader = conn.getInputStream();
                URLConnection.guessContentTypeFromStream(reader);
                StringBuilder builder = new StringBuilder();
                int a = 0;
                while ((a = reader.read()) != -1) {
                    builder.append((char) a);
                }
                //System.out.println(builder.toString());

                String lines[] = builder.toString().split("\n");
                if (lines.length > 1) {
                    String fields[] = lines[1].split("\t", -1);
                    int i = 0;
                    //0Entry	1Gene names  (primary )	2Protein names	3Organism ID	
                    //4EC number  5Pathway  6 Alternative products (isoforms)  7 Temperature dependence	8 pH dependence
                    for (String prop : fields) {
                        if (i == 0 && prop.length() > 1) {
                            swissObj.setUniprotACC(prop);
                        } else if (i == 1 && prop.length() > 1) {
                            swissObj.setGen_name(sUtils.scapeSQL(prop));
                        } else if (i == 2 && prop.length() > 1) {
                            swissObj.setUniprotName(sUtils.scapeSQL(prop));
                        } else if (i == 3 && prop.length() > 1) {
                            swissObj.setTaxID(prop);
                        } else if (i == 4 && prop.length() > 1) {
                            swissObj.setEc(prop);
                        } else if (i == 5 && prop.length() > 1) {
                            swissObj.setPathway(sUtils.scapeSQL(prop));
                        } else if (i == 6 && prop.length() > 1) {
                            swissObj.setAlternative_product(sUtils.scapeSQL(prop));
                        } else if (i == 7 && prop.length() > 1) {
                            swissObj.setDependencia_temp(sUtils.scapeSQL(prop));
                        } else if (i == 8 && prop.length() > 1) {
                            swissObj.setDependencia_ph(prop);
                        }
                        i++;
                    }

                } else {
                    System.err.println("Output incomplete " + builder.toString());
                    return null;
                }

            } else {
                System.err.println("Failed, got " + conn.getResponseMessage() + " for "
                        + locationBuilder.toString());
                return null;
            }
            conn.disconnect();
        } catch (MalformedURLException ex) {
            Logger.getLogger(SwissProt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SwissProt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SwissProt.class.getName()).log(Level.SEVERE, null, ex);

        } catch (URISyntaxException ex) {
            Logger.getLogger(SwissProt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return swissObj;
    }

    /**
     * En un principio, este método parseaba el archivo xml con toda la
     * referencia de uniprot
     * ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/uniref/uniref100/uniref100.xml.gz
     * y anotaba todos los genes, pero ahora este método es usado como post
     * procesamiento y una vez anotadas las predicciones de uniprot en genes, se
     * corre este método el cual únicamente anota aquellas entradas para las
     * cuales exista un gen predicho
     *
     * @param xmlFile uniref100.xml
     * @param debug
     * @return
     */
    public String loadSwissProtFromXML(String xmlFile, boolean debug) {
        String log = "";
        BufferedReader reader = null;
        double lnum = 0;
        try {
            reader = new BufferedReader(new FileReader(xmlFile));
            String line;
            SwissProtObj sObjt = null;
            int elements = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                lnum++;
                //<entry id="UniRef100_Q197F3" updated="2012-11-28">
                if (line.startsWith("<entry")) {
                    if (sObjt != null) {
                        if (transacciones.validaGenUniprotID(sObjt.getUniprotID()) && !transacciones.validaUniprotID(sObjt.getUniprotID())) {//si no tiene laa proteina
                            boolean ok = transacciones.insertSwissProt(sObjt.getUniprotID(), sObjt.getUniprotACC(), sObjt.getTaxID(), sObjt.getUniprotName(), sObjt.getSequence(), sObjt.getSeqLength(), sObjt.getClusterId(), sObjt.getClusterName(), sObjt.getTaxID());
                            sObjt = null;
                            if (!ok) {
                                System.err.println("Error storing " + sObjt.getUniprotID() + " at line" + lnum);
                            } else {
                                elements++;
                            }
                        }
                    }
                    String clusterID = line.substring(line.indexOf("id=") + 4, line.indexOf("updated") - 2);
                    sObjt = new SwissProtObj();
                    sObjt.setClusterId(clusterID);
                    while ((line = reader.readLine()) != null && !line.equals("</entry>")) {
                        line = line.trim();
                        lnum++;
                        if (line.contains("<name>")) {
                            String clusterName = line.substring(line.indexOf("<name>") + 6, line.indexOf("</name>"));
                            sObjt.setClusterName(clusterName);
                        } else if (line.contains("common taxon ID")) {
                            String custerTax = line.substring(line.indexOf("value=") + 7, line.lastIndexOf("\""));
                            sObjt.setClusterTax(custerTax);
                        } else if (line.contains("<representativeMember>")) {
                            while ((line = reader.readLine().trim()) != null && !line.contains("</representativeMember>")) {
                                if (line.startsWith("<dbReference")) {
                                    String id = line.substring(line.indexOf("id=") + 4, line.lastIndexOf("\""));
                                    sObjt.setUniprotID(id);
                                } else if (line.contains("UniProtKB accession")) {
                                    String acc = line.substring(line.indexOf("value=") + 7, line.lastIndexOf("\""));
                                    sObjt.setUniprotACC(acc);
                                } else if (line.contains("NCBI taxonomy")) {
                                    String tax = line.substring(line.indexOf("value=") + 7, line.lastIndexOf("\""));
                                    sObjt.setTaxID(tax);
                                } else if (line.contains("protein name")) {
                                    String pName = line.substring(line.indexOf("value=") + 7, line.lastIndexOf("\""));
                                    sObjt.setUniprotName(pName);
                                    /*} else if (line.contains("length")) {
                                     String lng = line.substring(line.indexOf("value=") + 7, line.lastIndexOf("\""));
                                     try {
                                     sObjt.setSeqLength(Integer.parseInt(lng));
                                     } catch (NumberFormatException nfe) {
                                     sObjt.setSeqLength(-1);
                                     log += "Error seq_length at line" + lnum;
                                     if (debug) {
                                     System.out.println("Error seq_length at line" + lnum);
                                     }
                                     }
                                     */
                                } else if (line.contains("<sequence")) {
                                    String lng = line.substring(line.indexOf("length=") + 8, line.lastIndexOf("checksum") - 2);
                                    try {
                                        sObjt.setSeqLength(Integer.parseInt(lng));
                                    } catch (NumberFormatException nfe) {
                                        sObjt.setSeqLength(-1);
                                        log += "Error seq_length at line" + lnum;
                                        if (debug) {
                                            System.out.println("Error seq_length at line" + lnum);
                                        }
                                    }
                                    // String ltmp = reader.readLine().trim();
                                    String sec = "";
                                    while ((line = reader.readLine()) != null && !line.contains("</sequence>")) {
                                        line = line.trim();
                                        lnum++;
                                        sec += line;
                                    }
                                    sObjt.setSequence(sec);
                                    if (sec.length() != sObjt.getSeqLength()) {
                                        sObjt.setSeqLength(sec.length());
                                        System.err.println("Sequence length mismatch at: " + sObjt.getUniprotID() + " ~ line " + lnum);
                                    }
                                }
                            }
                        }

                    }
                }
            }
            if (sObjt != null) {
                if (transacciones.validaGenUniprotID(sObjt.getUniprotID()) && !transacciones.validaUniprotID(sObjt.getUniprotID())) {//si no tiene laa proteina
                    boolean ok = transacciones.insertSwissProt(sObjt.getUniprotID(), sObjt.getUniprotACC(), sObjt.getTaxID(), sObjt.getUniprotName(), sObjt.getSequence(), sObjt.getSeqLength(), sObjt.getClusterId(), sObjt.getClusterName(), sObjt.getClusterTax());
                    sObjt = null;
                    if (!ok) {
                        System.err.println("Error storing " + sObjt.getUniprotID() + " at line" + lnum);
                    } else {
                        elements++;
                    }
                }
            }
            log += "Elementos nuevos anotados = " + elements;
            return log;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SwissProt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SwissProt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(SwissProt.class.getName()).log(Level.SEVERE, null, ex);
            log += "Unexpected token or error at line " + lnum;
            if (debug) {
                System.out.println("Unexpected token or error at line " + lnum);
            }
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(SwissProt.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return log;
    }
}
