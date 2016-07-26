/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 *
 * @author Alejandro
 */
public class MyDate {

    int dia;
    int mes;
    int anio;
    String fecha;
    String time = "00:00:00";

    public MyDate(String fecha) {
        this.fecha = fecha;
    }

    public int getDia() {
        return dia;
    }

    public String getDiaPadded() {
        if (("" + dia).length() == 1) {
            return "0" + dia;
        } else {
            return "" + dia;
        }

    }

    public String getMesPadded() {
        if (("" + mes).length() == 1) {
            return "0" + mes;
        } else {
            return "" + mes;
        }

    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public boolean splitDDMMYY() {

        if (fecha.length() >= 6) {
            try {
                StringTokenizer st = new StringTokenizer(fecha, "/");
                dia = Integer.parseInt(st.nextToken());
                mes = Integer.parseInt(st.nextToken());
                anio = Integer.parseInt(st.nextToken());
                return true;
            } catch (NumberFormatException nfe) {
                System.out.println("Error NFE splitDDMMYY(): " + fecha);
                return false;

            } catch (NoSuchElementException nsee) {
                System.out.println("Error nsee splitDDMMYY(): " + fecha);
                return false;
            }

        } else {
            return false;
        }

    }

    public boolean splitMMDDYY() {

        if (fecha.length() >= 6) {
            try {
                StringTokenizer st = new StringTokenizer(fecha, "/");
                mes = Integer.parseInt(st.nextToken());
                dia = Integer.parseInt(st.nextToken());
                anio = Integer.parseInt(st.nextToken());
                return true;
            } catch (NumberFormatException nfe) {
                System.out.println("Error NFE splitDDMMYY(): " + fecha);
                return false;

            } catch (NoSuchElementException nsee) {
                System.out.println("Error nsee splitDDMMYY(): " + fecha);
                return false;
            }

        } else {
            return false;
        }

    }

    public String toSQLString(boolean withTime) {
        String date = "";
        if (fecha.length() > 6) {
            if (withTime) {
                date = "" + anio + "-" + getMesPadded() + "-" + getDiaPadded() + " " + time;
            } else {
                date = "" + anio + "-" + getMesPadded() + "-" + getDiaPadded() + " " + time;
            }
        } else {
            return "NULL";
        }
        return date;
    }

}
