/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import bobjects.Instrumento;
import bobjects.Medicion;
import bobjects.Muestra;
import bobjects.Muestreo;
import bobjects.Usuario;
import database.Transacciones;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alejandro
 */
public class MuestraDAO {

    private Transacciones transacciones;

    public MuestraDAO(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public String almacenaMuestra(Muestra muestra, boolean toFile, String outFile, boolean append, boolean addInstrumentos, boolean addUsuarios) {
        //String query = "INSERT INTO Muestreo (`idMuestreo`, `idCE`, `idTipoMuestreo`, 
        //`idTipoMuestra`, `etiqueta`, `fecha_i`, `fecha_f`, `latitud_r`, `longitud_r`, 
        //`protocolo`, `comentarios`, `latitud_a`, `longitud_a`, `lance`, `bioma`, `env_feature`, 
        //`env_material`, `tamano`, `profundidad`, `tipo_profundidad`) VALUES (NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);"
        String log = "";
        String query = muestra.toSQLString();
        FileWriter writer = null;
        if (toFile) {
            try {
                writer = new FileWriter(outFile, append);
            } catch (IOException ex) {
                Logger.getLogger(MuestreoDAO.class.getName()).log(Level.SEVERE, null, ex);
                log += "Error accesando archivo: " + outFile + "\n";
            }
        }
        if (!toFile) {
            if (!transacciones.insertaQuery(query)) {
                log += "Error insertando muestreo: " + muestra.getIdMuestra() + " - " + muestra.getEtiqueta() + " - " + query + "\n";
            }
        } else {
            try {
                writer.write(query + ";\n");
            } catch (IOException ex) {
                log = "Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n";
            }
        }
        //MEDICIONES
        for (Medicion medicion : muestra.getMediciones()) {
            String queryMedicion = "INSERT INTO muestra_valor(idMuestra, idVariable,"
                    + "orden, idMetodo, medicion_t1, comentarios) VALUES ("
                    + muestra.getIdMuestra() + "," + medicion.getIdVariable() + ","
                    + medicion.getOrden() + "," + medicion.getIdMetodoMedida() + ", '" + medicion.getMedicion_t1() + "','" + medicion.getComentarios() + "')";
            if (!toFile) {
                if (!transacciones.insertaQuery(queryMedicion)) {
                    log += "Error insertando relación muestra-valor: "
                            + muestra.getIdMuestra() + "(idmuestra) - " + medicion.getIdVariable() + "(idVariable) - " + query + "\n";
                }
            } else {
                try {
                    writer.write(queryMedicion + ";\n");
                } catch (IOException ex) {
                    log = "Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n";
                }
            }
        }

        if (addUsuarios) {
            for (Usuario usuario : muestra.getUsuarios()) {
                String queryUsuario = "INSERT INTO muestra_usuario(idMuestra, "
                        + "idUsuario, acciones) VALUES ("
                        + muestra.getIdMuestra() + "," + usuario.getIdUsuario() + ",'"
                        + usuario.getAcciones() + "')";
                if (!toFile) {
                    if (!transacciones.insertaQuery(queryUsuario)) {
                        log += "Error insertando relación muestra-usuarios: "
                                + muestra.getIdMuestra() + "(idmuestra) - " + usuario.getIdUsuario() + "(idUser) - " + query + "\n";
                    }
                } else {
                    try {
                        writer.write(queryUsuario + ";\n");
                    } catch (IOException ex) {
                        log = "Error I/O escribiendo archivo: " + outFile + "\n" + query + "\n";
                    }
                }
            }
        }

        if (toFile) {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(MuestreoDAO.class.getName()).log(Level.SEVERE, null, ex);
                log += "Error cerrando archivo: " + outFile + "\n";
            }
        }
        return log;
    }

}
