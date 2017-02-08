/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 *
 * @author Alejandro
 */
public class FastaReader extends BufferedReader {

    private HashMap<String, Sequence> seqMap;
    String currentLine;
    public FastaReader(Reader in) {
        super(in);
    }
    public void loadHash(int seqType) throws IOException{
        seqMap = new HashMap<>();
        Sequence seq;
        while((seq = readSequenceML(seqType))!= null){
            seqMap.put(seq.getSeqId(), seq);
        }
    }
    public Sequence getKey(String key, boolean remove){
        Sequence seq = seqMap.get(key);
        if(seq != null && remove){
            seqMap.remove(key);
        }
        return seq;
    }
    /**
     * Se encarga de leer una secuencia en un archivo fasta, toma en cuenta que
     * pueden venir muchas lienas de secuencia, no esta 100% probado pero usa
     * mark y reset para controlar bien ese tippo de lecturas. No considera
     * comentarios y trata de manejar un marcador para casos donde lee de mas
     *
     * @param seqType
     * @return
     * @throws IOException
     */
    public Sequence readSequenceML(int seqType) throws IOException {
        String l = readLine();
        //no lee los comentarios
        while (l != null && !l.startsWith(">")) {
            l = readLine();
        }
        if (l == null) {
            return null;
        }
        Sequence seq = new Sequence(seqType);
        seq.setHeader(l.substring(1));
        String secuencia;
        l = readLine();
        secuencia = l;
        this.mark(5000);
        while ((l = readLine()) != null && !l.startsWith(">")) {
            if (!l.startsWith("#") && l.length() > 0) {
                secuencia += l;
            }
            this.mark(5000);
        }
        if (l != null && l.startsWith(">")) {
            this.reset();
        }
        seq.setSequence(secuencia);
        //aca se podria validar la secuencia seq.test();        
        return seq;

    }

    /**
     * Lee una secuencia de un archivo fasta, procesa el header y la secuencia
     * para regresar un objto Sequence
     *
     * @param seqType
     * @return
     * @throws IOException
     */
    public Sequence readSequence(int seqType) throws IOException {
        String l = readLine();
        //no lee los comentarios
        while (l != null && !l.startsWith(">")) {
            l = readLine();
        }
        if (l == null) {
            return null;
        }
        Sequence seq = new Sequence(seqType);
        seq.setHeader(l.substring(1));
        seq.setSequence(readLine());
        //aca se podria validar la secuencia seq.test();        
        return seq;

    }
}
