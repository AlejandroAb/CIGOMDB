/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import bobjects.COGObj;
import bobjects.EGGObj;
import bobjects.NOGObj;
import dao.CogDAO;
import dao.NogDAO;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
     * Este metodo se hizo para procesar el archivo NOG.description de EggNOG,
     * el acul trae todas las familias NOG y algunas descripciones, ya que no
     * todos los elementos están clasificados
     *
     * @param inputFile
     * @param toFile
     * @param outFile
     * @return
     */
    public String parseNOGNames(String inputFile, boolean toFile, String outFile) {
        String log = "";
        StringUtils sUtils = new StringUtils();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1"));
            String linea;
            NogDAO nogDAO = new NogDAO(transacciones);
            while ((linea = reader.readLine()) != null) {
                NOGObj nog = null;
                if (!linea.startsWith("#")) {
                    StringTokenizer st = new StringTokenizer(linea, "\t");
                    boolean addCOG = false;
                    nog = new NOGObj(st.nextToken());
                    if (st.hasMoreTokens()) {
                        nog.setNog_description(sUtils.scapeSQL(st.nextToken()));
                    } else {
                        nog.setNog_description("");
                    }

                    addCOG = true;
                    if (addCOG) {
                        nogDAO.insertaNog(nog, toFile, outFile, true);
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
     * Se encarga de procesar los archivos con anotaciones funcionales de EGGNOG
     * como ser:
     * http://eggnogdb.embl.de/download/eggnog_4.5/data/NOG/NOG.annotations.tsv.gz
     *
     * @param inputFile el archivo de eggnog
     * @param validateCOG por default false, si true -> valida si la
     * descripccion de cogs es la misma que la del NCBI
     */
    public void eggNogAnnotationParser(String inputFile, boolean validateCOG) {
        StringUtils su = new StringUtils();
        try {
            CogDAO cdao = new CogDAO(transacciones);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1"));
            String line;
            int num = 0;
            int dif = 0;
            int cog = 0;
            while ((line = reader.readLine()) != null) {
                num++;
                StringTokenizer st = new StringTokenizer(line, "\t");
                st.nextToken(); //NOG
                String ideggnog = st.nextToken().trim();
                String prots = st.nextToken().trim();
                String species = st.nextToken().trim();
                String functions = st.nextToken();
                String description = su.scapeSQL(st.nextToken());
                EGGObj egg = new EGGObj(ideggnog);
                egg.setDescription(description);
                egg.setFun(functions);
                egg.setProts(prots);
                egg.setSpecies(species);
                cdao.insertaEggNog(egg);
                if (validateCOG) {
                    if (egg.getIdEGG().startsWith("COG")) {
                        cog++;
                        if (egg.getDescription().equals(transacciones.getCOGDescription(egg.getIdEGG()))) {
                            dif++;
                        }
                    }
                }
            }
            if (validateCOG) {
                System.out.println("Se procesaron: " + num + " registros\nCOGS: " + cog + " \tDif: " + dif + " iguales: " + (cog - dif));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(COGProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(COGProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(COGProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
