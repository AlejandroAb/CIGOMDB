/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

/**
 *
 * @author Alejandro
 */
//import database.Conexion;
//import java.sql.SQLException;
import bobjects.EstacionObj;
import bobjects.Muestreo;
import java.util.ArrayList;

/**
 *
 * @author Alejandro
 */
public class Transacciones {

    Conexion conexion;
    boolean estamosConectados = true;
    String tipoConexion = "";
    private String database;
    private String user;
    private String ip;
    private String password;
    private String query;
    private boolean debug = false;

    public Conexion getConexion() {
        return conexion;
    }

    public Transacciones() {
        conecta(true);
    }

    public Transacciones(boolean local) {
        conecta(local);
    }

    public Transacciones(String database, String user, String ip, String password) {
        this.database = database;
        this.user = user;
        this.ip = ip;
        this.password = password;
        conecta(true);
    }

    public void desconecta() {
        conexion.shutDown();
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getIp() {
        return ip;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void conecta(boolean conex) {
        if (conex) {
            // ArchivoIP aip = new ArchivoIP();
            //String[]config =  aip.obtieneIP();
            // conexion = new Conexion(config[1], config[0]);
            conexion = new Conexion(database, ip, user, password);
            //System.out.println(config[1] + "  " + config[0]);
            //  JOptionPane.showMessageDialog(null, config[1],config[0],JOptionPane.INFORMATION_MESSAGE);
            estamosConectados = conexion.seConecto;
            tipoConexion = "remota";
        } else {
            //conexion = new Conexion("mantenimiento", "localhost");
            // conexion = new Conexion("bio", "localhost", "root", "AMORPHIS");
            estamosConectados = conexion.seConecto;
            tipoConexion = "local";
        }
    }

    /**
     * Obtiene cual es el max id de muestro para poder asignar nuevos ya que no
     * esta declarado como auto_increment
     *
     * @return
     */
    public int getMaxIDMuestreo() {
        String query = "SELECT MAX(idMuestreo) FROM muestreo";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        int id = -1;
        if (dbResult == null || dbResult.isEmpty()) {
            id = -1;
        } else {
            try {
                id = Integer.parseInt((String) dbResult.get(0).get(0));
            } catch (NumberFormatException nfe) {
                id = -1;
            }
        }
        return ++id;

    }

    /**
     * Obtiene cual es el max id de muestra para poder asignar nuevos.
     *
     * @return
     */
    public int getMaxIDMuestra() {
        String query = "SELECT MAX(idMuestra) FROM muestra";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        int id = -1;
        if (dbResult == null || dbResult.isEmpty()) {
            id = -1;
        } else {
            try {
                id = Integer.parseInt((String) dbResult.get(0).get(0));
            } catch (NumberFormatException nfe) {
                id = -1;
            }
        }
        return ++id;

    }

    /**
     * Obtiene cual es el max id de archivo para poder asignar nuevos.
     *
     * @return
     */
    public int getMaxIDArchivo() {
        String query = "SELECT MAX(idarchivo) FROM archivo";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        int id = -1;
        if (dbResult == null || dbResult.isEmpty()) {
            id = 0;
        } else {
            try {
                id = Integer.parseInt((String) dbResult.get(0).get(0));
            } catch (NumberFormatException nfe) {
                id = 0;
            }
        }
        return ++id;

    }

    /**
     * Recibe un objeto de tipo estacion y en base a su nombe ve si este existe
     * si no existe, la estacion es creada en la BD
     *
     * @param est
     * @return
     */
    public int testEstacionByName(EstacionObj est) {
        String query = "SELECT idEstacion from estacion WHERE estacion_nombre = '" + est.getNombre() + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        int id = -1;
        if (dbResult == null || dbResult.isEmpty()) {
            query = "INSERT INTO estacion(idEstacion, estacion_nombre, id_tipo_estacion, longitud, latitud, comentarios) "
                    + "VALUES(0,'" + est.getNombre() + "'," + est.getTipo_est() + "," + est.getLongitud().getCoordenadas()
                    + "," + est.getLatitud().getCoordenadas() + ",'" + est.getComentarios() + "')";
            id = conexion.queryUpdateWithKey(query);
            query = "INSERT INTO estacion_tipo_estacion VALUES(" + id + "," + est.getTipo_est() + ")";
            conexion.queryUpdate(query);
        } else {
            try {
                id = Integer.parseInt((String) dbResult.get(0).get(0));
            } catch (NumberFormatException nfe) {
                id = -1;
            }
        }
        return id;
    }

    /**
     * Recibe un objeto de tipo estacion y en base a su nombe ve si este existe
     * A diferencia del otro testEstacionByName pero con param Estacion, este
     * solo verifica y regresa el ID, no inserta nada a la BD
     *
     * @param est
     * @return
     */
    public int testEstacionByName(String est) {
        String query = "SELECT idEstacion from estacion WHERE estacion_nombre = '" + est + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        int id = -1;
        if (dbResult == null || dbResult.isEmpty()) {
            id = -1;
        } else {
            try {
                id = Integer.parseInt((String) dbResult.get(0).get(0));
            } catch (NumberFormatException nfe) {
                id = -1;
            }
        }
        return id;

    }

    /**
     * Esste metodo se encarga de obtener el ID del derrotero es decir la
     * convinación estacion y campaña. Es usado durante la carga de los
     * muestreos, dado que este es el ID que se relaciona con el muestreo. El
     * único problema es que si se visita mas de una vez la misma estación en
     * una sola campaña este método tiende aa fallar por lo que hay que hacer
     * uso de la fecha o algún otro campo para realizar la correcta validación.
     *
     * @param idEst id de la estación
     * @param idCampana id de la campaña.
     * @return
     */
    public int getIDDerrotero(int idEst, int idCampana) {
        String query = "SELECT idDerrotero from derrotero "
                + "WHERE idEstacion = " + idEst + " AND idCampana = " + idCampana;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        int id = -1;
        if (dbResult == null || dbResult.isEmpty()) {
            id = -1;
        } else {
            try {
                id = Integer.parseInt((String) dbResult.get(0).get(0));
            } catch (NumberFormatException nfe) {
                id = -1;
            }
        }
        return id;

    }

    /**
     * Busca el id de una secuencia dado su raw id original
     * @param raw_seq_id
     * @return 
     */
    public String getSecMarcadorByRawID(String raw_seq_id) {
        String query = "SELECT idseq_marcador FROM seq_marcador WHERE raw_seq_id ='" + raw_seq_id + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();        
        if (dbResult == null || dbResult.isEmpty()) {
            return "";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }
    /**
     * Trea el nccbi tax id de un nombre de nodo
     * @param node_name
     * @return 
     */
 public String getNCBITaxID(String node_name) {
        String query = "SELECT tax_id FROM ncbi_node WHERE name ='" + node_name + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        int id = -1;
        if (dbResult == null || dbResult.isEmpty()) {
            return "";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }
    /**
     * Crea la relación marcador_archivo
     *
     * @param idMarcador
     * @param idArchivo
     * @return
     */
    public boolean insertaArchivoMarcador(String idMarcador, int idArchivo) {
        String query = "INSERT INTO marcador_archivo VALUES(" + idMarcador + "," + idArchivo + ")";
        return conexion.queryUpdate(query);
    }

    
    /**
     * Este método inserta en la entidad seq_marcador_classif. Es la relación
     * que hay entre la asignacion taxonómica a una secuencia en particular,
     * mediante un análisis (y sus parámetros) en específico
     *
     * @param taxID
     * @param idseq_marcador
     * @param idAnalisis para este hay variables pre establecidos 1 para metaxa
     * @param identity
     * @param eval
     * @param score
     * @param comments
     * @return
     */
    public boolean insertMarcadorClassification(String taxID, String idseq_marcador, int idAnalisis, String identity, String eval, String score, String length, String comments) {
        String query = "INSERT INTO seq_marcador_classif VALUES(" + taxID + ",'" + idseq_marcador + "', "
                + idAnalisis + "," + identity + "," + eval + "," + score + "," + length +",'" + comments + "')";
        return conexion.queryUpdate(query);
    }

    public int insertaDerrotero(int idCampana, int idEstacion, String nombre, String fPlaneada, String fEjecutada, int numEstP, int numEstE, String comentarios) {
        String query = "INSERT INTO derrotero "
                + "VALUES(0," + idCampana + "," + idEstacion + ",'" + nombre + "'," + fPlaneada + "," + fEjecutada
                + "," + numEstP + "," + numEstE + ",'" + comentarios + "')";
        return conexion.queryUpdateWithKey(query);
    }

    public int insertaMarcadorWithKey(String idMuestra, String idTipoMarcador, String idTipoSecuenciacion, String idSecuenciador, String idPcr, String raw_data_path, String pro_data_path) {
        String query = "INSERT INTO marcador "
                + "VALUES(0," + idMuestra + "," + idTipoMarcador + "," + idTipoSecuenciacion + "," + idSecuenciador + "," + idPcr
                + ",'" + raw_data_path + "','" + pro_data_path + "')";
        return conexion.queryUpdateWithKey(query);
    }

    public boolean insertaMarcador(String mark_id, String idMuestra, String idTipoMarcador, String idTipoSecuenciacion, String idSecuenciador, String idPcr, String marc_name, String marc_desc, String lib_sel, String lib_lay, String raw_data_path, String pro_data_path, String data_pre_process, String data_qc) {
        String query = "INSERT INTO marcador "
                + "VALUES(" + mark_id + "," + idMuestra + "," + idTipoMarcador + "," + idTipoSecuenciacion + "," + idSecuenciador + "," + idPcr
                + ", '" + marc_name + "','" + marc_desc + "',0,'" + lib_sel + "','" + lib_lay + "','"
                + raw_data_path + "','" + pro_data_path + "','" + data_pre_process + "','" + data_qc + "')";
        return conexion.queryUpdate(query);
    }

    public boolean updateSeqNumMarcador(String idMarcador, int seq_num) {
        String query = "UPDATE marcador set seq_num_total = " + seq_num + " WHERE idmarcador = " + idMarcador;
        return conexion.queryUpdate(query);
    }

    public boolean insertaSeqMarcador(String idSeq, String idMarcador, String raw_id, String seq) {
        String query = "INSERT INTO seq_marcador VALUES('"
                + idSeq + "'," + idMarcador + ",'" + raw_id + "','" + seq + "'," + seq.length() + ")";
        return conexion.queryUpdate(query);
    }

    public boolean testConnection() {
        String query = "select 1";
        conexion.executeStatement(query);
        //Vector paraRegresar = conexion.getFilas();
        ArrayList<ArrayList> dbResult = conexion.getTabla();

        //if (paraRegresar.size() > 0) {
        if (dbResult != null && dbResult.size() > 0) {
            try {
                // return ( (Vector) paraRegresar.elementAt(0)).elementAt(
                //    0).toString();
                return true;
            } catch (NullPointerException npe) {
                return false;
            }
        } else {
            return false;
        }

    }

    public boolean updateHierarchyNCBINode(String taxid, String hierarchy) {
        String query = "UPDATE NCBI_NODE SET hierarchy = '" + hierarchy + "' WHERE tax_id =" + taxid;
        if (conexion.queryUpdate(query)) {
            return true;
        } else {
            System.out.println(conexion.getLog());
            return false;
        }
    }

    public boolean insertaQuery(String query) {
        if (debug) {
            System.out.println(query);
        }
        if (conexion.queryUpdate(query)) {
            return true;
        } else {
            System.out.println(conexion.getLog());
            return false;
        }

    }

    public boolean insertSwissProt(String uniprotID, String uniprotACC, String taxID, String uniprotName, String sequence, int seqLength, String clusterId, String clusterName, String clusterTax) {
        String query = "INSERT INTO Swiss_prot (uniprot_id, uniprot_acc, ncbi_tax_id, "
                + "prot_name, prot_seq, prot_length, cluster_id, cluster_name, cluster_ncbi_tax) VALUES ('"
                + uniprotID + "', '"
                + uniprotACC + "', '"
                + taxID + "', '"
                + uniprotName + "', '"
                + sequence + "', "
                + seqLength + ", '"
                + clusterId + "', '"
                + clusterName + "', '"
                + clusterTax + "');\n";
        if (debug) {
            System.out.println(query);
        }
        return conexion.queryUpdate(query);
    }

    public boolean writeFastaFileByOrg(String orgID, String seqType, String extra, String fileName) {
        String query = " SELECT DISTINCT(CONCAT('>',seq_gen_id,char(10),seq_seq)) "
                + "FROM gen_seq "
                + "WHERE seq_org = '" + orgID + "' "
                + "AND seq_type = '" + seqType + "' "
                + " " + extra
                + " INTO OUTFILE '" + fileName + "'"
                + "FIELDS ESCAPED BY ''";

        //Vector paraRegresar = conexion.getFilas();
        //return conexion.queryUpdate(query);
        conexion.executeStatementToFile(query);
        return true;
    }

}
