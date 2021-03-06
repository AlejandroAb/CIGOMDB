/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bobjects;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author Alejandro
 */
public class GenObj {

    String genID; //ID único del gen asignado por nosotros
    int idMetagenoma;//puede pertenecer a un metagenoma o a un genoma peor no ambos
    int idGenoma;
    String gen_src;//si viene de metagenoma o genoma     
    @Deprecated
    String object_id; //ID del objeto en la base de datos mongo (archivo de contigs/scaffolds)    
    String gene_map_id; //ID para mapear otros resultados por ejemplo FGS cnvierte los IDs a gen_id_1, gen_id_n
    String gen_name = ""; //nombre del gen
    String genType; //cds rna, trna, etc
    String gen_strand; //+ - ? .
    String gen_function = "";//funcion o definicion del gen
    int gen_length;//número de nucleótidos del gen
    int gen_num; //número del gen dentro de se contexto (genoma o metagenoma)
    double gen_score; //score (seter con string por que en gff puede venir con . )  
    String contig_id; //ID del contig al cual pertenece el gen
    String contig_gen_id; //ID del gen dentro del archivo de contigs
    int contig_from; //posicion de del gen dentro del contig
    int contig_to;//pos del gen dentro del ccontig
    ArrayList<GenSeqObj> sequences;//secuencias asociadas a ddicho gen
    ArrayList<DBProperty> props;//campos no modelados que pueden guardarse en MOngo
    ArrayList<ArchivoObj> archivos;
    String gen_phase;
    Intergenic inter3p;
    Intergenic inter5p;
    GenObj vecino;

    public int getIdMetagenoma() {
        return idMetagenoma;
    }

    public GenObj getVecino() {
        return vecino;
    }

    public void setVecino(GenObj vecino) {
        this.vecino = vecino;
    }

    public Intergenic getInter3p() {
        return inter3p;
    }

    public void setInter3p(Intergenic inter3p) {
        this.inter3p = inter3p;
    }

    public Intergenic getInter5p() {
        return inter5p;
    }

    public void setInter5p(Intergenic inter5p) {
        this.inter5p = inter5p;
    }

    public void setIdMetagenoma(int idMetagenoma) {
        this.idMetagenoma = idMetagenoma;
    }

    public String getGen_phase() {
        return gen_phase;
    }

    public void setGen_phase(String gen_phase) {
        this.gen_phase = gen_phase;
    }

    public int getIdGenoma() {
        return idGenoma;
    }

    public void setIdGenoma(int idGenoma) {
        this.idGenoma = idGenoma;
    }

    public String getGen_src() {
        return gen_src;
    }

    public void setGen_src(String gen_src) {
        this.gen_src = gen_src;
    }

    public String getGen_name() {
        return gen_name;
    }

    public void setGen_name(String gen_name) {
        this.gen_name = gen_name;
    }

    public int getGen_length() {
        return gen_length;
    }

    public void setGen_length(int gen_length) {
        this.gen_length = gen_length;
    }

    public int getGen_num() {
        return gen_num;
    }

    public void setGen_num(int gen_num) {
        this.gen_num = gen_num;
    }

    public double getGen_score() {
        return gen_score;
    }

    public void setGen_score(String gen_score) {
        try {
            this.gen_score = Double.parseDouble(gen_score);
        } catch (NumberFormatException nfe) {
            this.gen_score = -1;
        }
    }

    public ArrayList<DBProperty> getProps() {
        return props;
    }

    public void setProps(ArrayList<DBProperty> props) {
        this.props = props;
    }

    public ArrayList<ArchivoObj> getArchivos() {
        return archivos;
    }

    public void setArchivos(ArrayList<ArchivoObj> archivos) {
        this.archivos = archivos;
    }

    public String getGenID() {
        return genID;
    }

    public void addSequence(GenSeqObj seq) {
        sequences.add(seq);
    }

    public void addProperty(String key, String value) {
        props.add(new DBProperty(key, value));
    }

    public void addProperty(String key, String value, boolean isNumeric) {
        props.add(new DBProperty(key, value));
    }

    public void insertProperty(DBProperty dbProp) {
        props.add(dbProp);
    }

    public void addArchivo(ArchivoObj archivo) {
        this.archivos.add(archivo);
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
        StringTokenizer st = new StringTokenizer(contig_id, " |\t");
        //es únicamente el primer elemento
        if (st.countTokens() > 1) {
            this.contig_id = st.nextToken();
        } else {
            this.contig_id = contig_id;
        }

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
        this.archivos = new ArrayList<ArchivoObj>();
    }

}
