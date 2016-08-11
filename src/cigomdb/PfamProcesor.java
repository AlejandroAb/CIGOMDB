/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import bobjects.Pfam;
import dao.PfamDAO;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.StringUtils;

/**
 *
 * @author Alejandro
 */
public class PfamProcesor {

    public Transacciones transacciones;
    
    public PfamProcesor(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    /**
     * Este método se encarga de parsear los archivos de pfam que traen la
     * referencia de todas las familias y los clanes a los que pertenecen. Este
     * tipo de archivos se puede encontrar en:
     * ftp://ftp.ebi.ac.uk/pub/databases/Pfam/releases/Pfam30.0/Pfam-A.clans.tsv.gz
     * Formato esperado: PF_ACC \t CLAN_ACC \t CLAN_ID \t PFAM_ID \t PFAM_DEFF
     * Pero si no pertenece a algun clan no viene con esta información y trae
     * tabs extra: PF_ACC \t \t \t PFAM_ID \t PFAM_DEFF PF00026	CL0129
     * Peptidase_AA	Asp	Eukaryotic aspartyl protease PF00027	cNMP_binding	Cyclic
     * nucleotide-binding domain
     *
     * @param inputFile inputfile bajado del ftp de pfam
     * @param toFile si se escribe el query en archivo --> else se escribe en BD
     * @param outFile
     * @return
     */
    public String parsePfamAClans(String inputFile, boolean toFile, String outFile) {
        String log = "";
        StringUtils sUtils = new StringUtils();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1"));
            String linea;
            PfamDAO pfamDAO = new PfamDAO(transacciones);
            while ((linea = reader.readLine()) != null) {
                Pfam pfam = null;
                StringTokenizer st = new StringTokenizer(linea, "\t");
                int tokens = st.countTokens();
                boolean addPfam = false;
                if (tokens == 5) { //viene con clan
                    pfam = new Pfam(st.nextToken());
                    pfam.setClan_acc(st.nextToken());
                    st.nextToken();//descartamos clan_def
                    pfam.setId(sUtils.scapeSQL(st.nextToken()));
                    pfam.setDeffinition(sUtils.scapeSQL(st.nextToken()));
                    addPfam = true;
                } else if (tokens == 3) {
                    pfam = new Pfam(st.nextToken());
                    // pfam.setClan_acc(st.nextToken());
                    //st.nextToken();//descartamos clan_def
                    pfam.setId(sUtils.scapeSQL(st.nextToken()));
                    pfam.setDeffinition(sUtils.scapeSQL(st.nextToken()));
                    addPfam = true;
                } else if (tokens >= 1) {
                    log += "Error linea con " + tokens + " tokens en pfam_acc = " + st.nextToken();
                } else {
                    log += "Error linea con sin tokens en linea = " + linea;
                }
                if (addPfam) {
                    pfamDAO.insertaPfam(pfam, toFile, outFile, true);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PfamProcesor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(PfamProcesor.class.getName()).log(Level.SEVERE, null, ioe);
        }
        return log;
    }
}
