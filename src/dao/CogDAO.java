/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import bobjects.COGObj;
import bobjects.Pfam;
import database.Transacciones;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alejandro
 */
public class CogDAO {

    Transacciones transacciones;

    public CogDAO(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public String insertaCog(COGObj cog, boolean toFile, String outFile, boolean append) {
        String log = "";
        String query = "INSERT INTO cog (id_cog, cog_description, cog_function) VALUES "
                + "('" + cog.getIdCOG() + "','" +cog.getCog_description() + "','" + cog.getCog_fun() + "')";
        FileWriter writer = null;
        if (toFile) {
            try {
                writer = new FileWriter(outFile, append);
            } catch (IOException ex) {
                Logger.getLogger(MuestreoDAO.class.getName()).log(Level.SEVERE, null, ex);
                log += "Error accesando archivo: " + outFile + "\n";
            }
        }
        if (!toFile) {
            if (!transacciones.insertaQuery(query)) {
                log += "Error insertando COG: " + cog.getIdCOG() + " - " + query + "\n";
            }
        } else {
            try {
                writer.write(query + ";\n");
                writer.close();
            } catch (IOException ex) {
                log = "Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n";
            }
        }
        return log;
    }

}
