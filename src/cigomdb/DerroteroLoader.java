/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import bobjects.EstacionObj;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import database.Transacciones;
import utils.MyCoord;
import utils.MyDate;

/**
 *
 * @author Alejandro
 */
public class DerroteroLoader {

    private Transacciones transacciones;

    public DerroteroLoader(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    /**
     * Este metodo se encarga de parsear una tabla que contien el derrotero de
     * un crucero
     *
     * @param idCampana el id de la campaña a la cual pertenece este derrotero
     * @param fileIn Archivo con la tabla del derrotero. Se espera que el
     * archivo venga con un header el cual por lo menos tiene que tener estacion
     * latitud longitud fecha planeada, fecha ejecutada y comentarios. Cualquier
     * linea con # es considerada comentario.
     * @param sep
     * @return
     */
    public String parseMatrizDerrotero(int idCampana, String fileIn, String sep) {
        String log = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn)));
            String linea;
            int numLinea = 0;
            //defaults
            int idxEst = 1;
            int idxLat = 2;
            int idxLong = 3;
            int idxDateP = 10;
            int idxDateR = 11;
            int idxComm = 12;
            int idxTipo = 13;
            int estNum = 0;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().startsWith("#")) {
                    numLinea++;
                    if (numLinea == 1) {
                        StringTokenizer headerST = new StringTokenizer(linea, sep);
                        int toks = 0;
                        while (headerST.hasMoreTokens()) {
                            toks++;
                            String tok = headerST.nextToken().trim().toLowerCase();
                            if (tok.contains("est")) {//estacion
                                idxEst = toks;
                            } else if (tok.contains("lat")) {//latitud
                                idxLat = toks;
                            } else if (tok.contains("lon")) {//longitud
                                idxLong = toks;
                            } else if (tok.contains("plan")) {//planned date
                                idxDateP = toks;
                            } else if (tok.contains("ejec")) {//executed date
                                idxDateR = toks;
                            } else if (tok.contains("com")) {//comentarios
                                idxComm = toks;
                            } else if (tok.contains("tipo")) {//tipo de estacion
                                idxTipo = toks;
                            }
                        }
                    } else {
                        StringTokenizer st = new StringTokenizer(linea, sep);
                        int tok = 0;
                        String nombre = "", com = "";
                        int tipo = -1;
                        MyCoord lat = null, lon = null;
                        MyDate planeada = null;
                        MyDate ejecutada = null;
                        while (st.hasMoreTokens()) {
                            tok++;
                            if (tok == idxEst) {
                                nombre = st.nextToken();
                            } else if (tok == idxLat) {
                                lat = new MyCoord(st.nextToken());
                                lat.parseAnyCoordGMS();
                            } else if (tok == idxLong) {
                                lon = new MyCoord(st.nextToken());
                                lon.parseAnyCoordGMS();
                            } else if (tok == idxDateP) {
                                planeada = new MyDate(st.nextToken());
                            } else if (tok == idxDateR) {
                                ejecutada = new MyDate(st.nextToken());
                            } else if (tok == idxComm) {
                                com = st.nextToken();
                                if(com.toLowerCase().equals("na") || com.toLowerCase().equals("nd")){
                                    com = "";
                                }
                            } else if (tok == idxTipo) {
                                try {
                                    tipo = Integer.parseInt(st.nextToken());
                                } catch (NumberFormatException nfe) {
                                    tipo = 1;
                                }
                            } else {
                                st.nextToken();
                            }
                        }
                        EstacionObj estacion = new EstacionObj(nombre, transacciones);
                        estacion.setLatitud(lat);
                        estacion.setLongitud(lon);
                        estacion.setTipo_est(tipo);
                        int idEst = estacion.testExistance();
                        // int idEst = estacion.getIdEstacion();
                        System.out.println("Estacion: " + idEst);
                        estNum++;
                        String fPlaneada = "NULL";
                        String fEjecutada = "NULL";
                        if (planeada != null) {
                            planeada.splitDDMMYY();
                            fPlaneada = planeada.toSQLString(false);
                            if (!fPlaneada.equals("NULL")) {
                                fPlaneada = "'" + fPlaneada + "'";
                            }
                        }
                        if (ejecutada != null) {
                            ejecutada.splitDDMMYY();
                            fEjecutada = ejecutada.toSQLString(false);
                            if (!fEjecutada.equals("NULL")) {
                                fEjecutada = "'" + fEjecutada + "'";
                            }
                        }
                        if (transacciones.insertaDerrotero(idCampana, idEst, estacion.getNombre(), fPlaneada, fEjecutada, estNum, estNum, com) == -1) {
                            System.out.print("ERROR INSERTING: " + numLinea);
                        }
                    }

                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DerroteroLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(DerroteroLoader.class.getName()).log(Level.SEVERE, null, ioe);
        }

        return log;
    }
}
