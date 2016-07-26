package cigomdb;

import bobjects.Instrumento;
import bobjects.Medicion;
import bobjects.Muestra;
import bobjects.Muestreo;
import bobjects.Usuario;
import bobjects.Variable;
import dao.MuestreoDAO;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.MyCoord;
import utils.MyDate;

/**
 * Esta clase esta pensada para realizar el procesamientode archivos de excel,
 * los cuales traen la información de las muestras tomadas en el crucero. Hay
 * que intentar establecer un formato unico para estos archivos y así mediante
 * esta clase poder introducir info desde la aplicación.
 *
 * @author Alejandro
 */
public class EventProcesor {

    private int nextIDMuestreo = -1;
    public Transacciones transacciones;

    public EventProcesor(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public int getNextIDMuestreo() {
        return nextIDMuestreo;
    }

    public void setNextIDMuestreo(int nextIDMuestreo) {
        this.nextIDMuestreo = nextIDMuestreo;
    }

    public Transacciones getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    /**
     *
     * @param inputFile
     * @param outputFile
     * @param toFile
     * @param fileSep
     * @param idCampana
     * @return
     */
    public String parseFileMMFI_Muestreo(String inputFile, String outputFile, boolean toFile, String fileSep, int idCampana) {
        String log = "";
        if (nextIDMuestreo == -1) {
            nextIDMuestreo = transacciones.getMaxIDMuestreo();
            if (nextIDMuestreo == -1) {
                return "ERROR No se puede determinar el siguiente ID de muestreo/lance";
            } else {
                nextIDMuestreo++;
            }
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            String linea;
            int numLinea = 0;
            //defaults
            int idxEst = -1/*EST%*/, idxEtiqueta = -1/*(LABEL% || ETIQ%)*/, idxLance = -1/*LAN% */,
                    idxFechaI = -1/*(FECHA I% || F%I%)*/, idxFechaF = -1/*(FECHA F% || F%F%)*/,
                    idxHoraI = -1/*(HORA I% || H%I%)*/, idxHoraF = -1/*(HORA F% || H%F%)*/,
                    idxMatriz = -1/*(MATRIZ || Tipo Muestra || PACKAGE)*/, idxLatI = -1,/*(LATITUD I% || LAT%I%)*/
                    idxLongI = -1/*(LONGITUD I% || LONG%I%)*/, idxLatF = -1/*(LATITUD F% || LAT%F%)*/,
                    idxLongF = -1/*(LONGITUD F% || LONG%F%)*/, idxProfundidad = -1/*(PROFUNDIDAD)*/,
                    idxMaxF = -1/* %MAX%F% */, idxMinO = -1 /*%MIN%O*/, idx1K = -1/* %1000% */, idxFondo = -1,
                    idxBioma = -1, idxEnvFeat = -1, idxEnvMat = -1, idxSampSize = -1/*(tama)*/, idxComentarios = -1,
                    idxProtocolo = -1, idxTemp = -1, idxSalinidad = -1, idxpH = -1, idxODisuelto = -1, idxFluor = -1;
            ArrayList<Integer> idxInstrumentos = new ArrayList<Integer>();
            ArrayList<Integer> idxUsuarios = new ArrayList<Integer>();
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().startsWith("#")) {
                    numLinea++;
                    if (numLinea == 1) {
                        StringTokenizer headerST = new StringTokenizer(linea, fileSep);
                        int toks = 0;
                        while (headerST.hasMoreTokens()) {
                            toks++;
                            String tok = headerST.nextToken().trim().toUpperCase();
                            if (tok.contains("EST")) {//estacion
                                idxEst = toks;
                            } else if (tok.contains("ETIQ")) {//ETIQUETA
                                idxEtiqueta = toks;
                            } else if (tok.contains("LANCE")) {//LANCE
                                idxLance = toks;
                            } else if (tok.contains("PROTOCOLO") || tok.contains("PROCESAMIENTO")) {//PROTocolo
                                idxProtocolo = toks;
                            } else if (tok.contains("COMENTARIO") || tok.contains("OBSERVACIONES")) {//COMENTARIOS || OBSERVACIONES
                                idxComentarios = toks;
                            } else if (tok.contains("BIOMA") || tok.contains("BIOME")) {//BIOMA
                                idxBioma = toks;
                            } else if (tok.contains("CARACTERÍSTICA") || tok.contains("CARACTERISTICA") || tok.contains("FEATURE")) {//ENV FEATURE
                                idxEnvFeat = toks;
                            } else if (tok.contains("MATERIAL")) {//ENV MATERIAL
                                idxEnvMat = toks;
                            } else if (tok.contains("TAMAÑO") || tok.contains("TAMANIO") || tok.contains("SIZE") || tok.contains("TAMA")) {//SAMPLE SIZE
                                idxSampSize = toks;
                            } else if (tok.equals("PROFUNDIDAD")) {//SOLO IGUAL A PROFUNDIDAD HAY VARIOS PROF
                                idxProfundidad = toks;
                            } else if (tok.contains("MAX. F") /*|| tok.contains("FLUORESC")*/) {//MAX. FLUORESC
                                idxMaxF = toks;
                            } else if (tok.contains("MIN. O") /*|| tok.contains("DISUELTO")*/) {//MIN. O2
                                idxMinO = toks;
                            } else if (tok.contains("1000")) {//1000 M
                                idx1K = toks;
                            } else if (tok.contains("FONDO")) {//FONDO
                                idxFondo = toks;
                            } else if (tok.contains("MATRIZ") || tok.contains("PACKAGE") || tok.contains("TIPO DE MUESTRA")) {//MATRIZ 
                                idxMatriz = toks;
                            } else if (tok.contains("FECHA I")) {//FECHA INICIAL
                                idxFechaI = toks;
                            } else if (tok.contains("FECHA F")) {//FECHA FINAL
                                idxFechaF = toks;
                            } else if (tok.contains("HORA I")) {//HORA INICIAL
                                idxHoraI = toks;
                            } else if (tok.contains("HORA F")) {//HORA FINAL
                                idxHoraF = toks;
                            } else if (tok.contains("LATITUD I") || tok.contains("LAT I")) {//LAT INICIAL
                                idxLatI = toks;
                            } else if (tok.contains("LONGITUD I") || tok.contains("LONG I")) {//LONG INICIAL
                                idxLongI = toks;
                            } else if (tok.contains("LATITUD F") || tok.contains("LAT F")) {//LAT FINAL
                                idxLatF = toks;
                            } else if (tok.contains("LONGITUD F") || tok.contains("LONG F")) {//LONG FINAL
                                idxLongF = toks;
                            } else if (tok.contains("INSTRUMENTO")) {//INSTRUMENTO
                                idxInstrumentos.add(toks);
                            } else if (tok.contains("USUARIO")) {//INSTRUMENTO
                                idxUsuarios.add(toks);
                            } else if (tok.contains("TEMPERATURA")) {//TEMPERATURA
                                idxLongF = toks;
                            } else if (tok.contains("SALINIDAD")) {//TEMPERATURA
                                idxLongF = toks;
                            } else if (tok.equals("PH")) {//SALINIDAD
                                idxLongF = toks;
                            } else if (tok.contains("DISUELTO")) {//OXIGENO DISUELTO
                                idxLongF = toks;
                            } else if (tok.contains("FLUORES")) {//TEMPERATURA
                                idxLongF = toks;
                            }
                        }
                    } else {
                        StringTokenizer st = new StringTokenizer(linea, fileSep);
                        int tok = 0;
                        MyCoord tmpCoord = null;
                        MyDate tmpDate = null;
                        String tmpString = null;
                        Muestreo muestreo = new Muestreo(nextIDMuestreo);
                        nextIDMuestreo++;
                        int idEstacion = -1;
                        boolean lineaOK = true;
                        while (st.hasMoreTokens()) {
                            tok++;
                            if (tok == idxEst) { //ESTACION EN DERROTERO
                                tmpString = st.nextToken();
                                idEstacion = transacciones.testEstacionByName(tmpString);
                                if (idEstacion == -1) {
                                    log += "ERROR EN linea: " + numLinea
                                            + "\nNo se puede determinar la estación con nombre: " + idEstacion
                                            + "\nDar de alta la estación para poder continuar";
                                    lineaOK = false;
                                    break;
                                }
                                int idDerrotero = transacciones.getIDDerrotero(idEstacion, idCampana);
                                if (idDerrotero == -1) {
                                    log += "ERROR EN linea: " + numLinea
                                            + "\nNo se puede determinar el derrotero para la estación: " + idEstacion + " - " + tmpString + " y id de campaña: " + idCampana
                                            + "\nDar de alta la ruta para poder continuar";
                                    lineaOK = false;
                                    break;
                                }
                                muestreo.setIdDerrotero(idDerrotero);
                            } else if (tok == idxEtiqueta) { //ETIQUETA O LABEL
                                tmpString = st.nextToken().trim();
                                //if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {                                    
                                muestreo.setEtiqueta(tmpString.replaceAll(" ", "_"));
                                // }                                
                            } else if (tok == idxLance) { //NUMERO DE LANCE
                                tmpString = st.nextToken().trim();
                                //if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {                                    
                                muestreo.setLance(tmpString);
                                // }                                
                            } else if (tok == idxMatriz) { //MATRIZ PACKAGE O TIPO DE MUESTRA
                                tmpString = st.nextToken().trim();
                                //if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {                                    
                                if (tmpString.toLowerCase().contains("agua")) {
                                    muestreo.setIdTipoMuestra(Muestreo.MATRIZ_AGUA);
                                    muestreo.setIdTipoMuestreo(Muestreo.M_AGUAS_PROFUNDAS);
                                } else {
                                    muestreo.setIdTipoMuestra(Muestreo.MATRIZ_SEDIMENTO);
                                    muestreo.setIdTipoMuestreo(Muestreo.M_SEDIMENTO);
                                }
                            } else if (tok == idxLatI) { //LATITTUD REAL O LATITUD INICIAL
                                tmpString = st.nextToken();
                                tmpCoord = new MyCoord(tmpString);
                                if (!tmpCoord.parseAnyCoordGMS()) {
                                    log += "ERROR EN linea: " + numLinea
                                            + "\nNo se puede formatear la coordenada LAT I: " + tmpString
                                            + "\nRevisar que el dato sea correcto y que esté en algún formato de los esperados.";
                                    lineaOK = false;
                                    break;
                                }
                                muestreo.setLatitud_r(tmpCoord);
                            } else if (tok == idxLongI) { //LONGITUD INICIAL O LONGITUD REAL
                                tmpString = st.nextToken();
                                tmpCoord = new MyCoord(tmpString);
                                if (!tmpCoord.parseAnyCoordGMS()) {
                                    log += "ERROR EN linea: " + numLinea
                                            + "\nNo se puede formatear la coordenada LONG I: " + tmpString
                                            + "\nRevisar que el dato sea correcto y que esté en algún formato de los esperados.";
                                    lineaOK = false;
                                    break;
                                }
                                muestreo.setLongitud_r(tmpCoord);
                            } else if (tok == idxLatF) { //LATITUD FINAL O LATITUD AJUSTADA
                                tmpString = st.nextToken();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    tmpCoord = new MyCoord(tmpString);
                                    if (!tmpCoord.parseAnyCoordGMS()) {
                                        log += "ERROR EN linea: " + numLinea
                                                + "\nNo se puede formatear la coordenada LAT F: " + tmpString
                                                + "\nRevisar que el dato sea correcto y que esté en algún formato de los esperados.";
                                        lineaOK = false;
                                        break;
                                    }
                                    muestreo.setLatitud_a(tmpCoord);
                                } else {
                                    tmpCoord = new MyCoord("0");
                                    muestreo.setLongitud_a(tmpCoord);
                                }
                            } else if (tok == idxLongF) { //LONGITUD FINAL O LONGITUD AJUSTADA
                                tmpString = st.nextToken();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    tmpCoord = new MyCoord(tmpString);
                                    if (!tmpCoord.parseAnyCoordGMS()) {
                                        log += "ERROR EN linea: " + numLinea
                                                + "\nNo se puede formatear la coordenada LONG F: " + tmpString
                                                + "\nRevisar que el dato sea correcto y que esté en algún formato de los esperados.";
                                        lineaOK = false;
                                        break;
                                    }
                                    muestreo.setLongitud_a(tmpCoord);
                                } else {
                                    tmpCoord = new MyCoord("0");
                                    muestreo.setLongitud_a(tmpCoord);
                                }
                            } else if (tok == idxFechaI) { //FECHA INICIAL
                                tmpString = st.nextToken();
                                tmpDate = new MyDate(tmpString);
                                if (!tmpDate.splitDDMMYY()) {
                                    log += "ERROR EN linea: " + numLinea
                                            + "\nNo se puede formatear la fecha inicial: " + tmpString
                                            + "\nRevisar que el dato sea correcto y que esté en algún formato de los esperados (dd/mm/yyyy).";
                                    lineaOK = false;
                                    break;
                                }
                                muestreo.setFechaInicial(tmpDate);
                            } else if (tok == idxHoraI) { //HORA INICIAL
                                tmpString = st.nextToken();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    tmpDate = muestreo.getFechaInicial();
                                    if (tmpDate == null) {
                                        log += "ERROR EN linea: " + numLinea
                                                + "\nNo se puede asignar la hora inicial sin una fecha inicial: " + tmpString
                                                + "\nRevisar que la fecha venga antes de la hora";
                                        lineaOK = false;
                                        break;
                                    }
                                    tmpDate.setTime(tmpString);
                                    muestreo.setFechaInicial(tmpDate);
                                }
                            } else if (tok == idxFechaF) { //FECHA FINAL                                
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    tmpDate = new MyDate(tmpString);
                                    if (!tmpDate.splitDDMMYY()) {
                                        log += "ERROR EN linea: " + numLinea
                                                + "\nNo se puede formatear la fecha final: " + tmpString
                                                + "\nRevisar que el dato sea correcto y que esté en algún formato de los esperados (dd/mm/yyyy).";
                                        lineaOK = false;
                                        break;
                                    }
                                    muestreo.setFechaFinal(tmpDate);
                                }
                            } else if (tok == idxHoraF) { //HORA FINAL
                                tmpString = st.nextToken();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    tmpDate = muestreo.getFechaFinal();
                                    if (tmpDate == null) {
                                        log += "ERROR EN linea: " + numLinea
                                                + "\nNo se puede asignar la hora final sin una fecha final: " + tmpString
                                                + "\nRevisar que la fecha venga antes de la hora";
                                        lineaOK = false;
                                        break;
                                    }
                                    tmpDate.setTime(tmpString);
                                    muestreo.setFechaFinal(tmpDate);
                                }
                            } else if (tok == idxMinO) { //PROFUNDIDAD MIN O
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    try {
                                        double profundidad = Double.parseDouble(tmpString);
                                        muestreo.setProfundidad(profundidad);
                                        muestreo.setTipo_prof(Muestreo.PROF_MIN_O);
                                    } catch (NumberFormatException nfe) {
                                        log += "ERROR EN linea: " + numLinea
                                                + "\nNo se puede formatear la profundidad: " + tmpString
                                                + "\nSe espera un numero con la representación de la profundidad en metros (Minimo Oxigeno)";
                                        lineaOK = false;
                                        break;
                                    }

                                }
                            } else if (tok == idxMaxF) { //PROFUNDIDAD MAX F
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    try {
                                        double profundidad = Double.parseDouble(tmpString);
                                        muestreo.setProfundidad(profundidad);
                                        muestreo.setTipo_prof(Muestreo.PROF_MAX_F);
                                    } catch (NumberFormatException nfe) {
                                        log += "ERROR EN linea: " + numLinea
                                                + "\nNo se puede formatear la profundidad: " + tmpString
                                                + "\nSe espera un numero con la representación de la profundidad en metros (Maximo Clorofila)";
                                        lineaOK = false;
                                        break;
                                    }
                                }
                            } else if (tok == idx1K) { //PROFUNDIDAD 1000M
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    try {
                                        double profundidad = Double.parseDouble(tmpString);
                                        muestreo.setProfundidad(profundidad);
                                        muestreo.setTipo_prof(Muestreo.PROF_MIL_M);
                                    } catch (NumberFormatException nfe) {
                                        log += "ERROR EN linea: " + numLinea
                                                + "\nNo se puede formatear la profundidad: " + tmpString
                                                + "\nSe espera un numero con la representación de la profundidad en metros (Prof 1000m)";
                                        lineaOK = false;
                                        break;
                                    }

                                }
                            } else if (tok == idxFondo) { //PROFUNDIDAD Fondo
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    try {
                                        double profundidad = Double.parseDouble(tmpString);
                                        muestreo.setProfundidad(profundidad);
                                        muestreo.setTipo_prof(Muestreo.PROF_FONDO);
                                    } catch (NumberFormatException nfe) {
                                        log += "ERROR EN linea: " + numLinea
                                                + "\nNo se puede formatear la profundidad: " + tmpString
                                                + "\nSe espera un numero con la representación de la profundidad en metros (Fondo)";
                                        lineaOK = false;
                                        break;
                                    }

                                }
                            } else if (tok == idxProfundidad && muestreo.getProfundidad() == -1) { //PROFUNDIDAD "NORMAL" PARA SED y verifica que no haya sido asignada aún
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    try {
                                        double profundidad = Double.parseDouble(tmpString);
                                        muestreo.setProfundidad(profundidad);
                                        muestreo.setTipo_prof(Muestreo.PROF_FONDO); //es sedimento así que es el fondo...
                                    } catch (NumberFormatException nfe) {
                                        log += "ERROR EN linea: " + numLinea
                                                + "\nNo se puede formatear la profundidad: " + tmpString
                                                + "\nSe espera un numero con la representación de la profundidad en metros (Fondo)";
                                        lineaOK = false;
                                        break;
                                    }

                                }
                            } else if (tok == idxBioma) { //Bioma
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    muestreo.setBioma(tmpString);

                                }
                            } else if (tok == idxEnvFeat) { //Environmental feature
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    muestreo.setEnv_feature(tmpString);

                                }
                            } else if (tok == idxEnvMat) { //Environmental material
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    muestreo.setEnv_material(tmpString);
                                }
                            } else if (tok == idxSampSize) { //Tamaño de la muestra || sample size
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    muestreo.setTamano(tmpString);
                                    /*  try {
                                     double size = Double.parseDouble(tmpString.replaceAll("[^\\d.]", ""));
                                     muestreo.setTamano(size);
                                     } catch (NumberFormatException nfe) {
                                     log += "ERROR EN linea: " + numLinea
                                     + "\nNo se puede formatear el tamaño de la muestra: " + tmpString
                                     + "\nSe espera un numero con la representación del tamaño de la muestra en litros";
                                     lineaOK = false;
                                     break;
                                     }*/
                                }
                            } else if (tok == idxComentarios) { //comentarios
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    //algo para escapar caracteres eseciales!!
                                    muestreo.setComentarios(tmpString);
                                }
                            } else if (tok == idxProtocolo) { //protocolo
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    //algo para escapar caracteres eseciales!!
                                    muestreo.setProtocolo(tmpString);
                                }
                            } else if (tok == idxTemp) { //temperatura
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    Medicion medicion = new Medicion(muestreo.getIdMuestreo(), Variable.TEMPERATURA);
                                    medicion.setMedicion_t1(tmpString);
                                    medicion.setOrden(1);
                                    medicion.setIdMetodoMedida(Medicion.MEDIDO_CTD);
                                    muestreo.addNewMedicion(medicion);
                                }
                            } else if (tok == idxpH) { //pH
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    Medicion medicion = new Medicion(muestreo.getIdMuestreo(), Variable.PH);
                                    medicion.setMedicion_t1(tmpString);
                                    medicion.setOrden(3);
                                    medicion.setIdMetodoMedida(Medicion.MEDIDO_CTD);
                                    muestreo.addNewMedicion(medicion);
                                }
                            } else if (tok == idxSalinidad) { //Salinidad
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    Medicion medicion = new Medicion(muestreo.getIdMuestreo(), Variable.SALINIDAD);
                                    medicion.setMedicion_t1(tmpString);
                                    medicion.setOrden(2);
                                    medicion.setIdMetodoMedida(Medicion.MEDIDO_CTD);
                                    muestreo.addNewMedicion(medicion);
                                }
                            } else if (tok == idxODisuelto) { //O2 DIsuelto
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    Medicion medicion = new Medicion(muestreo.getIdMuestreo(), Variable.O2_DISUELTO);
                                    medicion.setMedicion_t1(tmpString);
                                    medicion.setOrden(4);
                                    medicion.setIdMetodoMedida(Medicion.MEDIDO_CTD);
                                    muestreo.addNewMedicion(medicion);
                                }
                            } else if (tok == idxFluor) { //Fluorescencia
                                tmpString = st.nextToken().trim();
                                if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                    Medicion medicion = new Medicion(muestreo.getIdMuestreo(), Variable.FLUORESCENCIA);
                                    medicion.setMedicion_t1(tmpString);
                                    medicion.setOrden(5);
                                    medicion.setIdMetodoMedida(Medicion.MEDIDO_CTD);
                                    muestreo.addNewMedicion(medicion);
                                }
                            } else if (idxInstrumentos.contains(tok)) { //INSTRUMENTOS

                                tmpString = st.nextToken().trim();
                                /* Do something para obtener id instrumnto y cantidad
                                 if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                 //algo para escapar caracteres eseciales!!
                                 
                                 }
                                 */
                                if (muestreo.getIdTipoMuestra() == Muestreo.MATRIZ_AGUA) {
                                    Instrumento instrumento1 = new Instrumento(Instrumento.ROSSETTA);
                                    instrumento1.setCantidad("1");
                                    Instrumento instrumento2 = new Instrumento(Instrumento.NISKIN_20L);
                                    instrumento2.setCantidad("12");
                                    Instrumento instrumento3 = new Instrumento(Instrumento.CTD);
                                    instrumento3.setCantidad("1");
                                    muestreo.addNewInstrumento(instrumento1);
                                    muestreo.addNewInstrumento(instrumento2);
                                    muestreo.addNewInstrumento(instrumento3);
                                } else if (muestreo.getIdTipoMuestra() == Muestreo.MATRIZ_SEDIMENTO) {
                                    Instrumento instrumento1 = new Instrumento(Instrumento.NUC_KASTEN);
                                    instrumento1.setCantidad("1");
                                    Instrumento instrumento2 = new Instrumento(Instrumento.CTD);
                                    instrumento2.setCantidad("1");
                                    muestreo.addNewInstrumento(instrumento1);
                                    muestreo.addNewInstrumento(instrumento2);
                                }
                            } else if (idxUsuarios.contains(tok)) { //Usuarios
                                tmpString = st.nextToken().trim();
                                /* if (!tmpString.equals("-") && !tmpString.equals("NA") && !tmpString.equals("ND") && !tmpString.equals("x")) {
                                 Hacer algo para leer usuarios y asignar acciones
                                 }*/
                                Usuario usuario1 = new Usuario(3);//A estrada
                                usuario1.setAcciones("Coordinación de toma de muestras");
                                Usuario usuario2 = new Usuario(4);//D Ramirez
                                usuario2.setAcciones("Toma de muestras");
                                muestreo.addNewUsuario(usuario1);
                                muestreo.addNewUsuario(usuario2);
                            } else {
                                //algo no estamos leyendo pero no nos importa...por ahora
                                st.nextToken();
                            }
                        }
                        if (lineaOK) {
                            /**
                             * Hacer algo para que lea otro archivo o
                             * automatizar esto pero por protoclo son las mismas
                             * muestras por muestreo
                             */
                            if (muestreo.getIdTipoMuestra() == Muestreo.MATRIZ_AGUA) {
                                Muestra muestra = new Muestra(0, muestreo.getIdMuestreo());//0 ID de la muestra auto_increment
                                muestra.setProfundidad(muestreo.getProfundidad());
                                muestra.setEtiqueta(muestreo.getEtiqueta() + ".millipore");
                                muestra.setProcess("100 ml filtrados con filtros de membrana de "
                                        + "0.22 um GV en un sistema de filtración Millipore, "
                                        + "los filtros fueron depositados en tubos Falcon de 15 ml");
                                muestra.setContenedor("Nitrógeno líquido");//isol growth conditions
                                muestra.setSamp_size("100 ml");

                                Muestra muestra2 = new Muestra(0, muestreo.getIdMuestreo());//0 ID de la muestra auto_increment
                                muestra2.setProfundidad(muestreo.getProfundidad());
                                muestra2.setEtiqueta(muestreo.getEtiqueta() + ".falcon");
                                muestra2.setProcess("100 ml almacenados en dos tubos Falcon de 50ml cada uno ");
                                muestra2.setContenedor("Almacenado a 4 grados centigrados");//isol growth conditions
                                muestra2.setSamp_size("100 ml");

                                Muestra muestra3 = new Muestra(0, muestreo.getIdMuestreo());//0 ID de la muestra auto_increment
                                muestra3.setProfundidad(muestreo.getProfundidad());
                                muestra3.setEtiqueta(muestreo.getEtiqueta() + ".sterivex");
                                muestra3.setProcess("Agua filtrada con Sterivex");
                                muestra3.setSamp_size("Entre 2 y 3 lt.");
                                muestra3.setContenedor("Almacenado a 4 grados centigrados");//isol growth conditions
                                
                                muestreo.addNewMuestra(muestra);
                                muestreo.addNewMuestra(muestra2);
                                muestreo.addNewMuestra(muestra3);
                            } else if (muestreo.getIdTipoMuestra() == Muestreo.MATRIZ_SEDIMENTO) {
                                Muestra muestra = new Muestra(0, muestreo.getIdMuestreo());//0 ID de la muestra auto_increment
                                /**
                                 * En sedimentos el muestreo lleva la
                                 * profundidad a la cual se encuentra el
                                 * sedimento. Sin embargo la muestra debe
                                 * indicar la profundidad del "tope" del
                                 * sedimento hacia abajo. Como los tubos son de
                                 * 10cm aprox todos se anotan con esa
                                 * profundidad
                                 */
                                muestra.setProfundidad(0.1);
                                muestra.setEtiqueta(muestreo.getEtiqueta() + ".nucleos");
                                muestra.setProcess("Una vez puesto en cubierta el sedimento, "
                                        + "se tomaron 4 subnúcleos en jeringas de 60 cc, empaquetadas en paper egapack");
                                muestra.setContenedor("Nitrógeno líquido");//isol growth conditions
                                muestra.setSamp_size("73.51 cc");
                                muestreo.addNewMuestra(muestra);
                                if (muestreo.getComentarios().contains("Se tomaron muestras")) {//Se tomaron muestras anaeróbicas.
                                    Muestra muestra2 = new Muestra(0, muestreo.getIdMuestreo());//0 ID de la muestra auto_increment
                                    muestra2.setProfundidad(0.05);
                                    muestra2.setEtiqueta(muestreo.getEtiqueta() + ".anaerobio");
                                    muestra2.setProcess("Una vez puesto en cubierta el sedimento, "
                                            + "se tomó 1 subnúcleo en jeringas de 5 cc. "
                                            + "Se inoculó el sedimento en tubos anaerobios "
                                            + "en  medios de cultivo y almacenados en obscuridad");
                                    muestra2.setContenedor("Tubo anaerobio inoculado con sedimento y almacenado en obscuridad");//isol growth conditions
                                    muestra2.setSamp_size("5 cc");
                                    muestreo.addNewMuestra(muestra2);
                                }
                                /**
                                 * !!!!!!!!!!!!!! Importante!!!!!!!!!!!!!!!!!!!!
                                 * el tamaño de muestra se calcula mas arriba,
                                 * pero para sedimentos hay probleas (ver
                                 * documentacion del proyecto) razón por la cual
                                 * aca lo hardcodeamos pero en un futuro dejar
                                 * el bueno, es decir el que viene en el archivo
                                 * original.
                                 */
                                muestreo.setTamano(linea);
                            }
                            MuestreoDAO muestreoDAO = new MuestreoDAO(transacciones);
                            muestreoDAO.almacenaMuestreo(muestreo, toFile, outputFile, toFile, true, true, true);
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EventProcesor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(EventProcesor.class.getName()).log(Level.SEVERE, null, ioe);
        }
        return log;
    }
}
