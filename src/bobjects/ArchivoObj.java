/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bobjects;

import java.util.ArrayList;
import utils.MyDate;

/**
 *
 * @author Alejandro
 */
public class ArchivoObj {

    public static int TIPO_RAW = 1;
    public static int TIPO_PRE = 2;
    public static int TIPO_MTX = 3;
    public static int TIPO_KRN = 4;
    public static int TIPO_ASS = 5;
    private int idArchivo;
    private int tipoArchivo;
    private String nombre;
    private String extension;
    private String path;//sin el nombre, termina en / path + nombre = full_path
    private String checksum = "";
    private String description = "";
    //OLD ATTRIBS
    private int poor_q_secs = 0;
    private int num_secs = 0;
    private float seq_length = 0; //promedio
    private float gc_percent = 0;
    //END OLD ATTRIBS
    //NEW ONES
    private MyDate date;
    private String alcance = "";
    private String editor = "";
    private String derechos = "";
    private String origen = "";
    private String tipo = "";
    private String tags;
    ArrayList<Usuario> usuarios = new ArrayList<>();
    private long size;

    public MyDate getDate() {
        return date;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setDate(MyDate date) {
        this.date = date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getAlcance() {
        return alcance;
    }

    public void setAlcance(String alcance) {
        this.alcance = alcance;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getDerechos() {
        return derechos;
    }

    public void setDerechos(String derechos) {
        this.derechos = derechos;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public ArrayList<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(ArrayList<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public ArchivoObj(int idArchivo) {
        this.idArchivo = idArchivo;
    }

    public int getIdArchivo() {
        return idArchivo;
    }

    public void setIdArchivo(int idArchivo) {
        this.idArchivo = idArchivo;
    }

    public int getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(int tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPoor_q_secs() {
        return poor_q_secs;
    }

    public void setPoor_q_secs(int poor_q_secs) {
        this.poor_q_secs = poor_q_secs;
    }

    public int getNum_secs() {
        return num_secs;
    }

    public void setNum_secs(int num_secs) {
        this.num_secs = num_secs;
    }

    public float getSeq_length() {
        return seq_length;
    }

    public void setSeq_length(float seq_length) {
        this.seq_length = seq_length;
    }

    public float getGc_percent() {
        return gc_percent;
    }

    public void setGc_percent(float gc_percent) {
        this.gc_percent = gc_percent;
    }

    public void addUser(Usuario usuario) {
        this.usuarios.add(usuario);
    }

    public String toNewSQLString() {
        String query = "INSERT INTO archivo(idarchivo, idtipo_archivo, nombre, extension, path, checksum, "
                + "descripcion, fecha, alcance, editor, derechos, origen,tags, tipo, size) VALUES "
                + "(" + this.getIdArchivo() + "," + this.getTipoArchivo() + ", '" + this.getNombre() + "','" + this.getExtension()
                + "','" + this.getPath() + "','" + this.getChecksum() + "','" + this.getDescription() + "','" + this.getDate().toSQLString(true)
                + "','" + this.getAlcance() + "','" + this.getEditor() + "','" + this.getDerechos() + "','" + this.getOrigen() + "','" + this.getTags() + "','" + this.getTipo() + "'," + this.getSize() + ")";
        return query;
    }

    public ArrayList<String> archivoUsuariosToSQLString() {
        ArrayList<String> querys = new ArrayList<String>();
        for (Usuario u : usuarios) {
            querys.add("INSERT INTO usuario_archivo VALUES(" + u.getIdUsuario() + "," + this.getIdArchivo() + ",'" + u.getAcciones() + "','" + u.getComentarios() + "')");
        }
        return querys;
    }
    /**
     * Cambia con la inclusión de DublinCore
     * @deprecated use toNewSQLString()
     * @return 
     */
    public String toSQLString() {
        String query = "INSERT INTO archivo (idarchivo, idtipo_archivo, nombre, extension, path, checksum, descripcion, poor_q_secs, num_secs, seq_length, gc_percent) VALUES "
                + "(" + this.getIdArchivo() + "," + this.getTipoArchivo() + ", '" + this.getNombre() + "','" + this.getExtension()
                + "','" + this.getPath() + "','" + this.getChecksum() + "','" + this.getDescription() + "'," + this.getPoor_q_secs()
                + "," + this.getNum_secs() + "," + this.getSeq_length() + "," + this.getGc_percent() + ")";
        return query;
    }

}
