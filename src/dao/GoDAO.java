/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import bobjects.GOObj;
import database.Transacciones;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Alejandro
 */
public class GoDAO {

    Transacciones transacciones;

    public GoDAO(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    /**
     * Metodo para ingresar GO a la BD ya sea via driver o crea sql script.
     *
     * @param go
     * @param toFile
     * @param outFile
     * @param append
     * @return
     */
    public String insertaGO(GOObj go, boolean toFile, String outFile, boolean append) {
        String log = "";
        String query = "INSERT INTO gontology (id_go, go_name, namespace,definition,is_a, relationship, "
                + "is_obsolete, replaced_by, comentario, url) VALUES "
                + "('" + go.getId() + "','" + go.getName() + "','" + go.getNamespace() + "','"
                + go.getDefinition() + "','" + go.getIs_a() + "','" + go.getRelationship() + "',"
                + go.getIs_obsolete() + ",'" + go.getReplace_by() + "','" + go.getCommentario() + "','" + go.getUrl() + "')";
        FileWriter writer = null;
        if (!toFile) {
            if (!transacciones.insertaQuery(query)) {
                log += "Error insertando GO: " + go.getId() + " - " + query + "\n";
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
