/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import bobjects.GenObj;
import bobjects.GenSeqObj;
import database.Transacciones;

/**
 *
 * @author Alejandro
 */
public class GenDAO {

    private Transacciones transacciones;
    private boolean debug = false;

    public String almacenaGen(GenObj gen) {
        String log = "";
        String query = "INSERT INTO Gen (gen_id,object_id,gen_map_id,gen_type,"
                + "gen_strand,gen_function,contig_id,contig_gen_id,contig_from,contig_to) "
                + "VALUES ("
                + "'" + gen.getGenID() + "','', '" + gen.getGene_map_id()
                + "', '" + gen.getGenType() + "', '" + gen.getGen_strand()
                + "', '" + gen.getGen_function() + "', '" + gen.getContig_id()
                + "', '" + gen.getContig_gen_id() + "', '" + gen.getContig_from() + "', '" + gen.getContig_to() + "')";
        if (!transacciones.insertaQuery(query)) {
            log += "Error insertando gen: " + gen.getGenID() + " - " + query + "\n";
        }
        for (GenSeqObj seq : gen.getSequences()) {
            String querySeq = "INSERT INTO Gen_Seq (gen_id, seq_type, seq_from, seq_to, seq_size, sequence) VALUES ("
                    + "'" + gen.getGenID() + "', '" + seq.getSeqType()
                    + "', '" + seq.getSeq_from() + "', '" + seq.getSeq_to() + "', " + seq.getSeq_size()
                    + ", '" + seq.getSequence() + "')";
            if (!transacciones.insertaQuery(querySeq)) {
                log += "Error insertando secuencia: " + gen.getGenID() + " - " + querySeq + "\n";
            }
        }

        return log;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public GenDAO(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

}
