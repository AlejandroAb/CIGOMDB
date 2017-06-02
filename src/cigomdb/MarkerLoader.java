/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import bobjects.AnalisisClasificacion;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import bobjects.ArchivoObj;
import bobjects.Marcador;
import bobjects.StatsObj;
import bobjects.Usuario;
import dao.ArchivoDAO;
import dao.KronaDAO;
import dao.MarcadorDAO;
import dao.ClasificacionDAO;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import utils.FileUtils;
import utils.MyDate;

/**
 *
 * @author Alejandro
 */
public class MarkerLoader {

    public Transacciones transacciones;
    private String proc_combined_file = "flash.extendedFrags.fastq";
    private String proc_nc1_file = "flash.notCombined_1.fastq";
    private String proc_nc2_file = "flash.notCombined_2.fastq";
    private String proc_metaxa_file = "metaxa/metaxa.taxonomy.txt";
    private int nextIDMarcador = -1;
    private int nextIDArchivo = -1;
    boolean onlyComputeFiles = false;
    private String runKrona = "/scratch/share/apps/KronaTools-2.7/scripts/ImportText.pl";
    boolean onlyCreateFiles = false;

    public String getRunKrona() {
        return runKrona;
    }

    public void setRunKrona(String runKrona) {
        this.runKrona = runKrona;
    }

    /**
     * Hay veces que solo queremos regenerar las secuencias sin necesidad de
     * anotar nuevamente los archivos y las relaciones que hay entre ellos, por
     * def esta bandera es true, pero el usuario puedo configurar para evitar la
     * reinclusión de archivos en un archivo de salida
     */
    private boolean generaArchivos = true;
    /**
     * set de caracteres que se puede utilizar para hacer split en algun raw id
     * de secuencia por ejemplo caso de
     * /data/cigom_proc_data/MMF1/amplicon/samples/B6_MIN_2/metaxa/metaxa_out.taxonomy.txt.extended
     * el raw_id es
     *
     * @M03516_004_FC_000000000-APFV8:1:2107:18953:22442#CGGAGCCT+CTCTCTAT y en
     * metaxa viene como: M03516_004_FC_000000000-APFV8:1:2107:18953:22442 hay
     * varios archivos donde el raw tiene el # y metaxa también entonces si
     * hacemos el split por default funciona para este caso pero truena para los
     * demás, por lo tanto se creó esta variable, la cual permite asignar un
     * caracter o set de caracteres para realziar el split para casos como
     * estos.
     */
    private String splitSpecial = "";

    public Transacciones getTransacciones() {
        return transacciones;
    }

    public String getSplitSpecial() {
        return splitSpecial;
    }

    public void setSplitSpecial(String splitSpecial) {
        this.splitSpecial = splitSpecial;
    }

    public int getNextIDMarcador() {
        return nextIDMarcador;
    }

    public void setNexIDMarcador(int nexIDMarcador) {
        this.nextIDMarcador = nexIDMarcador;
    }

    public int getNextIDArchivo() {
        return nextIDArchivo;
    }

    public void setNextIDArchivo(int nextIDArchivo) {
        this.nextIDArchivo = nextIDArchivo;
    }

    public void setTransacciones(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public String getProc_combined_file() {
        return proc_combined_file;
    }

    public void setProc_combined_file(String proc_combined_file) {
        this.proc_combined_file = proc_combined_file;
    }

    public String getProc_nc1_file() {
        return proc_nc1_file;
    }

    public void setProc_nc1_file(String proc_nc1_file) {
        this.proc_nc1_file = proc_nc1_file;
    }

    public String getProc_nc2_file() {
        return proc_nc2_file;
    }

    public void setProc_nc2_file(String proc_nc2_file) {
        this.proc_nc2_file = proc_nc2_file;
    }

    public String getProc_metaxa_file() {
        return proc_metaxa_file;
    }

    public void setProc_metaxa_file(String proc_metaxa_file) {
        this.proc_metaxa_file = proc_metaxa_file;
    }

    public MarkerLoader(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public boolean isOnlyCreateFiles() {
        return onlyCreateFiles;
    }

    public void setOnlyCreateFiles(boolean onlyCreateFiles) {
        this.onlyCreateFiles = onlyCreateFiles;
    }

    /**
     * Parsea un archivo hecho a la medida para cargar los amplicones procesados
     * en la campaña SOGOM-I. Archivo:
     *
     * @param inputFile Archivo tab delim con header
     * @param insertAmplicones true si va a insertar los marcadores en la BD
     * false si estos ya estan en la BD y se corre el programa para actualizar
     * datos procesados
     * @param processOut true si va a procesar los archivos de secuencias
     * @param processTaxonomia true si va a procesar archivos de clasificación
     * taxonómica para cargar la matriz
     * @param raw_ext la extención del archivo crudo por default es fastq o
     * fastq.gz
     * @param outFile el archivo donde se guarda todo lo referente a los
     * marcadores y los archivos relacionados a estos
     * @param outFileFasta el archivo donde se escriben las secuencias a ser
     * almaccenadas
     * @param outFileMetaxa el archivo donde se escribe la clasificación
     * taxonómica a partir de metaxa
     * @param processNotPaired por default es false, ya que se decidió trabajar
     * únicamente con las secuencias pareadas!
     * @param processKrona para procesar krona. Básicamente revisa si existe el
     * archivo html y en caso de que no venga, crea un arhcivo de salida que
     * tiene el script necesario para generar el html y de toddos moddos anota
     * el archivo e la BD, para que pueda ser llamado desde la aplicación
     * @param metodoTaxo Método taxonómico bajo el cual se va a anotar
     * @processNotPaired Si es true procesa los archivos de fragmentos no
     * pareados, default es false
     * @return String con log de l proceso de anotación.
     *
     */
    public String parseMarkerFileFormatI(String inputFile, boolean insertAmplicones, boolean processOut, boolean processTaxonomia, String raw_ext, String outFile, String outFileFasta, String outFileMetaxa, boolean processNotPaired, boolean processKrona, String metodoTaxo) {
        String log = "";
        try {
            if (nextIDMarcador == -1) {
                nextIDMarcador = transacciones.getNextIDMarcador();
                if (nextIDMarcador == -1) {
                    return "ERROR No se puede determinar el siguiente ID de marcador";
                }
            }
            if (nextIDArchivo == -1) {
                nextIDArchivo = transacciones.getNextIDArchivos();
                if (nextIDArchivo == -1) {
                    return "ERROR No se puede determinar el siguiente ID de archivo";
                }
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
            String linea;
            ArchivoDAO adao = new ArchivoDAO(transacciones);
            ClasificacionDAO cDAO = new ClasificacionDAO(transacciones);
            MarcadorDAO mdao = new MarcadorDAO(transacciones);
            HashMap<String, String> seqMap = new HashMap<>();
            int idxIdMuestra = -1, idxTag = -1, idxRaw = -1, idxProc = -1,
                    idxMarcName = -1, idxMarcDesc = -1, idxSelection = -1,
                    idxLayout = -1, idxIdMarcador = -1, idxTipoMarcador = -1,
                    idxTipoSec = -1, idxSecuenciador = -1, idxPcr = -1, idxClasificacion = -1,
                    idxQC = -1, idxComments = -1, idxPre = -1, idxVol = -1, idxVector = -1,
                    idxExtended = -1, idxMetaxa = -1, idxNC1 = -1, idxNC2 = -1, idxSplit = -1,
                    idxProcesamiento = -1, idxAnalisis = -1, idxKit = -1, idxCleanMethod = -1, idxIdLib = -1, idxCita = -1;
            int numLinea = 0;
            while (((linea = reader.readLine()) != null)) {
                if (linea.length() > 0 && !linea.startsWith("#")) {
                    numLinea++;
                    if (numLinea == 1) {
                        StringTokenizer headerST = new StringTokenizer(linea, "\t");
                        int toks = 0;
                        while (headerST.hasMoreTokens()) {
                            toks++;
                            String tok = headerST.nextToken().trim().toUpperCase();
                            if ((tok.contains("ID") && tok.contains("MUESTRA")) || (tok.contains("ID") && tok.contains("BD"))) {//estacion
                                idxIdMuestra = toks;
                            } else if (tok.contains("ETIQ") || tok.equals("TAG")) {//TAG O ETIQUETA DE LA MUESTRA
                                idxTag = toks;
                            } else if (tok.contains("RAW")) {//RAW DATA PATH
                                idxRaw = toks;
                            } else if (tok.contains("PRO") && tok.contains("PATH")) {//PROC DATA PATH
                                idxProc = toks;
                            } else if (tok.contains("NAME")) {//NOMBE DEL MARCADOR
                                idxMarcName = toks;
                            } else if (tok.contains("DESC")) {//DESCRIPCION DEL MARCADOR
                                idxMarcDesc = toks;
                            } else if (tok.contains("SELECTION")) {//METODO DE SELECCION
                                idxSelection = toks;
                            } else if (tok.contains("LAYOUT")) {//LIBRARY LAYOUT
                                idxLayout = toks;
                            } else if (tok.contains("TIPO") && tok.contains("SEC")) {//PROC DATA PATH
                                idxTipoSec = toks;
                            } else if (tok.contains("TIPO") && tok.contains("MARC")) {//PROC DATA PATH
                                idxTipoMarcador = toks;
                            } else if (tok.contains("SECUENCIADOR")) {//PROC DATA PATH
                                idxSecuenciador = toks;
                            } else if (tok.contains("PCR")) {//PROC DATA PATH
                                idxPcr = toks;
                            } else if (tok.contains("PRE") && tok.contains("PROC")) {//PROC DATA PATH
                                idxPre = toks;
                            } else if (tok.contains("VOL") || tok.contains("DNA")) {//PROC DATA PATH
                                idxVol = toks;
                            } else if (tok.contains("ID") && tok.contains("MARCADOR")) {//PROC DATA PATH
                                idxIdMarcador = toks;
                            } else if (tok.contains("VECTOR")) {
                                idxVector = toks;
                            } else if (tok.contains("QC")) {
                                idxQC = toks;
                            } else if (tok.contains("CLASIFICACION")) {//ARCHIVO DE CLASIFICACION TAXONOMICA
                                idxClasificacion = toks;
                            } else if (tok.contains("EXTENDED")) {//Extended file name
                                idxExtended = toks;
                            } else if (tok.contains("METAXA")) {//metaxa file name
                                idxMetaxa = toks;
                            } else if (tok.contains("SPLIT")) {//metaxa file name
                                idxSplit = toks;
                            } else if (tok.contains("COMENTARIOS")) {//COMENTARIOS
                                idxComments = toks;
                            } else if (tok.contains("ANALISIS")) {//ANALISIS DEL MARCADOR
                                idxAnalisis = toks;
                            } else if (tok.contains("PROCESAMIENTO")) {//PROCESAMIENTO DE LA MUESTRA
                                idxProcesamiento = toks;
                            } else if (tok.contains("KIT")) {//CLEAN UP KIT
                                idxKit = toks;
                            } else if (tok.contains("METHOD") && tok.contains("CLEAN")) {//CLEAN UP METHOD
                                idxCleanMethod = toks;
                            } else if (tok.contains("ID") && tok.contains("LIBRERIA")) {//LIBRERIA
                                idxIdLib = toks;
                            } else if (tok.contains("CITA") || tok.contains("REFERENCIA")) {//CITAR ESTA LIBRERIA
                                idxCita = toks;
                            } else if (tok.contains("NC1")) {//Fragmentos no extendidos 1
                                idxNC1 = toks;
                            } else if (tok.contains("NC2")) {//Fragmentos no extendidos 2
                                idxNC2 = toks;
                            }

                        }
                    } else {
                        StringTokenizer st = new StringTokenizer(linea, "\t");
                        Marcador marcador = new Marcador();
                        marcador.setExtendedFName(this.proc_combined_file);
                        marcador.setMetaxaFName(this.proc_metaxa_file);
                        marcador.setNc1FName(this.proc_nc1_file);
                        marcador.setNc2FName(this.proc_nc2_file);
                        int tok = 0;
                        String idMuestra = "";
                        String tag = "";
                        String raw_data_path = "";
                        String proc_data_path = "";
                        String marc_name = "";
                        String marc_desc = "";
                        String library_selection = "";
                        String library_layout = "";
                        String library_vector = "";
                        String idMarcador = "" + nextIDMarcador;
                        marcador.setIdMarcador(idMarcador);
                        nextIDMarcador++;
                        String idTipoMarcador = "";
                        String idTipoSec = "";
                        String idSecuenciador = "";
                        String idPcr = "";
                        String pre_process = "";
                        String data_qc = "";
                        String volumen = "";
                        boolean marcadorInsertado = false;
                        while (st.hasMoreTokens()) {
                            tok++;
                            if (tok == idxIdMuestra) {
                                idMuestra = st.nextToken().trim();
                                marcador.setIdMuestra(idMuestra);
                            } else if (tok == idxRaw) {
                                raw_data_path = st.nextToken().trim();
                                marcador.setRaw_data_path(raw_data_path);
                                if (!raw_data_path.endsWith("/")) {
                                    raw_data_path += "/";
                                }
                            } else if (tok == idxIdMarcador) {
                                String tmpIdMarcador = st.nextToken().trim();
                                if (!tmpIdMarcador.toUpperCase().equals("NA") && !tmpIdMarcador.toUpperCase().equals("ND")) {
                                    try {
                                        Integer.parseInt(tmpIdMarcador);
                                        marcador.setIdMarcador(tmpIdMarcador);
                                    } catch (NumberFormatException nfe) {

                                    }
                                }
                            } else if (tok == idxPre) {
                                pre_process = st.nextToken().trim();
                                marcador.setPre_process(pre_process);
                            } else if (tok == idxPcr) {
                                idPcr = st.nextToken().trim();
                                marcador.setIdPcr(idPcr);
                            } else if (tok == idxTag) {
                                tag = st.nextToken().trim();
                            } else if (tok == idxTipoMarcador) {
                                idTipoMarcador = st.nextToken().trim();
                                marcador.setIdTipoMarcador(idTipoMarcador);
                            } else if (tok == idxProc) {
                                proc_data_path = st.nextToken().trim();
                                marcador.setProc_data_path(proc_data_path);
                                if (!proc_data_path.endsWith("/")) {
                                    proc_data_path += "/";
                                }
                            } else if (tok == idxMarcName) {
                                marc_name = st.nextToken().trim();
                                marcador.setMarc_name(marc_name);
                                idMarcador = transacciones.getIdMarcadorByLabel(marc_name);
                                if (idMarcador.length() > 1) {
                                    marcador.setIdMarcador(idMarcador);
                                }
                            } else if (tok == idxMarcDesc) {
                                marc_desc = st.nextToken().trim();
                                marcador.setMarc_desc(marc_desc);
                            } else if (tok == idxSelection) {
                                library_selection = st.nextToken().trim();
                                marcador.setLibrary_selection(library_selection);
                            } else if (tok == idxLayout) {
                                library_layout = st.nextToken().trim();
                                marcador.setLibrary_layout(library_layout);
                            } else if (tok == idxVector) {
                                library_vector = st.nextToken().trim();
                                marcador.setLibrary_vector(library_vector);
                            } else if (tok == idxTipoSec) {
                                idTipoSec = st.nextToken().trim();
                                marcador.setIdTipoSec(idTipoSec);
                            } else if (tok == idxSecuenciador) {
                                idSecuenciador = st.nextToken().trim();
                                marcador.setIdSecuenciador(idSecuenciador);
                            } else if (tok == idxVol) {
                                volumen = st.nextToken().trim();
                                marcador.setVolumen(volumen);
                            } else if (tok == idxQC) {
                                data_qc = st.nextToken().trim();
                                marcador.setData_qc(data_qc);
                            } else if (tok == idxExtended) {
                                marcador.setExtendedFName(st.nextToken().trim());
                            } else if (tok == idxMetaxa) {
                                marcador.setMetaxaFName(st.nextToken().trim());
                            } else if (tok == idxClasificacion) {
                                marcador.setClasificacionFName(st.nextToken().trim());
                            } else if (tok == idxNC1) {
                                marcador.setNc1FName(st.nextToken().trim());
                            } else if (tok == idxNC2) {
                                marcador.setNc2FName(st.nextToken().trim());
                            } else if (tok == idxComments) {
                                marcador.setComentarios(st.nextToken().trim());
                            } else if (tok == idxIdLib) {
                                marcador.setIdLibreria(st.nextToken().trim());
                            } else if (tok == idxKit) {
                                marcador.setClean_up_kit(st.nextToken().trim());
                            } else if (tok == idxCleanMethod) {
                                marcador.setClean_up_method(st.nextToken().trim());
                            } else if (tok == idxAnalisis) {
                                marcador.setAnalisis(st.nextToken().trim());
                            } else if (tok == idxProcesamiento) {
                                marcador.setProcesamiento(st.nextToken().trim());
                            } else if (tok == idxSplit) {
                                String tmpSplit = st.nextToken().trim();
                                if (!tmpSplit.toUpperCase().equals("ND") && !tmpSplit.toUpperCase().equals("NA")) {
                                    this.splitSpecial = tmpSplit;
                                }
                            } else {
                                st.nextToken();
                            }
                        }
                        if (idMuestra.equals("")) {
                            idMuestra = transacciones.getIdMuestraByLabel(tag);
                            marcador.setIdMuestra(idMuestra);
                        }
                        //Inserta marcadores
                        if (insertAmplicones) {
                            if (idMuestra.length() == 0) {
                                System.err.println("No se encontró el ID  de la muestra y no se pudo determinar mediante la etiqueta: " + tag);
                            } else {
                                marcadorInsertado = processAmplicones(marcador, outFile, raw_data_path, raw_ext, mdao);
                            }
                        }
                        //PROCESAR ARCHIVOS DE SECUENCIAS FASTA
                        if ((processOut && insertAmplicones && marcadorInsertado) || (processOut && !insertAmplicones)) {
                            processSecuencias(marcador, processNotPaired, proc_data_path, outFileFasta, seqMap, adao);
                        }
                        //    marcadorInsertado = mdao.almacenaMarcador(marcador, toFile, outFile, true, true);
                        if (processTaxonomia /*&& processOut*/) {
                            //to impl cargar metaxa
                            if (metodoTaxo.toUpperCase().equals("METAXA")) {
                                processMeta(proc_data_path, marcador.getMetaxaFName(), marcador.getIdMarcador(), adao, cDAO, outFileMetaxa, seqMap, processNotPaired);
                            } else if (metodoTaxo.toUpperCase().equals("PARALLEL")) {
                                processParallel(proc_data_path, marcador.getMetaxaFName(), marcador.getIdMarcador(), adao, cDAO, outFileMetaxa, seqMap, processNotPaired);
                            } else {
                                System.err.println("El método de asignación taxonómica: " + metodoTaxo + " aún no está implementado!");
                            }
                            seqMap = new HashMap<>();
                        }

                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MarkerLoader.class.getName()).log(Level.SEVERE, null, ex);
            log += "No existe el archivo ";
        } catch (IOException ex) {
            Logger.getLogger(MarkerLoader.class.getName()).log(Level.SEVERE, null, ex);
            log += "No existe el archivo " + ex.getLocalizedMessage();
        }
        return log;
    }

    /**
     * Este método se encarga de leer los archivo pre procesados de flash y en
     * base a estos, anota las secuencias en la BD
     *
     * @param marcador El objeto reconstruido en la iteración
     * @param processNotPaired Def es false si es truee procesa los archivos con
     * las secuencias que no empalmaron
     * @param proc_data_path el path donde se espera encontrar los archivos de
     * lecturas pareadas
     * @param outFileFasta el outFile donde se escriben las secuencias, si no
     * viene nada escribe en la BD
     * @param seqMap es un Hash, donde se guarda la relación raw_seq_id (key) -
     * val bd_seq_id, con esto luego se mapean los resultados de metaxa
     * @param adao objeto para almacenar loss archivoss
     * @throws IOException
     */
    public void processSecuencias(Marcador marcador, boolean processNotPaired, String proc_data_path, String outFileFasta, HashMap<String, String> seqMap, ArchivoDAO adao) throws IOException {
        BufferedReader extendedReader = new BufferedReader(
                new FileReader(proc_data_path + marcador.getExtendedFName()));
        BufferedReader nc1Reader = null;
        BufferedReader nc2Reader = null;
        if (processNotPaired) {
            nc1Reader = new BufferedReader(new FileReader(proc_data_path + marcador.getNc1FName()));
            nc2Reader = new BufferedReader(new FileReader(proc_data_path + marcador.getNc2FName()));
        }
        boolean toFile = outFileFasta.length() > 0;
        FileWriter writer = null;
        if (toFile) {
            writer = new FileWriter(outFileFasta, true);
        }
        String lineFastQ;
        int sec_num = 0;
        int counterTotal = 0;
        float avg = 0;
        if (!onlyCreateFiles) {
            while ((lineFastQ = extendedReader.readLine()) != null) {
                if (lineFastQ.startsWith("@M") /*&& lineFastQ.length() < 50*/) {
                    sec_num++;
                    counterTotal++;
                    String idSec = marcador.getIdSeqFormat() + counterTotal;
                    int indx = lineFastQ.indexOf(" ");
                    /**
                     * Antes estaba asi.....hay que revisar por que se dejo
                     * asi...quizas HC de algun archivo en especial...abajo se
                     * implementó algo con mas lógica if (indx <= 0) { indx =
                     * lineFastQ.length() - 1; } String raw_seq_id =
                     * lineFastQ.substring(1, indx).trim();
                     *
                     */
                    String raw_seq_id;
                    if (indx >= 1) {
                        //indx = lineFastQ.length() - 1;
                        raw_seq_id = lineFastQ.substring(1, indx).trim();
                    } else {
                        raw_seq_id = lineFastQ.substring(1).trim();
                    }

                    String sec = extendedReader.readLine();
                    avg += sec.length();
                    if (toFile) {
                        String query = "INSERT INTO seq_marcador (idseq_marcador, idmarcador,raw_seq_id,seq,seq_length) VALUES('"
                                + idSec + "'," + marcador.getIdMarcador() + ",'" + raw_seq_id + "','" + sec + "'," + sec.length() + ");\n";
                        writer.write(query);
                        if (this.splitSpecial.length() > 0) {
                            raw_seq_id = raw_seq_id.split(splitSpecial)[0];
                        }
                        //limitacion metaxa raw id max 59
                        if (raw_seq_id.length() > 59) {
                            seqMap.put(raw_seq_id.substring(0, 59), idSec);
                        } else {
                            seqMap.put(raw_seq_id, idSec);
                        }

                    } else {
                        if (!transacciones.insertaSeqMarcador(idSec, "" + marcador.getIdMarcador(), raw_seq_id, sec)) {
                            System.err.println("Error insertando secuencia: " + idSec);

                        }
                    }
                }
            }
        }
        extendedReader.close();
        if (generaArchivos) {
            //Archivo merged
            ArchivoObj mergeFile = new ArchivoObj(nextIDArchivo);
            File tmpFile = new File(proc_data_path + marcador.getExtendedFName());
            nextIDArchivo++;
            mergeFile.setTipoArchivo(ArchivoObj.TIPO_PRE);
            mergeFile.setNombre(marcador.getExtendedFName().substring(marcador.getExtendedFName().lastIndexOf("/") + 1));
            mergeFile.setPath(proc_data_path + marcador.getExtendedFName().substring(0, marcador.getExtendedFName().indexOf("/") + 1));
            mergeFile.setDescription("Fastq con lecturas pareadas - merge de FW y RV. Se construye el archivo usando FLASH.");
            int tmpID = nextIDArchivo;
            String tmpSource = "";
            for (int i = 1; i <= marcador.getArchivos().size(); i++) {
                if (i == 1) {
                    tmpSource += "" + (tmpID - i);
                } else {
                    tmpSource += "," + (tmpID - i);
                }
            }
            mergeFile.setOrigen(tmpSource);
            mergeFile.setExtension("fastq");
            MyDate date = new MyDate(tmpFile.lastModified());
            mergeFile.setDate(date);
            mergeFile.setSize(tmpFile.length());
            if (tmpFile.length() / 1048576 < 1000) {//si es menor a un Gb
                mergeFile.setChecksum(FileUtils.getMD5File(proc_data_path + tmpFile.getName()));
            } else {
                mergeFile.setChecksum("TBD");
            }
            //mergeFile.setChecksum(FileUtils.getMD5File(proc_data_path + marcador.getExtendedFName()));
            mergeFile.setAlcance("Grupo de bioinformática");
            mergeFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
            mergeFile.setDerechos("Acceso limitado a miembros");
            mergeFile.setTags("Secuencias pareadas, amplicones");
            mergeFile.setTipo("Text");
            Usuario user = new Usuario(31);//ALES
            user.setAcciones("creator");
            user.setComentarios("Se encarga de ejecutar el programa Flash para parear las lecturas que da como resultado este archivo.");
            mergeFile.addUser(user);
            Usuario user2 = new Usuario(9);//ALEXSF
            user2.setAcciones("contributor");
            user2.setComentarios("Investigador responsable de subproyecto");
            mergeFile.addUser(user2);
            marcador.addArchivo(mergeFile);
            if (toFile) {
                writer.write(mergeFile.toNewSQLString() + ";\n");
                writer.write("INSERT INTO marcador_archivo VALUES(" + marcador.getIdMarcador() + "," + mergeFile.getIdArchivo() + ");\n");
                for (String qUsuarios : mergeFile.archivoUsuariosToSQLString()) {
                    writer.write(qUsuarios + ";\n");
                }
            } else {
                adao.insertaArchivo(mergeFile, false, "", true);
                transacciones.insertaArchivoMarcador(marcador.getIdMarcador(), mergeFile.getIdArchivo());
                for (String qUsuarios : mergeFile.archivoUsuariosToSQLString()) {
                    if (!transacciones.insertaQuery(qUsuarios)) {
                        System.err.println("Error insertando relación usuario-archivo: "
                                + marcador.getIdMarcador() + "(idmarcador) - " + nextIDArchivo + "(idArchivo) - q: " + qUsuarios);
                    }
                }
            }
        }

        if (processNotPaired && !onlyCreateFiles) {
            avg = 0;
            sec_num = 0;
            while ((lineFastQ = nc1Reader.readLine()) != null) {
                if (lineFastQ.startsWith("@M")) {
                    sec_num++;
                    counterTotal++;
                    String idSec = marcador.getIdSeqFormat() + counterTotal;
                    int indx = lineFastQ.indexOf(" ");
                    if (indx <= 0) {
                        indx = lineFastQ.length() - 1;
                    }
                    String raw_seq_id = lineFastQ.substring(1, indx);
                    String sec = nc1Reader.readLine();
                    avg += sec.length();
                    if (toFile) {
                        String query = "INSERT INTO seq_marcador (idseq_marcador, idmarcador,raw_seq_id,seq,seq_length) VALUES('"
                                + idSec + "'," + marcador.getIdMarcador() + ",'" + raw_seq_id + "','" + sec + "'," + sec.length() + ");\n";
                        writer.write(query);
                        //limitacion metaxa raw id max 59
                        if (raw_seq_id.length() > 59) {
                            seqMap.put(raw_seq_id.substring(0, 59), idSec);
                        } else {
                            seqMap.put(raw_seq_id, idSec);
                        }

                    } else {
                        if (!transacciones.insertaSeqMarcador(idSec, "" + marcador.getIdMarcador(), raw_seq_id, sec)) {
                            System.err.println("Error insertando secuencia: " + idSec);

                        }
                    }
                }
            }
            nc1Reader.close();
            avg = 0;
            sec_num = 0;
            while ((lineFastQ = nc2Reader.readLine()) != null) {
                if (lineFastQ.startsWith("@M")) {
                    sec_num++;
                    counterTotal++;
                    String idSec = marcador.getMarc_name() + "_" + counterTotal;
                    int indx = lineFastQ.indexOf(" ");
                    if (indx <= 0) {
                        indx = lineFastQ.length() - 1;
                    }
                    String raw_seq_id = lineFastQ.substring(1, indx);
                    String sec = nc2Reader.readLine();
                    avg += sec.length();
                    if (toFile) {
                        String query = "INSERT INTO seq_marcador (idseq_marcador, idmarcador,raw_seq_id,seq,seq_length) VALUES('"
                                + idSec + "'," + marcador.getIdMarcador() + ",'" + raw_seq_id + "','" + sec + "'," + sec.length() + ");\n";
                        writer.write(query);
                        //limitacion metaxa raw id max 59
                        if (raw_seq_id.length() > 59) {
                            seqMap.put(raw_seq_id.substring(0, 59), idSec);
                        } else {
                            seqMap.put(raw_seq_id, idSec);
                        }

                    } else {
                        if (!transacciones.insertaSeqMarcador(idSec, "" + marcador.getIdMarcador(), raw_seq_id, sec)) {
                            System.err.println("Error insertando secuencia: " + idSec);

                        }
                    }
                }
            }
            nc2Reader.close();
        }
        if (generaArchivos) {
            File proFolder = new File(proc_data_path);
            if (proFolder.exists()) {
                for (File f : proFolder.listFiles()) {
                    if (f.getName().toLowerCase().contains("flash") && f.getName().toLowerCase().contains("notcombined")) {
                        ArchivoObj notCombinedFile = new ArchivoObj(nextIDArchivo);
                        nextIDArchivo++;
                        notCombinedFile.setTipoArchivo(ArchivoObj.TIPO_PRE);
                        notCombinedFile.setNombre(f.getName());
                        notCombinedFile.setPath(proc_data_path);
                        notCombinedFile.setExtension("fastq");
                        MyDate date = new MyDate(f.lastModified());
                        notCombinedFile.setDate(date);
                        notCombinedFile.setSize(f.length());
                        if (f.getName().contains("1")) {
                            notCombinedFile.setDescription("Fastq con las secuencias FW que no se pudieron empalmar con las de RV");
                        } else if (f.getName().contains("2")) {
                            notCombinedFile.setDescription("Fastq con las secuencias RV que no se pudieron empalmar con las de FW");
                        } else {
                            notCombinedFile.setDescription("Fastq con secuencias que no se pudieron empalmar");
                        }
                        notCombinedFile.setChecksum(FileUtils.getMD5File(proc_data_path + f.getName()));
                        notCombinedFile.setAlcance("Grupo de bioinformática");
                        notCombinedFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
                        notCombinedFile.setDerechos("Acceso limitado a miembros");
                        notCombinedFile.setTags("Secuencias pre pocesadas, amplicones");
                        notCombinedFile.setTipo("Text");
                        Usuario user = new Usuario(20);//ALES
                        user.setAcciones("creator");
                        user.setComentarios("Se encarga de ejecutar el programa Flash para parear las lecturas que da como resultado este archivo.");
                        notCombinedFile.addUser(user);
                        Usuario user2 = new Usuario(25);//ALEXSF
                        user2.setAcciones("contributor");
                        user2.setComentarios("Investigador responsable de subproyecto");
                        notCombinedFile.addUser(user2);

                        marcador.addArchivo(notCombinedFile);
                        if (toFile) {
                            writer.write(notCombinedFile.toNewSQLString() + ";\n");
                            writer.write("INSERT INTO marcador_archivo VALUES(" + marcador.getIdMarcador() + "," + notCombinedFile.getIdArchivo() + ");\n");
                            for (String qUsuarios : notCombinedFile.archivoUsuariosToSQLString()) {
                                writer.write(qUsuarios + ";\n");
                            }
                        } else {
                            adao.insertaArchivo(notCombinedFile, false, "", true);
                            transacciones.insertaArchivoMarcador(marcador.getIdMarcador(), notCombinedFile.getIdArchivo());
                            for (String qUsuarios : notCombinedFile.archivoUsuariosToSQLString()) {
                                if (!transacciones.insertaQuery(qUsuarios)) {
                                    System.err.println("Error insertando relación usuario-archivo: "
                                            + marcador.getIdMarcador() + "(idmarcador) - " + nextIDArchivo + "(idArchivo) - q: " + qUsuarios);
                                }
                            }
                        }
                    }
                }
            } else {
                System.err.println("No existe directorio: " + proc_data_path);
                //  return false;
            }

        }

        if (toFile) {
            writer.close();
        }
    }

    /**
     * Este método se encarga de asignar las estadisticas de los amplicones
     * procesados. Cuando se desarrolló hubo un problema para la identificación
     * del marcador a partir de la etiqueta usada en los archivos generados por
     * el equipo de bioinformática. Este erro esta presenta para las
     * estadísticas de SOGOM2, pues la clave o etiqueta está como: S2_08_SED_1
     * en lugar de S2_S08_SED_1 como lo es para SOGOM1: S01_SED_1 Es por esto
     * que este método tiene un parche para estas etiquetas y concatena la S
     * para dichas estadísticas
     *
     * @param input el archivo de entrada con las estadísticas
     * @param output si viene este parámetro escribe a archivo, de lo conntrario
     * escribe directo a laa BD
     */
    public void cargaEstadisticas(String input, String output) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(input));
            String linea;
            int numLinea = 0;
            int nextIDStats = transacciones.getNextIDStats();
            boolean toFile = false;
            FileWriter writer = null;
            if (output.length() > 0) {
                toFile = true;
                writer = new FileWriter(output);
            }
            while ((linea = reader.readLine()) != null) {
                numLinea++;
                if (!linea.startsWith("#") && linea.trim().length() > 2 && numLinea > 1) {
                    String cols[] = linea.split("\t");
                    if (cols.length < 10) {
                        System.err.println("Error en linea " + numLinea + " Se esperan por lo menos 10 columnas: Sample  Total_reads     Total_bases     Long_promedio   GC(%)   Calidad_promedio        Lecturas_Ns(%)  Q20(%)  Q30(%)  flash_combined");
                    } else {
                        String sample = cols[0];
                        String idMarcador;
                        try {
                            Integer.parseInt(sample);
                            idMarcador = sample;
                        } catch (NumberFormatException nfe) {
                            idMarcador = transacciones.getIdMarcadorByProPath(sample);
                            //parche SOGOM2 - S2_08_SED_1 to S2_S08_SED_1
                            if (idMarcador.equals("-1") && sample.startsWith("S2")) {
                                sample = sample.substring(0, 3) + "S" + sample.substring(3);
                                idMarcador = transacciones.getIdMarcadorByProPath(sample);
                            }
                        }
                        if (!idMarcador.equals("-1")) {
                            StatsObj stats = new StatsObj(nextIDStats);
                            nextIDStats++;
                            stats.setReads(cols[1]);
                            stats.setBases(cols[2]);
                            stats.setLong_avg(cols[3]);
                            stats.setGc_prc(cols[4]);
                            stats.setQc_avg(cols[5]);
                            stats.setNs_prc(cols[6]);
                            stats.setQ20(cols[7]);
                            stats.setQ30(cols[8]);
                            stats.setCombined_prc(cols[9]);
                            String qUpdate = "UPDATE marcador SET idstats = " + stats.getIdStats() + " WHERE idmarcador = " + idMarcador;
                            if (toFile) {
                                writer.write(stats.toSQLString() + "\n");
                                writer.write(qUpdate + ";\n");
                            } else {
                                if (!transacciones.insertaQuery(stats.toSQLString())) {
                                    System.err.println("Error insertando stats: " + stats.toSQLString());
                                } else {
                                    if (!transacciones.insertaQuery(qUpdate)) {
                                        System.err.println("Error updating marcador-stats: " + qUpdate);
                                    }
                                }
                            }
                        } else {
                            System.err.println("No se puede determinar el ID: " + idMarcador + " dato leído: " + sample);
                        }

                    }
                }
            }
            if (toFile) {
                writer.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MarkerLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MarkerLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void procesaKraken(String input, String output, int idMetagenoma) {

    }

    /**
     * Se encarga de anotar/ relacionar el archivo de krona, con un marcador. En
     * caso de que no exxista el archivo, este metodo tratará de crearlo, por lo
     * que puede generar el input file necesario para generar el archivo html
     * que luego es desplegado en la aplicación
     *
     * @param inputFile archivo con ids o etiquetas de marcadores
     * @param outFile
     * @param withNoRank esta bandera es usada al momento de crear la matriz, si
     * viene true rellena los "huecos": no_order, no_genus, no_etc...con false,
     * de lo contrario lo deja en blanco
     * @param force por mas de que exista el html o la matriiz, fuerza su
     * creación
     */
    public void processKrona(String inputFile, String outFile, boolean withNoRank, boolean force, String idanalisis) {
        if (nextIDArchivo == -1) {
            nextIDArchivo = transacciones.getNextIDArchivos();
            if (nextIDArchivo == -1) {
                System.err.println("ERROR No se puede determinar el siguiente ID de archivo");
            }
        }
        boolean toFile = false;
        if (outFile != null && outFile.length() > 0) {
            toFile = true;
        }
        try {
            FileWriter writer = null;
            if (toFile) {
                writer = new FileWriter(outFile, true);
            }
            KronaDAO kdao = new KronaDAO(transacciones);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line = "";
            FileUtils fu = new FileUtils();
            String htmlName = "";
            String proDataPath = "";
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0 && !line.startsWith("#")) {
                    boolean esID = false;
                    String tmpLine[] = line.split("\t");
                    String id = "";
                    if (tmpLine.length > 1) {
                        id = tmpLine[0].trim();
                    } else {
                        id = line.trim();
                    }
                    String etiqueta = "";
                    try {
                        Integer.parseInt(id);
                        esID = true;//else es etiqueta
                    } catch (NumberFormatException nfe) {

                    }
                    if (!esID) {
                        if (tmpLine.length > 1) {
                            etiqueta = tmpLine[1].trim();
                        } else {
                            etiqueta = id;
                        }

                        id = transacciones.getIdMarcadorByLabel(etiqueta);
                    } else {
                        etiqueta = transacciones.getEtiquetaMarcadorByLabel(id);
                    }
                    proDataPath = transacciones.getProcessDataPathByMarcadorID(id);
                    /**
                     * Para los kronas se espera que en el proc_data_path exista
                     * la karpeta krona y dentro de esta el archivo html. si no
                     * existe se crea como krona/krona.html
                     */
                    proDataPath += "krona/";
                    File kronaPath = new File(proDataPath);
                    fu.validateFile(proDataPath, true);//crea el directorio si no existe
                    boolean findHTML = false;

                    for (File f : kronaPath.listFiles()) {
                        //la bandera force hace que se creen tanto la matriz como el html por mas de que existan
                        if (f.getName().equals("krona.html") && !force) {
                            htmlName = f.getName();
                            findHTML = true;
                            System.out.println("Se encontro html en: " + f.getAbsolutePath());
                            //String idMarcador, String proDataPath, String fName, boolean isFromApp, FileWriter writer
                            addKronaFile(id, proDataPath, htmlName, false, writer);
                            break;
                        }
                    }
                    if (!findHTML) {
                        htmlName = "krona.html";
                        if (kdao.writeKronaInputMarcador(proDataPath + "matrix.krona.txt", id, withNoRank, idanalisis)) {
                            addMatrixFile(id, proDataPath, "matrix.krona.txt", true, writer);

                            String command = runKrona + " -o " + proDataPath + htmlName + " " + proDataPath + "matrix.krona.txt," + etiqueta;
                            if (executeKronaScript(command)) {
                                addKronaFile(id, proDataPath, htmlName, true, writer);
                            } else {
                                System.err.println("Error ejecutando: " + command);
                            }
                        }
                    }
                }
            }
            if (toFile) {
                writer.close();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MarkerLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MarkerLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Este método se encarga de crear la relación marcador archivo, así como
     * dar de alta el archivo en la BD
     *
     * @param idMarcador El id del marcador al cual pertenece el archivo
     * @param proDataPath el path donde se encuentraa el archivo
     * @param fName el nombre del archivo
     * @param isFromApp true si el archivo krona fue generado mediante esta
     * aplicación
     * @param writer null si va a directo a la BD o un writer válido si se
     * escribe a archivo
     * @return true si no hubo problemas
     * @throws IOException
     */
    public boolean addKronaFile(String idMarcador, String proDataPath, String fName, boolean isFromApp, FileWriter writer) throws IOException {
        ArchivoObj kronaFile = new ArchivoObj(nextIDArchivo);
        File f = new File(proDataPath + fName);
        nextIDArchivo++;
        kronaFile.setTipoArchivo(ArchivoObj.TIPO_KRN);
        kronaFile.setNombre(fName);
        kronaFile.setPath(proDataPath);
        kronaFile.setExtension("html");
        MyDate date = new MyDate(f.lastModified());
        kronaFile.setDate(date);
        kronaFile.setSize(f.length());
        kronaFile.setDescription("Archivo HTML generado por Krona a aprtir de la matriz de abundancia del marcador");
        kronaFile.setChecksum(FileUtils.getMD5File(proDataPath + fName));
        kronaFile.setAlcance("CIGOM");
        kronaFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        kronaFile.setDerechos("Acceso limitado a miembros del consorcio");
        kronaFile.setTags("krona, matriz, abundancia, html");
        kronaFile.setTipo("HTML");
        if (isFromApp) {
            Usuario user = new Usuario(1);//A.ABDALA
            user.setAcciones("creator");
            user.setComentarios("Se encargó del desarrollo y  ejecución de un programa automatizado para la generación de este archivo");
            kronaFile.addUser(user);
            Usuario user2 = new Usuario(20);//A. Escaobar
            user2.setAcciones("contributor");
            user2.setComentarios("Encargada de las asignaciones taxonómicas a partir de las cuales se generar la matriz de abundancia que sirve de entrada para generar este archivo");
            kronaFile.addUser(user2);
        } else {
            Usuario user = new Usuario(20);//A.ABDALA
            user.setAcciones("creator");
            user.setComentarios("Se encargó de la generación de este archivo mediante la implemntación de un pipeline para anotación taxonómica");
            kronaFile.addUser(user);
        }
        if (writer != null) {
            writer.write(kronaFile.toNewSQLString() + ";\n");
            writer.write("INSERT INTO marcador_archivo VALUES(" + idMarcador + "," + kronaFile.getIdArchivo() + ");\n");
            for (String qUsuarios : kronaFile.archivoUsuariosToSQLString()) {
                writer.write(qUsuarios + ";\n");
            }
        } else {
            ArchivoDAO adao = new ArchivoDAO(transacciones);
            adao.insertaArchivo(kronaFile, false, "", true);
            transacciones.insertaArchivoMarcador(idMarcador, kronaFile.getIdArchivo());
            for (String qUsuarios : kronaFile.archivoUsuariosToSQLString()) {
                if (!transacciones.insertaQuery(qUsuarios)) {
                    System.err.println("Error insertando relación usuario-archivo: "
                            + idMarcador + "(idmarcador) - " + nextIDArchivo + "(idArchivo) - q: " + qUsuarios);
                }
            }
        }

        return true;
    }

    /**
     * Este método se encarga de crear la relación marcador archivo, así como
     * dar de alta el archivo en la BD
     *
     * @param idMarcador El id del marcador al cual pertenece el archivo
     * @param proDataPath el path donde se encuentraa el archivo
     * @param fName el nombre del archivo
     * @param isFromApp true si el archivo krona fue generado mediante esta
     * aplicación
     * @param writer null si va a directo a la BD o un writer válido si se
     * escribe a archivo
     * @return true si no hubo problemas
     * @throws IOException
     */
    public boolean addMatrixFile(String idMarcador, String proDataPath, String fName, boolean isFromApp, FileWriter writer) throws IOException {
        ArchivoObj matrixFile = new ArchivoObj(nextIDArchivo);
        nextIDArchivo++;
        File f = new File(proDataPath + fName);
        matrixFile.setTipoArchivo(ArchivoObj.TIPO_MTX);
        matrixFile.setNombre(fName);
        matrixFile.setPath(proDataPath);
        matrixFile.setExtension("txt");
        MyDate date = new MyDate(f.lastModified());
        matrixFile.setDate(date);
        matrixFile.setSize(f.length());
        matrixFile.setDescription("Archivo generado a partir de las anotaciones realizadas en la base de datos");
        matrixFile.setChecksum(FileUtils.getMD5File(proDataPath + fName));
        matrixFile.setAlcance("CIGOM - bioinformática");
        matrixFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        matrixFile.setDerechos("Acceso limitado a miembros del consorcio");
        matrixFile.setTags("krona, matriz, abundancia");
        matrixFile.setTipo("txt");
        if (isFromApp) {
            Usuario user = new Usuario(1);//A.ABDALA
            user.setAcciones("creator");
            user.setComentarios("Se encargó del desarrollo y  ejecución de un programa automatizado para la generación de este archivo");
            matrixFile.addUser(user);
            Usuario user2 = new Usuario(20);//A. Escaobar
            user2.setAcciones("contributor");
            user2.setComentarios("Encargada de las asignaciones taxonómicas a partir de las cuales se generar la matriz de abundancia");
            matrixFile.addUser(user2);
        } else {
            Usuario user = new Usuario(20);//A. Escobar
            user.setAcciones("creator");
            user.setComentarios("Se encargó de la generación de este archivo mediante la implemntación de un pipeline para anotación taxonómica");
            matrixFile.addUser(user);
        }
        if (writer != null) {
            writer.write(matrixFile.toNewSQLString() + ";\n");
            writer.write("INSERT INTO marcador_archivo VALUES(" + idMarcador + "," + matrixFile.getIdArchivo() + ");\n");
            for (String qUsuarios : matrixFile.archivoUsuariosToSQLString()) {
                writer.write(qUsuarios + ";\n");
            }
        } else {
            ArchivoDAO adao = new ArchivoDAO(transacciones);
            adao.insertaArchivo(matrixFile, false, "", true);
            transacciones.insertaArchivoMarcador(idMarcador, matrixFile.getIdArchivo());
            for (String qUsuarios : matrixFile.archivoUsuariosToSQLString()) {
                if (!transacciones.insertaQuery(qUsuarios)) {
                    System.err.println("Error insertando relación usuario-archivo: "
                            + idMarcador + "(idmarcador) - " + nextIDArchivo + "(idArchivo) - q: " + qUsuarios);
                }
            }
        }

        return true;
    }

    public boolean executeKronaScript(String command) {
        try {
            // System.out.println("Rscript c:\\Users\\Alejandro\\Documents\\Cursos\\R\\Project\\release\\taxascript.R " + workingDir + fileName + " " + longitudInicial + " " + longitudFinal + " " + latitudInicial + " " + latitudFinal + " " + cellWG + " " + cellHG + " " + fDir);
            //Process proc = Runtime.getRuntime().exec("c:/Program Files/R/R-3.0.3/bin/Rscript c:/Users/Alejandro/Documents/Cursos/R/Project/release/taxascript.R " + workingDir + fileName + " " + longitudInicial + " " + longitudFinal + " " + latitudInicial + " " + latitudFinal + " " + cellW + " " + cellH + " " + fDir+" "+taxaGroups);
            //String commandLine = "c:/Program Files/R/R-3.0.3/bin/Rscript \"" + workingDir + "scripts/scriptDiversidad.R\" \"" + workingDir + "\" " + nameMatriz + " " + sc.getRealPath("") + fileNameRare + " " + sc.getRealPath("") + fileNameRenyi + " " + betaIndex + " " + sc.getRealPath("") + fileNameBeta + " " + imgExtraTitle;        

            Process proc = Runtime.getRuntime().exec(command);
            //Process proc = Runtime.getRuntime().exec("c:/Program Files/R/R-3.0.3/bin/Rscript " + scriptDir + "script_abundancia.R " + workingDir +" "+ scriptDir + " " +file + " " + imageName);
            proc.waitFor();
            InputStream inputstream = proc.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
            String line = "";
            while ((line = bufferedreader.readLine()) != null) {
                //Si el mensaje de error viene desde nuestro script de R
                //es decir un error controlado
                System.out.println(line);
                if (line.indexOf("ERROR") != -1) {
                    System.err.println(line);
                }
            }
            //
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            System.out.println("" + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isGeneraArchivos() {
        return generaArchivos;
    }

    public void setGeneraArchivos(boolean generaArchivos) {
        this.generaArchivos = generaArchivos;
    }

    /**
     * Este método se encarga de insertar los amplicones en la BD y asociar los
     * archivos crudos a este marcador
     *
     * @param marcador
     * @param outFile
     * @param raw_data_path
     * @param raw_ext
     * @param mdao
     * @return
     */
    public boolean processAmplicones(Marcador marcador, String outFile, String raw_data_path, String raw_ext, MarcadorDAO mdao) {

        boolean toFile = outFile.length() > 1;
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
                        rawFile.setDescription("Datos crudos de amplicones con las secuencias FW");
                    } else if (f.getName().contains("R2")) {
                        rawFile.setDescription("Datos crudos de amplicones con las secuencias RV");
                    } else {
                        rawFile.setDescription("Datos crudos de amplicones ");
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
                    Usuario user = new Usuario(31);//UUSM
                    user.setAcciones("creator");
                    user.setComentarios("Se encargaron de generar las librerías que se mandaron a secuenciar y de donde se obtienen las secuencias");
                    rawFile.addUser(user);
                    marcador.addArchivo(rawFile);
                    //   Usuario user2 = new Usuario(24);//Ricardo Grande
                    //   user2.setAcciones("contributor");
                    //   user2.setComentarios("Encargado de la secuenciación/envío de librerías");
                    //   rawFile.addUser(user2);
                    //   marcador.addArchivo(rawFile);
                    // log += adao.insertaArchivo(rawFile, false, "", true);
                    //transacciones.insertaArchivoMarcador(idMarcador, rawFile.getIdArchivo());
                }
            }
        } else {
            System.err.println("No existe directorio: " + raw_data_path);
            //  return false;
        }
        if (onlyCreateFiles) {
            return mdao.almacenaArchivosMarcadorNew(marcador, toFile, outFile, true);
        } else {
            return mdao.almacenaMarcador(marcador, toFile, outFile, true, true);
        }

    }

    /**
     * Este método se encarga de procesar los archivos de metaxa
     *
     * @param proc_data_path path al archivo de metaxa
     * @param idMarcador id del marcador relacionado a dicho archivo de metaxa
     * @param adao objeto para escribir archivo
     * @param metaxa objeto para escribir metaxa
     * @return true si todo salio bien
     * @throws FileNotFoundException
     * @throws IOException
     */
    public boolean processMeta(String proc_data_path, String metaxFile, String idMarcador, ArchivoDAO adao, ClasificacionDAO metaxa, String outFile, HashMap<String, String> seqMap, boolean processNotPaired) throws FileNotFoundException, IOException {
        File metaxaF = new File(proc_data_path + metaxFile);
        boolean toFile = outFile.length() > 0;
        //FileUtils fUtils = new FileUtils();
        if (metaxaF.exists()) {
            BufferedReader metaxaReader = new BufferedReader(new FileReader(proc_data_path + metaxFile));
            FileWriter writer = null;
            if (generaArchivos) {
                ArchivoObj metaxaFile = new ArchivoObj(nextIDArchivo);
                nextIDArchivo++;
                metaxaFile.setTipoArchivo(ArchivoObj.TIPO_MTX);
                metaxaFile.setNombre(metaxFile.substring(metaxFile.lastIndexOf("/") + 1));
                metaxaFile.setPath(proc_data_path + metaxFile.substring(0, metaxFile.indexOf("/") + 1));
                //metaxaFile.setPath(proc_data_path + "metaxa/");
                metaxaFile.setDescription("Este archivo tiene toda la asignación taxonómica por secuencia. Es generado a partir del programa Metaxa");
                metaxaFile.setExtension("txt");
                int tmpID = nextIDArchivo;
                String tmpSource = "";
                if (processNotPaired) {
                    tmpSource += "" + (tmpID - 1);//NC2;
                    tmpSource += "," + (tmpID - 2);//NC1;
                    tmpSource += "," + (tmpID - 3);//MERGE FILE            
                } else {
                    tmpSource += "" + (tmpID - 1);//MERGE FILE
                }

                metaxaFile.setOrigen(tmpSource);
                MyDate date = new MyDate(metaxaF.lastModified());
                metaxaFile.setDate(date);
                metaxaFile.setSize(metaxaF.getTotalSpace());
                metaxaFile.setChecksum(FileUtils.getMD5File(proc_data_path + metaxFile));
                metaxaFile.setAlcance("Grupo de bioinformática");
                metaxaFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
                metaxaFile.setDerechos("Acceso limitado a miembros");
                metaxaFile.setTags("Asignación taxonómica,Metaxa, amplicones");
                metaxaFile.setTipo("Text");
                Usuario user = new Usuario(20);//ALES
                user.setAcciones("creator");
                user.setComentarios("Desarrollo y ejecución del pipeline para obtener la clasificación taxonómica según Metaxa");
                metaxaFile.addUser(user);
                Usuario user2 = new Usuario(25);//ALEXSF
                user2.setAcciones("contributor");
                user2.setComentarios("Investigador responsable de subproyecto");
                metaxaFile.addUser(user2);
                if (toFile) {
                    writer = new FileWriter(outFile, true);
                    writer.write(metaxaFile.toNewSQLString() + ";\n");
                    writer.write("INSERT INTO marcador_archivo VALUES(" + idMarcador + "," + metaxaFile.getIdArchivo() + ");\n");
                    for (String qUsuarios : metaxaFile.archivoUsuariosToSQLString()) {
                        writer.write(qUsuarios + ";\n");
                    }
                } else {
                    adao.insertaArchivo(metaxaFile, false, "", true);
                    transacciones.insertaArchivoMarcador(idMarcador, metaxaFile.getIdArchivo());
                    for (String qUsuarios : metaxaFile.archivoUsuariosToSQLString()) {
                        if (!transacciones.insertaQuery(qUsuarios)) {
                            System.err.println("Error insertando relación usuario-archivo-metaxa: "
                                    + idMarcador + "(idmarcador) - " + metaxaFile.getIdArchivo() + "(idArchivo) - q: " + qUsuarios);
                        }
                    }
                }
            }
            String lineaMetaxa;
            if (!onlyCreateFiles) {
                while ((lineaMetaxa = metaxaReader.readLine()) != null) {
                    metaxa.processMetaxaLine(lineaMetaxa, idMarcador, proc_data_path + metaxFile, splitSpecial, AnalisisClasificacion.METAXA_REGULAR, writer, seqMap);
                }
            }
            if (toFile) {
                writer.close();
            }
            metaxaReader.close();
        } else {
            System.err.println("no se puede encontrar archivo de metaxa: " + proc_data_path + metaxFile + "\nidMarcador = " + idMarcador);
        }
        return true;
    }

    public boolean processParallel(String proc_data_path, String parallelFile, String idMarcador, ArchivoDAO adao, ClasificacionDAO cDAO, String outFile, HashMap<String, String> seqMap, boolean processNotPaired) throws FileNotFoundException, IOException {
        File parallelF = new File(proc_data_path + parallelFile);
        boolean toFile = outFile.length() > 0;
        //FileUtils fUtils = new FileUtils();
        if (parallelF.exists()) {
            BufferedReader parallelReader = new BufferedReader(new FileReader(proc_data_path + parallelFile));
            FileWriter writer = null;
            if (generaArchivos) {
                ArchivoObj pFile = new ArchivoObj(nextIDArchivo);
                nextIDArchivo++;
                pFile.setTipoArchivo(ArchivoObj.TIPO_MTX);
                pFile.setNombre(parallelFile.substring(parallelFile.lastIndexOf("/") + 1));
                pFile.setPath(proc_data_path + parallelFile.substring(0, parallelFile.lastIndexOf("/") + 1));
                //metaxaFile.setPath(proc_data_path + "metaxa/");
                pFile.setDescription("Este archivo tiene toda la asignación taxonómica por secuencia. Es generado a partir del programa ParallelMeta con la base de datos de Metaxa");
                pFile.setExtension("txt");
                int tmpID = nextIDArchivo;
                /*String tmpSource = "";
                 if (processNotPaired) {
                 tmpSource += "" + (tmpID - 1);//NC2;
                 tmpSource += "," + (tmpID - 2);//NC1;
                 tmpSource += "," + (tmpID - 3);//MERGE FILE            
                 } else {
                 tmpSource += "" + (tmpID - 1);//MERGE FILE
                 }

                 pFile.setOrigen(tmpSource);*/
                MyDate date = new MyDate(parallelF.lastModified());
                pFile.setDate(date);
                pFile.setSize(parallelF.getTotalSpace());
                pFile.setChecksum(FileUtils.getMD5File(proc_data_path + parallelFile));
                pFile.setAlcance("Grupo de bioinformática");
                pFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
                pFile.setDerechos("Acceso limitado a miembros");
                pFile.setTags("Asignación taxonómica,ParallelMetaMetaxa DB, amplicones");
                pFile.setTipo("Text");
                Usuario user = new Usuario(20);//ALES
                user.setAcciones("creator");
                user.setComentarios("Desarrollo y ejecución del pipeline para obtener la clasificación taxonómica según ParallelMeta con la base de datos de Metaxa");
                pFile.addUser(user);
                Usuario user2 = new Usuario(25);//ALEXSF
                user2.setAcciones("contributor");
                user2.setComentarios("Investigador responsable de subproyecto");
                pFile.addUser(user2);
                if (toFile) {
                    writer = new FileWriter(outFile, true);
                    writer.write(pFile.toNewSQLString() + ";\n");
                    writer.write("INSERT INTO marcador_archivo VALUES(" + idMarcador + "," + pFile.getIdArchivo() + ");\n");
                    for (String qUsuarios : pFile.archivoUsuariosToSQLString()) {
                        writer.write(qUsuarios + ";\n");
                    }
                } else {
                    adao.insertaArchivo(pFile, false, "", true);
                    transacciones.insertaArchivoMarcador(idMarcador, pFile.getIdArchivo());
                    for (String qUsuarios : pFile.archivoUsuariosToSQLString()) {
                        if (!transacciones.insertaQuery(qUsuarios)) {
                            System.err.println("Error insertando relación usuario-archivo-metaxa: "
                                    + idMarcador + "(idmarcador) - " + pFile.getIdArchivo() + "(idArchivo) - q: " + qUsuarios);
                        }
                    }
                }
            }
            String lineaParallel;
            if (!onlyCreateFiles) {
                int numLinea = 0;
                while ((lineaParallel = parallelReader.readLine()) != null) {
                    if (!lineaParallel.startsWith("#") && numLinea > 0) {
                        cDAO.processParallelLine(lineaParallel, idMarcador, proc_data_path + parallelFile, splitSpecial, AnalisisClasificacion.PARALLEL_W_METAXADB, writer, seqMap);
                    }
                    numLinea++;
                }
            }
            if (toFile) {
                writer.close();
            }
            parallelReader.close();
        } else {
            System.err.println("no se puede encontrar archivo de clasificacion: " + proc_data_path + parallelFile + "\nidMarcador = " + idMarcador);
        }
        return true;
    }

    /**
     * Método igual a parseMarkerFileFormatI pero con unas modificaciones pues
     * no hay un pre procesamiento como es el de flash por lo que el fasta a
     * leer para extraer las secuencias el mismo de raw data y el id de la
     * secuencia raw tiene que ser seq_# donde # num sec pues así esta reportado
     * en metaxa.taxonomy.txt
     *
     * @param inputFile Archivo tab delim con header
     * @param insertAmplicones true si va a insertar los marcadores en la BD
     * false si estos ya estan en la BD y se corre el programa para actualizar
     * datos procesados
     * @param processOut true si va a procesar los archivos de secuencias
     * @param processMetaxa true si va a procesar archivos de metaxa para cargar
     * la matriz
     * @param raw_ext la extención del archivo crudo por default es fastq o
     * fastq.gz
     * @return
     *
     */
    public String parseMarkerFileFormatIPacbio(String inputFile, boolean insertAmplicones, boolean processOut, boolean processMetaxa, String raw_ext) {
        String log = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1"));
            String linea;
            ArchivoDAO adao = new ArchivoDAO(transacciones);
            ClasificacionDAO metaxa = new ClasificacionDAO(transacciones);
            nextIDArchivo = transacciones.getNextIDArchivos();
            while (((linea = reader.readLine()) != null)) {
                if (linea.length() > 0 && !linea.startsWith("#")) {
                    StringTokenizer st = new StringTokenizer(linea, "\t");
                    String sample_db_id = st.nextToken();
                    String tag = st.nextToken();
                    String map = st.nextToken();
                    String raw_data_path = st.nextToken();
                    String proc_data_path = st.nextToken();
                    String marc_name = st.nextToken();
                    String marc_desc = st.nextToken();
                    String library_selection = st.nextToken();
                    String library_layout = st.nextToken();
                    String idMarcador = st.nextToken();
                    String idTipoMarcador = st.nextToken();
                    String idTipoSec = st.nextToken();
                    String idSecuenciador = st.nextToken();
                    String idPcr = st.nextToken();
                    String pre_process = st.nextToken();
                    boolean mark_id = false;

                    if (insertAmplicones) {
                        mark_id = transacciones.insertaMarcador(idMarcador, sample_db_id, idTipoMarcador, idTipoSec, idSecuenciador, idPcr, marc_name, marc_desc, library_selection, library_layout, raw_data_path, proc_data_path, pre_process, "");
                        if (!mark_id) {
                            System.err.println("ERROR QUERY:  INSERT INTO marcador VALUES"
                                    + "(" + idMarcador + "," + sample_db_id + "," + idTipoMarcador + "," + idTipoSec + "," + idSecuenciador + "," + idPcr + ","
                                    + raw_data_path + "','" + proc_data_path + "')\n");

                        } else {
                            File rawFolder = new File(raw_data_path);
                            for (File f : rawFolder.listFiles()) {
                                if (f.getName().endsWith(raw_ext)) {
                                    ArchivoObj rawFile = new ArchivoObj(nextIDArchivo);
                                    nextIDArchivo++;
                                    rawFile.setTipoArchivo(ArchivoObj.TIPO_RAW);
                                    rawFile.setNombre(f.getName());
                                    rawFile.setPath(raw_data_path);
                                    rawFile.setDescription("Datos crudos de amplicones");
                                    rawFile.setExtension(raw_ext);
                                    log += adao.insertaArchivo(rawFile, false, "", true);
                                    transacciones.insertaArchivoMarcador(idMarcador, rawFile.getIdArchivo());
                                    if ((processOut && insertAmplicones && mark_id) || (processOut && !insertAmplicones)) {
                                        BufferedReader extendedReader = new BufferedReader(
                                                new FileReader(raw_data_path + f.getName()));

                                        String lineFastQ;
                                        int sec_num = 0;
                                        int counterTotal = 0;
                                        float avg = 0;
                                        while ((lineFastQ = extendedReader.readLine()) != null) {
                                            if (lineFastQ.startsWith("@M") || lineFastQ.startsWith("@m")) {
                                                sec_num++;
                                                counterTotal++;
                                                String idSec = marc_name + "_" + counterTotal;
                                                String raw_seq_id = "seq_" + counterTotal;//lineFastQ.substring(1, lineFastQ.indexOf(" "));
                                                String sec = extendedReader.readLine();
                                                avg += sec.length();
                                                if (!transacciones.insertaSeqMarcador(idSec, "" + idMarcador, raw_seq_id, sec)) {
                                                    System.out.println("Error insertando secuencia: " + idSec);
                                                    log += "Error insertando secuencia: " + idSec + "\n";
                                                }
                                            }
                                        }
                                        extendedReader.close();

                                        if (!transacciones.updateSeqNumMarcador(idMarcador, counterTotal)) {
                                            System.err.println("Error actualizando marcador id : sec_num_t : " + idMarcador + ":" + sec_num);
                                        }
                                    }
                                }
                            }

                        }

                    }
                    //SI EL USUARIO QUIERE BUSCAR POR ARCHIVOS DE SECUENCIAS PARA PROCESARLAS Y ANOTARLAS EN LA BD
                    //aca es diferente con el otro método

                    if (processMetaxa) {
                        //to impl cargar metaxa
                        processMeta(proc_data_path, "", idMarcador, adao, metaxa, null, null, false);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MarkerLoader.class.getName()).log(Level.SEVERE, null, ex);
            log += "No existe el archivo ";
        } catch (IOException ex) {
            Logger.getLogger(MarkerLoader.class.getName()).log(Level.SEVERE, null, ex);
            log += "No existe el archivo " + ex.getLocalizedMessage();
        }
        return log;
    }
}
