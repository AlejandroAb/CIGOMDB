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
import bobjects.Usuario;
import dao.ArchivoDAO;
import dao.MarcadorDAO;
import dao.MetaxaDAO;
import java.io.FileInputStream;
import java.io.FileWriter;
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

    public Transacciones getTransacciones() {
        return transacciones;
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

    /**
     * Parsea un archivo hecho a la medida para cargar los amplicones procesados
     * en la campaña SOGOM-I. Archivo:
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
     * @processNotPaired Si es true procesa los archivos de fragmentos no
     * pareados, default es false
     * @return String con log de l proceso de anotación.
     *
     */
    public String parseMarkerFileFormatI(String inputFile, boolean insertAmplicones, boolean processOut, boolean processMetaxa, String raw_ext, String outFile, String outFileFasta, String outFileMetaxa, boolean processNotPaired, boolean processKrona) {
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
            MetaxaDAO metaxa = new MetaxaDAO(transacciones);
            MarcadorDAO mdao = new MarcadorDAO(transacciones);
            HashMap<String, String> seqMap = new HashMap<>();
            int idxIdMuestra = -1, idxTag = -1, idxRaw = -1, idxProc = -1,
                    idxMarcName = -1, idxMarcDesc = -1, idxSelection = -1,
                    idxLayout = -1, idxIdMarcador = -1, idxTipoMarcador = -1,
                    idxTipoSec = -1, idxSecuenciador = -1, idxPcr = -1,
                    idxQC = -1, idxPre = -1, idxVol = -1, idxVector = -1;
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
                            } else if (tok.contains("VECTOR")) {//PROC DATA PATH
                                idxVector = toks;
                            } else if (tok.contains("QC")) {//PROC DATA PATH
                                idxQC = toks;
                            }
                        }
                    } else {
                        StringTokenizer st = new StringTokenizer(linea, "\t");
                        Marcador marcador = new Marcador();
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
                            } else if (tok == idxIdMarcador) {
                                idMarcador = st.nextToken().trim();
                                marcador.setIdMarcador(idMarcador);
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
                            } else {
                                st.nextToken();
                            }
                        }
                        if (idMuestra.equals("")) {
                            idMuestra = transacciones.getIdMuestraByLabel(tag);
                            marcador.setIdMuestra(idMuestra);
                        }
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
                        if (processMetaxa && processOut) {
                            //to impl cargar metaxa
                            processMeta(proc_data_path, marcador.getIdMarcador(), adao, metaxa, outFileMetaxa, seqMap, processNotPaired);
                            seqMap = new HashMap<>();
                        }
                        if (processKrona) {
                            //  processKrona(proc_data_path, idMarcador, "");
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
                new FileReader(proc_data_path + proc_combined_file));
        BufferedReader nc1Reader = null;
        BufferedReader nc2Reader = null;
        if (processNotPaired) {
            nc1Reader = new BufferedReader(new FileReader(proc_data_path + proc_nc1_file));
            nc2Reader = new BufferedReader(new FileReader(proc_data_path + proc_nc2_file));
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
        while ((lineFastQ = extendedReader.readLine()) != null) {
            if (lineFastQ.startsWith("@M")) {
                sec_num++;
                counterTotal++;               
                String idSec = marcador.getIdSeqFormat()+counterTotal;
                  int indx = lineFastQ.indexOf(" ");
                if (indx <= 0) {
                    indx = lineFastQ.length() - 1;
                }
                String raw_seq_id = lineFastQ.substring(1, indx);
                String sec = extendedReader.readLine();
                avg += sec.length();
                if (toFile) {
                    String query = "INSERT INTO seq_marcador (idseq_marcador, idmarcador,raw_seq_id,seq,seq_length) VALUES('"
                            + idSec + "'," + marcador.getIdMarcador() + ",'" + raw_seq_id + "','" + sec + "'," + sec.length() + ");\n";
                    writer.write(query);
                    seqMap.put(raw_seq_id, idSec);

                } else {
                    if (!transacciones.insertaSeqMarcador(idSec, "" + marcador.getIdMarcador(), raw_seq_id, sec)) {
                        System.err.println("Error insertando secuencia: " + idSec);

                    }
                }
            }
        }
        extendedReader.close();
        //Archivo merged
        ArchivoObj mergeFile = new ArchivoObj(nextIDArchivo);
        File tmpFile = new File(proc_data_path + proc_combined_file);
        nextIDArchivo++;
        mergeFile.setTipoArchivo(ArchivoObj.TIPO_PRE);
        mergeFile.setNombre(proc_combined_file.substring(proc_combined_file.lastIndexOf("/") + 1));
        mergeFile.setPath(proc_data_path + proc_combined_file.substring(0, proc_combined_file.indexOf("/") + 1));
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
        mergeFile.setSize(tmpFile.getTotalSpace());
        mergeFile.setChecksum(FileUtils.getMD5File(proc_data_path + proc_combined_file));
        mergeFile.setAlcance("Grupo de bioinformática");
        mergeFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        mergeFile.setDerechos("Acceso limitado a miembros");
        mergeFile.setTags("Secuencias pareadas, amplicones");
        mergeFile.setTipo("Text");
        Usuario user = new Usuario(20);//ALES
        user.setAcciones("creator");
        user.setComentarios("Se encarga de ejecutar el programa Flash para parear las lecturas que da como resultado este archivo.");
        mergeFile.addUser(user);
        Usuario user2 = new Usuario(25);//ALEXSF
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

        if (processNotPaired) {
            avg = 0;
            sec_num = 0;
            while ((lineFastQ = nc1Reader.readLine()) != null) {
                if (lineFastQ.startsWith("@M")) {
                    sec_num++;
                    counterTotal++;
                    String idSec = marcador.getIdSeqFormat()+counterTotal;
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
                        seqMap.put(raw_seq_id, idSec);

                    } else {
                        if (!transacciones.insertaSeqMarcador(idSec, "" + marcador.getIdMarcador(), raw_seq_id, sec)) {
                            System.err.println("Error insertando secuencia: " + idSec);

                        }
                    }
                }
            }
            nc1Reader.close();
            ArchivoObj nc1File = new ArchivoObj(nextIDArchivo);
            nextIDArchivo++;
            nc1File.setTipoArchivo(ArchivoObj.TIPO_PRE);
            nc1File.setNombre(proc_nc1_file.substring(proc_nc1_file.lastIndexOf("/") + 1));
            nc1File.setPath(proc_data_path + proc_nc1_file.substring(0, proc_nc1_file.indexOf("/") + 1));
            nc1File.setDescription("Fastq con las secuencias FW que no se pudieron empalmar con las de RV");
            nc1File.setExtension("fastq");
            nc1File.setNum_secs(sec_num);
            nc1File.setSeq_length(avg / sec_num);
            if (toFile) {
                writer.write(nc1File.toNewSQLString() + ";\n");
                writer.write("INSERT INTO marcador_archivo VALUES(" + marcador.getIdMarcador() + "," + nc1File.getIdArchivo() + ");\n");
                for (String qUsuarios : nc1File.archivoUsuariosToSQLString()) {
                    writer.write(qUsuarios + ";\n");
                }
            } else {
                adao.insertaArchivo(nc1File, false, "", true);
                transacciones.insertaArchivoMarcador(marcador.getIdMarcador(), nc1File.getIdArchivo());
                for (String qUsuarios : nc1File.archivoUsuariosToSQLString()) {
                    if (!transacciones.insertaQuery(qUsuarios)) {
                        System.err.println("Error insertando relación usuario-archivo: "
                                + marcador.getIdMarcador() + "(idmarcador) - " + nc1File.getIdArchivo() + "(idArchivo) - q: " + qUsuarios);
                    }
                }
            }
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
                        seqMap.put(raw_seq_id, idSec);

                    } else {
                        if (!transacciones.insertaSeqMarcador(idSec, "" + marcador.getIdMarcador(), raw_seq_id, sec)) {
                            System.err.println("Error insertando secuencia: " + idSec);

                        }
                    }
                }
            }
            nc2Reader.close();
            ArchivoObj nc2File = new ArchivoObj(nextIDArchivo);
            nextIDArchivo++;
            nc2File.setTipoArchivo(ArchivoObj.TIPO_PRE);
            nc2File.setNombre(proc_nc2_file.substring(proc_nc2_file.lastIndexOf("/") + 1));
            nc2File.setPath(proc_data_path + proc_nc2_file.substring(0, proc_nc2_file.indexOf("/") + 1));
            nc2File.setDescription("Fastq con las secuencias RV que no se pudieron empalmar con las de FW");
            nc2File.setExtension("fastq");
            nc2File.setNum_secs(sec_num);
            nc2File.setSeq_length(avg / sec_num);
            if (toFile) {
                writer.write(nc2File.toNewSQLString() + ";\n");
                writer.write("INSERT INTO marcador_archivo VALUES(" + marcador.getIdMarcador() + "," + nc2File.getIdArchivo() + ");\n");
                writer.write("UPDATE marcador set seq_num_total = " + counterTotal + " WHERE idmarcador = " + marcador.getIdMarcador() + ";\n");
                for (String qUsuarios : nc2File.archivoUsuariosToSQLString()) {
                    writer.write(qUsuarios + ";\n");
                }
                writer.close();
            } else {
                adao.insertaArchivo(nc2File, false, "", true);
                transacciones.insertaArchivoMarcador(marcador.getIdMarcador(), nc2File.getIdArchivo());
                for (String qUsuarios : nc2File.archivoUsuariosToSQLString()) {
                    if (!transacciones.insertaQuery(qUsuarios)) {
                        System.err.println("Error insertando relación usuario-archivo: "
                                + marcador.getIdMarcador() + "(idmarcador) - " + nc1File.getIdArchivo() + "(idArchivo) - q: " + qUsuarios);
                    }
                }
                if (!transacciones.updateSeqNumMarcador(marcador.getIdMarcador(), counterTotal)) {
                    System.err.println("Error actualizando marcador id : sec_num_t : " + marcador.getIdMarcador() + ":" + sec_num);
                }

            }
        }
    }

    /**
     * Se encarga de anotar/ relacionar el archivo de krona, con un marcador. En
     * caso de que no exxista el archivo, este metodo tratará de crearlo, por lo
     * que puede generar el input file necesario para generar el archivo html
     * que luego es desplegado en la aplicación
     *
     * @param proc_data_path
     * @param idMarcador
     * @param inPut
     */
    public void processKrona(Marcador marcador, String proc_data_path, String inPut) {
        ArchivoObj kronaFile = new ArchivoObj(nextIDArchivo);
        nextIDArchivo++;
        kronaFile.setTipoArchivo(ArchivoObj.TIPO_KRN);
        //rawFile.setNombre(f.getName());
        kronaFile.setPath(proc_data_path + "krona");
        kronaFile.setExtension("html");

        kronaFile.setDescription("Datos crudos de amplicones con las secuencias FW");

        //  kronaFile.setChecksum(FileUtils.getMD5File(raw_data_path + f.getName()));
        kronaFile.setAlcance("Grupo de bioinformática");
        kronaFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        kronaFile.setDerechos("Acceso limitado a miembros");
        kronaFile.setTags("Secuencias crudas, amplicones");
        kronaFile.setTipo("Text");
        Usuario user = new Usuario(26);//UUSM
        user.setAcciones("creator");
        user.setComentarios("Se encargaron de generar las librerías que se mandaron a secuenciar y de donde se obtienen las secuencias");
        kronaFile.addUser(user);
        Usuario user2 = new Usuario(24);//Ricardo Grande
        user2.setAcciones("contributor");
        user2.setComentarios("Encargado de la secuenciación/envío de librerías");
        kronaFile.addUser(user2);
        marcador.addArchivo(kronaFile);
        if (inPut.length() > 0) {

        } else {
            File proDir = new File(proc_data_path);
           // MyDate date = new MyDate(f.lastModified());
            // kronaFile.setDate(date);
            //  kronaFile.setSize(f.getTotalSpace());
            if (proDir.isDirectory()) {
                boolean findKronaDir = false;
                for (File f : proDir.listFiles()) {
                    if (f.isDirectory() && f.getName().equals("krona")) {
                    }
                }
            } else {
                System.err.println("No se puede determinar el directorio de trabajao para paracador con ID: " + marcador.getIdMarcador() + " ruta: " + proc_data_path);
            }
        }
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
    public boolean processAmplicones(Marcador marcador,  String outFile, String raw_data_path, String raw_ext, MarcadorDAO mdao) {

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
                    rawFile.setSize(f.getTotalSpace());

                    if (f.getName().contains("R1")) {
                        rawFile.setDescription("Datos crudos de amplicones con las secuencias FW");
                    } else if (f.getName().contains("R2")) {
                        rawFile.setDescription("Datos crudos de amplicones con las secuencias RV");
                    } else {
                        rawFile.setDescription("Datos crudos de amplicones ");
                    }
                    rawFile.setChecksum(FileUtils.getMD5File(raw_data_path + f.getName()));
                    rawFile.setAlcance("Grupo de bioinformática");
                    rawFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
                    rawFile.setDerechos("Acceso limitado a miembros");
                    rawFile.setTags("Secuencias crudas, amplicones");
                    rawFile.setTipo("Text");
                    Usuario user = new Usuario(26);//UUSM
                    user.setAcciones("creator");
                    user.setComentarios("Se encargaron de generar las librerías que se mandaron a secuenciar y de donde se obtienen las secuencias");
                    rawFile.addUser(user);
                    Usuario user2 = new Usuario(24);//Ricardo Grande
                    user2.setAcciones("contributor");
                    user2.setComentarios("Encargado de la secuenciación/envío de librerías");
                    rawFile.addUser(user2);
                    marcador.addArchivo(rawFile);
                    // log += adao.insertaArchivo(rawFile, false, "", true);
                    //transacciones.insertaArchivoMarcador(idMarcador, rawFile.getIdArchivo());
                }
            }
        } else {
            System.err.println("No existe directorio: " + raw_data_path);
            //  return false;
        }

        return mdao.almacenaMarcador(marcador, toFile, outFile, true, true);

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
    public boolean processMeta(String proc_data_path, String idMarcador, ArchivoDAO adao, MetaxaDAO metaxa, String outFile, HashMap<String, String> seqMap, boolean processNotPaired) throws FileNotFoundException, IOException {
        File metaxaF = new File(proc_data_path + proc_metaxa_file);
        //FileUtils fUtils = new FileUtils();
        if (metaxaF.exists()) {
            BufferedReader metaxaReader = new BufferedReader(new FileReader(proc_data_path + proc_metaxa_file));
            FileWriter writer = null;

            ArchivoObj metaxaFile = new ArchivoObj(nextIDArchivo);
            nextIDArchivo++;
            metaxaFile.setTipoArchivo(ArchivoObj.TIPO_MTX);
            metaxaFile.setNombre(proc_metaxa_file.substring(proc_metaxa_file.lastIndexOf("/") + 1));
            metaxaFile.setPath(proc_data_path + proc_metaxa_file.substring(0, proc_metaxa_file.indexOf("/") + 1));
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
            metaxaFile.setChecksum(FileUtils.getMD5File(proc_data_path + proc_metaxa_file));
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

            boolean toFile = outFile.length() > 0;
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
            int lineas = 0;
            String lineaMetaxa;
            while ((lineaMetaxa = metaxaReader.readLine()) != null) {
                metaxa.processMetaxaLine(lineaMetaxa, AnalisisClasificacion.METAXA_REGULAR, writer, seqMap);
            }
            if (toFile) {
                writer.close();
            }
            metaxaReader.close();
        } else {
            System.err.println("no se puede encontrar archivo de metaxa: " + proc_data_path + "metaxa/metaxa.taxonomy.txt\nidMarcador = " + idMarcador);
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
            MetaxaDAO metaxa = new MetaxaDAO(transacciones);
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
                        processMeta(proc_data_path, idMarcador, adao, metaxa, null, null, false);
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
