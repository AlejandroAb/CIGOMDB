/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    public MyDate(long milliDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date d = new Date(milliDate);
        String tmpDate = dateFormat.format(d);
        String tmp[] = tmpDate.split("[/ ]");
        if (tmp.length >= 3) {
            try {
                anio = Integer.parseInt(tmp[0]);
                mes = Integer.parseInt(tmp[1]);
                dia = Integer.parseInt(tmp[2]);
            } catch (NumberFormatException nfe) {
                System.err.println("Error parsing date: " + tmpDate);
                dia = 0;
                mes = 1;
                anio = 2;
            }
        } else {
            dia = 0;
            mes = 0;
            anio = 0;
        }
        if (tmp.length >= 4) {
            time = tmp[3];
        }
        fecha = tmpDate;
    }

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

    public boolean splitMyDate() {
        // String regDDMMYY = "^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[1,3-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$";
        // String regMMDDYY = "^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[1,3-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$";
        //String reg_YYYYMMDD = "^\\d\\d\\d\\d(\\/|-|\\.)(0?[1-9]|1[0-2])(\\/|-|\\.)(0?[1-9]|[12][0-9]|3[01])(\\s*.*)$";
        String reg_YYYYMMDD = "^\\d\\d\\d\\d(\\/|-|\\.)(0?[0-9]|1[0-2])(\\/|-|\\.)(0?[0-9]|[12][0-9]|3[01])(\\s*.*)$";
        String reg_DDMMYYYY = "^(0?[1-9]|[12][0-9]|3[01])(\\/|-|\\.)(0?[1-9]|1[0-2])(\\/|-|\\.)\\d\\d\\d\\d(\\s*.*)$";
        String reg_MMDDYYYY = "^(0?[1-9]|1[0-2])(\\/|-|\\.)(0?[1-9]|[12][0-9]|3[01])(\\/|-|\\.)\\d\\d\\d\\d(\\s*.*)$";
        if (fecha.matches(reg_DDMMYYYY)) {
            return splitDDMMYY();
        } else if (fecha.matches(reg_MMDDYYYY)) {
            return splitMMDDYY();
        } else if (fecha.matches(reg_YYYYMMDD)) {
            return splitYYYYMMDD();
        }
        return false;
    }

    public boolean parseTime(String time) {
        String tokens[] = time.split(":");
        int i = 0;
        String horas = "";
        String minutos = "";
        String segundos = "";
        for (String tok : tokens) {
            try {
                if (i == 0) {
                    horas = tok.trim();
                    if (horas.length() < 2) {
                        horas = "0" + horas;
                    }
                    Integer.parseInt(horas);
                } else if (i == 1) {
                    minutos = tok.trim();
                    if (minutos.length() < 2) {
                        minutos = "0" + minutos;
                    }
                    Integer.parseInt(minutos);
                } else if (i == 2) {
                    segundos = tok.trim();
                    if (segundos.length() < 2) {
                        segundos = "0" + segundos;
                    }
                    Integer.parseInt(segundos);
                }
            } catch (NumberFormatException nfe) {
                return false;
            }
            i++;
        }
        if (segundos.length() == 0) {
            segundos = "00";
        }
        if (minutos.length() == 0) {
            minutos = "00";
        }
        if (horas.length() == 0) {
            return false;
        }
        this.time = horas + ":" + minutos + ":" + segundos;
        return true;

    }

    public boolean splitDDMMYY() {

        if (fecha.length() >= 6) {
            try {
                StringTokenizer st = new StringTokenizer(fecha, "/.- T");
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

    public boolean splitYYYYMMDD() {

        if (fecha.length() >= 6) {
            try {
                StringTokenizer st = new StringTokenizer(fecha, "/.- T");
                anio = Integer.parseInt(st.nextToken());
                mes = Integer.parseInt(st.nextToken());
                dia = Integer.parseInt(st.nextToken());
                if (st.hasMoreTokens()) {//significa que trae la 
                    time = st.nextToken();
                }
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
                StringTokenizer st = new StringTokenizer(fecha, "/.- T");
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
                date = "" + anio + "-" + getMesPadded() + "-" + getDiaPadded();
            }
        } else {
            return "NULL";
        }
        return date;
    }

}
