/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

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
public class PfamDAO {
    Transacciones transacciones;

    public PfamDAO(Transacciones transacciones) {
        this.transacciones = transacciones;
    }
    
    public String insertaPfam(Pfam pfam, boolean toFile, String outFile, boolean append){
    String log = "";
    String query = "INSERT INTO pfam (pfam_acc, clan_acc, id_GO, id_pfam, pfam_deff, pfam_comments) VALUES "
            + "('" + pfam.getAccession()+"','"+pfam.getClan_acc()+"','"+pfam.getIdGO()
            +"','"+ pfam.getId()+"','"+pfam.getDeffinition()+"','"+pfam.getComments()+"')"; 
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
                log += "Error insertando pfam: " + pfam.getAccession() + " - " + query + "\n";
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
