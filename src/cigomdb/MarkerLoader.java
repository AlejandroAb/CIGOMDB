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
import dao.ArchivoDAO;
import dao.MetaxaDAO;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 *
 * @author Alejandro
 */
public class MarkerLoader {

    public Transacciones transacciones;

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
     * @return
     *
     */
    public String parseMarkerFileFormatI(String inputFile, boolean insertAmplicones, boolean processOut, boolean processMetaxa, String raw_ext) {
        String log = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1"));
            String linea;
            ArchivoDAO adao = new ArchivoDAO(transacciones);
            MetaxaDAO metaxa = new MetaxaDAO(transacciones);
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
                            System.out.println("ERROR QUERY:  INSERT INTO marcador VALUES"
                                    + "(" + idMarcador + "," + sample_db_id + "," + idTipoMarcador + "," + idTipoSec + "," + idSecuenciador + "," + idPcr + ","
                                    + raw_data_path + "','" + proc_data_path + "')\n");
                            log += "ERROR QUERY:  INSERT INTO marcador VALUES"
                                    + "(" + idMarcador + "," + sample_db_id + "," + idTipoMarcador + "," + idTipoSec + "," + idSecuenciador + "," + idPcr + ","
                                    + raw_data_path + "','" + proc_data_path + "')\n";
                        } else {
                            File rawFolder = new File(raw_data_path);
                            for (File f : rawFolder.listFiles()) {
                                if (f.getName().endsWith(raw_ext)) {
                                    ArchivoObj rawFile = new ArchivoObj(transacciones.getMaxIDArchivo());
                                    rawFile.setTipoArchivo(ArchivoObj.TIPO_RAW);
                                    rawFile.setNombre(f.getName());
                                    rawFile.setPath(raw_data_path);
                                    rawFile.setDescription("Datos crudos de amplicones");
                                    rawFile.setExtension(raw_ext);
                                    log += adao.insertaArchivo(rawFile, false, "", true);
                                    transacciones.insertaArchivoMarcador(idMarcador, rawFile.getIdArchivo());
                                }
                            }

                        }

                    }
                    //SI EL USUARIO QUIERE BUSCAR POR ARCHIVOS DE SECUENCIAS PARA PROCESARLAS Y ANOTARLAS EN LA BD
                    if ((processOut && insertAmplicones && mark_id) || (processOut && !insertAmplicones)) {
                        BufferedReader extendedReader = new BufferedReader(
                                new FileReader(proc_data_path + "flash.extendedFrags.fastq"));
                        BufferedReader nc1Reader = new BufferedReader(new FileReader(proc_data_path + "flash.notCombined_1.fastq"));
                        BufferedReader nc2Reader = new BufferedReader(new FileReader(proc_data_path + "flash.notCombined_2.fastq"));

                        String lineFastQ;
                        int sec_num = 0;
                        int counterTotal = 0;
                        float avg = 0;
                        while ((lineFastQ = extendedReader.readLine()) != null) {
                            if (lineFastQ.startsWith("@M")) {
                                sec_num++;
                                counterTotal++;
                                String idSec = marc_name + "_" + counterTotal;
                                int indx = lineFastQ.indexOf(" ");
                                if(indx <=0){
                                    indx = lineFastQ.length()-1;
                                }
                                String raw_seq_id = lineFastQ.substring(1, indx);                                
                                String sec = extendedReader.readLine();
                                avg += sec.length();
                                if (!transacciones.insertaSeqMarcador(idSec, "" + idMarcador, raw_seq_id, sec)) {
                                    System.out.println("Error insertando secuencia: " + idSec);
                                    log += "Error insertando secuencia: " + idSec + "\n";
                                }
                            }
                        }
                        extendedReader.close();
                        //Archivo merged
                        ArchivoObj mergeFile = new ArchivoObj(transacciones.getMaxIDArchivo());
                        mergeFile.setTipoArchivo(ArchivoObj.TIPO_PRE);
                        mergeFile.setNombre("flash.extendedFrags.fastq");
                        mergeFile.setPath(proc_data_path);
                        mergeFile.setDescription("Fastq con lecturas pareadas - merge de FW y RV");
                        mergeFile.setExtension(raw_ext);
                        mergeFile.setNum_secs(sec_num);
                        mergeFile.setSeq_length(avg / sec_num);
                        adao.insertaArchivo(mergeFile, false, "", true);
                        transacciones.insertaArchivoMarcador(idMarcador, mergeFile.getIdArchivo());
                        avg = 0;
                        sec_num = 0;
                        while ((lineFastQ = nc1Reader.readLine()) != null) {
                            if (lineFastQ.startsWith("@M")) {
                                sec_num++;
                                counterTotal++;
                                String idSec = marc_name + "_" + counterTotal;
                                int indx = lineFastQ.indexOf(" ");
                                if(indx <=0){
                                    indx = lineFastQ.length()-1;
                                }
                                String raw_seq_id = lineFastQ.substring(1, indx);                                
                                String sec = nc1Reader.readLine();
                                avg += sec.length();
                                if (!transacciones.insertaSeqMarcador(idSec, "" + idMarcador, raw_seq_id, sec)) {
                                    System.out.println("Error insertando secuencia: " + idSec);
                                    log += "Error insertando secuencia: " + idSec + "\n";

                                }
                            }
                        }
                        nc1Reader.close();
                        ArchivoObj nc1File = new ArchivoObj(transacciones.getMaxIDArchivo());
                        nc1File.setTipoArchivo(ArchivoObj.TIPO_PRE);
                        nc1File.setNombre("flash.notCombined_1.fastq");
                        nc1File.setPath(proc_data_path);
                        nc1File.setDescription("Fastq con las secuencias FW que no se pudieron empalmar con las de RV");
                        nc1File.setExtension(raw_ext);
                        nc1File.setNum_secs(sec_num);
                        nc1File.setSeq_length(avg / sec_num);
                        log += adao.insertaArchivo(nc1File, false, "", true);
                        transacciones.insertaArchivoMarcador(idMarcador, nc1File.getIdArchivo());
                        avg = 0;
                        sec_num = 0;
                        while ((lineFastQ = nc2Reader.readLine()) != null) {
                            if (lineFastQ.startsWith("@M")) {
                                sec_num++;
                                counterTotal++;
                                String idSec = marc_name + "_" + counterTotal;
                                int indx = lineFastQ.indexOf(" ");
                                if(indx <=0){
                                    indx = lineFastQ.length()-1;
                                }
                                String raw_seq_id = lineFastQ.substring(1, indx);                                
                                String sec = nc2Reader.readLine();
                                avg += sec.length();
                                if (!transacciones.insertaSeqMarcador(idSec, "" + idMarcador, raw_seq_id, sec)) {
                                    System.out.println("Error insertando secuencia: " + idSec);
                                    log += "Error insertando secuencia: " + idSec + "\n";
                                }
                            }
                        }
                        nc2Reader.close();
                        ArchivoObj nc2File = new ArchivoObj(transacciones.getMaxIDArchivo());
                        nc2File.setTipoArchivo(ArchivoObj.TIPO_PRE);
                        nc2File.setNombre("flash.notCombined_2.fastq");
                        nc2File.setPath(proc_data_path);
                        nc2File.setDescription("Fastq con las secuencias RV que no se pudieron empalmar con las de FW");
                        nc2File.setExtension(raw_ext);
                        nc2File.setNum_secs(sec_num);
                        nc2File.setSeq_length(avg / sec_num);
                        log += adao.insertaArchivo(nc2File, false, "", true);
                        transacciones.insertaArchivoMarcador(idMarcador, nc2File.getIdArchivo());
                        if (!transacciones.updateSeqNumMarcador(idMarcador, counterTotal)) {
                            log += "Error actualizando marcador id : sec_num_t : " + idMarcador + ":" + sec_num;
                        }
                    }
                    if (processMetaxa) {
                        //to impl cargar metaxa
                        processMeta(proc_data_path, idMarcador, adao, metaxa);
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
    public boolean processMeta(String proc_data_path, String idMarcador, ArchivoDAO adao, MetaxaDAO metaxa) throws FileNotFoundException, IOException {
        File metaxaF = new File(proc_data_path + "metaxa/metaxa.taxonomy.txt");
        if (metaxaF.exists()) {
            BufferedReader metaxaReader = new BufferedReader(new FileReader(proc_data_path + "metaxa/metaxa.taxonomy.txt"));
            ArchivoObj metaxaFile = new ArchivoObj(transacciones.getMaxIDArchivo());
            metaxaFile.setTipoArchivo(ArchivoObj.TIPO_MTX);
            metaxaFile.setNombre("metaxa.taxonomy.txt");
            metaxaFile.setPath(proc_data_path + "metaxa/");
            metaxaFile.setDescription("Archivo que sirve para asignar un tax a una secuencia");
            metaxaFile.setExtension("txt");
            adao.insertaArchivo(metaxaFile, false, "", true);
            transacciones.insertaArchivoMarcador(idMarcador, metaxaFile.getIdArchivo());
            int lineas = 0;
            String lineaMetaxa;
            while ((lineaMetaxa = metaxaReader.readLine()) != null) {
                metaxa.processMetaxaLine(lineaMetaxa, AnalisisClasificacion.METAXA_REGULAR);
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
                                    ArchivoObj rawFile = new ArchivoObj(transacciones.getMaxIDArchivo());
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
                                                String raw_seq_id = "seq_"+counterTotal;//lineFastQ.substring(1, lineFastQ.indexOf(" "));
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
                        processMeta(proc_data_path, idMarcador, adao, metaxa);
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
