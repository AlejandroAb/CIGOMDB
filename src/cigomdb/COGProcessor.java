/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import bobjects.COGObj;
import bobjects.Pfam;
import dao.CogDAO;
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
public class COGProcessor {

    public Transacciones transacciones;

    public COGProcessor(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    /**
     * Este método se encarga de parsear los archivos de cog que traen la
     * referencia de todos los grupos a los que pertenecen. Este tipo de
     * archivos se puede encontrar en:
     * ftp://ftp.ncbi.nih.gov/pub/COG/COG2014/data/cognames2003-2014.tab Formato
     * esperado: COGXXXX \t COG_FUNC \t COG_DEFF COG4862	KTN	Negative regulator
     * of genetic competence, sporulation and motility o archivod ccomo
     * C:\Users\Alejandro\Documents\Projects\pemex\4 db\COG\cogs.csv
     *
     * @param inputFile inputfile bajado del ftp de NCBI
     * @param toFile si se escribe el query en archivo --> else se escribe en BD
     * @param outFile
     * @param delim se espera tab (\t) o coma (,)
     * @return
     */
    public String parseCOGNames(String inputFile, boolean toFile, String outFile, String delim) {
        String log = "";
        StringUtils sUtils = new StringUtils();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1"));
            String linea;
            CogDAO cogDAO = new CogDAO(transacciones);
            while ((linea = reader.readLine()) != null) {
                COGObj cog = null;
                if (!linea.startsWith("#")) {
                    StringTokenizer st = new StringTokenizer(linea, delim);
                    int tokens = st.countTokens();
                    boolean addCOG = false;
                    if (tokens >= 3) { //viene bien (cuendo es con "," as delim -> esta procesando un csv donde la descripcion puede traer comas así que pueden llegar a ser mas tokens)
                        cog = new COGObj(st.nextToken());
                        cog.setCog_fun(st.nextToken());
                        String desc = "";
                        while (st.hasMoreTokens()) {
                            desc += st.nextToken() + ",";
                        }
                        desc = desc.substring(0, desc.length() - 1);
                        cog.setCog_description(sUtils.scapeSQL(desc));
                        addCOG = true;
                    } else if (tokens >= 1) {
                        log += "Error linea con " + tokens + " tokens en cog = " + st.nextToken();
                    } else {
                        log += "Error linea sin tokens en linea = " + linea;
                    }
                    if (addCOG) {
                        cogDAO.insertaCog(cog, toFile, outFile, true);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PfamProcesor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(PfamProcesor.class.getName()).log(Level.SEVERE, null, ioe);
        }
        return log;
    }
    /**
     * Este metodo se hizo para clasificar las funciones de grupos COG. Lee el archivo 
     * C:\Users\Alejandro\Documents\Projects\pemex\4 db\COG\cog_function_ok.txt
     * Se espera que el archivo venga con formato:
     * COG super Func  \t ID o letra de Funcion  \t    Descripción de la función
     * INFORMATION STORAGE AND PROCESSING	J	Translation, ribosomal structure and biogenesis
     * @param inputFile
     * @param toFile
     * @param outFile
     * @return 
     */
    public String parseCOGFuncion(String inputFile, boolean toFile, String outFile) {
         String log = "";
        StringUtils sUtils = new StringUtils();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1"));
            String linea;
            CogDAO cogDAO = new CogDAO(transacciones);
            while ((linea = reader.readLine()) != null) {
                COGObj cog = null;
                if (!linea.startsWith("#")) {
                    StringTokenizer st = new StringTokenizer(linea, "\t");
                    int tokens = st.countTokens();
                    boolean addCOG = false;
                    if (tokens >= 3) { //viene bien (cuendo es con "," as delim -> esta procesando un csv donde la descripcion puede traer comas así que pueden llegar a ser mas tokens)
                        cog = new COGObj(st.nextToken());
                        cog.setCog_fun(st.nextToken());
                        String desc = "";
                        while (st.hasMoreTokens()) {
                            desc += st.nextToken() + ",";
                        }
                        desc += desc.substring(0, desc.length() - 1);
                        cog.setCog_description(sUtils.scapeSQL(desc));
                        addCOG = true;
                    } else if (tokens >= 1) {
                        log += "Error linea con " + tokens + " tokens en cog = " + st.nextToken();
                    } else {
                        log += "Error linea con sin tokens en linea = " + linea;
                    }
                    if (addCOG) {
                        cogDAO.insertaCog(cog, toFile, outFile, true);
                    }
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
