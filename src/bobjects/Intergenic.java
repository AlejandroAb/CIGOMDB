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
public class Intergenic implements Cloneable {

    public static final int I3P = 1;
    public static final int I5P = 2;
    private int type;
    int size;
    int from;
    int to;
    String secuencia;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Intergenic(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public String getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(String secuencia) {
        this.secuencia = secuencia;
    }

    public void setSecuenciaValidada(Sequence secuencia, int from, int to) {
        try {
            this.secuencia = secuencia.getSequence().substring(from, to);
        } catch (StringIndexOutOfBoundsException sioobe) {
            //Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, sioobe);
            System.err.println("StringIndexOutOfBoundsException - contig " + secuencia.getSeqId() + " from: " + from + " to: " + to);
            if (from < secuencia.getSequence().length()) {
                this.secuencia = secuencia.getSequence().substring(from, secuencia.getSequence().length());
            }else{
                this.secuencia = "";
            }
        }
    }
}
