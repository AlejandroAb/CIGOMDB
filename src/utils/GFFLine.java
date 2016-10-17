/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 *
 * @author Alejandro
 */
public class GFFLine {

    String line;
    String id;//The ID of the landmark used to establish the coordinate system for the current feature.
    String metodo = ".";//The source is a free text qualifier intended to describe the algorithm or operating procedure that generated this feature.
    String seq_type;//The type of the feature (previously called the "method").This is constrained to be either: (a) a term from the "lite" sequence ontology, SOFA; or (b) a SOFA accession number.
    int from;// Start is always less than or equal to end
    int to;
    String score = ".";//The score of the feature, a floating point number. As in earlier versions of the format, the semantics of the score are ill-defined. It is strongly recommended that E-values be used for sequence similarity features, and that P-values be used for ab initio gene prediction features. If there is no score, put a "." (a period) in this field
    String strand = "."; //The strand of the feature. + for positive strand (relative to the landmark), - for minus strand, and . for features that are not stranded. In addition, ? can be used for features whose strandedness is relevant, but unknown.
    String phase = ".";//For features of type "CDS", the phase indicates where the feature begins with reference to the reading frame. The phase is one of the integers 0, 1, or 2, indicating the number of bases that should be removed from the beginning of this feature to reach the first base of the next codon.
    Map<String, String> atributos;//A list of feature attributes in the format tag=value. Multiple tag=value pairs are separated by semicolons. URL escaping rules are used for tags or values containing the following characters: ",=;"
    String atributoDelim = ",;"; //por reglas de convensión en gff3 tendrian que estar separados por ; pero en gff2 hay casos donde estan separados por , 

    public GFFLine(String line) {
            this.line = line;
            this.parse();        
    }

    /**
     * Parsea una linea GFF
     */
    public final void parse() {
        try {
            StringTokenizer st = new StringTokenizer(line, "\t");
            this.id = st.nextToken().trim();
            this.metodo = st.nextToken().trim();
            this.seq_type = st.nextToken().trim();
            try {
                this.from = Integer.parseInt(st.nextToken());
            } catch (NumberFormatException nfe) {
                System.err.println("Seq Start: Se esperaba un valor numérico!");
            }
            try {
                this.to = Integer.parseInt(st.nextToken());
            } catch (NumberFormatException nfe) {
                System.err.println("Seq end: Se esperaba un valor numérico!");
            }
            this.score = st.nextToken().trim();
            this.strand = st.nextToken().trim();
            this.phase = st.nextToken().trim();
            atributos = new HashMap<>();
            String atribs = st.nextToken();
            StringTokenizer miscToks = new StringTokenizer(atribs, atributoDelim);
            while (miscToks.hasMoreTokens()) {
                String field = miscToks.nextToken().trim();
                String fieldArr[] = field.split("=");
                if (fieldArr.length == 2) {
                    atributos.put(fieldArr[0], fieldArr[1]);
                }
            }

        } catch (NoSuchElementException nsee) {
            System.err.println("Error de formato GFF se esperan 9 columnas!");
        }
    }

    public String getLine() {
        return line;
    }

    public String getId() {
        return id;
    }

    public void setAtributoDelim(String atributoDelim) {
        this.atributoDelim = atributoDelim;
    }

    public String getMetodo() {
        return metodo;
    }

    public String getSeq_type() {
        return seq_type;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String getScore() {
        return score;
    }

    public String getStrand() {
        return strand;
    }

    public String getPhase() {
        return phase;
    }

    public Map<String, String> getAtributos() {
        return atributos;
    }

    public String getAtributoDelim() {
        return atributoDelim;
    }

    public void setLine(String line) {
        this.line = line;
    }
    public String getAtrributeValue(String key){
        return atributos.get(key);
    }
}
