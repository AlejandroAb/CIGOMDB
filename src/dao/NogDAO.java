/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import bobjects.NOGObj;
import database.Transacciones;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Alejandro
 */
public class NogDAO {
    Transacciones transacciones;

    public NogDAO(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    /**
     * Crea el query para insertar NOGs en la BD
     *
     * @param nog el nog a insertar/crear query
     * @param toFile if true -> se guarda en archivo
     * @param outFile -> archivo ccon los queries
     * @param append -> si se concatena al archivo. (Los cogs sn pequeños y
     * aunque genera un overhead abrir y cerrar tanto un archivo, aca no hay
     * imparcto pero no es na practica muy recomendada, en otras ocaciones mejor
     * paras un writer ya abierto)
     * @return un log en blanco si todo bien o mensaje de error...
     */
    public String insertaNog(NOGObj nog, boolean toFile, String outFile, boolean append) {
        String log = "";
        String query = "INSERT INTO Nog (id_nog, nog_description) VALUES "
                + "('" + nog.getId_NOG() + "','" + nog.getNog_description() + "')";
        FileWriter writer = null;
        if (!toFile) {
            if (!transacciones.insertaQuery(query)) {
                log += "Error insertando NOG: " + nog.getId_NOG() + " - " + query + "\n";
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


