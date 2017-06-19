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

    public int getNextIDMarcador() {
        String query = "SELECT MAX(idMarcador) FROM marcador";
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
     * Trae todas las proteinas DISTINTAS predichas por trinotate ya sea en
     * metagenoma o genoma
     *
     * @return
     */
    public ArrayList<ArrayList> getAllDistinctPredictedSwissProt() {
        String query = "SELECT distinct(uniprot_id) FROM gen_swiss_prot";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        return dbResult;
    }

    public ArrayList<ArrayList> getNCBINodes(String where) {
        String query = "SELECT ncbi_node.tax_id, ncbi_node.rank, ncbi_node.name, hierarchy FROM ncbi_node " + where;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        return dbResult;
    }

    public ArrayList<ArrayList> getCountsByMarcador(String idMarcador, String idanalisis) {
        String query = "SELECT tax_id, counts FROM conteos where idmarcador = " + idMarcador+ " AND idanalisis_clasificacion = " + idanalisis;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        return dbResult;
    }
     public ArrayList<ArrayList> getCountsByMetagenoma(String idMetagenoma) {
        String query = "SELECT tax_id, counts FROM conteos_shotgun where idmetagenoma = " + idMetagenoma;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        return dbResult;
    }
    //public Arr
    public ArrayList getRegularPhylogenyByTaxID(String tax_id) {
        String query = "SELECT kingdom, phylum, class, orden, family, genus, species "
                + "FROM taxon WHERE tax_id = " + tax_id;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult != null && dbResult.size() > 0) {
            return dbResult.get(0);
        } else {
            return null;
        }

    }

    public ArrayList<ArrayList> testUsuarioByFullName(String nombre, String apellido, boolean exact) {
        String query;
        if (exact) {
            query = "SELECT idusuario, nombres, apellidos FROM usuario "
                    + "WHERE nombres ='" + nombre + "' and apellidos='" + apellido + "'";
        } else {
            query = "SELECT idusuario, nombres, apellidos FROM usuario "
                    + "WHERE nombres like '%" + nombre + "%' and apellidos like '%" + apellido + "%'";
        }
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        return dbResult;
    }

    public ArrayList<ArrayList> testUsuarioByName(String nombre, boolean exact) {
        String query;
        if (exact) {
            query = "SELECT idusuario, nombres, apellidos FROM usuario "
                    + "WHERE nombres ='" + nombre + "'";
        } else {
            query = "SELECT idusuario, nombres, apellidos FROM usuario "
                    + "WHERE nombres like '%" + nombre + "%'";
        }
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        return dbResult;
    }

    public ArrayList<ArrayList> testUsuarioByID(String id) {
        String query = "SELECT idusuario, nombres, apellidos FROM usuario "
                + "WHERE idusuario =" + id + "";

        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        return dbResult;
    }

    public ArrayList<ArrayList> testUsuarioBySurname(String apellido, boolean exact) {
        String query;
        if (exact) {
            query = "SELECT idusuario, nombres, apellidos FROM usuario "
                    + "WHERE apellidos ='" + apellido + "'";
        } else {
            query = "SELECT idusuario, nombres, apellidos FROM usuario "
                    + "WHERE apellidos like '%" + apellido + "%'";
        }
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        return dbResult;
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
    public int getNextIDArchivos() {
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
     * Obtiene cual es el max id de archivo para poder asignar nuevos.
     *
     * @return
     */
    public int getNextIDStats() {
        String query = "SELECT MAX(idstats) FROM stats";
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

    public int testEstacionByNameAndID(String est, int idTipoEstacion) {
        String query = "SELECT idEstacion from estacion WHERE estacion_nombre = '" + est + "' AND id_tipo_estacion = " + idTipoEstacion;
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
     * Recibe una etiqueta y en base a esta busca su ID coreespondiente
     *
     * @param etiqueta
     * @return
     */
    public int getMuestreoByLabel(String etiqueta) {
        String query = "SELECT idMuestreo from muestreo WHERE etiqueta = '" + etiqueta + "'";
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
     *
     * @param raw_seq_id
     * @return
     */
    public String getSecMarcadorByRawID(String raw_seq_id, String idmarcador) {
        String query = "SELECT idseq_marcador FROM seq_marcador WHERE raw_seq_id ='" + raw_seq_id + "' AND idmarcador = " + idmarcador;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            return "";
        } else {
            if(dbResult.size()>1){
                System.err.println("RAW_ID repetido: " + raw_seq_id);
            }
            return dbResult.get(0).get(0).toString();
        }
    }

    /**
     * Busca el id de una muestra dada su etiqueta
     *
     * @param raw_seq_id
     * @return
     */
    public String getIdMuestraByLabel(String label) {
        String query = "SELECT idMuestra FROM muestra WHERE etiqueta ='" + label + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            return "";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }

    public String getIdMuestraByMetagenoma(String idMetagenoma) {
        String query = "SELECT idMuestra FROM metagenoma WHERE idMetagenoma =" + idMetagenoma;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            return "";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }

    /**
     * Busca el id de unmarcador, dado su nombre
     *
     * @param label
     * @return
     */
    public String getIdMarcadorByLabel(String label) {
        String query = "SELECT idmarcador FROM marcador WHERE marc_name ='" + label + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            return "";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }
    /**
     * Busca el id de unmarcador, dado su nombre
     *
     * @param label
     * @return
     */
    public String getIdMetagenomaByLabel(String label) {
        String query = "SELECT idmetagenoma FROM metagenoma WHERE meta_name ='" + label + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            return "";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }

    /**
     * Busca el ID de un marcador dado un fragmento de su path de datos
     * procesados. Particularmete sirve para la reconstrucción de estadisticas
     * ya que en sample viene parte del pro_data_path y en base a esto podemos
     * identificar a que marcador se refiere
     *
     * @param proPath el path del marcador: ejjemplos son: E03_SED05_1,
     * C13_MAX_1
     * @return
     */
    public String getIdMarcadorByProPath(String proPath) {
        String query = "SELECT idmarcador FROM marcador "
                + "WHERE pro_data_path LIKE '%/" + proPath + "/%'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            return "-1";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }

    /**
     * Busca la etiqueta de un marcador dado su nombre
     *
     * @param idMarcador
     * @return
     */
    public String getEtiquetaMarcadorByLabel(String idMarcador) {
        String query = "SELECT marc_name FROM marcador WHERE idmarcador =" + idMarcador;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            return "";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }
    
     /**
     * Busca la etiqueta de un marcador dado su nombre
     *
     * @param idMarcador
     * @return
     */
    public String getEtiquetaMetagenomaByLabel(String idMetagenoma) {
        String query = "SELECT meta_name FROM metagenoma WHERE idmetagenoma =" + idMetagenoma;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            return "";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }

    public String getHierarchy(String taxID) {
        String query = "SELECT hierarchy FROM ncbi_node WHERE tax_id = " + taxID;
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
     *
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
     * Este método es usado para traer la descripción de un cog dado su ID,
     * sirve para validar diferencias entre eggnog y cog
     *
     * @param cog
     * @return
     */
    public String getCOGDescription(String cog) {
        String query = "SELECT cog_description FROM cog WHERE id_cog ='" + cog + "'";
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
     * Este método se encarga de traer el ID real (de la BD - gen_id) de la
     * entidad gen, de acuerdo al gene_map_id del gen en cuestión. Como hay
     * muchos genes que pueden tener el mismo map_id, es necesario dar el id del
     * genoma o metagenoma al cual pertenece el gen, es por esto que en los
     * parámetross se incluye "group" el cual tiene que ser genoma o metagenoma
     *
     * @param group genoma o metagenoma
     * @param groupID el id del genoma o metagenoma
     * @param geneMapID el map id a buscar
     * @return
     */
    public String getGeneIDByMapID(String group, String groupID, String geneMapID) {
        String query = "SELECT gen_id FROM gen WHERE id" + group + " = " + groupID + " AND  gen_map_id = '" + geneMapID + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        int id = -1;
        if (dbResult == null || dbResult.isEmpty()) {
            System.err.println("No se puede enconrar gen_id para: " + geneMapID);
            System.err.println("Q: " + query);
            return "";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }
 public String getGeneIDByContigInfo(String group, String groupID, String contigId, String from, String to) {
        String query = "SELECT gen_id FROM gen WHERE id" + group + " = " + groupID + " "
                + "AND contig_id = '" + contigId +"' AND contig_from = " + from +" AND contig_to = " + to;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        int id = -1;
        if (dbResult == null || dbResult.isEmpty()) {
            System.err.println("No se puede enconrar gen_id para contig_id = '" + contigId +"' AND contig_from = " + from +" AND contig_to = " + to);
            System.err.println("Q: " + query);
            return "ERROR";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }

    /**
     * Trae el path de procesamiento para un marcador
     * @param idMarcador
     * @return 
     */
    public String getProcessDataPathByMarcadorID(String idMarcador) {
        String query = "SELECT pro_data_path FROM marcador WHERE idMarcador = " + idMarcador;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            System.err.println("No se puede enconrar el path para ID: " + idMarcador);
            System.err.println("Q: " + query);
            return "";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }
    
    /**
     * Trae el path donde se encuentra el archivo kraken.out
     * @param idmetagenoma
     * @return 
     */
    public String getKrakenPathByMetagenomaID(String idmetagenoma) {
        String query = "SELECT path FROM archivo "
                + "INNER JOIN metagenoma_archivo AS ma ON ma.idarchivo = archivo.idarchivo "
                + "WHERE idmetagenoma = " + idmetagenoma +" AND idtipo_archivo = 7";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            System.err.println("No se puede enconrar el path para ID: " + idmetagenoma);
            System.err.println("Q: " + query);
            return "";
        } else {
            return dbResult.get(0).get(0).toString();
        }
    }

    public String getProcessDataPathByMarcadorName(String etiqueta) {
        String query = "SELECT id_marcador, pro_data_path FROM marcador WHERE marc_name = '" + etiqueta + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            System.err.println("No se puede enconrar el path para etiqueta: " + etiqueta);
            System.err.println("Q: " + query);
            return "";
        } else {
            if (dbResult.size() > 1) {
                System.err.println("Mas de un resultado para etiqueta: " + etiqueta);
            }
            return dbResult.get(0).get(0).toString();
        }
    }

    /**
     * Este método valida que tengamos la información de uniprot en nuestra
     * base, de lo contrario se busca anotar la protena en nuestra entidad de
     * referencia: "Swiss-prot"
     *
     * @param uniID
     * @return
     */
    public boolean validaUniprotID(String uniID) {
        String query = "SELECT uniprot_id FROM swiss_prot WHERE uniprot_id = '" + uniID + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            if (debug) {
                System.err.println("No se encontró proteína: " + uniID);
                System.err.println("Q: " + query);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Cuando se corre el programa que parsea trinotate si no exsite una entrada
     * de uniprot, se lee el id, el nombre y se anota pero el accesion queda con
     * -1, es por esto que este query, basado en el id verifica si el registro
     * tiene esta procedencia y en ese caso se encarga de actualizar el registro
     *
     * @param uniID
     * @return
     */
    public boolean validaUniprotAcc(String uniID) {
        String query = "SELECT uniprot_id FROM swiss_prot WHERE uniprot_id = '" + uniID + "' AND uniprot_acc = '-1'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            if (debug) {
                System.err.println("No se encontró proteína: " + uniID);
                System.err.println("Q: " + query);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Este método valida que exista un gen asignado a un uniprot id (leido de
     * un archivo xml) y si tenemos dicha asignación y no existe el uni ID con
     * toda su info en nuestra BD, entonces este registro es anotado.
     *
     *
     * @param uniID
     * @return
     */
    public boolean validaGenUniprotID(String uniID) {
        String query = "SELECT uniprot_id FROM gen_swiss_prot WHERE uniprot_id = '" + uniID + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            if (debug) {
                System.err.println("No se encontró proteína: " + uniID);
                System.err.println("Q: " + query);
            }
            return false;
        } else {
            return true;
        }
    }

    public boolean validaGO(String idGO) {
        String query = "SELECT id_GO FROM gontology WHERE id_GO = '" + idGO + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        int id = -1;
        if (dbResult == null || dbResult.isEmpty()) {
            System.err.println("No se encontró GO: " + idGO);
            System.err.println("Q: " + query);
            return false;
        } else {
            return true;
        }
    }

    public boolean validaNOG(String idEggNog) {
        String query = "SELECT ideggnog FROM eggnog WHERE ideggnog = '" + idEggNog + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        if (dbResult == null || dbResult.isEmpty()) {
            query = "SELECT id_nog FROM nog WHERE id_nog = '" + idEggNog + "'";
            conexion.executeStatement(query);
            dbResult = conexion.getTabla();
        }
        if (dbResult == null || dbResult.isEmpty()) {
            System.err.println("No se encontró NOG - ENOG: " + idEggNog);
            System.err.println("Q: " + query);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Este método valida que tengamos la información de pfam en nuestra base,
     * de lo contrario se busca anotar la familia pfam en nuestra entidad de
     * referencia: "pfam"
     *
     * @param pf_code
     * @return
     */
    public boolean validaPfamAccID(String pf_code) {
        String query = "SELECT pfam_acc FROM pfam WHERE pfam_acc = '" + pf_code + "'";
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        int id = -1;
        if (dbResult == null || dbResult.isEmpty()) {
            System.err.println("No se encontró pfam: " + pf_code);
            System.err.println("Q: " + query);
            return false;
        } else {
            return true;
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
        String query = "INSERT INTO marcador_archivo (idmarcador, idarchivo) VALUES(" + idMarcador + "," + idArchivo + ")";
        return conexion.queryUpdate(query);
    }

    public boolean insertaArchivoMetagenoma(String idMetagenoma, int idArchivo) {
        String query = "INSERT INTO metagenoma_archivo (idmetagenoma, idarchivo) VALUES(" + idMetagenoma + "," + idArchivo + ")";
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
                + idAnalisis + "," + identity + "," + eval + "," + score + "," + length + ",'" + comments + "')";
        return conexion.queryUpdate(query);
    }
public boolean insertMarcadorClassificationParallel(String taxID, String idseq_marcador, int idAnalisis, String identity, String eval, String score, String length, String comments) {
        String query = "INSERT INTO seq_marcador_classif_parallel VALUES(" + taxID + ",'" + idseq_marcador + "', "
                + idAnalisis + "," + identity + "," + eval + "," + score + "," + length + ",'" + comments + "')";
        return conexion.queryUpdate(query);
    }

    public boolean updateTaxaSeqMarcador(String taxid, String idseq) {
        String query = "UPDATE seq_marcador SET taxon_tax_id = " + taxid + " WHERE idseq_marcador = '" + idseq + "';\n";
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

    public boolean updateGenKO(String genID, String idKO) {
        String query = "UPDATE gen SET idKO= '" + idKO + "' WHERE gen_id = '" + genID + "'";
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
            System.err.println(conexion.getLog());
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
