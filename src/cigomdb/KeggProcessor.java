/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import database.Transacciones;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alejandro
 */
public class KeggProcessor {

    private Transacciones transacciones;

    public KeggProcessor(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    /**
     *
     * @param inputFile Archivo con la relacion gen_id KO
     * @param group genoma|metagenoma textual
     * @param groupID el id del genoma o metagenoma
     * @param outputFile el archivo donde se escribe el sl para actuaalizar los
     * genes
     * @param toFile si es tru escribe directo en la BD y no crea archivo
     */
    public void procesaKOList(String inputFile, String group, String groupID, String outputFile, boolean toFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String linea;
            int numLinea = 0;
            FileWriter writer = null;
            if (toFile) {
                writer = new FileWriter(outputFile);
            }
            while ((linea = reader.readLine()) != null) {
                numLinea++;
                if (!linea.startsWith("#")) {
                    String partes[] = linea.split("\t");
                    if (partes.length >= 2) {
                        String mapID = partes[0].trim();
                        String ko = partes[1].trim();
                        mapID = mapID.indexOf("|") != -1 ? mapID.substring(0, mapID.indexOf("|")) : mapID;
                        String genID = transacciones.getGeneIDByMapID(group, groupID, mapID);
                        if (genID.length() > 1) {
                            if (toFile) {
                                writer.write("UPDATE gen SET idKO= '" + ko + "' WHERE gen_id = '" + genID + "';\n");
                            } else {
                                if (!transacciones.updateGenKO(mapID, linea)) {
                                    System.err.println("Error actualizando gen: " + "UPDATE gen SET idKO= '" + ko + "' WHERE gen_id = '" + genID + "'");
                                }
                            }
                        }
                    }
                }
            }
            if (toFile) {
                writer.close();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(KeggProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(KeggProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
