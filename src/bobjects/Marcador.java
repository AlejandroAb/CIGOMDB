/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bobjects;

import java.util.ArrayList;

/**
 *
 * @author Alejandro
 */
public class Marcador {

    String idMarcador = "-1";
    String idMuestra = "-1";
    String idTipoMarcador = "-1";
    String idTipoSec = "-1";
    String idSecuenciador = "-1";
    String idPcr = "-1";
    String marc_name = "";
    String marc_desc = "";
    int seq_num_total = 0;
    String library_selection = "";
    String library_layout = "";
    String library_vector = "";
    String raw_data_path = "";
    String proc_data_path = "";
    String pre_process = "";
    String data_qc = "";
    int idStats = -1;
    boolean visible = true;
    String volumen = "";
    ArrayList<ArchivoObj> archivos = new ArrayList<>();
    public String getIdMarcador() {
        return idMarcador;
    }
    public void addArchivo(ArchivoObj archivo){
        archivos.add(archivo);
    }
    public ArrayList<ArchivoObj> getArchivos() {
        return archivos;
    }

    public void setArchivos(ArrayList<ArchivoObj> archivos) {
        this.archivos = archivos;
    }

    public void setIdMarcador(String idMarcador) {
        this.idMarcador = idMarcador;
    }

    public String getIdMuestra() {
        return idMuestra;
    }

    public void setIdMuestra(String idMuestra) {
        this.idMuestra = idMuestra;
    }

    public String getIdTipoMarcador() {
        return idTipoMarcador;
    }

    public void setIdTipoMarcador(String idTipoMarcador) {
        this.idTipoMarcador = idTipoMarcador;
    }

    public String getIdTipoSec() {
        return idTipoSec;
    }

    public void setIdTipoSec(String idTipoSec) {
        this.idTipoSec = idTipoSec;
    }

    public String getIdSecuenciador() {
        return idSecuenciador;
    }

    public void setIdSecuenciador(String idSecuenciador) {
        this.idSecuenciador = idSecuenciador;
    }

    public String getIdPcr() {
        return idPcr;
    }

    public void setIdPcr(String idPcr) {
        this.idPcr = idPcr;
    }

    public String getMarc_name() {
        return marc_name;
    }

    public void setMarc_name(String marc_name) {
        this.marc_name = marc_name;
    }

    public String getMarc_desc() {
        return marc_desc;
    }

    public void setMarc_desc(String marc_desc) {
        this.marc_desc = marc_desc;
    }

    public int getSeq_num_total() {
        return seq_num_total;
    }

    public void setSeq_num_total(int seq_num_total) {
        this.seq_num_total = seq_num_total;
    }

    public String getLibrary_selection() {
        return library_selection;
    }

    public void setLibrary_selection(String library_selection) {
        this.library_selection = library_selection;
    }

    public String getLibrary_layout() {
        return library_layout;
    }

    public void setLibrary_layout(String library_layout) {
        this.library_layout = library_layout;
    }

    public String getLibrary_vector() {
        return library_vector;
    }

    public void setLibrary_vector(String library_vector) {
        this.library_vector = library_vector;
    }

    public String getRaw_data_path() {
        return raw_data_path;
    }

    public void setRaw_data_path(String raw_data_path) {
        this.raw_data_path = raw_data_path;
    }

    public String getProc_data_path() {
        return proc_data_path;
    }

    public void setProc_data_path(String proc_data_path) {
        this.proc_data_path = proc_data_path;
    }

    public String getPre_process() {
        return pre_process;
    }

    public void setPre_process(String pre_process) {
        this.pre_process = pre_process;
    }

    public String getData_qc() {
        return data_qc;
    }

    public void setData_qc(String data_qc) {
        this.data_qc = data_qc;
    }

    public int getIdStats() {
        return idStats;
    }

    public void setIdStats(int idStats) {
        this.idStats = idStats;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getVolumen() {
        return volumen;
    }

    public void setVolumen(String volumen) {
        this.volumen = volumen;
    }

    public String toSQLString() {
        String sql = "INSERT INTO marcador (idmarcador, idMuestra, idtipo_marcador, "
                + "idtipo_secuenciacion, idSecuenciador, idpcr, marc_name, marc_desc, "
                + "seq_num_total, library_selection, library_layout, library_vector, "
                + "raw_data_path, pro_data_path, data_pre_process,data_qc, idstats,"
                + "visible, cantidad_dna) "
                + "VALUES(" + idMarcador + "," + idMuestra + "," + idTipoMarcador + "," + idTipoSec + "," + idSecuenciador + "," + idPcr
                + ", '" + marc_name + "','" + marc_desc + "'," + seq_num_total + ",'" + library_selection + "','" + library_layout + "','" + library_vector + "','"
                + raw_data_path + "','" + proc_data_path + "','" + pre_process + "','" + data_qc + "'," + idStats + "," + visible + ",'" + volumen + "')";
        return sql;

    }

}
