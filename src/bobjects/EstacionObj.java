/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bobjects;

import database.Transacciones;
import utils.MyCoord;
/**
 *
 * @author Alejandro
 */
public class EstacionObj {
    private int idEstacion;
    private String nombre="";
    private MyCoord latitud;
    private MyCoord longitud;
    private String comentarios="";
    private int tipo_est;
    private Transacciones transacciones;
    private String region;
    
    public EstacionObj(int idEstacion) {
        this.idEstacion = idEstacion;
    }

    public int getTipo_est() {
        return tipo_est;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setTipo_est(int tipo_est) {
        this.tipo_est = tipo_est;
    }

    public EstacionObj(String nombre, Transacciones transacciones) {
        this.nombre = nombre;
        this.transacciones = transacciones;
       
    }
    public int testExistance(){
        int id = transacciones.testEstacionByName(this);
        this.idEstacion = id;
        return id;
        
    }

    public int getIdEstacion() {
        return idEstacion;
    }

    public void setIdEstacion(int idEstacion) {
        this.idEstacion = idEstacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public MyCoord getLatitud() {
        return latitud;
    }

    public void setLatitud(MyCoord latitud) {
        this.latitud = latitud;
    }

    public MyCoord getLongitud() {
        return longitud;
    }

    public void setLongitud(MyCoord longitud) {
        this.longitud = longitud;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public Transacciones getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(Transacciones transacciones) {
        this.transacciones = transacciones;
    }
    
}
