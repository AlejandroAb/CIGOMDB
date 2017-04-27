/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import database.Transacciones;
import java.io.FileWriter;
import java.io.IOException;
import bobjects.ArchivoObj;

/**
 *
 * @author Alejandro
 */
public class ArchivoDAO {

    private Transacciones transacciones;

    public ArchivoDAO(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public boolean insertaArchivo(ArchivoObj archivo, boolean toFile, String outFile, boolean append) {

        String query = archivo.toNewSQLString();
        FileWriter writer = null;
        if (!toFile) {
            if (!transacciones.insertaQuery(query)) {
                System.err.println("Error insertando archivo: " + archivo.getIdArchivo() + " - " + query + "\n");
                return false;
            } else {
                for (String qUsuarios : archivo.archivoUsuariosToSQLString()) {
                    if (!transacciones.insertaQuery(qUsuarios)) {
                        System.err.println("Error insertando relación usuario-archivo: "
                                + archivo.getIdArchivo() + "(idArchivo) - q: " + qUsuarios);
                    }
                }
            }
        } else {
            try {
                writer = new FileWriter(outFile, append);
                writer.write(query + ";\n");
                writer.close();
            } catch (IOException ex) {
                System.err.println("Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n");
                return false;
            }
        }
        return true;
    }

    /**
     * Metodo para guardar o escribir el query para crear una relaacion entre un
     * objeto de tipo archivo y un genoma o metagenoma
     *
     * @param archivo
     * @param id
     * @param source metagenoma o genoma
     * @param toFile
     * @param outFile
     * @param append
     * @return
     */
    public boolean insertaArchivoMetaGenoma(ArchivoObj archivo, int id, String source, boolean toFile, String outFile, boolean append) {

        String query = archivo.toNewSQLString();
        String queryArchivoMetagenoma = "INSERT INTO "+source+"_archivo "
                + "VALUES(" + id + "," + archivo.getIdArchivo() + ")";
        FileWriter writer = null;
        if (!toFile) {
            if (!transacciones.insertaQuery(query)) {
                System.err.println("Error insertando archivo: " + archivo.getIdArchivo() + " - " + query + "\n");
                return false;
            } else {
                if (!transacciones.insertaQuery(queryArchivoMetagenoma)) {
                    System.err.println("Error insertando MetaGenoma_archivo: " + archivo.getIdArchivo() + " - " + query + "\n");
                    return false;
                } else {
                    for (String qUsuarios : archivo.archivoUsuariosToSQLString()) {
                        if (!transacciones.insertaQuery(qUsuarios)) {
                            System.err.println("Error insertando relación usuario-archivo: "
                                    + id + "(idmetagenoma) - " + archivo.getIdArchivo() + "(idArchivo) - q: " + qUsuarios);
                        }
                    }
                }
            }
        } else {
            try {
                writer = new FileWriter(outFile, append);
                writer.write(query + ";\n");
                writer.write(queryArchivoMetagenoma + ";\n");
                for (String qUsuarios : archivo.archivoUsuariosToSQLString()) {
                    writer.write(qUsuarios + ";\n");
                }
                writer.close();
            } catch (IOException ex) {
                System.err.println("Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n");
                return false;
            }
        }
        return true;
    }
}
