/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import bobjects.ArchivoObj;
import bobjects.Marcador;
import bobjects.Muestra;
import bobjects.Usuario;
import database.Transacciones;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alejandro
 */
public class MarcadorDAO {

    private Transacciones transacciones;

    public MarcadorDAO(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public boolean almacenaMarcador(Marcador marcador, boolean toFile, String outFile, boolean append, boolean addArchivos) {
        boolean marcadorOk = true;
        String query = marcador.toSQLString();
        FileWriter writer = null;
        if (toFile) {
            try {
                writer = new FileWriter(outFile, append);
            } catch (IOException ex) {
                Logger.getLogger(MuestreoDAO.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Error accesando archivo: " + outFile + "\n");
                return false;
            }
        }
        if (!toFile) {
            if (!transacciones.insertaQuery(query)) {
                System.err.println("Error insertando marcador: " + marcador.getIdMarcador() + " - " + marcador.getMarc_name() + " - " + query + "\n");
                return false;
            }
        } else {
            try {
                writer.write(query + ";\n");
            } catch (IOException ex) {
                System.err.println("Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n");
                return false;
            }
        }

        if (addArchivos) {
            for (ArchivoObj archivo : marcador.getArchivos()) {
                if (!toFile) {
                    if (!transacciones.insertaQuery(archivo.toNewSQLString())) {
                        System.err.println("Error insertando archivo obj: "
                                + archivo.getIdArchivo() + "(idArchivo) \n");
                    } else {
                        if (!transacciones.insertaArchivoMarcador(marcador.getIdMarcador(), archivo.getIdArchivo())) {
                            System.err.println("Error insertando relación marcador-archivo: "
                                    + marcador.getIdMarcador() + "(idmarcador) - " + archivo.getIdArchivo() + "(idArchivo)");
                        }
                        for (String qUsuarios : archivo.archivoUsuariosToSQLString()) {
                            if (!transacciones.insertaQuery(qUsuarios)) {
                                System.err.println("Error insertando relación usuario-archivo: "
                                        + marcador.getIdMarcador() + "(idmarcador) - " + archivo.getIdArchivo() + "(idArchivo) - q: " + qUsuarios);
                            }
                        }
                    }
                } else {
                    try {
                        writer.write(archivo.toNewSQLString() + ";\n");
                        writer.write("INSERT INTO marcador_archivo (idmarcador, idarchivo) VALUES(" + marcador.getIdMarcador() + "," + archivo.getIdArchivo() + ");\n");
                        for (String qUsuarios : archivo.archivoUsuariosToSQLString()) {
                            writer.write(qUsuarios + ";\n");
                        }
                    } catch (IOException ex) {
                        System.err.println("Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n");
                    }
                }
            }
        }
        if (toFile) {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(MuestreoDAO.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Error cerrando archivo: " + outFile + "\n");
            }
        }
        return marcadorOk;
    }

    public boolean almacenaArchivosMarcadorNew(Marcador marcador, boolean toFile, String outFile, boolean append) {
        boolean marcadorOk = true;

        FileWriter writer = null;
        if (toFile) {
            try {
                writer = new FileWriter(outFile, append);
            } catch (IOException ex) {
                Logger.getLogger(MuestreoDAO.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Error accesando archivo: " + outFile + "\n");
                return false;
            }
        }

        for (ArchivoObj archivo : marcador.getArchivos()) {
            if (!toFile) {
                if (!transacciones.insertaQuery(archivo.toNewSQLString())) {
                    System.err.println("Error insertando archivo obj: "
                            + archivo.getIdArchivo() + "(idArchivo) \n");
                } else {
                    if (!transacciones.insertaArchivoMarcador(marcador.getIdMarcador(), archivo.getIdArchivo())) {
                        System.err.println("Error insertando relación marcador-archivo: "
                                + marcador.getIdMarcador() + "(idmarcador) - " + archivo.getIdArchivo() + "(idArchivo)");
                    }
                    for (String qUsuarios : archivo.archivoUsuariosToSQLString()) {
                        if (!transacciones.insertaQuery(qUsuarios)) {
                            System.err.println("Error insertando relación usuario-archivo: "
                                    + marcador.getIdMarcador() + "(idmarcador) - " + archivo.getIdArchivo() + "(idArchivo) - q: " + qUsuarios);
                        }
                    }
                }
            } else {
                try {
                    writer.write(archivo.toNewSQLString() + ";\n");
                    writer.write("INSERT INTO marcador_archivo (idmarcador, idarchivo) VALUES(" + marcador.getIdMarcador() + "," + archivo.getIdArchivo() + ");\n");
                    for (String qUsuarios : archivo.archivoUsuariosToSQLString()) {
                        writer.write(qUsuarios + ";\n");
                    }
                } catch (IOException ex) {
                    System.err.println("Error I/O escribiendo archivo: " + outFile + "\n" + archivo.toNewSQLString() + "\n");
                }
            }
        }

        if (toFile) {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(MuestreoDAO.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Error cerrando archivo: " + outFile + "\n");
            }
        }
        return marcadorOk;
    }

    /**
     * @deprecated use almacenaArchivosMarcadorNew()
     * Este método se usa cuando el marcador ya fue escrito en archivo o en la
     * BD, por lo que solo es neceesario anexar nuevos archivos como ser los
     * fastq con las lecturas pareadas o los archivos de metaxa
     *
     * @param marcador
     * @param toFile
     * @param outFile
     * @param append
     * @param addArchivos
     * @return
     */
    public boolean almacenaArchivosMarcador(Marcador marcador, boolean toFile, String outFile, boolean append, boolean addArchivos) {
        boolean marcadorOk = true;
        String query = marcador.toSQLString();
        FileWriter writer = null;
        if (toFile) {
            try {
                writer = new FileWriter(outFile, append);
            } catch (IOException ex) {
                Logger.getLogger(MuestreoDAO.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Error accesando archivo: " + outFile + "\n");
                return false;
            }
        }
        if (!toFile) {
            if (!transacciones.insertaQuery(query)) {
                System.err.println("Error insertando marcador: " + marcador.getIdMarcador() + " - " + marcador.getMarc_name() + " - " + query + "\n");
                return false;
            }
        } else {
            try {
                writer.write(query + ";\n");
            } catch (IOException ex) {
                System.err.println("Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n");
                return false;
            }
        }

        if (addArchivos) {
            for (ArchivoObj archivo : marcador.getArchivos()) {
                if (!toFile) {
                    if (!transacciones.insertaQuery(archivo.toNewSQLString())) {
                        System.err.println("Error insertando archivo obj: "
                                + archivo.getIdArchivo() + "(idArchivo) \n");
                    } else {
                        if (!transacciones.insertaArchivoMarcador(marcador.getIdMarcador(), archivo.getIdArchivo())) {
                            System.err.println("Error insertando relación marcador-archivo: "
                                    + marcador.getIdMarcador() + "(idmarcador) - " + archivo.getIdArchivo() + "(idArchivo)");
                        }
                        for (String qUsuarios : archivo.archivoUsuariosToSQLString()) {
                            if (!transacciones.insertaQuery(qUsuarios)) {
                                System.err.println("Error insertando relación usuario-archivo: "
                                        + marcador.getIdMarcador() + "(idmarcador) - " + archivo.getIdArchivo() + "(idArchivo) - q: " + qUsuarios);
                            }
                        }
                    }
                } else {
                    try {
                        writer.write(archivo.toNewSQLString() + ";\n");
                        writer.write("INSERT INTO marcador_archivo (idmarcador, idarchivo) VALUES(" + marcador.getIdMarcador() + "," + archivo.getIdArchivo() + ");\n");
                        for (String qUsuarios : archivo.archivoUsuariosToSQLString()) {
                            writer.write(qUsuarios + ";\n");
                        }
                    } catch (IOException ex) {
                        System.err.println("Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n");
                    }
                }
            }
        }
        if (toFile) {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(MuestreoDAO.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Error cerrando archivo: " + outFile + "\n");
            }
        }
        return marcadorOk;
    }
}
