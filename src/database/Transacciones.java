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

    public ArrayList<ArrayList> getOrgsByPhylo(String phyloCond) {
        String query = "SELECT org_id FROM orgs WHERE org_phylo " + phyloCond;
        //conexion.executePreparedS(query);
        conexion.executeStatement(query);
        //Vector paraRegresar = conexion.getFilas();
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        return dbResult;
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

    public int insertaDerrotero(int idCampana, int idEstacion, String nombre, String fPlaneada, String fEjecutada, int numEstP, int numEstE, String comentarios) {
        String query = "INSERT INTO derrotero "
                + "VALUES(0," + idCampana + "," + idEstacion + ",'" + nombre + "'," + fPlaneada + "," + fEjecutada
                + "," + numEstP + "," + numEstE + ",'" + comentarios + "')";
        return conexion.queryUpdateWithKey(query);
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
    public boolean updateHierarchyNCBINode(String taxid, String hierarchy){
        String query = "UPDATE NCBI_NODE SET hierarchy = '"+hierarchy+"' WHERE tax_id ="+taxid;
         if(conexion.queryUpdate(query)){
            return true;
        }else{
            System.out.println(conexion.getLog());
            return false;
        }
    }
    public boolean insertaQuery(String query) {
        if (debug) {
            System.out.println(query);
        }
        if(conexion.queryUpdate(query)){
            return true;
        }else{
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
