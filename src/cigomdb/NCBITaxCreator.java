/**
 * Esta clase esta creada para procesar archivos del ftp de NCBI y compararlos o
 * usarlos para obtener información anotada en la BD de nuestro grupo
 *
 * @author Alejandro Abdala
 * @version 1.0
 * @date Septiembre 2015
 */
package cigomdb;

import database.Transacciones;
import bobjects.NCBINode;
import bobjects.NCBISyn;
import bobjects.Taxon;
//import dbPersistantObjects.Phylo;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.StringUtils;
//import utils.Config;
//import utils.FileUtils;

/**
 *
 * @author Alejandro
 */
public class NCBITaxCreator {

    Map<String, NCBINode> dataFile; //hash map con información de los nodos taxxonomicos del ncbi
    Transacciones transacciones; //conexion a la BD
    // Config cfg;

    public NCBITaxCreator(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public NCBITaxCreator() {
    }

    /**
     * Este método se encarga de todo el proceso para leer los archivos de ncbi,
     * leer la BD del grupo y en base a eso poder asignar un nivel taxonómico a
     * los diferentes niveles taxonómicos para cada organismos anotado en la BD
     * del grupo.
     *
     * @param nodesFileName nodes.dmp ->
     * ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.zip
     * @param namesFileName names.dmp -> ->
     * ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.zip
     */
    public String createTaxaListFromNCBI(String nodesFileName, String namesFileName) {
        dataFile = new HashMap<String, NCBINode>();
        String log = "";
        try {
            BufferedReader readerNodos = new BufferedReader(new FileReader(nodesFileName));
            BufferedReader readerNames = new BufferedReader(new FileReader(namesFileName));
            String linea;
            long start = System.currentTimeMillis();
            double numLinea = 0;
            double count = 0;
            System.out.println("Procesando nodes.dmp");
            /**
             * Este ciclo lee y procesa el archivo nodes.dmp Popula por primera
             * vez la tabla hash dataFile
             */
            while ((linea = readerNodos.readLine()) != null) {
                numLinea++;
                count++;
                StringTokenizer st = new StringTokenizer(linea, "|\t");
                String taxID = st.nextToken().trim();
                String parentTaxID = st.nextToken().trim();
                String rankName = st.nextToken().trim();
                //cuando no hay dato viene |\t\t| entonces todo lo lee como delim 
                //por lo cual se puede prodeucir un desfaz. el smbl_code no se usa pero
                //sirve de ayuda ya que esta antes del div_id y tienen que ser dos letras
                //si el valor es numérico en lugar de caracteres, se supone que ya es div_id
                String embCode = st.nextToken().trim();
                String div_id = st.nextToken().trim();
                try {
                    Integer.parseInt(embCode);
                    div_id = embCode;
                } catch (NumberFormatException nfe) {

                }
                NCBINode node = new NCBINode(taxID);
                node.setParent_tax_id(parentTaxID);
                node.setRank(rankName);
                node.setDiv_code(div_id);
                dataFile.put(taxID, node);
            }
            readerNodos.close();
            long finish = System.currentTimeMillis() - start;
            System.out.println("Total time = " + finish / 1000 + " s.");
            System.out.println("**********************");
            start = System.currentTimeMillis();
            numLinea = 0;
            count = 0;
            System.out.println("Procesando names.dmp");
            /**
             * Este ciclo lee el archivo names, el cual tiene muchos nombres
             * para cada nodo taxonomico, por lo cual se busca aquel que esté
             * descrito como scientific name y ese es asociado al objeto
             * NCBINode del hashmpa dataFile
             */
            NCBINode node;
            while ((linea = readerNames.readLine()) != null) {
                numLinea++;
                count++;
                StringTokenizer st = new StringTokenizer(linea, "|\t");
                String taxIDName = st.nextToken().trim();
                String name = st.nextToken().trim().replaceAll("'", "\\\\'");
                String nameType = st.nextToken().trim();
                //en algunos casos hay una columna intermedia entre el tipo de nombre y el nombre
                //en esos casos hay que hacer la lectura de un token "extra"
                if (st.countTokens() > 0) {
                    nameType = st.nextToken().trim();
                }
                if (nameType.equals("scientific name")) {
                    node = dataFile.get(taxIDName);
                    if (node != null) {
                        node.setName(name);
                        node.setClass_name(nameType);
                        dataFile.put(taxIDName, node);
                    } else {
                        System.out.println("No entry for: " + taxIDName);
                    }
                } else {
                    node = dataFile.get(taxIDName);
                    if (node != null) {
                        NCBISyn syn = new NCBISyn(taxIDName);
                        syn.setClass_name(nameType);
                        syn.setSynonim(name);
                        node.addSyn(syn);
                        dataFile.put(taxIDName, node);
                    } else {
                        System.out.println("No entry for: " + taxIDName);
                    }
                }
            }
            readerNames.close();
            System.gc();
            finish = System.currentTimeMillis() - start;
            System.out.println("Total time 2 = " + finish / 1000 + " s.");
            System.out.println("**********************");
            System.out.println("Insertando organismos nuevos en la BD....");

            start = System.currentTimeMillis();
            int totOK = 0;
            //NCBINode node;
            NCBINode tmpNode;
            String hierarchy;
            for (String key : dataFile.keySet()) {
                node = dataFile.get(key);
                hierarchy = node.getParent_tax_id().trim();
                tmpNode = node;
                while (tmpNode.getParent_tax_id() != null && !tmpNode.getParent_tax_id().equals("1") && !tmpNode.getParent_tax_id().equals(tmpNode.getTax_id())) {
                    tmpNode = dataFile.get(tmpNode.getParent_tax_id());
                    hierarchy = tmpNode.getParent_tax_id().trim() + "," + hierarchy;
                }
                node.setHirarchy(hierarchy);
                ///boolean ok = transacciones.insertaQuery(node.toSQLInsertString());
                boolean ok = transacciones.updateHierarchyNCBINode(node.getTax_id(), node.getHirarchy());
                if (ok) {
                    totOK++;
                    //    for (NCBISyn syn : node.getSynms()) {
                    //       ok &= transacciones.insertaQuery(syn.toSQLInsertString());
                    //      if (!ok) {
                    //         log += "Error insertando NCBI_SYN: " + syn.toString();
                    //    }
                    // }
                } else {
                    log += "Error insertando NCBI_NODE: " + node.toString();
                }
            }

            finish = System.currentTimeMillis() - start;
            System.out.println("Total time 3 = " + finish / 1000 + " s.");
            System.out.println("**********************");
            System.out.println("Total de nodos procesados: " + this.dataFile.keySet().size()
                    + "\nTotal de nodos insertados: " + totOK
                    + "\nTotal de nodos descartados: " + (this.dataFile.keySet().size() - totOK));
            System.out.println("**********************");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(NCBITaxCreator.class.getName()).log(Level.SEVERE, null, ex);
            return "Error no se encontro archivo de entrada:\n" + ex.getLocalizedMessage();
        } catch (IOException ex) {
            Logger.getLogger(NCBITaxCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return log;
    }

    /**
     * Este método popula la tabla taxon con los datos de ncbi_node
     *
     * @param outFile
     * @param toFile
     */
    public void createTaxon(String outFile, boolean toFile, String where) {
        //  ArrayList<ArrayList> nodos = transacciones.getNCBINodes(" WHERE hierarchy not like '%,2759,%'");
        System.out.println("Leyendo Nodos");
        ArrayList<ArrayList> nodos = transacciones.getNCBINodes(where);
        System.out.println("Nodos: " + nodos.size());
        FileWriter writer = null;
        StringUtils su = new StringUtils();
        try {
            if (toFile) {
                writer = new FileWriter(outFile);
            }
            ArrayList<ArrayList> linaje;
            int taxones = 0;
            for (ArrayList<String> nodo : nodos) {
                taxones++;
                int idTax = Integer.parseInt(nodo.get(0));
                String rank = nodo.get(1);
                if (rank.equals("superkingdom")) {
                    rank = "kingdom";
                }
                String name = su.scapeSQL(nodo.get(2));
                String hierarchy = nodo.get(3);
                Taxon tax = new Taxon(idTax);
                tax.setRank(rank);
                tax.setTaxon(name);
                linaje = transacciones.getNCBINodes(" WHERE tax_id IN(" + hierarchy + ")");
                for (ArrayList<String> nodoPadre : linaje) {
                    String rankP = nodoPadre.get(1);
                    String nameP = su.scapeSQL(nodoPadre.get(2));
                    if (rankP.equals("superkingdom")) {
                        rank = "kingdom";
                    }
                    tax.assignRank(nameP, rankP);
                }
                if (!toFile) {
                    if (!transacciones.insertaQuery(tax.toSQLString())) {
                        System.err.println("Error insertando: " + tax.toSQLString());
                    }
                } else {
                    writer.write(tax.toSQLString() + ";\n");
                }
                if (taxones % 25000 == 0) {
                    System.out.println("Taxones procesados: " + taxones);
                }

            }
            System.out.println("#########SE FINI###########");
            System.out.println("Taxones procesados: " + taxones);

            if (toFile) {
                writer.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(NCBITaxCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Este métod se encarga de tomar el hashmap con los nodos taxonómicos, el
     * arreglo de organismos a comprar y en base a eso determinal la filogenia y
     * su clasificación para cada organismo en la lista
     *
     * @param organismos lista de Organimos
     * @param map mapa con key ncbi tax y ncbinode asociado
     * @return regresa un ArrayList de Phylo, objeto que representa la
     * clasificación taxonómica para un organismo
     */
    /*<--  public ArrayList<Phylo> createNCBI_DB(ArrayList<ArrayList> organismos, Map<String, NCBINode> map) {
     ArrayList<Phylo> phylos = new ArrayList<Phylo>();
     //itera sobre cada organismo en la lista
     for (ArrayList<String> organismo : organismos) {
     String org_id = organismo.get(0);
     //String org_definition = organismo.get(1);
     String org_phylo = organismo.get(2);
     String org_taxID = organismo.get(3);
     NCBINode node = map.get(org_taxID);
     if (node != null) {
     Phylo phylo = new Phylo(org_id, true);
     phylo.setRank_name(node.getRank());
     phylo.addPhyloRank(node.getRank(), node.getName());
     String hierarchy = node.getTax_id();
     int counter = 0;
     /**
     * Este ciclo se repite hasta que se llega hasta la
     * determinación del reino
     */
    /*<--   while (phylo.getKingdom().length() == 0) {
     counter++;
     node = map.get(node.getParent_tax_id());
     phylo.addPhyloRank(node.getRank(), node.getName());
     hierarchy = node.getTax_id() + "-" + hierarchy;
     //evita que se cicle en caso de error
     //al principio limit 20 pero orgs como:api,aml,ame,bmor
     //tiene mas de 20 hay con mas de 30
     if (counter > 40) {
     System.out.println("Error SKIP: " + org_id + " " + node.toString() + " Phylo " + hierarchy);
     break;
     }
     }
     phylo.setHierarchy(hierarchy);
     phylos.add(phylo);
     } else {
     System.out.println("No se encontro: " + org_id + " taxID " + org_taxID);
     }

     }
     return phylos;
    
     }*/
}
