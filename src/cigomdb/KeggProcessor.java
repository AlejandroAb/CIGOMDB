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
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.FileUtils;
import utils.MyDate;

/**
 *
 * @author Alejandro
 */
public class KeggProcessor {

    private Transacciones transacciones;
    private int nextIDArchivo = -1;
    public KeggProcessor(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public int getNextIDArchivo() {
        return nextIDArchivo;
    }

    public void setNextIDArchivo(int nextIDArchivo) {
        this.nextIDArchivo = nextIDArchivo;
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
            if (nextIDArchivo == -1) {
                nextIDArchivo = transacciones.getNextIDArchivos();
                if (nextIDArchivo == -1) {
                    System.err.println("ERROR No se puede determinar el siguiente ID de archivo");
                }
            }
            loadGhostKoalaFileIntoDB(inputFile,outputFile,groupID, group);
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
                            /*if (toFile) {
                             writer.write("UPDATE gen SET idKO= '" + ko + "' WHERE gen_id = '" + genID + "';\n");
                             } else {
                             if (!transacciones.updateGenKO(mapID, linea)) {
                             System.err.println("Error actualizando gen: " + "UPDATE gen SET idKO= '" + ko + "' WHERE gen_id = '" + genID + "'");
                             }
                             }*/
                            String q = "INSERT INTO  gen_KO(gen_id, idKO, metodo) VALUES ('" + genID + "','" + ko + "', 'GhostKOALA')";
                            if (toFile) {
                                try {
                                    writer.write(q + ";\n");
                                    //por ahora lo mantenemos pro ver de quitar 
                                    writer.write("UPDATE gen SET idKO= '" + ko + "' WHERE gen_id = '" + genID + "';\n");
                                } catch (IOException ex) {
                                    System.err.println("Error escribiendo archivo para gen_KO: " + genID );
                                    Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } else {
                                if (!transacciones.insertaQuery(q)) {
                                    System.err.println("Error insertando gen_KO: " + genID + "\nQ:" + q);
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
     public void loadGhostKoalaFileIntoDB(String ghostFile, String outFile, String groupID, String group) {
        ArchivoDAO adao = new ArchivoDAO(transacciones);
        File tmpFile = new File(ghostFile);
        ArchivoObj archivoGhost = new ArchivoObj(nextIDArchivo);
        archivoGhost.setTipoArchivo(ArchivoObj.TIPO_FUN);
        archivoGhost.setNombre(ghostFile.substring(ghostFile.lastIndexOf("/") + 1));
        int idx = ghostFile.lastIndexOf("/") != -1 ? ghostFile.lastIndexOf("/") + 1 : ghostFile.length();
        archivoGhost.setPath(ghostFile.substring(0, idx));
        archivoGhost.setDescription("Archivo proveninete de la ejecución de GhostKOALA para la asignación de grupos KO a los genes predichos.");
        archivoGhost.setExtension(ghostFile.substring(ghostFile.lastIndexOf(".") + 1));
        MyDate date = new MyDate(tmpFile.lastModified());
        archivoGhost.setDate(date);
        archivoGhost.setSize(tmpFile.length());
        if (tmpFile.length() / 1048576 < 1000) {//si es menor a un Gb
            archivoGhost.setChecksum(FileUtils.getMD5File(ghostFile));
        } else {
            archivoGhost.setChecksum("TBD");
        }
        archivoGhost.setAlcance("Grupo de bioinformática");
        archivoGhost.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        archivoGhost.setDerechos("Acceso limitado a miembros");
        archivoGhost.setTags("ortología, KO, predicción funcional, GhostKOALA");
        archivoGhost.setTipo("Text");
        Usuario user = new Usuario(31);//ALES
        user.setAcciones("creator");
        user.setComentarios("Se encarga de ejecutar el programa GhostKOALA para realizar la asignación de grupos KO");
        archivoGhost.addUser(user);
        Usuario user2 = new Usuario(9);//ALEXSF
        user2.setAcciones("contributor");
        user2.setComentarios("Investigador responsable de subproyecto");
        archivoGhost.addUser(user2);
        int id = -1;
        try {
            id = Integer.parseInt(groupID);
        } catch (NumberFormatException nfe) {
            System.err.println("Error al determinar el ID del " + group + " val :" + group);
        }
        adao.insertaArchivoMetaGenoma(archivoGhost, id, groupID, outFile.length() > 2, outFile, true);
        nextIDArchivo++;
    }

}
