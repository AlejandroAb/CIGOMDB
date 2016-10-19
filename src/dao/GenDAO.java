/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import bobjects.GenObj;
import bobjects.GenSeqObj;
import bobjects.Intergenic;
import database.Transacciones;
import utils.StringUtils;

/**
 *
 * @author Alejandro
 */
public class GenDAO {

    private Transacciones transacciones;
    private boolean debug = false;

    public String almacenaGen(GenObj gen) {
        String log = "";
        String query = "INSERT INTO gen (gen_id,object_id,gen_map_id,gen_type,"
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
            String querySeq = "INSERT INTO gen_seq (gen_id, seq_type, seq_from, seq_to, seq_size, sequence) VALUES ("
                    + "'" + gen.getGenID() + "', '" + seq.getSeqType()
                    + "', '" + seq.getSeq_from() + "', '" + seq.getSeq_to() + "', " + seq.getSeq_size()
                    + ", '" + seq.getSequence() + "')";
            if (!transacciones.insertaQuery(querySeq)) {
                log += "Error insertando secuencia: " + gen.getGenID() + " - " + querySeq + "\n";
            }
        }

        return log;
    }

    public boolean almacenaValidaGen(GenObj gen) {
        boolean log = true;
        String query = "INSERT INTO gen (gen_id,idmetagenoma, idgenoma, gen_src, gen_map_id,gen_name, gen_type,"
                + "gen_strand,gen_function,gen_length, gen_num, gen_score,contig_id,contig_gen_id,contig_from,contig_to) "
                + "VALUES ("
                + "'" + gen.getGenID() + "'," + gen.getIdMetagenoma() + "," + gen.getIdGenoma() + ",'" + gen.getGen_src()
                + "','" + gen.getGene_map_id() + "','" + gen.getGen_name() + "', '" + gen.getGenType() + "','" + gen.getGen_strand()
                + "', '" + gen.getGen_function() + "', " + gen.getGen_length() + "," + gen.getGen_num() + "," + gen.getGen_score() + ",'" + gen.getContig_id()
                + "', '" + gen.getContig_gen_id() + "', '" + gen.getContig_from() + "', '" + gen.getContig_to() + "')";
        if (!transacciones.insertaQuery(query)) {
            System.err.println("Error insertando gen: " + gen.getGenID() + " - " + query);
            return false;
        }
        StringUtils sutils = new StringUtils();
        String nc = null;
        for (GenSeqObj seq : gen.getSequences()) {
            boolean insertSeq = true;
            if (seq.getSeqType().equals("NC")) {
                nc = seq.getSequence();
                if (gen.getGen_strand().equals("-")) {
                    nc = sutils.reversoComplementarioNuc(nc);
                    Intergenic i3p = gen.getInter5p();
                    i3p.setSecuencia(sutils.reversoComplementarioNuc(gen.getInter5p().getSecuencia()));
                    Intergenic i5p = gen.getInter3p();
                    i5p.setSecuencia(sutils.reversoComplementarioNuc(gen.getInter3p().getSecuencia()));
                    gen.setInter3p(i3p);
                    gen.setInter5p(i5p);
                }
                insertSeq = false;
            } else if (seq.getSeqType().equals("NC_2")) {
                //para que se guarde bien en la BD por que esta es la que se guarda
                seq.setSeqType("NC");
                if (nc != null && !seq.getSequence().equals(nc)) {
                    log = false;
                    if (Math.abs(seq.getSeq_size() - nc.length()) > 3) {
                        if (debug) {
                            System.err.println("Seq err -> NC != NC2: " + gen.getGenID() + " dif: " + Math.abs(seq.getSeq_size() - nc.length()));
                        }
                        //  System.err.println("phase: " + gen.getGen_phase() + " from: " + gen.getContig_from() + " to: " + gen.getContig_to() + "\n" + nc + "\n" + seq.getSequence());
                    }
                    if (((gen.getContig_to() - gen.getContig_from()) + 1) == nc.length()) {
                        seq.setSequence(nc);
                        //   System.err.println("Insertando secuencia completa");
                    }
                }
            }
            if (insertSeq) {
                String querySeq = "INSERT INTO gen_seq (gen_id, seq_type, seq_from, seq_to, seq_size, sequence) VALUES ("
                        + "'" + gen.getGenID() + "', '" + seq.getSeqType()
                        + "', '" + seq.getSeq_from() + "', '" + seq.getSeq_to() + "', " + seq.getSeq_size()
                        + ", '" + seq.getSequence() + "')";
                if (!transacciones.insertaQuery(querySeq)) {
                    System.err.println("Error insertando secuencia: " + gen.getGenID() + " - " + querySeq + "\n");
                    log = false;
                }
            }
        }

        //EN el from de la intergénica, se le suma 1 pues al obener las coordenadas se calcula las necesarias 
        //para realizar de manera adecuada el substr al momento de extraer la sec, pero para fines de anotación, la sec debe de tener el +1 
        //INGRESA 3P
        if (gen.getInter3p().getSize() != 0) {
            String querySeq = "INSERT INTO gen_seq (gen_id, seq_type, seq_from, seq_to, seq_size, sequence) VALUES ("
                    + "'" + gen.getGenID() + "', '3P', '" + (gen.getInter3p().getFrom() + 1) + "', '" + gen.getInter3p().getTo() + "', " + gen.getInter3p().getSize()
                    + ", '" + gen.getInter3p().getSecuencia() + "')";
            if (!transacciones.insertaQuery(querySeq)) {
                System.err.println("Error insertando secuencia 3P: " + gen.getGenID() + " - " + querySeq + "\n");
                log = false;
            }
        }
        //INGRESA 5P
        if (gen.getInter5p().getSize() != 0) {
            String querySeq5P = "INSERT INTO gen_seq (gen_id, seq_type, seq_from, seq_to, seq_size, sequence) VALUES ("
                    + "'" + gen.getGenID() + "', '5P', '" + (gen.getInter5p().getFrom() + 1) + "', '" + gen.getInter5p().getTo() + "', " + gen.getInter5p().getSize()
                    + ", '" + gen.getInter5p().getSecuencia() + "')";
            if (!transacciones.insertaQuery(querySeq5P)) {
                System.err.println("Error insertando secuencia 5P: " + gen.getGenID() + " - " + querySeq5P + "\n");
                log = false;
            }
        }

        return true;
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
