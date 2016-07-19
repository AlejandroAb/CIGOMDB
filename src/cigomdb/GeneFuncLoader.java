/*
 * Esta clase esta diseñada como clase principal para procesar resultados producto
 * del análisis de metgenomas por tecnologia shotgun
 */
package cigomdb;

import bobjects.GenObj;
import bobjects.GenSeqObj;
import dao.GenDAO;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CIGOM. MAYO 2016
 *
 * @author Alejandro Abdala
 */
public class GeneFuncLoader {

    private Transacciones transacciones = null;
    private boolean debug = false;

    public GeneFuncLoader(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public String loadFragileScanFiles(String idPre, String gffFile, String nucFile, String aaFile, String mapPrefix) {
        String log = "";
        GenDAO genDAO = new GenDAO(transacciones);
        int line = 0;
        try {
            BufferedReader gffReader = null;
            BufferedReader nucReader = null;
            BufferedReader aaReader = null;
            gffReader = new BufferedReader(new InputStreamReader(new FileInputStream(gffFile)));
            if (nucFile.length() > 0) {
                nucReader = new BufferedReader(new InputStreamReader(new FileInputStream(nucFile)));
            }
            if (aaFile.length() > 0) {
                aaReader = new BufferedReader(new InputStreamReader(new FileInputStream(aaFile)));
            }
            String gffLine;
            String nucLine = null;
            String aaLine = null;
            int gen_num = 0;
            while ((gffLine = gffReader.readLine()) != null) {
                line++;
                if (!gffLine.startsWith("#")) {
                    gen_num++;
                    GenObj gen = new GenObj(idPre + "" + gen_num);
                    gen.setGene_map_id(mapPrefix + "" + gen_num);
                    StringTokenizer st = new StringTokenizer(gffLine, "\t");
                    gen.setContig_id(st.nextToken());
                    st.nextToken();//FGS metodo
                    gen.setGenType(st.nextToken());
                    try {
                        int from = Integer.parseInt(st.nextToken());
                        gen.setContig_from(from);
                    } catch (NumberFormatException nfe) {
                        gen.setContig_from(0);
                        log += "Error " + line + " CONTIG_FROM gff FILE";
                    }
                    try {
                        int to = Integer.parseInt(st.nextToken());
                        gen.setContig_to(to);
                    } catch (NumberFormatException nfe) {
                        gen.setContig_from(0);
                        log += "Error " + line + " CONTIG_TO gff FILE";
                    }
                    st.nextToken();//un punto
                    gen.setGen_strand(st.nextToken());
                    st.nextToken();//0 uno o dos ver que es este campo
                    String varios = st.nextToken(); //ID=contig-100_0_1_661_+;product=predicted protein
                    StringTokenizer miscToks = new StringTokenizer(varios, ";");
                    while (miscToks.hasMoreTokens()) {
                        String field = miscToks.nextToken();
                        String fieldArr[] = field.split("=");
                        if (fieldArr.length == 2) {
                            String key = fieldArr[0];
                            String val = fieldArr[1];
                            if (key.toUpperCase().equals("ID")) {
                                gen.setContig_gen_id(val);
                            } else if (key.toLowerCase().equals("product")) {
                                gen.setGen_function(val);
                            } else {
                                gen.addProperty(key, val);

                            }
                        }
                    }
                    if (nucReader != null) {
                        if (nucLine == null) {
                            nucLine = nucReader.readLine();
                        }
                    }
                    if (aaReader != null) {
                        if (aaLine == null) {
                            aaLine = aaReader.readLine();
                        }
                    }
                    if ((">" + gen.getContig_gen_id()).equals(nucLine.trim()) && nucReader != null) {
                        String seqNuc = "";
                        while (((nucLine = nucReader.readLine()) != null) && !nucLine.startsWith(">")) {
                            seqNuc += nucLine;
                        }
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(seqNuc); //also fix de length
                        seqObj.setSeqType("NC");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    }
                    if ((">" + gen.getContig_gen_id()).equals(aaLine.trim()) && aaReader != null) {
                        String seqAmino = "";
                        while (((aaLine = aaReader.readLine()) != null) && !aaLine.startsWith(">")) {
                            seqAmino += aaLine;
                        }
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(seqAmino); //also fix de length
                        seqObj.setSeqType("AA");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    }
                    log += genDAO.almacenaGen(gen);
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, ioe);
        } catch (NoSuchElementException nsee) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, nsee);
            log += "Error token linea: " + line;
        }
        return log;

    }
}
