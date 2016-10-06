/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bobjects;

/**
 *
 * @author Alejandro
 */
public class ArchivoObj {
   public static int TIPO_RAW = 1;
   public static int TIPO_PRE = 2;
   public static int TIPO_MTX = 3;
   private int idArchivo;
   private int tipoArchivo;
   private String nombre;
   private String extension;
   private String path;//sin el nombre, termina en / path + nombre = full_path
   private String checksum = "";
   private String description=""; 
   private int poor_q_secs = 0;
   private int num_secs = 0;
   private float seq_length = 0; //promedio
   private float gc_percent = 0;   

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
   
  }
