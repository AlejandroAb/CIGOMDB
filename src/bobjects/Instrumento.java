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
public class Instrumento {
    static int INST_MEDICION = 1;
    static int INST_MUESTREO = 2;
    //ids instrumentos actuales
    public static int CTD = 1;
    public static int NISKIN_20L = 2;
    public static int ROSSETTA = 3;
    public static int NUC_KASTEN = 4;    
    private int idInsrumento=-1;
    private int idTipoInstrumento=-1;
    private String nombre="";
    private String marca="";
    private String modelo="";
    private String descripcion="";
    private String cantidad=""; //wild key para muestreo_instrumento
    private String comentarios="";//wild key para muestreo_instrumento
    public Instrumento() {
    }

    public Instrumento(int idInsrumento) {
        this.idInsrumento = idInsrumento;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public int getIdInsrumento() {
        return idInsrumento;
    }

    public void setIdInsrumento(int idInsrumento) {
        this.idInsrumento = idInsrumento;
    }

    public int getIdTipoInstrumento() {
        return idTipoInstrumento;
    }

    public void setIdTipoInstrumento(int idTipoInstrumento) {
        this.idTipoInstrumento = idTipoInstrumento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }
    
}
