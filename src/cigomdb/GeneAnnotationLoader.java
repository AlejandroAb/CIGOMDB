/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import bobjects.ArchivoObj;
import bobjects.Usuario;
import dao.ArchivoDAO;
import dao.ClasificacionDAO;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.FileUtils;
import utils.MyDate;
import utils.StringUtils;

/**
 * Esta clase esta diseñada para parssear archivos de anotación funcional de
 * genes como ser resultrados por Trinotate o EggNogMapper
 *
 * @author Alejandro
 */
public class GeneAnnotationLoader {

    private Transacciones transacciones;
    private boolean toFile = false;
    private String outFile;
    private String equiv_names_file;
    private HashMap<String, String> mapaEquivalencias;
    boolean useEquivalencia;
    private String postFix = "_c0_g1";
    private int nextIDArchivo = -1;

    public GeneAnnotationLoader(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public HashMap<String, String> getMapaEquivalencias() {
        return mapaEquivalencias;
    }

    public void setMapaEquivalencias(HashMap<String, String> mapaEquivalencias) {
        this.mapaEquivalencias = mapaEquivalencias;
    }

    public int getNextIDArchivo() {
        return nextIDArchivo;
    }

    public void setNextIDArchivo(int nextIDArchivo) {
        this.nextIDArchivo = nextIDArchivo;
    }

    public boolean usaEquivalencias() {
        return useEquivalencia;
    }

    public boolean isToFile() {
        return toFile;
    }

    public void setToFile(boolean toFile) {
        this.toFile = toFile;
    }

    public String getOutFile() {
        return outFile;
    }

    public void setOutFile(String outFile) {
        this.outFile = outFile;
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
            if (usaEquivalencias()) {
                llenaHashEquivalencias();
            }
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
                            String genMapTrinotate = st.nextToken();
                            if (usaEquivalencias()) {
                                genMapTrinotate = mapaEquivalencias.get(genMapTrinotate);
                            }
                            if (genMapTrinotate != null && genMapTrinotate.length() > 0) {
                                geneID = transacciones.getGeneIDByMapID(group, groupID, st.nextToken());
                                if (geneID.length() == 0) {
                                    System.err.println("No se encontró gen en BD. genMap = " + genMapTrinotate);
                                    break;
                                }
                            } else {
                                System.err.println("No se encontró gen en hashmapa. Entry = " + genMapTrinotate);
                                break;
                            }

                        } else if (tok == idxBlastX) {
                            procesaLineaBlastTrinotate(st.nextToken(), "BLASTX", geneID);
                        } else if (tok == idxBlastP) {
                            procesaLineaBlastTrinotate(st.nextToken(), "BLASTP", geneID);
                        } else if (tok == idxPfam) {
                            procesaLineaPfamTrinotate(st.nextToken(), geneID);
                        } else if (tok == idxSignalP) {
                            procesaSignalPTrinotate(st.nextToken(), geneID, null);
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

    public void loadTrinotateFileIntoDB(String trinoFile, String outFile, String groupID, String group) {
        ArchivoDAO adao = new ArchivoDAO(transacciones);
        File tmpFile = new File(trinoFile);
        ArchivoObj archivoTrino = new ArchivoObj(nextIDArchivo);
        archivoTrino.setTipoArchivo(ArchivoObj.TIPO_FUN);
        archivoTrino.setNombre(trinoFile.substring(trinoFile.lastIndexOf("/") + 1));
        int idx = trinoFile.lastIndexOf("/") != -1 ? trinoFile.lastIndexOf("/") + 1 : trinoFile.length();
        archivoTrino.setPath(trinoFile.substring(0, idx));
        archivoTrino.setDescription("Archivo proveninete de la ejecución de Trinotate v 3.0.1 para obtener la predicción funcional de genes."
                + "Este archivo provee la información para la anotación de COGs EggNOG, UniProt y términos GO.");
        archivoTrino.setExtension(trinoFile.substring(trinoFile.lastIndexOf(".") + 1));
        MyDate date = new MyDate(tmpFile.lastModified());
        archivoTrino.setDate(date);
        archivoTrino.setSize(tmpFile.length());
        if (tmpFile.length() / 1048576 < 1000) {//si es menor a un Gb
            archivoTrino.setChecksum(FileUtils.getMD5File(trinoFile));
        } else {
            archivoTrino.setChecksum("TBD");
        }
        archivoTrino.setAlcance("Grupo de bioinformática");
        archivoTrino.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        archivoTrino.setDerechos("Acceso limitado a miembros");
        archivoTrino.setTags("predicción funcional, trinotate, cogs, eggnog, uniprot");
        archivoTrino.setTipo("Text");
        Usuario user = new Usuario(20);//ALES 31 bio cicese
        user.setAcciones("creator");
        user.setComentarios("Se encarga de ejecutar el programa trinotate el cual realiza la predicción funcionaal sobre los genes predichos");
        archivoTrino.addUser(user);
        Usuario user2 = new Usuario(25);//ALEXSF   9 alexey
        user2.setAcciones("contributor");
        user2.setComentarios("Investigador responsable de subproyecto");
        archivoTrino.addUser(user2);
        int id = -1;
        try {
            id = Integer.parseInt(groupID);
        } catch (NumberFormatException nfe) {
            System.err.println("Error al determinar el ID del " + group + " val :" + group);
        }
        adao.insertaArchivoMetaGenoma(archivoTrino, id, group, toFile, outFile, true);
        nextIDArchivo++;
    }

    public void loadCovergaeFileIntoDB(String covergaeFile, String outFile, String groupID, String group) {
        ArchivoDAO adao = new ArchivoDAO(transacciones);
        File tmpFile = new File(covergaeFile);
        ArchivoObj archivoTrino = new ArchivoObj(nextIDArchivo);
        archivoTrino.setTipoArchivo(ArchivoObj.TIPO_FUN);
        archivoTrino.setNombre(covergaeFile.substring(covergaeFile.lastIndexOf("/") + 1));
        int idx = covergaeFile.lastIndexOf("/") != -1 ? covergaeFile.lastIndexOf("/") + 1 : covergaeFile.length();
        archivoTrino.setPath(covergaeFile.substring(0, idx));
        archivoTrino.setDescription("Archivo con las coverturas de los genes mapeados acorde a los ensambles y predicción de genes realizada. Este archivo es generado mediante sam y bam tools.");
        archivoTrino.setExtension(covergaeFile.substring(covergaeFile.lastIndexOf(".") + 1));
        MyDate date = new MyDate(tmpFile.lastModified());
        archivoTrino.setDate(date);
        archivoTrino.setSize(tmpFile.length());
        if (tmpFile.length() / 1048576 < 1000) {//si es menor a un Gb
            archivoTrino.setChecksum(FileUtils.getMD5File(covergaeFile));
        } else {
            archivoTrino.setChecksum("TBD");
        }
        archivoTrino.setAlcance("Grupo de bioinformática");
        archivoTrino.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        archivoTrino.setDerechos("Acceso limitado a miembros");
        archivoTrino.setTags("cobertura, mapeo, sam, bam, coverage");
        archivoTrino.setTipo("Text");
        Usuario user = new Usuario(20);//ALES
        user.setAcciones("creator");
        user.setComentarios("Se encarga de ejecutar el programa trinotate el cual realiza la predicción funcionaal sobre los genes predichos");
        archivoTrino.addUser(user);
        Usuario user2 = new Usuario(25);//ALEXSF
        user2.setAcciones("contributor");
        user2.setComentarios("Investigador responsable de subproyecto");
        archivoTrino.addUser(user2);
        int id = -1;
        try {
            id = Integer.parseInt(groupID);
        } catch (NumberFormatException nfe) {
            System.err.println("Error al determinar el ID del " + group + " val :" + group);
        }
        adao.insertaArchivoMetaGenoma(archivoTrino, id, group, toFile, outFile, true);
        nextIDArchivo++;
    }

    /**
     * Este método se encarga de cargar los archivos de trinotate. Hay que tomar
     * en cuenta que algunos parámetros se controlan como variables de clase y
     * se hace llamado de sus seters a la hora de invocar el metodo, el mas
     * importante de ellos, es la variable para equivnames useEquivalencia
     *
     * @param inputFile trinotate.reduced
     * @param group genoma o metagenoma
     * @param groupID el id del metagenoma o genoma
     * @return
     */
    public boolean splitTrinotateFile(String inputFile, String group, String groupID) {
        try {
            if (nextIDArchivo == -1) {
                nextIDArchivo = transacciones.getNextIDArchivos();
                if (nextIDArchivo == -1) {
                    System.err.println("ERROR No se puede determinar el siguiente ID de archivo");
                }
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1"));
            loadTrinotateFileIntoDB(inputFile, outFile, groupID, group);
            FileWriter writer = null;
            if (toFile) {
                writer = new FileWriter(outFile,true);
            }
            if (usaEquivalencias()) {
                llenaHashEquivalencias();
            }
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
            int idxKegg = 8;
            int idxGOBlast = 9;
            int idxGOPfam = 10;

            while ((linea = reader.readLine()) != null) {
                numLinea++;
                if (numLinea == 1) {
                    int toks = 0;
                    for (String tok : linea.split("\t")) {
                        toks++;
                        tok = tok.trim().toLowerCase();
                        if (tok.contains("ontology") && tok.contains("blast")) {//GO
                            idxGOBlast = toks;
                        } else if (tok.contains("ontology") && tok.contains("pfam")) {//PFAM ONTOLGY
                            idxGOPfam = toks;
                        } else if (tok.contains("gene_id")) {
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
                        } else if (tok.contains("tmhmm")) {
                            idxTransM = toks;
                        } else if (tok.equals("kegg")) {
                            idxKegg = toks;
                        }
                    }
                } else {
                    ArrayList<String> gos = new ArrayList<String>();
                    int tok = 0;
                    String geneID = null;
                    for (String token : linea.split("\t")) {
                        tok++;
                        if (tok == idxGeneID) {
                            String genMapTrinotate = token;
                            if (usaEquivalencias()) {
                                genMapTrinotate = mapaEquivalencias.get(genMapTrinotate);
                            }
                            if (genMapTrinotate != null && genMapTrinotate.length() > 0) {
                                geneID = transacciones.getGeneIDByMapID(group, groupID, genMapTrinotate);
                                if (geneID.length() == 0) {
                                    System.err.println("No se encontró gen en BD. genMap = " + genMapTrinotate);
                                    break;
                                }
                            } else {
                                System.err.println("No se encontró gen en hashmapa. Entry = " + genMapTrinotate);
                                break;
                            }
                        } else if (tok == idxBlastX) {
                            splitLineaBlastTrinotate(token, "BLASTX", geneID, writer);
                        } else if (tok == idxBlastP) {
                            splitLineaBlastTrinotate(token, "BLASTP", geneID, writer);
                        } else if (tok == idxPfam) {
                            splitLineaPfamTrinotate(token, geneID, writer);
                        } else if (tok == idxSignalP) {
                            procesaSignalPTrinotate(token, geneID, writer);
                        } else if (tok == idxTransM) {
                            //procesaLineTrans

                        } else if (tok == idxCog) {
                            splitLineaCogTrinotate(token, geneID, writer);
                        } else if (tok == idxGOBlast || tok == idxGOPfam) {
                            splitLineaGOTrinotate(token, geneID, gos, writer);
                        } else if (tok == idxKegg) {
                            splitLineaKegg(token, "TRINITY", geneID, writer);
                        } else {
                            // st.nextToken();

                        }
                    }
                }

            }
            if (toFile) {
                writer.close();
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
     * Método para capturar las coberuras
     *
     * @param idmetagenoma el id del metagenoma para el cual corresponde la
     * cobertura
     * @param inputFile el archivo dde entrada con las coberturas
     * @return
     */
    public String creaCoverage(int idmetagenoma, String inputFile) {
        String log = "";
        if (nextIDArchivo == -1) {
            nextIDArchivo = transacciones.getNextIDArchivos();
            if (nextIDArchivo == -1) {
                System.err.println("ERROR No se puede determinar el siguiente ID de archivo");
            }
        }
        try {
            FileWriter writer = null;
            if (toFile) {
                writer = new FileWriter(outFile);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            loadCovergaeFileIntoDB(inputFile, outFile, "" + idmetagenoma, "metagenoma");
            String line;
            String query;
            while ((line = reader.readLine()) != null) {
                String parts[] = line.split("\t");
                String gen_id = transacciones.getGeneIDByContigInfo("metagenoma", "" + idmetagenoma, parts[0], parts[1], parts[2]);
                query = "UPDATE gen SET cobertura = " + parts[3] + " WHERE gen_id = '" + gen_id + "'";
                if (!gen_id.equals("ERROR")) {
                    if (toFile) {
                        writer.write(query+";\n");
                    }
                }
            }
            if (toFile) {
                writer.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return log;

    }

    public String getEquiv_names_file() {
        return equiv_names_file;
    }

    public void setEquiv_names_file(String equiv_names_file) {
        this.equiv_names_file = equiv_names_file;
    }

    public boolean isUseEquivalencia() {
        return useEquivalencia;
    }

    public void setUseEquivalencia(boolean useEquivalencia) {
        this.useEquivalencia = useEquivalencia;
    }

    public String getPostFix() {
        return postFix;
    }

    public void setPostFix(String postFix) {
        this.postFix = postFix;
    }

    /**
     * Este método se encarga de llenar una tabla hash con las equivalencias
     * entre el archivo de trinotate y los genes anotados en la base de datos.
     * El problema es que para algunas anotaciones funcionales, el gene_1 en el
     * archivo de trinotate no equivale al gene_1 que sale del ensamble. Es poor
     * esto que se hace una tabla de equivalencias. Algo importante es que en el
     * archivo generalmente llamado equiv_names.txt (a la altura de
     * trinotate_annotation_report... tiene dos columnas:______________________
     * *** gene_221859_c0_g1 gene_99994|GeneMark.hmm|651_nt|-|2|652 **********
     * *** gene_221860_c0_g1 gene_99995|GeneMark.hmm|423_nt|-|649|1071 ********
     * En la primera viene el nombre del gen como aparece en el archivo de
     * trinotate ORIGINAL, el REDUCED se llama igual pero sin el postfijo
     * "_c0_g1" y en la segunda el nombre con el que se encuentra en el
     * ensamble.
     *
     */
    public void llenaHashEquivalencias() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(equiv_names_file), "ISO-8859-1"));
            mapaEquivalencias = new HashMap<>();
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("gene_")) {
                    String genes[] = linea.split("\t");
                    String trinoGen = genes[0].substring(0, genes[0].indexOf(postFix));
                    int index = genes[1].indexOf("|") != -1 ? genes[1].indexOf("|") : genes[1].length();
                    String dbMapGen = genes[1].substring(0, index);
                    mapaEquivalencias.put(trinoGen, dbMapGen);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);

        }
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

                        ClasificacionDAO mdao = new ClasificacionDAO(transacciones);
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
    public void splitLineaBlastTrinotate(String linea, String metodo, String gen_id, FileWriter writer) {
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
                    if (toFile) {
                        try {
                            writer.write(q + ";\n");
                        } catch (IOException ex) {
                            System.err.println("Error escribiendo archivo para swiss_prot_gen: " + gen_id + " - " + outFile);
                            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando swiss_prot: " + id1 + "\nQ:" + q);
                        } else {
                            System.out.println("Nueva SwissProt: " + id1);
                        }
                    }
                }
                String q = "INSERT INTO gen_swiss_prot (uniprot_id, gen_id, prediction_method, eval,identity, query) "
                        + "VALUES('" + id1 + "','" + gen_id + "','" + metodo + "'," + eval + "," + identity + ",'" + query + "')";
                if (toFile) {
                    try {
                        writer.write(q + ";\n");
                    } catch (IOException ex) {
                        System.err.println("Error escribiendo archivo para swiss_prot_gen: " + gen_id + " - " + outFile);
                        Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error insertando swiss_prot_gen: " + id1 + " - " + gen_id);
                    }
                }
            }

        }
    }

    /**
     * Este método se encarga de anotar en la BD un resultado de asignación de
     * ortología de kegg acorde a trinotate
     *
     * @param linea la linea con todo el resultado
     * @param metodo el método con el cual fue obtenido dicho resultado, en este
     * caso Trinity
     * @param gen_id El id del gen para el cual fue obtenido este hit
     * @param writer el writer para escribir el resultado
     */
    public void splitLineaKegg(String linea, String metodo, String gen_id, FileWriter writer) {
        if (!linea.trim().equals(".")) {
            // StringTokenizer st_l = new StringTokenizer(linea, "`");
            StringUtils su = new StringUtils();
            boolean findGen = false; //solo anota el primer gen de kegg que referencía
            for (String kegg_line : linea.split("`")) {
                if (kegg_line.startsWith("KEGG:") && !findGen) {
                    findGen = true;
                    String q = "UPDATE gen SET kegg_gen = '" + kegg_line.substring(5).replaceAll(":", "-") + "' "
                            + "WHERE gen_id = '" + gen_id + "'";
                    if (toFile) {
                        try {
                            writer.write(q + ";\n");
                        } catch (IOException ex) {
                            System.err.println("Error escribiendo archivo para actualizar gen kegg_gen: " + gen_id + " - " + outFile);
                            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error actualizando gen kegg_gen: " + gen_id + "\nQ:" + q);
                        }
                    }
                } else if (kegg_line.startsWith("KO:")) {
                    String q = "INSERT INTO  gen_KO(gen_id, idKO, metodo) VALUES ('" + gen_id + "','" + kegg_line.substring(3) + "', '" + metodo + "')";

                    if (toFile) {
                        try {
                            writer.write(q + ";\n");
                        } catch (IOException ex) {
                            System.err.println("Error escribiendo archivo para gen_KO: " + gen_id + " - " + outFile);
                            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error actualizando gen_KO: " + gen_id + "\nQ:" + q);
                        }
                    }
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
    public void splitLineaPfamTrinotate(String linea, String gen_id, FileWriter writer) {
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
                        if (toFile) {
                            try {
                                writer.write(query + ";\n");
                            } catch (IOException ex) {
                                System.err.println("Error escribiendo archivo para pfam_gen: " + gen_id + " - " + outFile);
                                Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } else {
                            if (!transacciones.insertaQuery(query)) {
                                System.err.println("Error insertando pfam: " + query);
                            } else {
                                System.out.println("Nuevo PFAM: " + pf);
                            }
                        }
                    }
                    String q = "INSERT INTO gen_pfam (gen_id,pfam_acc,pfam_from,pfam_to,eval) "
                            + "VALUES('" + gen_id + "','" + pf + "'," + from + "," + to + "," + eval + ")";
                    if (toFile) {
                        try {
                            writer.write(q + ";\n");
                        } catch (IOException ex) {
                            System.err.println("Error escribiendo archivo para pfam_gen: " + gen_id + " - " + outFile);
                            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando pfam_gen: " + pf + " - " + gen_id + "\nQ:" + q);
                        }
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
    public void splitLineaCogTrinotate(String linea, String gen_id, FileWriter writer) {
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
                        if (toFile) {
                            try {
                                writer.write(q + ";\n");
                            } catch (IOException ex) {
                                System.err.println("Error escribiendo archivo para swiss_prot_gen: " + gen_id + " - " + outFile);
                                Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            if (!transacciones.insertaQuery(q)) {
                                System.err.println("Error insertando eggnog: " + ideggnog + "\nQ:" + q);
                            }
                        }
                    } else if (ideggnog.startsWith("N")) {
                        //INSERTA NOG
                        String q = "INSERT INTO nog (id_nog, nog_description) "
                                + "VALUES('" + ideggnog + "','" + desc + "')";
                        if (toFile) {
                            try {
                                writer.write(q + ";\n");
                            } catch (IOException ex) {
                                System.err.println("Error escribiendo archivo para nog: " + ideggnog + " - " + outFile);
                                Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } else {
                            if (!transacciones.insertaQuery(q)) {
                                System.err.println("Error insertando nog: " + ideggnog + "\nQ:" + q);
                            }
                        }
                    } else {
                        System.err.println("ID no esperado COG|EGGNOG|NOG: " + ideggnog);
                    }
                }
                if (ideggnog.startsWith("E")) {
                    String q = "INSERT INTO gen_eggnog (gen_id,ideggnog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (toFile) {
                        try {
                            writer.write(q + ";\n");
                        } catch (IOException ex) {
                            System.err.println("Error escribiendo archivo para gen_egnog: " + gen_id + " - " + outFile);
                            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando gen_eggnog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
                        }
                    }
                } else if (ideggnog.startsWith("C")) {
                    String q = "INSERT INTO gen_eggnog (gen_id,ideggnog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (toFile) {
                        try {
                            writer.write(q + ";\n");
                        } catch (IOException ex) {
                            System.err.println("Error escribiendo archivo para gen_eggnog: " + gen_id + " - " + outFile);
                            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando gen_eggnog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
                        }
                    }
                    q = "INSERT INTO gen_cog (gen_id,id_cog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (toFile) {
                        try {
                            writer.write(q + ";\n");
                        } catch (IOException ex) {
                            System.err.println("Error escribiendo archivo para gen_cog: " + gen_id + " - " + outFile);
                            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando gen_cog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
                        }
                    }
                } else if (ideggnog.startsWith("N")) {
                    String q = "INSERT INTO gen_nog (gen_id,id_nog) "
                            + "VALUES('" + gen_id + "','" + ideggnog + "')";
                    if (toFile) {
                        try {
                            writer.write(q + ";\n");
                        } catch (IOException ex) {
                            System.err.println("Error escribiendo archivo para gen_nog: " + gen_id + " - " + outFile);
                            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando gen_nog: " + ideggnog + " - " + gen_id + "\nQ:" + q);
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
    public void splitLineaGOTrinotate(String linea, String gen_id, ArrayList<String> gos, FileWriter writer) {
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
                    if (toFile) {
                        try {
                            writer.write(q + ";\n");
                        } catch (IOException ex) {
                            System.err.println("Error escribiendo archivo para swiss_prot_gen: " + gen_id + " - " + outFile);
                            Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        if (!transacciones.insertaQuery(q)) {
                            System.err.println("Error insertando gen_go: " + idGO + " - " + gen_id + "\nQ:" + q);
                        }
                    }

                    if (!transacciones.validaGO(idGO)) {
                        String query = "INSERT INTO gontology(id_GO, go_name,namespace) VALUES ('"
                                + idGO + "','" + name + "','" + namespace + "')";
                        if (toFile) {
                            try {
                                writer.write(q + ";\n");
                            } catch (IOException ex) {
                                System.err.println("Error escribiendo archivo para swiss_prot_gen: " + gen_id + " - " + outFile);
                                Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } else {
                            if (!transacciones.insertaQuery(query)) {
                                System.err.println("Error insertando GO: " + idGO + "\nQ:" + q);
                            }
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
    public void procesaSignalPTrinotate(String linea, String gen_id, FileWriter writer) {
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
                if (toFile) {
                    try {
                        writer.write(q + ";\n");
                    } catch (IOException ex) {
                        System.err.println("Error escribiendo archivo para signalP: " + gen_id + " - " + outFile);
                        Logger.getLogger(GeneAnnotationLoader.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    if (!transacciones.insertaQuery(q)) {
                        System.err.println("Error actualizando signalP: " + gen_id + "\nQ:" + q);
                    }
                }
            } catch (NumberFormatException nfe) {
                System.err.println("Error parseando SignaP f-t-v:" + from + " - " + to + " - " + value);
            }

        }
    }
}
