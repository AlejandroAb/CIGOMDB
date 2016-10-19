/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bobjects;

import cigomdb.GeneFuncLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Sequence;

/**
 *
 * @author Alejandro
 */
public class GenSeqObj {

    private String seqType;
    private int seq_from;
    private int seq_to;
    private int seq_size;
    private String sequence;

    public GenSeqObj() {
    }

    public String getSeqType() {
        return seqType;
    }

    public void setSeqType(String seqType) {
        this.seqType = seqType;
    }

    public int getSeq_from() {
        return seq_from;
    }

    public void setSeq_from(int seq_from) {
        this.seq_from = seq_from;
    }

    public int getSeq_to() {
        return seq_to;
    }

    public void setSeq_to(int seq_to) {
        this.seq_to = seq_to;
    }

    public int getSeq_size() {
        return seq_size;
    }

    public void setSeq_size(int seq_size) {
        this.seq_size = seq_size;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
        this.seq_size = sequence.length();
    }

    public void setSecuenciaValidada(Sequence sequence, int from, int to) {
        //  this.sequence = sequence;

        try {
            this.sequence = sequence.getSequence().substring(from, to);
            this.seq_size = this.sequence.length();
        } catch (StringIndexOutOfBoundsException sioobe) {
            //Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, sioobe);
            System.err.println("StringIndexOutOfBoundsException - contig " + sequence.getSeqId() + " from: " + from + " to: " + to);
            if (from < sequence.getSequence().length()) {
                this.sequence = sequence.getSequence().substring(from, sequence.getSequence().length());
            }else{
                this.sequence = "";
            }
        }
    }

}
