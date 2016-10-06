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

    public String insertaArchivo(ArchivoObj archivo, boolean toFile, String outFile, boolean append) {
        String log = "";
        String query = "INSERT INTO archivo (idarchivo, idtipo_archivo, nombre, extension, path, checksum, descripcion, poor_q_secs, num_secs, seq_length, gc_percent) VALUES "
                + "(" + archivo.getIdArchivo() + "," + archivo.getTipoArchivo() + ", '" + archivo.getNombre() + "','" + archivo.getExtension()
                + "','" + archivo.getPath() + "','" + archivo.getChecksum() + "','" + archivo.getDescription() + "'," + archivo.getPoor_q_secs()
                + "," + archivo.getNum_secs() + "," + archivo.getSeq_length() + "," + archivo.getGc_percent() + ")";
        FileWriter writer = null;
        if (!toFile) {
            if (!transacciones.insertaQuery(query)) {
                log += "Error insertando archivo: " + archivo.getIdArchivo() + " - " + query + "\n";
            }
        } else {
            try {
                writer = new FileWriter(outFile, append);
                writer.write(query + ";\n");               
                writer.close();
            } catch (IOException ex) {
                log = "Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n";
            }
        }
        return log;
    }
}
