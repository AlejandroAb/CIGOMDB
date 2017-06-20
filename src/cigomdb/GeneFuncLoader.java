/*
 * Esta clase esta diseñada como clase principal para procesar resultados producto
 * del análisis de metgenomas por tecnologia shotgun
 */
package cigomdb;

import bobjects.ArchivoObj;
import bobjects.GenObj;
import bobjects.GenSeqObj;
import bobjects.Intergenic;
import bobjects.Usuario;
import dao.ArchivoDAO;
import dao.GenDAO;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.FastaReader;
import utils.FileUtils;
import utils.GFFLine;
import utils.GFFReader;
import utils.MyDate;
import utils.Sequence;

/**
 * CIGOM. MAYO 2016
 *
 * @author Alejandro Abdala
 */
public class GeneFuncLoader {

    private Transacciones transacciones = null;
    private boolean debug = false;
    private int nextIDArchivo = -1;
    private String raw_ext="";
    private String raw_data_path = "";
    public GeneFuncLoader(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public String getRaw_ext() {
        return raw_ext;
    }

    public void setRaw_ext(String raw_ext) {
        this.raw_ext = raw_ext;
    }

    public String getRaw_data_path() {
        return raw_data_path;
    }

    public void setRaw_data_path(String raw_data_path) {
        this.raw_data_path = raw_data_path;
    }

    public boolean isDebug() {
        return debug;
    }

    public Transacciones getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public int getNextIDArchivo() {
        return nextIDArchivo;
    }

    public void setNextIDArchivo(int nextIDArchivo) {
        this.nextIDArchivo = nextIDArchivo;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
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

    /**
     * Este métoddo se encarga de ccrear los archivos y sus respectivas
     * relaciones para un metagenoma
     *
     * @param id ID del metagenoma
     * @param source metagenoma|genoma
     * @param outFile si es a outfile nombre y path completo
     * @param contigFile path absoluto al archivo de contigs
     * @param protFile path absoluto al archivo de proteinas
     * @param nucFile path absoluto al archivo de nucleótidos
     * @param gffFile path absoluto al archivo de coordenadas
     */
    public void loadFiles(int id, String source, String outFile, String contigFile, String protFile, String nucFile, String gffFile) {
        ArchivoDAO adao = new ArchivoDAO(transacciones);
        /**
         * Archivo de contigs
         */
        int idContigs = nextIDArchivo;
        File tmpFile = new File(contigFile);
        ArchivoObj contigA = new ArchivoObj(nextIDArchivo);
        contigA.setTipoArchivo(ArchivoObj.TIPO_PRE);
        contigA.setNombre(contigFile.substring(contigFile.lastIndexOf("/") + 1));
        int idx = contigFile.lastIndexOf("/") != -1 ? contigFile.lastIndexOf("/") + 1 : contigFile.length();
        contigA.setPath(contigFile.substring(0, idx));
        contigA.setDescription("Archivo fasta con las secuencias de los contigs ensamblados");
        contigA.setExtension(contigFile.substring(contigFile.lastIndexOf(".") + 1));
        MyDate date = new MyDate(tmpFile.lastModified());
        contigA.setDate(date);
        contigA.setSize(tmpFile.length());
        if (tmpFile.length() / 1048576 < 1000) {//si es menor a un Gb
            contigA.setChecksum(FileUtils.getMD5File(contigFile));
        } else {
            contigA.setChecksum("TBD");
        }
        contigA.setAlcance("Grupo de bioinformática");
        contigA.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        contigA.setDerechos("Acceso limitado a miembros");
        contigA.setTags("Ensamble, assembly, contigs");
        contigA.setTipo("Text");
        Usuario user = new Usuario(21);//ALES
        user.setAcciones("creator");
        user.setComentarios("Se encarga de ejecutar el programa IDBA el cual realiza el ensamble de las secuencias crudas");
        contigA.addUser(user);
        Usuario user2 = new Usuario(25);//ALEXSF
        user2.setAcciones("contributor");
        user2.setComentarios("Investigador responsable de subproyecto");
        contigA.addUser(user2);
        adao.insertaArchivoMetaGenoma(contigA, id, source, outFile.length() > 2, outFile, true);
        nextIDArchivo++;

        /**
         * Archivo de proteinas
         */
        tmpFile = new File(protFile);
        ArchivoObj protA = new ArchivoObj(nextIDArchivo);
        protA.setTipoArchivo(ArchivoObj.TIPO_ASS);
        protA.setNombre(protFile.substring(protFile.lastIndexOf("/") + 1));
        idx = protFile.lastIndexOf("/") != -1 ? protFile.lastIndexOf("/") + 1 : protFile.length();
        protA.setPath(protFile.substring(0, idx));
        protA.setDescription("Archivo fasta con las secuencias de proteinas predichas por MGM");
        protA.setExtension(protFile.substring(protFile.lastIndexOf(".") + 1));
        date = new MyDate(tmpFile.lastModified());
        protA.setDate(date);
        protA.setSize(tmpFile.length());
        if (tmpFile.length() / 1048576 < 1000) {//si es menor a un Gb
            protA.setChecksum(FileUtils.getMD5File(protFile));
        } else {
            protA.setChecksum("TBD");
        }
        protA.setAlcance("Grupo de bioinformática");
        protA.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        protA.setDerechos("Acceso limitado a miembros");
        protA.setTags("Secuencias, aminoácidos, proteínas");
        protA.setTipo("Text");
        protA.setOrigen("" + idContigs);
        Usuario userP = new Usuario(21);//ALES
        userP.setAcciones("creator");
        userP.setComentarios("Se encarga de ejecutar el programa MetaGeneMark para predicción de CDS");
        protA.addUser(userP);
        Usuario user2P = new Usuario(25);//ALEXSF
        user2P.setAcciones("contributor");
        user2P.setComentarios("Investigador responsable de subproyecto");
        protA.addUser(user2P);
        adao.insertaArchivoMetaGenoma(protA, id, source, outFile.length() > 2, outFile, true);
        nextIDArchivo++;
        /**
         * Archivo de nucleótidos
         */
        tmpFile = new File(nucFile);
        ArchivoObj nucA = new ArchivoObj(nextIDArchivo);
        nucA.setTipoArchivo(ArchivoObj.TIPO_ASS);
        nucA.setNombre(nucFile.substring(nucFile.lastIndexOf("/") + 1));
        idx = nucFile.lastIndexOf("/") != -1 ? nucFile.lastIndexOf("/") + 1 : nucFile.length();
        nucA.setPath(nucFile.substring(0, idx));
        nucA.setDescription("Archivo fasta con las secuencias de nucleótidos predichas por MGM");
        nucA.setExtension(nucFile.substring(nucFile.lastIndexOf(".") + 1));
        date = new MyDate(tmpFile.lastModified());
        nucA.setDate(date);
        nucA.setSize(tmpFile.length());
        if (tmpFile.length() / 1048576 < 1000) {//si es menor a un Gb
            nucA.setChecksum(FileUtils.getMD5File(nucFile));
        } else {
            nucA.setChecksum("TBD");
        }
        nucA.setAlcance("Grupo de bioinformática");
        nucA.setOrigen("" + idContigs);
        nucA.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        nucA.setDerechos("Acceso limitado a miembros");
        nucA.setTags("Secuencias pareadas, amplicones");
        nucA.setTipo("Text");
        Usuario userN = new Usuario(21);//ALES
        userN.setAcciones("creator");
        userN.setComentarios("Se encarga de ejecutar el MetaGeneMark sobre el archivo de contigs.fa que da como resultado este archivo.");
        nucA.addUser(userN);
        Usuario user2N = new Usuario(25);//ALEXSF
        user2N.setAcciones("contributor");
        user2N.setComentarios("Investigador responsable de subproyecto");
        nucA.addUser(user2N);
        adao.insertaArchivoMetaGenoma(nucA, id, source, outFile.length() > 2, outFile, true);
        nextIDArchivo++;

        /**
         * Archivo GFF
         */
        tmpFile = new File(gffFile);
        ArchivoObj gffA = new ArchivoObj(nextIDArchivo);
        gffA.setTipoArchivo(ArchivoObj.TIPO_PRE);
        gffA.setNombre(gffFile.substring(gffFile.lastIndexOf("/") + 1));
        idx = gffFile.lastIndexOf("/") != -1 ? gffFile.lastIndexOf("/") + 1 : gffFile.length();
        gffA.setPath(gffFile.substring(0, idx));
        gffA.setDescription("Archivo gff con las coordenadas de las genes mapeados en el archivo de contigs. Este archivo es resultado de la prediccion de genes realizada por MGM");
        gffA.setExtension(gffFile.substring(gffFile.lastIndexOf(".") + 1));
        date = new MyDate(tmpFile.lastModified());
        gffA.setDate(date);
        gffA.setSize(tmpFile.length());
        if (tmpFile.length() / 1048576 < 1000) {//si es menor a un Gb
            gffA.setChecksum(FileUtils.getMD5File(gffFile));
        } else {
            gffA.setChecksum("TBD");
        }
        gffA.setAlcance("Grupo de bioinformática");
        gffA.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        gffA.setDerechos("Acceso limitado a miembros");
        gffA.setTags("GFF, Predicción de genes, coordenadas");
        gffA.setTipo("Text");
        Usuario userG = new Usuario(21);//ALES
        userG.setAcciones("creator");
        userG.setComentarios("Se encarga de ejecutar el predictor de genes para obbtener las coordenadas en los contigs.");
        gffA.addUser(userG);
        Usuario user2G = new Usuario(25);//ALEXSF
        user2G.setAcciones("contributor");
        user2G.setComentarios("Investigador responsable de subproyecto");
        gffA.addUser(user2G);
        adao.insertaArchivoMetaGenoma(gffA, id, source, outFile.length() > 2, outFile, true);
    }

    /**
     * Método que da de alta los archivos crudos en la base de datos.
     *
     * @param idgroup
     * @param group
     * @param outFile
     * @param raw_data_path
     * @param raw_ext
     */
    public void loadRawFileIntoDB(int idgroup, String group, String outFile, String raw_data_path, String raw_ext) {
        ArchivoDAO adao = new ArchivoDAO(transacciones);
        File rawFolder = new File(raw_data_path);
        if (rawFolder.exists()) {
            for (File f : rawFolder.listFiles()) {
                if (f.getName().endsWith(raw_ext)) {
                    ArchivoObj rawFile = new ArchivoObj(nextIDArchivo);
                    nextIDArchivo++;
                    rawFile.setTipoArchivo(ArchivoObj.TIPO_RAW);
                    rawFile.setNombre(f.getName());
                    rawFile.setPath(raw_data_path);
                    rawFile.setExtension(raw_ext);
                    MyDate date = new MyDate(f.lastModified());
                    rawFile.setDate(date);
                    rawFile.setSize(f.length());

                    if (f.getName().contains("R1")) {
                        rawFile.setDescription("Secuencias crudas de " + group + ". Secuencias FW");
                    } else if (f.getName().contains("R2")) {
                        rawFile.setDescription("Secueencias crudas de " + group + ". Secuencias RV");
                    } else {
                        rawFile.setDescription("Secuencias crudas de " + group);
                    }
                    if (f.length() / 1048576 < 1000) {//si es menor a un Gb
                        rawFile.setChecksum(FileUtils.getMD5File(raw_data_path + f.getName()));
                    } else {
                        rawFile.setChecksum("TBD");
                    }
                    rawFile.setAlcance("Grupo de bioinformática");
                    rawFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
                    rawFile.setDerechos("Acceso limitado a miembros");
                    rawFile.setTags("Secuencias crudas, amplicones");
                    rawFile.setTipo("Text");
                    Usuario user = new Usuario(24);//UUSM
                    user.setAcciones("creator");
                    user.setComentarios("Se encargaron de generar las librerías que se mandaron a secuenciar y de donde se obtienen las secuencias");
                    rawFile.addUser(user);

                    Usuario user2 = new Usuario(26);//unidad de secuenciacion
                    user2.setAcciones("contributor");
                    user2.setComentarios("Centro de secuenciación");
                    rawFile.addUser(user2);
                    //marcador.addArchivo(rawFile);
                    adao.insertaArchivoMetaGenoma(rawFile, idgroup, group, true, outFile, true);
                    //log += adao.insertaArchivo(rawFile, false, "", true);
                    //transacciones.insertaArchivoMarcador(idMarcador, rawFile.getIdArchivo());
                }
            }
        } else {
            System.err.println("No existe directorio: " + raw_data_path);
            //  return false;
        }
    }
    //public boolean 
    //String idPre, String gffFile, String nucFile, String aaFile, String mapPrefix
    /**
     *
     * @param idPrefix el prefijo que es usado para el id dl gen
     * @param idMetageno el metagenoma al que se relaciona
     * @param idGenoma el genoma al que esta relaccionado, puede ser a un genoma
     * o a un metagenoma pero no ambos
     * @param gffFile el archivo de coordenadas
     * @param contigFile el archivo de secuencias
     * @param nucFile el archivo de nucleotidos coorrespondiente a las
     * coordenadas y el contigfile
     * @param protFile la traducción del archivo de nucleotidos a proteinas
     * @param mapPrefix el prefijo ccon el cual luego se hara el mapping para la
     * anotacion funcional (ejemplo gen_id_)
     * @param mapStartsIn0 Hubo un caso donde el gen_id en el contig empezaa en
     * uno y en la anotacion funcional en cero esta bandera permite ese desfaz
     * @param startAtLine si se proceso un archivo grande, y este fallo y se
     * requiere re procesar desde algún punto se puede usar este parámetro
     * @param withHash lee los archivos de contigs, nucs y prots y los carga en
     * memoria en un hash, ha demostrado ser la mejor manera de procesar los
     * archivos, por esto por default est variable es true
     */
    public void parseEnsamble(String idPrefix, int idMetageno, int idGenoma, String gffFile, String contigFile, String nucFile, String protFile, String mapPrefix, boolean mapStartsIn0, int startAtLine, boolean withHash, boolean toFile, String outFile) {
        try {
            GFFReader gffReader = new GFFReader(new InputStreamReader(new FileInputStream(gffFile)));
            FastaReader contigReader = new FastaReader(new InputStreamReader(new FileInputStream(contigFile)));
            FastaReader nucReader = new FastaReader(new InputStreamReader(new FileInputStream(nucFile)));
            FastaReader protReader = new FastaReader(new InputStreamReader(new FileInputStream(protFile)));
            GenDAO genDAO = new GenDAO(transacciones);
            ArchivoDAO adao = new ArchivoDAO(transacciones);
            if (nextIDArchivo == -1) {
                nextIDArchivo = transacciones.getNextIDArchivos();
                if (nextIDArchivo == -1) {
                    System.err.println("ERROR No se puede determinar el siguiente ID de archivo");
                }
            }
            int idx;
            int id;
            String source = "";
            if (idGenoma != -1) {
                source = "genoma";
                id = idGenoma;
            } else {
                source = "metagenoma";
                id = idMetageno;
            }

            loadFiles(id, source, outFile, contigFile, protFile, nucFile, gffFile);
            loadRawFileIntoDB(id,source,outFile,raw_data_path,raw_ext);
            genDAO.setDebug(debug);
            GFFLine gffLine;
            int gen_num = 0;
            GenObj tmpGene = null;
            Sequence tmpContig = null;
            Sequence nucSeq = null;
            Sequence protSeq = null;
            Sequence contig = null;
            if (withHash) {
                nucReader.loadHash(Sequence.NUCLEOTIDOS);
                protReader.loadHash(Sequence.PROTEINAS);
                contigReader.loadHash(Sequence.NUCLEOTIDOS);
            } else {
                contig = contigReader.readSequence(Sequence.NUCLEOTIDOS);
            }
            //posiciona gff en número de linea
            while (gen_num < startAtLine) {
                gffLine = gffReader.readGffLine();
                gen_num++;
                if (gffLine == null) {
                    System.err.println("Fin de archivo no se proceso ningún gen. Registros en archivo: " + gen_num + " bandera startAt: " + 77);
                }
            }
            //Primero procesa el archivo GFF - todo se mueve en base a este archivo
            while ((gffLine = gffReader.readGffLine()) != null) {
                gen_num++;
                GenObj gen = new GenObj(idPrefix + "" + gen_num);
                gen.setContig_id(gffLine.getId());
                gen.setGenType(gffLine.getSeq_type());
                gen.setContig_from(gffLine.getFrom());
                gen.setContig_to(gffLine.getTo());
                gen.setGen_score(gffLine.getScore());
                gen.setGen_strand(gffLine.getStrand());
                gen.setGen_phase(gffLine.getPhase());
                gen.setIdGenoma(idGenoma);
                gen.setIdMetagenoma(idMetageno);
                gen.setGen_num(gen_num);
                if (idGenoma != -1) {
                    gen.setGen_src("GEN");
                } else {
                    gen.setGen_src("MET");
                }
                if (mapStartsIn0) {
                    gen.setGene_map_id(mapPrefix + "" + (gen_num - 1));
                } else {
                    gen.setGene_map_id(mapPrefix + "" + gen_num);
                }

                for (String key : gffLine.getAtributos().keySet()) {
                    if (key.toUpperCase().equals("ID")) {
                        gen.setContig_gen_id(gffLine.getAtrributeValue(key));
                    } else if (key.toLowerCase().equals("product")) {
                        gen.setGen_function(gffLine.getAtrributeValue(key));
                    } else if (key.toLowerCase().equals("gene_id")) {
                        //gen.setContig_gen_id("gene_id_" + gffLine.getAtrributeValue(key));
                        //MANEJAR ESTO COMO PARAMETRO EN LOS GENOMAS SE NECESITABA  gene_id y para meta GM se necesita solo gene
                        gen.setContig_gen_id("gene_" + gffLine.getAtrributeValue(key));
                    } else {
                        gen.addProperty(key, gffLine.getAtrributeValue(key));
                    }
                }
                //aca termina de crear un gen en base a los datos del gff, ahora faltan las secuencias y las intergénicas 
                //se espera que el primer registro del gff corresponda con la primer secuencia tanto del archivo de nucs como el de prots
                if (!withHash) {
                    nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                    protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                }
                if (tmpGene == null) { //esto pasa ùnicamente para el primer gen                     
                    Intergenic cincop = new Intergenic(Intergenic.I5P);
                    cincop.setFrom(0);
                    cincop.setTo(gen.getContig_from() - 1);
                    // cincop.setSize(gen.getContig_from() - 1);
                    GenSeqObj seqObj = new GenSeqObj();
                    //esto hace posible que podamos usar la bandera startAtLine //IF WITH HASH
                    while (!withHash && contig != null && contig.getSeqId().equals(gen.getContig_id())) {
                        tmpContig = contig;
                        contig = contigReader.readSequence(Sequence.NUCLEOTIDOS);
                    }
                    if (withHash) {
                        contig = contigReader.getKey(gen.getContig_id(), false);
                    }
                    if (contig != null) {
                        if (cincop.getTo() - cincop.getFrom() < 0) {
                            cincop.setSecuenciaValidada(contig, cincop.getTo(), cincop.getFrom());
                        } else {
                            cincop.setSecuenciaValidada(contig, cincop.getFrom(), cincop.getTo());
                        }
                        seqObj.setSecuenciaValidada(contig, gen.getContig_from() - 1, gen.getContig_to());
                    } else {
                        System.err.println("No se encontró contig: " + gen.getContig_id());
                        System.exit(1);
                    }
                    /*else if (tmpContig != null && tmpContig.getSeqId().equals(gen.getContig_id())) {
                     tresp.setSecuencia(tmpContig.getSequence().substring(tresp.getFrom(), tresp.getTo()));
                     seqObj.setSequence(tmpContig.getSequence().substring(gen.getContig_from() - 1, gen.getContig_to()));
                     } */

                    gen.setInter5p(cincop);
                    seqObj.setSeqType("NC");
                    seqObj.setSeq_from(gen.getContig_from());
                    seqObj.setSeq_to(gen.getContig_to());
                    gen.addSequence(seqObj);
                } else {//del segundo gen en adelante siempre existe tmpGene
                    //valida si el gen actual esta en el mismo contig que el gen anterior
                    if (gen.getContig_id().equals(tmpGene.getContig_id())) {
                        //crea la 5p del current misma que se asigna como 3p Del anterior
                        Intergenic cincop = new Intergenic(Intergenic.I5P);
                        cincop.setFrom(tmpGene.getContig_to());
                        cincop.setTo(gen.getContig_from() - 1);
                        //    cincop.setSize(cincop.getTo() - cincop.getFrom());
                        GenSeqObj seqObj = new GenSeqObj();
                        //  String contig = "";//something getcontig_seq
                        if (contig.getSeqId().equals(gen.getContig_id())) {
                            if (cincop.getTo() - cincop.getFrom() < 0) {
                                cincop.setSecuenciaValidada(contig, cincop.getTo(), cincop.getFrom());
                            } else {
                                cincop.setSecuenciaValidada(contig, cincop.getFrom(), cincop.getTo());
                            }
                            seqObj.setSecuenciaValidada(contig, gen.getContig_from() - 1, gen.getContig_to());
                        }//este else if en teoria no tendrìa que pasar... 
                        else if (tmpContig != null && tmpContig.getSeqId().equals(gen.getContig_id())) {
                            if (cincop.getTo() - cincop.getFrom() < 0) {
                                cincop.setSecuenciaValidada(tmpContig, cincop.getTo(), cincop.getFrom());
                            } else {
                                cincop.setSecuenciaValidada(tmpContig, cincop.getFrom(), cincop.getTo());
                            }
                            seqObj.setSecuenciaValidada(tmpContig, gen.getContig_from() - 1, gen.getContig_to());
                        } else {
                            //esto no se...si se hace esto hay que trae nuevamente el primer if o todo dentro de un while
                            tmpContig = contig;
                            contig = contigReader.readSequence(Sequence.NUCLEOTIDOS);
                            System.err.println("LECTURA RARA DE CONTIG");
                        }
                        //se asigna 3p del anterior que es cinco ' del actual
                        tmpGene.setInter3p((Intergenic) cincop.clone());
                        //ANOTA TMP
                        genDAO.almacenaValidaGen(tmpGene, toFile, outFile);
                        //completamos los datos restantes del current (queda pendiente 5p)
                        gen.setInter5p(cincop);
                        seqObj.setSeqType("NC");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    } else { //ES NUEVO CONTIG
                        //como el nuevo gen, pertenece a un nuevo contig, el anterior gen tmpGene
                        //tiene su intergènica 3' de donde se quedò hasta el final del contig 
                        Intergenic tresp = new Intergenic(Intergenic.I3P);
                        tresp.setFrom(tmpGene.getContig_to());
                        if (contig.getSeqId().equals(tmpGene.getContig_id())) {
                            tresp.setTo(contig.getSequence().length());
                            /*    if (tresp.getTo() - tresp.getFrom() < 0) {
                             tresp.setSecuenciaValidada(contig,tresp.getTo(), tresp.getFrom());
                             } else {
                             tresp.setSecuenciaValidada(contig,tresp.getFrom(), tresp.getTo());
                             }*/
                            if (tresp.getTo() - tresp.getFrom() > 0) {
                                tresp.setSecuenciaValidada(contig, tresp.getFrom(), tresp.getTo());
                            } else {
                                tresp.setSecuencia("");
                            }
                        } //como aca estamos hablando del anterior gen lo mas probable es que sea en el contig y no tmpContig
                        else if (tmpContig != null && tmpContig.getSeqId().equals(tmpGene.getContig_id())) {
                            System.err.println("ACA NUNCA TIENE QUE ENTRAR 1");
                            tresp.setTo(tmpContig.getSequence().length());
                            if (tresp.getTo() - tresp.getFrom() < 0) {
                                tresp.setSecuenciaValidada(tmpContig, tresp.getTo(), tresp.getFrom());
                            } else {
                                tresp.setSecuenciaValidada(tmpContig, tresp.getFrom(), tresp.getTo());
                            }
                        } else {
                            System.err.println("ACA NUNCA TIENE QUE ENTRAR 2");
                            //no se si tiene mucho sentido esto...
                            tmpContig = contig;
                            contig = contigReader.readSequence(Sequence.NUCLEOTIDOS);
                        }
                        //  tresp.setSize(tresp.getTo() - tresp.getFrom());
                        tmpGene.setInter3p(tresp);
                        //ANOTA TMP GENE
                        genDAO.almacenaValidaGen(tmpGene, toFile, outFile);
                        //ACA EMPIEZA EL NUEVO CONTIG
                        //TODO:ACA FALTA LEER NUEVO CONTIG!!!!!!
                        Intergenic cincop = new Intergenic(Intergenic.I5P);
                        cincop.setFrom(0);
                        cincop.setTo(gen.getContig_from() - 1);
                        // cincop.setSize(gen.getContig_from() - 1);
                        GenSeqObj seqObj = new GenSeqObj();
                        if (contig.getSeqId().equals(gen.getContig_id())) {
                            System.err.println("ACA NUNCA TIENE QUE ENTRAR 3");
                            if (cincop.getTo() - cincop.getFrom() < 0) {
                                cincop.setSecuenciaValidada(contig, cincop.getTo(), cincop.getFrom());
                            } else {
                                cincop.setSecuenciaValidada(contig, cincop.getFrom(), cincop.getTo());
                            }
                            seqObj.setSecuenciaValidada(contig, gen.getContig_from() - 1, gen.getContig_to());
                        }//este else if en teoria no tendrìa que pasar... 
                        else if (tmpContig != null && tmpContig.getSeqId().equals(gen.getContig_id())) {
                            if (cincop.getTo() - cincop.getFrom() < 0) {
                                cincop.setSecuenciaValidada(tmpContig, cincop.getTo(), cincop.getFrom());
                            } else {
                                cincop.setSecuenciaValidada(tmpContig, cincop.getFrom(), cincop.getTo());
                            }
                            seqObj.setSecuenciaValidada(tmpContig, gen.getContig_from() - 1, gen.getContig_to());
                        } else {
                            //Aca es mucho mas probable que pase esto, y es ca donde se va a realizar el cambio de contigs, por eso leemos nuevamente
                            tmpContig = contig;
                            if (withHash) {
                                contig = contigReader.getKey(gen.getContig_id(), false);
                            } else {
                                contig = contigReader.readSequence(Sequence.NUCLEOTIDOS);
                            }
                            if (contig != null && contig.getSeqId().equals(gen.getContig_id())) {
                                if (cincop.getTo() - cincop.getFrom() < 0) {
                                    cincop.setSecuenciaValidada(contig, cincop.getTo(), cincop.getFrom());
                                } else {
                                    cincop.setSecuenciaValidada(contig, cincop.getFrom(), cincop.getTo());
                                }
                                seqObj.setSecuenciaValidada(contig, gen.getContig_from() - 1, gen.getContig_to());
                            } else {
                                System.err.println("No se puede encontrar contig para: " + gen.getContig_id());
                            }
                        }
                        gen.setInter5p(cincop);
                        seqObj.setSeqType("NC");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    }
                }
                if (withHash) {
                    nucSeq = nucReader.getKey(gen.getContig_gen_id(), true);
                    if (nucSeq != null) {
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(nucSeq.getSequence());
                        seqObj.setSeqType("NC_2");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    } else {
                        System.err.println("No se encontró secuencia de nucleótidos para contig: " + gen.getContig_gen_id());
                    }
                } else {
                    while (nucSeq != null && !gen.getContig_gen_id().equals(nucSeq.getSeqId())) {
                        nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                        // protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                    }

                    if (nucSeq != null && gen.getContig_gen_id().equals(nucSeq.getSeqId())) {
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(nucSeq.getSequence());
                        seqObj.setSeqType("NC_2");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    } else { // es null
                        //no lo encontró así que reinicia el reader
                        nucReader = new FastaReader(new InputStreamReader(new FileInputStream(nucFile)));
                        nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                        while (nucSeq != null && !gen.getContig_gen_id().equals(nucSeq.getSeqId())) {
                            nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                            // protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                        }
                        if (nucSeq != null && gen.getContig_gen_id().equals(nucSeq.getSeqId())) {
                            GenSeqObj seqObj = new GenSeqObj();
                            seqObj.setSequence(nucSeq.getSequence());
                            seqObj.setSeqType("NC_2");
                            seqObj.setSeq_from(gen.getContig_from());
                            seqObj.setSeq_to(gen.getContig_to());
                            gen.addSequence(seqObj);
                        } else {
                            System.err.println("No se encontro NC para: " + gen.getContig_gen_id() + " - " + gen.getGenID());
                        }
                    }
                }
                if (withHash) {
                    protSeq = protReader.getKey(gen.getContig_gen_id(), true);
                    if (protSeq != null) {
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(protSeq.getSequence()); //also fix de length
                        seqObj.setSeqType("AA");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    } else {
                        System.err.println("No se encontró secuencia de proteinas para contig: " + gen.getContig_gen_id());
                    }
                } else {
                    while (protSeq != null && !gen.getContig_gen_id().equals(protSeq.getSeqId())) {
                        //nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                        protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                    }
                    if (protSeq != null && gen.getContig_gen_id().equals(protSeq.getSeqId())) {
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(protSeq.getSequence()); //also fix de length
                        seqObj.setSeqType("AA");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    } else {//ES NULL
                        protReader = new FastaReader(new InputStreamReader(new FileInputStream(nucFile)));
                        protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                        while (protSeq != null && !gen.getContig_gen_id().equals(protSeq.getSeqId())) {
                            //nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                            protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                        }
                        if (protSeq != null && gen.getContig_gen_id().equals(protSeq.getSeqId())) {
                            GenSeqObj seqObj = new GenSeqObj();
                            seqObj.setSequence(protSeq.getSequence()); //also fix de length
                            seqObj.setSeqType("AA");
                            seqObj.setSeq_from(gen.getContig_from());
                            seqObj.setSeq_to(gen.getContig_to());
                            gen.addSequence(seqObj);
                        } else {
                            System.err.println("No se encontro AA para: " + gen.getContig_gen_id() + " - " + gen.getGenID());
                        }
                    }
                }
                tmpGene = gen;
            }
            //ANOTA EL ULTIMO GEN QUE NUNCA LLEGO A ANOTARSE
            Intergenic tresp = new Intergenic(Intergenic.I3P);
            tresp.setFrom(tmpGene.getContig_to());
            if (contig != null && contig.getSeqId().equals(tmpGene.getContig_id())) {
                tresp.setTo(contig.getSequence().length());
                if (tresp.getTo() - tresp.getFrom() > 0) {
                    tresp.setSecuencia(contig.getSequence().substring(tresp.getFrom(), tresp.getTo()));
                } else {
                    tresp.setSecuencia("");
                }

            } //como aca estamos hablando del anterior gen lo mas probable es que sea en el contig y no tmpContig
            else if (tmpContig != null && tmpContig.getSeqId().equals(tmpGene.getContig_id())) {
                tresp.setTo(tmpContig.getSequence().length());
                tresp.setSecuencia(tmpContig.getSequence().substring(tresp.getFrom(), tresp.getTo()));
            }
            //  tresp.setSize(tresp.getTo() - tresp.getFrom());
            tmpGene.setInter3p(tresp);
            //ANOTA TMP GENE
            genDAO.almacenaValidaGen(tmpGene, toFile, outFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("No se encontró archivo GFF: " + gffFile);
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("No se encontró archivo GFF: " + gffFile);
            System.exit(1);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error al clonar objeto");
            System.exit(1);
        }
    }
}
