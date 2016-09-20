/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import database.Transacciones;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
     * @param inputFile Archivo tab delim con header ID BD	TAG	raw_data_pah
     * pro_data_path	idMarcador	idTipoMarcador	tipo_sec	idsecuenciador	idPcr
     * pre_proc
     *
     */
    public String parseSOGOMMarkerFile(String inputFile) {
        String log = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String linea;
            while (((linea = reader.readLine()) != null)) {
                if (linea.length() > 0 && !linea.startsWith("#")) {
                    StringTokenizer st = new StringTokenizer(linea, "\t");
                    String sample_db_id = st.nextToken();
                    String tag = st.nextToken();
                    String raw_data_path = st.nextToken();
                    String proc_data_path = st.nextToken();
                    String idMarcador = st.nextToken();
                    String idTipoMarcador = st.nextToken();
                    String idTipoSec = st.nextToken();
                    String idSecuenciador = st.nextToken();
                    String idPcr = st.nextToken();
                    String pre_process = st.nextToken();
                    boolean mark_id = transacciones.insertaMarcador(idMarcador, sample_db_id, idTipoMarcador, idTipoSec, idSecuenciador, idPcr, raw_data_path, proc_data_path, pre_process, "");
                    if (mark_id) {
                        BufferedReader extendedReader = new BufferedReader(new FileReader(proc_data_path + "flash.extendedFrags.fastq"));
                        BufferedReader nc1Reader = new BufferedReader(new FileReader(proc_data_path + "flash.notCombined_1.fastq"));
                        BufferedReader nc2Reader = new BufferedReader(new FileReader(proc_data_path + "flash.notCombined_2.fastq"));
                        String lineFastQ;
                        int sec_num = 0;
                        while ((lineFastQ = extendedReader.readLine()) != null) {
                            if (lineFastQ.startsWith("@M")) {
                                sec_num++;
                                String idSec = "seq_" + sec_num;
                                String raw_seq_id = lineFastQ.substring(0, lineFastQ.indexOf(" "));
                                String sec = extendedReader.readLine();
                                if (!transacciones.insertaSeqMarcador(idSec, "" + idMarcador, raw_seq_id, sec)) {
                                    System.out.println("Error insertando secuencia: " + idSec);
                                    log += "Error insertando secuencia: " + idSec + "\n";
                                }
                            }
                        }
                        extendedReader.close();
                        while ((lineFastQ = nc1Reader.readLine()) != null) {
                            if (lineFastQ.startsWith("@M")) {
                                sec_num++;
                                String idSec = "seq_" + sec_num;
                                String raw_seq_id = lineFastQ.substring(0, lineFastQ.indexOf(" "));
                                String sec = nc1Reader.readLine();
                                if (!transacciones.insertaSeqMarcador(idSec, "" + idMarcador, raw_seq_id, sec)) {
                                    System.out.println("Error insertando secuencia: " + idSec);
                                    log += "Error insertando secuencia: " + idSec + "\n";

                                }
                            }
                        }
                        nc1Reader.close();
                        while ((lineFastQ = nc2Reader.readLine()) != null) {
                            if (lineFastQ.startsWith("@M")) {
                                sec_num++;
                                String idSec = "seq_" + sec_num;
                                String raw_seq_id = lineFastQ.substring(0, lineFastQ.indexOf(" "));
                                String sec = nc2Reader.readLine();
                                if (!transacciones.insertaSeqMarcador(idSec, "" + idMarcador, raw_seq_id, sec)) {
                                    System.out.println("Error insertando secuencia: " + idSec);
                                    log += "Error insertando secuencia: " + idSec + "\n";
                                }
                            }
                        }
                        nc2Reader.close();
                        if (!transacciones.updateSeqNumMarcador(idMarcador, sec_num)) {
                            log += "Error actualizando marcador id : sec_num_t : " + idMarcador + ":" + sec_num;
                        }
                    } else {
                        System.out.println("ERROR QUERY:  INSERT INTO marcador VALUES"
                                + "(0," + sample_db_id + "," + idTipoMarcador + "," + idTipoSec + "," + idSecuenciador + "," + idPcr + ","
                                + raw_data_path + "','" + proc_data_path + "')\n");
                        log += "ERROR QUERY:  INSERT INTO marcador VALUES"
                                + "(0," + sample_db_id + "," + idTipoMarcador + "," + idTipoSec + "," + idSecuenciador + "," + idPcr + ","
                                + raw_data_path + "','" + proc_data_path + "')\n";
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
