/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import bobjects.COGObj;
import bobjects.EGGObj;
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

    /**
     * Crea el query para insertar COGs en la BD y la relación cog_has_functions
     *
     * @param cog el cog a insertar/crear query
     * @param toFile if true -> se guarda en archivo
     * @param outFile -> archivo ccon los queries
     * @param append -> si se concatena al archivo. (Los cogs sn pequeños y
     * aunque genera un overhead abrir y cerrar tanto un archivo, aca no hay
     * imparcto pero no es na practica muy recomendada, en otras ocaciones mejor
     * paras un writer ya abierto)
     * @return un log en blanco si todo bien o mensaje de error...
     */
    public String insertaCog(COGObj cog, boolean toFile, String outFile, boolean append) {
        String log = "";
        String query = "INSERT INTO cog (id_cog, cog_description, cog_function) VALUES "
                + "('" + cog.getIdCOG() + "','" + cog.getCog_description() + "','" + cog.getCog_fun() + "')";
        FileWriter writer = null;
        if (!toFile) {
            if (!transacciones.insertaQuery(query)) {
                log += "Error insertando COG: " + cog.getIdCOG() + " - " + query + "\n";
            } else {
                for (String func : cog.getCog_n_fun()) {
                    String q = "INSERT INTO cog_has_functions VALUES('" + cog.getIdCOG() + "', '" + func + "')";
                    if (!transacciones.insertaQuery(q)) {
                        log += "Error insertando COG_FUNCTIONS: " + cog.getIdCOG() + " - " + q + "\n";
                    }
                }
            }
        } else {
            try {
                writer = new FileWriter(outFile, append);
                writer.write(query + ";\n");
                for (String func : cog.getCog_n_fun()) {
                    String q = "INSERT INTO cog_has_functions VALUES('" + cog.getIdCOG() + "', '" + func + "');\n";
                    writer.write(q);
                }
                writer.close();
            } catch (IOException ex) {
                log = "Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n";
            }
        }
        return log;
    }
    /**
     * Este método se encarga de anotar un eggnog en la BD
     * @param egg 
     */
    public void insertaEggNog(EGGObj egg) {
        String query = "INSERT INTO eggnog (ideggnog, description, proteins, species) VALUES "
                + "('" + egg.getIdEGG() + "','" + egg.getDescription() + "'," + egg.getProts() + "," + egg.getSpecies() + ")";
        if (!transacciones.insertaQuery(query)) {
            System.err.println("Error insertando Egg: " + egg.getIdEGG() + " - " + query + "\n");
        } else {
            for (String func : egg.getCog_n_fun()) {
                String q = "INSERT INTO eggnog_has_cog_function VALUES('" + egg.getIdEGG() + "', '" + func + "')";
                if (!transacciones.insertaQuery(q)) {
                    System.err.println("Error insertando EGGNOG_COG_FUNCTIONS: " + egg.getIdEGG() + " - " + q + "\n");
                }
            }
        }
    }

}
