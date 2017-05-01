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
    String metaxaFName = "";
    String extendedFName = "";
    String nc1FName = "";
    String nc2FName = "";
    String comentarios = "";

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public String getIdMarcador() {
        return idMarcador;
    }

    public String getMetaxaFName() {
        return metaxaFName;
    }

    public void setMetaxaFName(String metaxaFName) {
        this.metaxaFName = metaxaFName;
    }

    public String getExtendedFName() {
        return extendedFName;
    }

    public void setExtendedFName(String extendedFName) {
        this.extendedFName = extendedFName;
    }

    public String getNc1FName() {
        return nc1FName;
    }

    public void setNc1FName(String nc1FName) {
        this.nc1FName = nc1FName;
    }

    public String getNc2FName() {
        return nc2FName;
    }

    public void setNc2FName(String nc2FName) {
        this.nc2FName = nc2FName;
    }

    public void addArchivo(ArchivoObj archivo) {
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

    /**
     * Se encarga de formatear el nombre del marcador, para que en base a este
     * genere un patron de etiqueta que es usado para asignar el id de
     * secuencias correspondiente a este marcador. Los nombres del marcador son
     * generalmente AMP-MAX-MMF1-A1.1, AMP-SED-SOG02-S02.1 para los cuales see
     * genera el siguiente patron AXMMF1A1.1.# y ASSOG02S02.1.# respectivamente
     *
     * @return
     */
    public String getIdSeqFormat() {
        String marcParts[] = getMarc_name().split("[-\\.]");
        String idSec = "";
        if (marcParts.length < 5) {
            System.err.println("Error de convención en el nombre del marcador!");
            idSec = getMarc_name();
        } else {
            String type = "";
            if (marcParts[1].equals("MAX")) {
                type = "X";
            } else if (marcParts[1].equals("MIN")) {
                type = "M";
            } else if (marcParts[1].equals("SED")) {
                type = "S";
            } else if (marcParts[1].equals("FON")) {
                type = "F";
            } else if (marcParts[1].equals("MIL")) {
                type = "K";
            } else {
                type = marcParts[1].substring(0, 1);
            }
            idSec = marcParts[0].substring(0, 1) + type + marcParts[2] + marcParts[3] + "." + marcParts[4] + ".";
        }
        return idSec;
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
                + "visible, cantidad_dna,comentarios) "
                + "VALUES(" + idMarcador + "," + idMuestra + "," + idTipoMarcador + "," + idTipoSec + "," + idSecuenciador + "," + idPcr
                + ", '" + marc_name + "','" + marc_desc + "'," + seq_num_total + ",'" + library_selection + "','" + library_layout + "','" + library_vector + "','"
                + raw_data_path + "','" + proc_data_path + "','" + pre_process + "','" + data_qc + "'," + idStats + "," + visible + ",'" + volumen + "','" + comentarios + "')";
        return sql;

    }

}
