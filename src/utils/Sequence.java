/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.StringTokenizer;

/**
 *
 * @author Alejandro
 */
public class Sequence {

    public static int NUCLEOTIDOS = 1;
    public static int PROTEINAS = 2;
    private String header;
    private String seqId; //el primer elemento dentro del header si es que hay mas de un token
    private String sequence;
    private String alphabet_nuc = "[ACTG]";
    private String alphabet_prot = "[ACDEFGHIKLMNPQRSTVWYBZX]";
    private String alphabet;
    private int type;

    public Sequence(int type) {
        this.type = type;
        if (type == NUCLEOTIDOS) {
            alphabet = alphabet_nuc;
        } else if (type == PROTEINAS) {
            alphabet = alphabet_prot;
        } else {
            alphabet = "[a-zA-Z]";
        }
    }

    public String getSeqId() {
        return seqId;
    }

    public void setSeqId(String seqId) {
        this.seqId = seqId;
    }

    public Sequence(String alphabet) {
        this.alphabet = alphabet;
    }

    public String getHeader() {
        return header;
    }

    public boolean test() {
        return sequence.matches(alphabet + "*");
    }

    public void setHeader(String header) {
        this.header = header;
        StringTokenizer st = new StringTokenizer(" |\t");
        //es únicamente el primer elemento
        if(st.countTokens()>1){
            seqId = st.nextToken();
        }else{
            seqId = header;
        }
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
