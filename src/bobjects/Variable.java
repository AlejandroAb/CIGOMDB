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
public class Variable {
    public static int TEMPERATURA=1;
    public static int SALINIDAD=6;
    public static int PH=2;
    public static int O2_DISUELTO=3;
    public static int FLUORESCENCIA=7;
    private int idVariable;
    private int idTipoVariable;
    private int idTipoDato;
    private String key = "";
    private ArrayList<String> dbx_ref;//antes id_wod //en BD solo como id_wod pero mejor almacenar todos las dbx_refs
    private String nombre_var;
    private String unidades;
    private String comentarios;

    public Variable(int idVariable) {
        this.idVariable = idVariable;
    }

    public int getIdVariable() {
        return idVariable;
    }

    public void setIdVariable(int idVariable) {
        this.idVariable = idVariable;
    }

    public int getIdTipoVariable() {
        return idTipoVariable;
    }

    public void setIdTipoVariable(int idTipoVariable) {
        this.idTipoVariable = idTipoVariable;
    }

    public int getIdTipoDato() {
        return idTipoDato;
    }

    public void setIdTipoDato(int idTipoDato) {
        this.idTipoDato = idTipoDato;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ArrayList<String> getDbx_ref() {
        return dbx_ref;
    }

    public void setDbx_ref(ArrayList<String> dbx_ref) {
        this.dbx_ref = dbx_ref;
    }

    public String getNombre_var() {
        return nombre_var;
    }

    public void setNombre_var(String nombre_var) {
        this.nombre_var = nombre_var;
    }

    public String getUnidades() {
        return unidades;
    }

    public void setUnidades(String unidades) {
        this.unidades = unidades;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
    
}
