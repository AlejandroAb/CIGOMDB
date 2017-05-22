/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import cigomdb.NCBITaxCreator;
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

    /**
     * Este método se encarga dee crear la matriz de abundancia que sirve de
     * input file para crear el krona.
     *
     * @param fileName el nombre y path completo de la matriz a crear
     * @param idMetagenoma el metagenoma para el cual se crea la matriz
     * @param withNoRank en caso de que no exista un nivel taxonómico, ya sea de
     * los niveles intermedios o de los más específicos, si esta bandera está en
     * true, estos valores serán creados con la palabra no_rank en ese nivel si
     * viene en false, este campo simplemente tendrá un tabulador.
     * @return
     */
    public boolean writeKronaInput(String fileName, String idMetagenoma, boolean withNoRank) {
        try {
            FileWriter writer = new FileWriter(fileName);
            ArrayList<ArrayList> taxaCounts = transacciones.getCountsByMetagenoma(idMetagenoma);
            if (taxaCounts != null) {
                NCBITaxCreator ncbi = new NCBITaxCreator(transacciones);
                for (ArrayList<String> taxa : taxaCounts) {
                    StringBuilder line = new StringBuilder();
                    line.append(taxa.get(1)).append("\t");//primero los conteos
                    // kingdom, phylum, class, orden, family, genus, species 
                    ArrayList<String> phylo = transacciones.getRegularPhylogenyByTaxID(taxa.get(0));
                    if (phylo == null || phylo.size() < 1) {
                        ncbi.createTaxon("", false, " WHERE tax_id = " + taxa.get(0));
                        phylo = transacciones.getRegularPhylogenyByTaxID(taxa.get(0));
                    }
                    if (phylo != null && phylo.size() > 0) {
                        int i = 0;
                        String rank = "";
                        for (String p : phylo) {
                            if (i == 0) {
                                rank = "reino";
                            } else if (i == 1) {
                                rank = "phylum";
                            } else if (i == 2) {
                                rank = "class";
                            } else if (i == 3) {
                                rank = "orden";
                            } else if (i == 4) {
                                rank = "family";
                            } else if (i == 5) {
                                rank = "genus";
                            } else if (i == 6) {
                                rank = "species";
                            }
                            if (p.trim().length() < 2) {
                                if (withNoRank) {
                                    line.append("no_").append(rank).append("\t");
                                } else {
                                    line.append("\t");
                                }
                            } else {
                                p = p.trim().replaceAll("\\s+", "_");
                                line.append(p).append("\t");
                            }
                            i++;
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
