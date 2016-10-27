/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import dao.MetaxaDAO;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.StringUtils;

/**
 * Esta clase esta diseñada para parssear archivos de anotación funcional de
 * genes como ser resultrados por Trinotate o EggNogMapper
 *
 * @author Alejandro
 */
public class GeneAnnotationLoader {

    private Transacciones transacciones;

    public GeneAnnotationLoader(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    /**
     * Este método se encarga de parsear un archivo de trinotate y en base a eso
     * hacer la anotación funcional de COGS/EggNOG, Pfams, blast hits, signalp
     * para los genes identificados. Es muy importante ver que el gen_map_id de
     * la BD cuadre con el gen_id (primera columna) del input file
     *
     * @param inputFile
     * @param group genoma o metagenoma
     * @param groupID el id del genoma o el metagenoma
     * @return
     */
    public boolean parseTrinotateFile(String inputFile, String group, String groupID) {
        try {
            //y`
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1"));
            String linea;
            int numLinea = 0;
            //defaults
            int idxGeneID = 1;
            int idxBlastX = 2;
            int idxBlastP = 3;
            int idxPfam = 4;
            int idxSignalP = 5;
            int idxTransM = 6;
            int idxCog = 7;
            int idxGOBlast = 8;
            int idxGOPfam = 9;
            while ((linea = reader.readLine()) != null) {
                numLinea++;
                if (numLinea == 1) {
                    StringTokenizer headerST = new StringTokenizer(linea, "\t");
                    int toks = 0;
                    while (headerST.hasMoreTokens()) {
                        toks++;
                        String tok = headerST.nextToken().trim().toLowerCase();
                        if (tok.contains("gene_id")) {
                            idxGeneID = toks;
                        } else if (tok.toLowerCase().contains("blastx")) {
                            idxBlastX = toks;
                        } else if (tok.toLowerCase().contains("blastp")) {
                            idxBlastP = toks;
                        } else if (tok.toLowerCase().trim().equals("pfam")) {
                            idxPfam = toks;
                        } else if (tok.toLowerCase().contains("signalp")) {
                            idxSignalP = toks;
                        } else if (tok.toLowerCase().contains("eggnog")) {
                            idxCog = toks;
                        } else if (tok.toLowerCase().contains("tmhmm")) {//comentarios
                            idxTransM = toks;
                        } else if (tok.toLowerCase().contains("ontology") && tok.toLowerCase().contains("blast")) {//tipo de estacion
                            idxGOBlast = toks;
                        } else if (tok.toLowerCase().contains("ontology") && tok.toLowerCase().contains("pfam")) {//tipo de estacion
                            idxGOPfam = toks;
                        }
                    }
                } else {
                    StringTokenizer st = new StringTokenizer(linea, "\t");
                    ArrayList<String> gos = new ArrayList<String>();
                    int tok = 0;
                    String geneID = null;
                    while (st.hasMoreTokens()) {
                        tok++;
                        if (tok == idxGeneID) {
                            geneID = transacciones.getGeneIDByMapID(group, groupID, st.nextToken());
                        } else if (tok == idxBlastX) {
                            procesaLineaBlastTrinotate(st.nextToken(), "BLASTX", geneID);
                        } else if (tok == idxBlastP) {
                            procesaLineaBlastTrinotate(st.nextToken(), "BLASTP", geneID);
                        } else if (tok == idxPfam) {
                            procesaLineaPfamTrinotate(st.nextToken(), geneID);
                        } else if (tok == idxSignalP) {
                            procesaSignalPTrinotate(st.nextToken(), geneID);
                        } else if (tok == idxTransM) {
                            //procesaLineTrans
                            st.nextToken();
                        } else if (tok == idxCog) {
                            procesaLineaCogTrinotate(st.nextToken(), geneID);
                        } else if (tok == idxGOBlast || tok == idxGOPfam) {
                            procesaLineaGOTrinotate(st.nextToken(), geneID, gos);
                        } else {
                            st.nextToken();
                        }

                    }
                }

            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public boolean splitTrinotateFile(String inputFile, String group, String groupID) {
        try {
            //y`
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1"));
            String linea;
            int numLinea = 0;
            //defaults
            int idxGeneID = 1;
            int idxBlastX = 2;
            int idxBlastP = 3;
            int idxPfam = 4;
            int idxSignalP = 5;
            int idxTransM = 6;
            int idxCog = 7;
            int idxGOBlast = 8;
            int idxGOPfam = 9;
            while ((linea = reader.readLine()) != null) {
                numLinea++;
                if (numLinea == 1) {
                    int toks = 0;
                    for (String tok : linea.split("\t")) {
                        toks++;
                        tok = tok.trim().toLowerCase();
                        if (tok.contains("gene_id")) {
                            idxGeneID = toks;
                        } else if (tok.contains("blastx")) {
                            idxBlastX = toks;
                        } else if (tok.contains("blastp")) {
                            idxBlastP = toks;
                        } else if (tok.equals("pfam")) {
                            idxPfam = toks;
                        } else if (tok.contains("signalp")) {
                            idxSignalP = toks;
                        } else if (tok.contains("eggnog")) {
                            idxCog = toks;
                        } else if (tok.contains("tmhmm")) {//comentarios
                            idxTransM = toks;
                        } else if (tok.contains("ontology") && tok.contains("blast")) {//tipo de estacion
                            idxGOBlast = toks;
                        } else if (tok.contains("ontology") && tok.contains("pfam")) {//tipo de estacion
                            idxGOPfam = toks;
                        }
                    }
                } else {
                    ArrayList<String> gos = new ArrayList<String>();
                    int tok = 0;
                    String geneID = null;
                    for (String token : linea.split("\t")) {
                        tok++;
                        if (tok == idxGeneID) {
                            geneID = transacciones.getGeneIDByMapID(group, groupID, token);
                        } else if (tok == idxBlastX) {
                            splitLineaBlastTrinotate(token, "BLASTX", geneID);
                        } else if (tok == idxBlastP) {
                            splitLineaBlastTrinotate(token, "BLASTP", geneID);
                        } else if (tok == idxPfam) {
                            splitLineaPfamTrinotate(token, geneID);
                        } else if (tok == idxSignalP) {
                            procesaSignalPTrinotate(token, geneID);
                        } else if (tok == idxTransM) {
                            //procesaLineTrans

                        } else if (tok == idxCog) {
                            splitLineaCogTrinotate(token, geneID);
                        } else if (tok == idxGOBlast || tok == idxGOPfam) {
                            splitLineaGOTrinotate(token, geneID, gos);
                        } else {
                            // st.nextToken();
                        }
                    }
                }

            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * Este método se encarga de anotar en la BD un resultado de blast contra
     * alguna de las proteínas predichas
     *
     * @param linea la linea con todo el resultado
     * @param metodo el método con el cual fue obtenido dicho resultado: BLASTX,
     * BLASTP son valores v+alidos
     * @param gen_id El id del gen para el cual fue obtenido este hit
     */
    public void procesaLineaBlastTrinotate(String linea, String metodo, String gen_id) {
        if (!linea.trim().equals(".")) {
            StringTokenizer st_l = new StringTokenizer(linea, "`");
            while (st_l.hasMoreTokens()) {
                String blast_line = st_l.nextToken();
                StringTokenizer st = new StringTokenizer(blast_line, "^");
                StringUtils su = new StringUtils();
                if (st.countTokens() < 7) {
                    System.err.println("Blast Res con 6 o menos cols: " + st.countTokens());
                } else {
                    String id1 = "";
                    String id2 = "";
                    String query = "";
                    String identity = "";
                    String eval = "";
                    String function = "";
                    String tax = "";
                    int idxTok = 0;
                    while (st.hasMoreTokens()) {
                        idxTok++;
                        String tok = st.nextToken().trim();
                        if (idxTok == 1) {
                            id1 = tok;
                        } else if (idxTok == 2) {
                            id2 = tok;
                        } else if (tok.startsWith("Q:")) {
                            query = tok;
                        } else if (tok.contains("%")) {
                            identity = tok.substring(0, tok.indexOf("%"));
                        } else if (tok.startsWith("E:")) {
                            eval = tok.substring(2);
                        } else if (tok.contains("RecName")) {
                            function = su.scapeSQL(tok.substring(tok.indexOf("=") + 1, tok.length() - 1));//-1 por que termina con;
                        } else {
                            tax = tok;
                        }
                    }
                    if (transacciones.validaUniprotID(id1)) {
                        id1 = id1;
                    } /*else if (transacciones.validaUniprotID(id2)) {
                     id1 = id2;
                     } */ else {//no está anotada
                        //transacciones inserta SWISS_PROT

                        MetaxaDAO mdao = new MetaxaDAO(transacciones);
                        String taxo[] = mdao.searchNCBINode(tax);
                        String q = "INSERT INTO swiss_prot (uniprot_id, uniprot_acc, ncbi_tax_id, prot_name) "
                                + "VALUES('" + id1 + "','-1','" + taxo[0] + "','" + function + "')";
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando swiss_prot: " + id1 + "\nQ:" + q);
                        } else {
                            System.out.println("Nueva SwissProt: " + id1);
                        }
                    }
                    String q = "INSERT INTO gen_swiss_prot (uniprot_id, gen_id, prediction_method, eval,identity, query) "
                            + "VALUES('" + id1 + "','" + gen_id + "','" + metodo + "'," + eval + "," + identity + ",'" + query + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando swiss_prot_gen: " + id1 + " - " + gen_id);
                    }
                }
            }
        }
    }

    /**
     * Este método se encarga de anotar en la BD un resultado de blast contra
     * alguna de las proteínas predichas. Es igual al metodo
     * parseLineaBlastTrinotate pero usar Str.split() emn lugar de
     * StringTokenizer
     *
     * @param linea la linea con todo el resultado
     * @param metodo el método con el cual fue obtenido dicho resultado: BLASTX,
     * BLASTP son valores v+alidos
     * @param gen_id El id del gen para el cual fue obtenido este hit
     */
    public void splitLineaBlastTrinotate(String linea, String metodo, String gen_id) {
        if (!linea.trim().equals(".")) {
            // StringTokenizer st_l = new StringTokenizer(linea, "`");
            StringUtils su = new StringUtils();
            for (String blast_line : linea.split("`")) {
                String id1 = "";
                String id2 = "";
                String query = "";
                String identity = "";
                String eval = "";
                String function = "";
                String tax = "";
                int idxTok = 0;
                //StringTokenizer st = new StringTokenizer(blast_line, "^");
                for (String tok : blast_line.split("\\^")) {
                    idxTok++;
                    if (idxTok == 1) {
                        id1 = tok;
                    } else if (idxTok == 2) {
                        id2 = tok;
                    } else if (tok.startsWith("Q:")) {
                        query = tok;
                    } else if (tok.contains("%")) {
                        identity = tok.substring(0, tok.indexOf("%"));
                    } else if (tok.startsWith("E:")) {
                        eval = tok.substring(2);
                    } else if (tok.contains("RecName")) {
                        function = su.scapeSQL(tok.substring(tok.indexOf("=") + 1, tok.length() - 1));//-1 por que termina con;
                    } else {
                        tax = tok;
                    }
                }
                if (transacciones.validaUniprotID(id1)) {
                    id1 = id1;
                } else {//no está anotada
                    //transacciones inserta SWISS_PROT
                    // MetaxaDAO mdao = new MetaxaDAO(transacciones);
                    // String taxo[] = mdao.searchNCBINode(tax);
                    String q = "INSERT INTO swiss_prot (uniprot_id, uniprot_acc, ncbi_tax_id, prot_name) "
                            + "VALUES('" + id1 + "','-1','-1','" + function + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando swiss_prot: " + id1 + "\nQ:" + q);
                    } else {
                        System.out.println("Nueva SwissProt: " + id1);
                    }
                }
                String q = "INSERT INTO gen_swiss_prot (uniprot_id, gen_id, prediction_method, eval,identity, query) "
                        + "VALUES('" + id1 + "','" + gen_id + "','" + metodo + "'," + eval + "," + identity + ",'" + query + "')";
                if (!transacciones.insertaQuery(q)) {
                    System.err.println("Error insertando swiss_prot_gen: " + id1 + " - " + gen_id);
                }
            }

        }
    }

    /**
     * Este método se encarga de parsear una linea de trinotate con ressultado
     * de pfam:
     * PF02441.14^Flavoprotein^Flavoprotein^6-134^E:2.4e-36`PF04127.10^DFP^DNA /
     * pantothenate metabolism flavoprotein^185-367^E:2.2e-70
     *
     * @param linea la linea a parsear
     * @param gen_id el id del gen al cual corresponde la predicción de pfam
     */
    public void procesaLineaPfamTrinotate(String linea, String gen_id) {
        if (!linea.equals(".")) {
            StringTokenizer st = new StringTokenizer(linea, "`");
            StringUtils su = new StringUtils();
            while (st.hasMoreTokens()) {
                String pfam_line = st.nextToken();
                StringTokenizer st_pfam = new StringTokenizer(pfam_line, "^");
                if (st_pfam.countTokens() < 5) {
                    System.err.println("Pfam con 5 o menos cols: " + st_pfam.countTokens());
                } else {
                    String pf = st_pfam.nextToken();
                    int indxPunto = pf.indexOf(".") != -1 ? pf.indexOf(".") : pf.length();
                    pf = pf.substring(0, indxPunto);
                    //si no exxiste el pfam llo anota en la base de datos.
                    String pf_id = st_pfam.nextToken();
                    String pfam_def = su.scapeSQL(st_pfam.nextToken());
                    String from_to = st_pfam.nextToken();
                    int from = 0;
                    int to = 0;
                    String eval = st_pfam.nextToken();
                    eval = eval.substring(2);
                    try {
                        from = Integer.parseInt(from_to.substring(0, from_to.indexOf("-")));
                        to = Integer.parseInt(from_to.substring(from_to.indexOf("-") + 1));
                    } catch (NumberFormatException nfe) {
                        from = 0;
                        to = 0;
                        System.err.println("Error parseando FROM-TO Pfam: " + from_to);
                    }
                    if (!transacciones.validaPfamAccID(pf)) {
                        String query = "INSERT INTO pfam(pfam_acc, id_pfam, pfam_deff, pfam_comments) VALUES"
                                + "('" + pf + "','" + pf_id + "','" + pfam_def + "','Annotated from trinotate')";
                        if (!transacciones.insertaQuery(query)) {
                            System.err.println("Error insertando pfam: " + query);
                        } else {
                            System.out.println("Nuevo PFAM: " + pf);
                        }
                    }
                    String q = "INSERT INTO gen_pfam (gen_id,pfam_acc,pfam_from,pfam_to,eval) "
                            + "VALUES('" + gen_id + "','" + pf + "'," + from + "," + to + "," + eval + ")";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando pfam_gen: " + pf + " - " + gen_id + "\nQ:" + q);
                    }
                }
            }
        }
    }

    /**
     * Este método se encarga de parsear una linea de trinotate con ressultado
     * de pfam:
     * PF02441.14^Flavoprotein^Flavoprotein^6-134^E:2.4e-36`PF04127.10^DFP^DNA /
     * pantothenate metabolism flavoprotein^185-367^E:2.2e-70 Es igual al metodo
     * procesaLineaPfamTrinotate pero utiliza Str.split()) en lugar de
     * strtokenizer
     *
     * @param linea la linea a parsear
     * @param gen_id el id del gen al cual corresponde la predicción de pfam
     */
    public void splitLineaPfamTrinotate(String linea, String gen_id) {
        if (!linea.equals(".")) {
            
            StringUtils su = new StringUtils();
            for (String pfam_line : linea.split("`")) {
                String tokens[] = pfam_line.split("\\^");
                if (tokens.length < 5) {

                } else {
                    String pf = tokens[0];
                    int indxPunto = pf.indexOf(".") != -1 ? pf.indexOf(".") : pf.length();
                    pf = pf.substring(0, indxPunto);
                    //si no exxiste el pfam llo anota en la base de datos.
                    String pf_id = tokens[1];
                    String pfam_def = su.scapeSQL(tokens[2]);
                    String from_to = tokens[3];
                    int from;
                    int to;
                    String eval = tokens[4];
                    eval = eval.substring(2);
                    try {
                        from = Integer.parseInt(from_to.substring(0, from_to.indexOf("-")));
                        to = Integer.parseInt(from_to.substring(from_to.indexOf("-") + 1));
                    } catch (NumberFormatException nfe) {
                        from = 0;
                        to = 0;
                        System.err.println("Error parseando FROM-TO Pfam: " + from_to);
                    }
                    if (!transacciones.validaPfamAccID(pf)) {
                        String query = "INSERT INTO pfam(pfam_acc, id_pfam, pfam_deff, pfam_comments) VALUES"
                                + "('" + pf + "','" + pf_id + "','" + pfam_def + "','Annotated from trinotate')";
                        if (!transacciones.insertaQuery(query)) {
                            System.err.println("Error insertando pfam: " + query);
                        } else {
                            System.out.println("Nuevo PFAM: " + pf);
                        }
                    }
                    String q = "INSERT INTO gen_pfam (gen_id,pfam_acc,pfam_from,pfam_to,eval) "
                            + "VALUES('" + gen_id + "','" + pf + "'," + from + "," + to + "," + eval + ")";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando pfam_gen: " + pf + " - " + gen_id + "\nQ:" + q);
                    }
                }

            }
        }
    }

    /**
     * Procesa una linea de trinotate para asignacion de COG/eggNOG.
     * Lamentablemente la anotación se asigna por inferencia del mejor hit de
     * blast, razón por la cual no se cuenta ni con un evalue ni ccon un from y
     * to para los cogs
     *
     * @param linea
     * @param gen_id
     */
    public void procesaLineaCogTrinotate(String linea, String gen_id) {
        if (!linea.trim().equals(".")) {
            StringTokenizer st = new StringTokenizer(linea, "`");
            StringUtils su = new StringUtils();
            while (st.hasMoreTokens()) {
                String cog_line = st.nextToken();
                StringTokenizer st_cog = new StringTokenizer(cog_line, "^");
                String ideggnog = st_cog.nextToken();
                if (!transacciones.validaNOG(ideggnog)) {
                    String desc = "";
                    if (st_cog.hasMoreTokens()) {
                        desc = su.scapeSQL(st_cog.nextToken());
                    }
                    if (ideggnog.startsWith("E")) {
                        //INSERTA EN EGGNOG
                        String q = "INSERT INTO eggnog (ideggnog, description) "
                                + "VALUES('" + ideggnog + "','" + desc + "')";
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando eggnog: " + ideggnog + "\nQ:" + q);
                        }
                    } else if (ideggnog.startsWith("N")) {
                        //INSERTA NOG
                        String q = "INSERT INTO nog (id_nog, nog_description) "
                                + "VALUES('" + ideggnog + "','" + desc + "')";
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando nog: " + ideggnog + "\nQ:" + q);
                        }
                    } else {
                        System.err.println("ID no esperado COG|EGGNOG|NOG: " + ideggnog);
                    }
                }
                if (ideggnog.startsWith("E")) {
                    String q = "INSERT INTO gen_eggnog (gen_id,ideggnog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando gen_eggnog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
                    }
                } else if (ideggnog.startsWith("C")) {
                    String q = "INSERT INTO gen_eggnog (gen_id,ideggnog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando gen_eggnog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
                    }
                    q = "INSERT INTO gen_cog (gen_id,id_cog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando gen_cog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
                    }
                } else if (ideggnog.startsWith("N")) {
                    String q = "INSERT INTO gen_nog (gen_id,id_nog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando gen_nog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
                    }
                }
            }
        }
    }

    /**
     * Procesa una linea de trinotate para asignacion de COG/eggNOG.
     * Lamentablemente la anotación se asigna por inferencia del mejor hit de
     * blast, razón por la cual no se cuenta ni con un evalue ni ccon un from y
     * to para los cogs
     *
     * @param linea
     * @param gen_id
     */
    public void splitLineaCogTrinotate(String linea, String gen_id) {
        if (!linea.trim().equals(".")) {
           
            StringUtils su = new StringUtils();
            for (String cog_line : linea.split("`")) {
                String tokens[] = cog_line.split("\\^");
                String ideggnog = tokens[0];
                if (!transacciones.validaNOG(ideggnog)) {
                    String desc = "";
                    if (tokens.length >= 2) {
                        desc = su.scapeSQL(tokens[1]);
                    }
                    if (ideggnog.startsWith("E")) {
                        //INSERTA EN EGGNOG
                        String q = "INSERT INTO eggnog (ideggnog, description) "
                                + "VALUES('" + ideggnog + "','" + desc + "')";
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando eggnog: " + ideggnog + "\nQ:" + q);
                        }
                    } else if (ideggnog.startsWith("N")) {
                        //INSERTA NOG
                        String q = "INSERT INTO nog (id_nog, nog_description) "
                                + "VALUES('" + ideggnog + "','" + desc + "')";
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando nog: " + ideggnog + "\nQ:" + q);
                        }
                    } else {
                        System.err.println("ID no esperado COG|EGGNOG|NOG: " + ideggnog);
                    }
                }
                if (ideggnog.startsWith("E")) {
                    String q = "INSERT INTO gen_eggnog (gen_id,ideggnog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando gen_eggnog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
                    }
                } else if (ideggnog.startsWith("C")) {
                    String q = "INSERT INTO gen_eggnog (gen_id,ideggnog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando gen_eggnog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
                    }
                    q = "INSERT INTO gen_cog (gen_id,id_cog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando gen_cog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
                    }
                } else if (ideggnog.startsWith("N")) {
                    String q = "INSERT INTO gen_nog (gen_id,id_nog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando gen_nog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
                    }
                }
            }
        }
    }

    /**
     * Este método procesa una linea de GO como la sgt:
     * GO:0005789^cellular_component^endoplasmic reticulum
     * membrane`GO:0016021^cellular_component^integral component of
     * membrane`GO:0016717^molecular_function^oxidoreductase activity, acting on
     * paired donors, with oxidation of a pair of donors resulting in the
     * reduction of molecular oxygen to two molecules of
     * water`GO:0006636^biological_process^unsaturated fatty acid biosynthetic
     * process`GO:0042761^biological_process^very long-chain fatty acid
     * biosynthetic process
     *
     * @param linea
     * @param gen_id
     */
    public void procesaLineaGOTrinotate(String linea, String gen_id, ArrayList<String> gos) {
        if (!linea.trim().equals(".")) {
            StringTokenizer st = new StringTokenizer(linea, "`");
            StringUtils su = new StringUtils();
            while (st.hasMoreTokens()) {
                String cog_line = st.nextToken();
                StringTokenizer st_go = new StringTokenizer(cog_line, "^");

                String idGO = st_go.nextToken();
                if (!gos.contains(idGO)) {
                    gos.add(idGO);
                    if (idGO.startsWith("GO:")) {
                        idGO = idGO.substring(3);//LE QUITAMOS EL GO:
                    }
                    String namespace = st_go.nextToken();
                    String name = su.scapeSQL(st_go.nextToken());
                    //no nos interesan mas tokens pues la anotacion funcional la tenemos nosotros
                    String q = "INSERT INTO gen_go (gen_id,id_GO) "
                            + "VALUES('" + gen_id + "','" + idGO + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando gen_go: " + idGO + " - " + gen_id + "\nQ:" + q);
                    }
                    if (!transacciones.validaGO(idGO)) {
                        String query = "INSERT INTO gontology(id_GO, go_name,namespace) VALUES ('"
                                + idGO + "','" + name + "','" + namespace + "')";
                        if (!transacciones.insertaQuery(query)) {
                            System.err.println("Error insertando GO: " + idGO + "\nQ:" + q);
                        }
                    }
                }
            }
        }
    }

    /**
     * Este método procesa una linea de GO como la sgt:
     * GO:0005789^cellular_component^endoplasmic reticulum
     * membrane`GO:0016021^cellular_component^integral component of
     * membrane`GO:0016717^molecular_function^oxidoreductase activity, acting on
     * paired donors, with oxidation of a pair of donors resulting in the
     * reduction of molecular oxygen to two molecules of
     * water`GO:0006636^biological_process^unsaturated fatty acid biosynthetic
     * process`GO:0042761^biological_process^very long-chain fatty acid
     * biosynthetic process
     *
     * @param linea
     * @param gen_id
     */
    public void splitLineaGOTrinotate(String linea, String gen_id, ArrayList<String> gos) {
        if (!linea.trim().equals(".")) {
           
            StringUtils su = new StringUtils();
            for (String go_line : linea.split("`")) {
                String tokens[] = go_line.split("\\^");
                String idGO = tokens[0];
                if (!gos.contains(idGO)) {
                    gos.add(idGO);
                    if (idGO.startsWith("GO:")) {
                        idGO = idGO.substring(3);//LE QUITAMOS EL GO:
                    }
                    String namespace = tokens[1];
                    String name = su.scapeSQL(tokens[2]);
                    //no nos interesan mas tokens pues la anotacion funcional la tenemos nosotros
                    String q = "INSERT INTO gen_go (gen_id,id_GO) "
                            + "VALUES('" + gen_id + "','" + idGO + "')";
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando gen_go: " + idGO + " - " + gen_id + "\nQ:" + q);
                    }
                    if (!transacciones.validaGO(idGO)) {
                        String query = "INSERT INTO gontology(id_GO, go_name,namespace) VALUES ('"
                                + idGO + "','" + name + "','" + namespace + "')";
                        if (!transacciones.insertaQuery(query)) {
                            System.err.println("Error insertando GO: " + idGO + "\nQ:" + q);
                        }
                    }
                }
            }
        }
    }

    /**
     * Este método se encarga de procesar una linea de signalP de trinotate y
     * actualizar el gen con dicha propiedad
     *
     * @param linea sigP:1^21^0.679^YES
     * @param gen_id
     */
    public void procesaSignalPTrinotate(String linea, String gen_id) {
        if (!linea.trim().equals(".")) {
            if (linea.startsWith("sigP:")) {
                linea = linea.substring(5);
            }           
            StringTokenizer st = new StringTokenizer(linea, "^");
            String from = st.nextToken();
            String to = st.nextToken();
            String value = st.nextToken();
            try {
                Integer.parseInt(from);
                Integer.parseInt(to);
                Double.parseDouble(value);
                String q = "UPDATE gen SET signal_p = true, "
                        + "signal_from = " + from + ", signal_to = " + to
                        + ", signal_val = " + value + " WHERE gen_id ='" + gen_id + "'";

                if (!transacciones.insertaQuery(q)) {
                    System.err.println("Error actualiando signalP: " + gen_id + "\nQ:" + q);
                }
            } catch (NumberFormatException nfe) {
                System.err.println("Error parseando SignaP f-t-v:" + from + " - " + to + " - " + value);
            }

        }
    }
}
