/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import bobjects.GOObj;
import dao.CogDAO;
import dao.GoDAO;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.StringUtils;

/**
 *
 * @author Alejandro
 */
public class OBOProcessor {

    private Transacciones transacciones;

    public OBOProcessor(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    /**
     * Este método procesa archivos OBO, en un principio está diseñado para
     * parsear go.obo files, sin embargo quedó bastante genérico pero hay que
     * revisar detalles...
     *
     * @param inputFile archivo OBO a parsear
     * @param URI al ser OBO Que tipo de ontologia se va a parsear, cada
     * ontología dentro de OBO tiene su propio URI como ser GO para Gene
     * Ontology o ENVO para Environmentl Ontology
     * @param IRI es el Internationalized Resource Identifier es el ID (URL)
     * única para cada termino de este OBO, se espera un prefijo, el cual
     * concatenado al id del OBO se la URL euivalente al IRI
     * @param toFile if true to file, else directo aa la BD
     * @param outFile si el anterior es true si o si tiene que traer el archivo
     * de salida
     * @return
     */
    public String processOBOFile(String inputFile, String URI, String IRI, boolean toFile, String outFile) {
        BufferedReader reader = null;
        String log = "";
        try {

            StringUtils sUtils = new StringUtils();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1"));
            String linea;
            GOObj go = new GOObj();
            GoDAO goDAO = new GoDAO(transacciones);
            // CogDAO cogDAO = new CogDAO(transacciones);
            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("[Term]")) {
                    go.reset();
                    while (((linea = reader.readLine()) != null) && linea.trim().length() > 1) {
                        if (!linea.startsWith("!")) {
                            if (linea.startsWith("id:")) {
                                go.setId(linea.substring(linea.lastIndexOf(":") + 1).trim());
                            } else if (linea.startsWith("name:")) {
                                go.setName(sUtils.scapeSQL(linea.substring(5).trim()));
                            } else if (linea.startsWith("namespace:")) {
                                go.setNamespace(linea.substring(10).trim());
                            } else if (linea.startsWith("def:")) {
                                go.setDefinition(sUtils.scapeSQL((linea.substring(linea.indexOf("\"") + 1, linea.lastIndexOf("\"") - 1).trim())));
                            } else if (linea.startsWith("is_a:")) {
                                //casi todos los is_a estan definidos asi 
                                //is_a: GO:0044428 ! nuclear part
                                //nosotros solo queremos guardar el ID
                                int indxComent = linea.indexOf("!");
                                if (indxComent == -1) {
                                    indxComent = linea.length() - 1;
                                }
                                int indxURI = linea.indexOf(URI + ":");
                                if (indxURI == -1) {
                                    indxURI = linea.indexOf(":");
                                } else {
                                    indxURI += (URI + ":").length();
                                }
                                go.appendIs_a(linea.substring(indxURI, indxComent).trim());
                            } else if (linea.startsWith("relationship:")) {
                                // aca no nos import el URI leemos la relacion y todo el ID
                                //relationship: negatively_regulates GO:0007231 ! osmosensory signaling pathway
                                int indxComent = linea.indexOf("!");
                                if (indxComent == -1) {
                                    //no tiene comentarios
                                    indxComent = linea.length() - 1;
                                }
                                go.appendRelationship(linea.substring(linea.indexOf(":") + 1, indxComent - 1).trim());
                            } else if (linea.startsWith("is_obsolete:")) {
                                if (linea.contains("true")) {
                                    go.setIs_obsolete(true);
                                }//false default del obj
                            } else if (linea.startsWith("consider:")) {
                                //consider: GO:0006342
                                int indxComent = linea.indexOf("!");
                                if (indxComent == -1) {
                                    //no tiene comentarios
                                    indxComent = linea.length() - 1;
                                }
                                //se guarda toda la linea por que es para el campo replaced by 
                                //cuando no hay replaced by....asi queda como que es una consideracion
                                go.appendReplace_by(linea.substring(0, indxComent).trim());

                            } else if (linea.startsWith("replaced_by:")) {
                                //replaced_by: GO:0003697
                                int indxComent = linea.indexOf("!");
                                if (indxComent == -1) {
                                    //no tiene comentarios
                                    indxComent = linea.length() - 1;
                                }
                                go.setReplace_by(linea.substring(linea.lastIndexOf(":") + 1, indxComent - 1));
                            } else if (linea.startsWith("comment:")) {
                                go.setCommentario(sUtils.scapeSQL(linea.substring(linea.indexOf(":") + 1).trim()));
                            }
                        }
                    }
                    //http://purl.obolibrary.org/obo/GO_0006385
                    go.setUrl(IRI + go.getId());
                    log += goDAO.insertaGO(go, toFile, outFile, true);
                }
            }

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(OBOProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OBOProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OBOProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
                return log;
            } catch (IOException ex) {
                Logger.getLogger(OBOProcessor.class.getName()).log(Level.SEVERE, null, ex);
                return log;
            }
        }

    }
}
