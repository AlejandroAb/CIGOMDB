/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import database.Transacciones;
import java.util.StringTokenizer;

/**
 *
 * @author Alejandro
 */
public class MetaxaDAO {
    
    public Transacciones transacciones;
    
    public MetaxaDAO(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    /**
     * Este método se encarga de parsear una linea del archivo taxonomy de
     * metaxa de acuerdo al documento
     * http://microbiology.se/publ/metaxa2_users_guide.pdf Column \t Description
     * <b>ID</b> The identifier of the query sequence
     * <b>Classification</b> The taxonomic tree for which Metaxa2 has been able
     * to a reliable prediction.
     * <b>Identity</b> The percent identity to the best BLAST match in the
     * database.
     * <b>Length</b> The length of the alignment of the input sequence and the
     * best BLAST match.
     * <b>Reliability</b> score The number of conserved domains for the most
     * likely origin that was found in the sequence. The reliability score is
     * calculated based on the percent identity to the best BLAST hit, and how
     * divergent the rest of the BLAST hits are from the first one. The maximal
     * score is 100, and the minimum score is determined by the -R cutoff used
     * (80 by default). Scores above 80 can generally be considered good.
     *
     * @param mtxLine linea con todas las columnas
     * @return log del proceso
     */
    public String processMetaxaLine(String mtxLine, int idAnalisis_clasificacion) {        
        StringTokenizer st = new StringTokenizer(mtxLine, "\t");
        String raw_id = st.nextToken();
        String classify = st.nextToken();
        String identity = st.nextToken();
        String length = st.nextToken();
        String score = st.nextToken();
        String seq_id = transacciones.getSecMarcadorByRawID(raw_id);
        if (seq_id == null || seq_id.length() == 0) {
            return "ERROR. No se encontró secuencia con raw_id = " + raw_id + "\n";
             
        }
        String taxid[] = searchNCBINode(classify);
        if (!transacciones.insertMarcadorClassification(taxid[0], seq_id, idAnalisis_clasificacion, identity, "-1", score,length, taxid[1])) {
            return "Error insertando seq_marcador_classif: " + "INSERT INTO seq_marcador_classif VALUES(" + taxid[0] + ",'" + seq_id + "', "
                + idAnalisis_clasificacion + "," + identity + ",-1," + score + ",'" + taxid[1] + "')";
        }
        return "";
    }

    /**
     * Recibe un string separado por ; donde cada token es un nivel en la
     * clasificación taxonómica. agarra el último elemento y lo busca een la
     * base de datos, si no hay ninguna coinsidencia busca un nivel más abajo e
     * intenta asignar ese nivel. Se obvian niveles que contengan "Incertae
     * Sedis" (término usado en taxonomía para indicar que el nivel taxonómico
     * es incierto)
     *
     * @param classification
     * @return un arreglo. en el primer elemento viene el tax_id y en el
     * segunddo el log, el cual se usa como comentarios para ya insertar en la
     * entidad, pues esta va registrando los niveles taxonomicos no
     * identificados en la base de NCBI
     */
    public String[] searchNCBINode(String classification) {
        String[] taxo = classification.split(";");
        String tax = "";
        String log = "";
        for (int i = taxo.length - 1; i <= 0; i++) {
            tax = transacciones.getNCBITaxID(taxo[i].trim());
            if (tax != null && tax.length() > 0) {
                break;
            } else {
                //algun otro método de busqueda como like?
                if (log.length() > 0) {
                    log += ";" + taxo[i].trim();
                } else {
                    log += taxo[i].trim();
                }
            }
        }
        String node[] = {tax, log};
        return node;
    }
}
