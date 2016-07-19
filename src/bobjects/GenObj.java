/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bobjects;

import java.util.ArrayList;

/**
 *
 * @author Alejandro
 */
public class GenObj {

    String genID; //ID único del gen asignado por nosotros
    String object_id; //ID del objeto en la base de datos mongo (archivo de contigs/scaffolds)
    String gene_map_id; //ID para mapear otros resultados por ejemplo FGS cnvierte los IDs a gen_id_1, gen_id_n
    String genType; //cds rna, trna, etc
    String gen_strand; //+ -
    String gen_function;//funcion o definicion del gen
    String contig_id; //ID del contig al cual pertenece el gen
    String contig_gen_id; //ID del gen dentro del archivo de contigs
    int contig_from; //posicion de del gen dentro del contig
    int contig_to;//pos del gen dentro del ccontig
    ArrayList<GenSeqObj> sequences;//secuencias asociadas a ddicho gen
    ArrayList<DBProperty> props;//campos no modelados que pueden guardarse en MOngo

    public String getGenID() {
        return genID;
    }

    public void addSequence(GenSeqObj seq) {
        sequences.add(seq);
    }
    public void addProperty(String key, String value){
        props.add(new DBProperty(key,value));
    }
    public void addProperty(String key, String value, boolean isNumeric){
        props.add(new DBProperty(key,value));
    }
    public void insertProperty(DBProperty dbProp){
        props.add(dbProp);
    }
    public String getGene_map_id() {
        return gene_map_id;
    }

    public void setGene_map_id(String gene_map_id) {
        this.gene_map_id = gene_map_id;
    }

    public ArrayList<GenSeqObj> getSequences() {
        return sequences;
    }

    public void setSequences(ArrayList<GenSeqObj> sequences) {
        this.sequences = sequences;
    }

    public void setGenID(String genID) {
        this.genID = genID;
    }

    public String getObject_id() {
        return object_id;
    }

    public void setObject_id(String object_id) {
        this.object_id = object_id;
    }

    public String getGenType() {
        return genType;
    }

    public void setGenType(String genType) {
        this.genType = genType;
    }

    public String getGen_strand() {
        return gen_strand;
    }

    public void setGen_strand(String gen_strand) {
        this.gen_strand = gen_strand;
    }

    public String getGen_function() {
        return gen_function;
    }

    public void setGen_function(String gen_function) {
        this.gen_function = gen_function;
    }

    public String getContig_id() {
        return contig_id;
    }

    public void setContig_id(String contig_id) {
        this.contig_id = contig_id;
    }

    public String getContig_gen_id() {
        return contig_gen_id;
    }

    public void setContig_gen_id(String contig_gen_id) {
        this.contig_gen_id = contig_gen_id;
    }

    public int getContig_from() {
        return contig_from;
    }

    public void setContig_from(int contig_from) {
        this.contig_from = contig_from;
    }

    public int getContig_to() {
        return contig_to;
    }

    public void setContig_to(int contig_to) {
        this.contig_to = contig_to;
    }

    public GenObj(String genID) {
        this.genID = genID;
        sequences = new ArrayList<GenSeqObj>();
        props = new ArrayList<DBProperty>();
    }

}
