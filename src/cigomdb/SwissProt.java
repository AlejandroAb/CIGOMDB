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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alejandro
 */
public class SwissProt {

    private Transacciones transacciones;

    public SwissProt(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public SwissProt() {
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
