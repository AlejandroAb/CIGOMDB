/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import database.Transacciones;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import utils.StringUtils;

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
     * @param idAnalisis_clasificacion el id del tipo de analisis que se realizó
     * @return log del proceso
     */
    public String processMetaxaLine(String mtxLine, int idAnalisis_clasificacion, FileWriter writer, HashMap<String, String> seqMap) throws IOException {
        StringTokenizer st = new StringTokenizer(mtxLine, "\t");
        String raw_id = st.nextToken();
        String classify = st.nextToken();
        String identity = st.nextToken();
        boolean toFile = writer != null;
        try {
            Float.parseFloat(identity);
        } catch (NumberFormatException nfe) {
            identity = "null";
        }
        String length = st.nextToken();
        try {
            Float.parseFloat(length);
        } catch (NumberFormatException nfe) {
            length = "null";
        }
        String score = st.nextToken();
        try {
            Float.parseFloat(score);
        } catch (NumberFormatException nfe) {
            score = "null";
        }
        String seq_id = "";
        if (toFile) {
            seq_id = seqMap.get(raw_id);
        } else {
            seq_id = transacciones.getSecMarcadorByRawID(raw_id);
        }
        if (seq_id == null || seq_id.length() == 0) {
            System.err.println("ERROR. No se encontró secuencia con raw_id = " + raw_id + "\n");

        }
        String taxid[] = searchNCBINode(classify);
        if (toFile) {
            String query = "INSERT INTO seq_marcador_classif VALUES(" + taxid[0] + ",'" + seq_id + "', "
                    + idAnalisis_clasificacion + "," + identity + "," + "-1" + "," + score + "," + length + ",'" + taxid[1] + "');\n";
            writer.write(query);
            String query2 = "UPDATE seq_marcador SET taxon_tax_id = " + taxid[0] + " WHERE idseq_marcador = '" + seq_id + "';\n";
            writer.write(query2);
            return "";
        } else {
            if (!transacciones.insertMarcadorClassification(taxid[0], seq_id, idAnalisis_clasificacion, identity, "-1", score, length, taxid[1])) {
                System.err.println("Error insertando seq_marcador_classif: " + "INSERT INTO seq_marcador_classif VALUES(" + taxid[0] + ",'" + seq_id + "', "
                        + idAnalisis_clasificacion + "," + identity + ",-1," + score + ",'" + taxid[1] + "')");
            } else {
                if (!transacciones.updateTaxaSeqMarcador(taxid[0], seq_id)) {
                    System.err.println("Error actualizando seq_marcador --  taxid: " + taxid[0] + "    seqid: " + seq_id);
                }
            }
            return "";
        }

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
        StringUtils su = new StringUtils();
        for (int i = taxo.length - 1; i >= 0; i--) {
            tax = transacciones.getNCBITaxID(su.scapeSQL(taxo[i].trim()));
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
        String node[] = {tax, su.scapeSQL(log)};
        return node;
    }
}
