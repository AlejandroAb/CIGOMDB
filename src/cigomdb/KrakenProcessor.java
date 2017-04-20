/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import bobjects.ArchivoObj;
import bobjects.Usuario;
import dao.ArchivoDAO;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.FileUtils;
import utils.MyDate;

/**
 *
 * @author Alejandro
 */
public class KrakenProcessor {

    private Transacciones transacciones;
    int nextIDArchivo = -1;
    HashMap<String, String> nodosObsoletos;

    public int getNextIDArchivo() {
        return nextIDArchivo;
    }

    public void setNextIDArchivo(int nextIDArchivo) {
        this.nextIDArchivo = nextIDArchivo;
    }

    public KrakenProcessor(Transacciones transacciones) {
        this.transacciones = transacciones;
        nodosObsoletos = new HashMap<>();
        nodosObsoletos.put("710686", "212767");
        nodosObsoletos.put("1380774", "93220");
    }

    /**
     * *
     * Este método se encarga de verificar si un nodo posiblemente obsoleto ya
     * tiene anotado su nodo actual
     *
     * @param tax_id el taxid a verificar
     * @return regresa el nuevo tax id o blank si no hay
     */
    public String verifyNCBINODE(String tax_id) {
        String current = nodosObsoletos.get(tax_id);
        if (current != null) {
            return current;
        } else {
            return "";
        }
    }

    /**
     * Este método se encarga de realizar la anotación para el archivo de
     * kraken.
     *
     * @param idMetagenoma el metagenoma para el cual se anota el arhivo de
     * kraken
     * @param file el archivo -out de kraken
     * @param writer el writer en caso de que sea la salida a archivo. Este
     * método no inicializa ni ciierra el writer!
     * @throws IOException
     */
    public void anotaArchivoKraken(int idMetagenoma, String file, FileWriter writer) throws IOException {
        File tmpFile = new File(file);
        ArchivoObj krakenFile = new ArchivoObj(nextIDArchivo);
        nextIDArchivo++;
        krakenFile.setTipoArchivo(ArchivoObj.TIPO_PRE);
        krakenFile.setNombre(file.substring(file.lastIndexOf("/") + 1));
        krakenFile.setPath(file.substring(0, file.indexOf("/") + 1));
        krakenFile.setDescription("Archivo de salida de Kraken. Predicción taxonómica mediante espectro de kámeros");
        int tmpID = nextIDArchivo;
        krakenFile.setExtension("out");
        MyDate date = new MyDate(tmpFile.lastModified());
        krakenFile.setDate(date);
        krakenFile.setSize(tmpFile.length());
        if (tmpFile.length() / 1048576 < 1000) {//si es menor a un Gb
            krakenFile.setChecksum(FileUtils.getMD5File(file));
        } else {
            krakenFile.setChecksum("TBD");
        }
        krakenFile.setAlcance("Grupo de bioinformática");
        krakenFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        krakenFile.setDerechos("Acceso limitado a miembros");
        krakenFile.setTags("Espectros de kameros, kraken, taxonomia");
        krakenFile.setTipo("Text");
        Usuario user = new Usuario(31);//ALES
        user.setAcciones("creator");
        user.setComentarios("Se encarga de ejecutar el programa Kraken el cual toma las secuencias crudas (fastq) y a partir de estas se genera el archivo con la clasificación taxonómica");
        krakenFile.addUser(user);
        Usuario user2 = new Usuario(9);//ALEXSF
        user2.setAcciones("contributor");
        user2.setComentarios("Investigador responsable de subproyecto");
        krakenFile.addUser(user2);
        if (writer != null) {
            writer.write(krakenFile.toNewSQLString() + ";\n");
            writer.write("INSERT INTO metagenoma_archivo VALUES(" + idMetagenoma + "," + krakenFile.getIdArchivo() + ");\n");
            for (String qUsuarios : krakenFile.archivoUsuariosToSQLString()) {
                writer.write(qUsuarios + ";\n");
            }
        } else {
            ArchivoDAO adao = new ArchivoDAO(transacciones);
            adao.insertaArchivo(krakenFile, false, "", true);
            transacciones.insertaArchivoMetagenoma("" + idMetagenoma, krakenFile.getIdArchivo());
            for (String qUsuarios : krakenFile.archivoUsuariosToSQLString()) {
                if (!transacciones.insertaQuery(qUsuarios)) {
                    System.err.println("Error insertando relación usuario-archivo: "
                            + idMetagenoma + "(idmetagenoma) - " + nextIDArchivo + "(idArchivo) - q: " + qUsuarios);
                }
            }
        }
    }

    /**
     * *
     * Este método se encarga de procesar la salida .out del programa Kraken
     *
     * @param idMetagenoma el id del metagenoma al cual esta relacionado la
     * salida de kraken
     * @param inputFile el archivo .out de kraken, entrada para nosotros
     * @param outFile el archivo de salida con la sentencia sql
     */
    public void processKrakenOut(int idMetagenoma, String inputFile, String outFile) {
        if (nextIDArchivo == -1) {
            nextIDArchivo = transacciones.getNextIDArchivos();
            if (nextIDArchivo == -1) {
                System.err.println("ERROR No se puede determinar el siguiente ID de archivo");
            }
        }
        NumberFormat formatter = new DecimalFormat("##.####");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String linea;
            int line = 0;
            HashMap<String, Integer> counts = new HashMap<>();
            boolean toFile = false;
            FileWriter writer = null;
            FileWriter writerObsoletes = new FileWriter(inputFile + ".obsoletes");
            if (outFile.length() > 2) {
                toFile = true;
                writer = new FileWriter(outFile);
            }
            anotaArchivoKraken(idMetagenoma, inputFile, writer);
            while ((linea = reader.readLine()) != null) {
                line++;
                if (linea.startsWith("C")) {
                    //0= C|U   1= SeqID   2=tax_id   3=seqLength 
                    //4= classif; taxid:kammers taxid:kammers
                    String fields[] = linea.split("\t");
                    if (fields.length < 5) {
                        System.err.println("Error en linea: " + line + " '" + linea + "'. Se esperaban 5 campos");
                    } else {
                        String classif[] = fields[4].split(" ");
                        int kmeros = 0;
                        int kmersOK = 0;
                        String hierarchyAssignedTaxID = transacciones.getHierarchy(fields[2]);
                        //si es posiblemente un nodo obsoleto
                        if (hierarchyAssignedTaxID.length() == 0) {
                            String tmpTaxID = verifyNCBINODE(fields[2]);
                            //si tiene un nuevo nodo se lo asigna, sino se queda como esta
                            fields[2] = tmpTaxID.length() > 0 ? tmpTaxID : fields[2];
                            hierarchyAssignedTaxID = transacciones.getHierarchy(fields[2]);
                        }
                        String hierarchy[] = hierarchyAssignedTaxID.split(",");
                        if (hierarchy[0].length() > 0) {
                            for (String asignacion : classif) {
                                String assign[] = asignacion.split(":");
                                if (assign.length < 2) {
                                    System.err.println("Error en linea: " + line + " '" + linea + "'. Parseando clasificación");
                                } else {
                                    String taxID = assign[0];
                                    String kmers = assign[1];
                                    int ks = 0;
                                    try {
                                        ks = Integer.parseInt(kmers);
                                        kmeros += ks;
                                    } catch (NumberFormatException nfe) {
                                        System.err.println("Error NFE kmers en Linea: " + line + " kmers = " + kmers);
                                    }
                                    boolean find = false;
                                    for (String tax : hierarchy) {
                                        //si el tax de los kmeros esta en el hierarchy o es el taxID asignado
                                        //0=uncluss 1=root 131567 = cellular organism
                                        if (!taxID.equals("0") && !taxID.equals("1") && !taxID.equals("A") && !taxID.equals("131567") && (taxID.equals(fields[2]) || taxID.equals(tax))) {
                                            kmersOK += ks;
                                            find = true;
                                            break;
                                        }
                                    }
                                    if (!find) {
                                        if (!taxID.equals("0") && !taxID.equals("1") && !taxID.equals("131567") && !taxID.equals("A")) {
                                            String hierarchySonTaxID = transacciones.getHierarchy(taxID);
                                            if (hierarchySonTaxID.length() == 0) {
                                                String tmpTaxID = verifyNCBINODE(taxID);
                                                //si tiene un nuevo nodo se lo asigna, sino se queda como esta
                                                taxID = tmpTaxID.length() > 0 ? tmpTaxID : taxID;
                                                hierarchyAssignedTaxID = transacciones.getHierarchy(taxID);
                                            }
                                            if (hierarchyAssignedTaxID.length() > 0) {
                                                String hierarchySon[] = hierarchyAssignedTaxID.split(",");

                                                //la otra opcion es que el taxID sea un hijo del taxID seleccionado (campo 3)
                                                //en ese caso también se cuenta como caso + y se suma pero hay que buscar en la jerarquía 
                                                //de dicho posible hijo                                            
                                                for (String taxSon : hierarchySon) {
                                                    if (taxSon.equals(fields[2])) {
                                                        kmersOK += ks;
                                                        break;
                                                    }
                                                }
                                            } else {
                                                System.err.println("Nodo Obsoleto: " + taxID);
                                            }
                                        }
                                    }
                                }
                            }
                            kmeros = kmeros == 0 ? 1 : kmeros;
                            if (counts.get(fields[2]) == null) {
                                counts.put(fields[2], 1);
                            } else {
                                Integer c = counts.get(fields[2]);
                                c++;
                                counts.put(fields[2], c);
                            }
                            double prc = ((double) kmersOK * 100) / kmeros;
                            //id analisis por default 2 poner en la BD kraken y sus params
                            String query = "INSERT INTO seq_metagenoma_classif (tax_id, idmetagenoma, idanalisis_clasificacion, raw_id, score, longitud) "
                                    + "VALUES(" + fields[2] + "," + idMetagenoma + ",2,'" + fields[1] + "'," + formatter.format(prc) + "," + fields[3] + ")";
                            if (toFile) {
                                writer.write(query + ";\n");
                            } else {
                                if (!transacciones.insertaQuery(query)) {
                                    System.err.println("");
                                }
                            }
                        } else {
                            //    String query = "INSERT INTO seq_metagenoma_classif (tax_id, idmetagenoma, idanalisis_clasificacion, raw_id, score, longitud) "
                            //         + "VALUES(" + fields[2] + "," + idMetagenoma + ",2,'" + fields[1] + "'," + -1 + "," + fields[3] + ")";

                            writerObsoletes.write(linea + "\n");
                            System.err.println("Nodo Obsoleto: " + fields[2]);
                        }

                    }
                }
            }
            writerObsoletes.close();
            if (toFile) {
                writer.close();
            }
            if (toFile) {
                FileWriter writerCounts = new FileWriter(outFile.substring(0, outFile.lastIndexOf(".")) + ".counts.sql");
                String idMuestra = transacciones.getIdMuestraByMetagenoma("" + idMetagenoma);
                for (String key : counts.keySet()) {
                    Integer c = counts.get(key);
                    writerCounts.write("INSERT INTO conteos_shotgun (idmetagenoma,tax_id, idMuestra, counts ) VALUES (" + idMetagenoma + "," + key + "," + idMuestra + "," + c + ");\n");
                }
                writerCounts.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KrakenProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(KrakenProcessor.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }

}
