/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

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
public class KronaDAO {

    private Transacciones transacciones;

    public Transacciones getTransacciones() {
        return transacciones;
    }

    public KronaDAO(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public void setTransacciones(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public boolean writeKronaInput(String fileName, String idMarcador) {
        try {
            FileWriter writer = new FileWriter(fileName);
            ArrayList<ArrayList> taxaCounts = transacciones.getCountsByMarcador(idMarcador);
            if (taxaCounts != null) {
                for (ArrayList<String> taxa : taxaCounts) {
                    StringBuilder line = new StringBuilder();
                    line.append(taxa.get(1)).append("\t");//primero los conteos
                    // kingdom, phylum, class, orden, family, genus, species 
                    ArrayList<String> phylo = transacciones.getRegularPhylogenyByTaxID(taxa.get(0));
                    if (phylo != null && phylo.size() > 0) {
                        for (String p : phylo) {
                            if (p.trim().length() < 2) {
                                line.append("no_rank").append("\t");
                            } else {
                                p = p.trim().replaceAll("\\s+", "_");
                                line.append(p).append("\t");
                            }                         
                        }
                        line.append("\n");
                        writer.write(line.toString());
                    } else {
                        System.err.println("No existe taxon: " + taxa.get(0));
                        return false;
                    }
                }
                writer.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(KronaDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

}
