/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bobjects;

/**
 *
 * @author Alejandro
 */
public class SwissProtObj {

    private String uniprotID = "";
    private String uniprotACC = "";
    private String uniprotName = "";
    private int seqLength = 0;
    private String sequence = "";
    private String taxID = "";//Taxon is the scientific name of the lowest common taxon shared by all UniRef cluster members.
    private String clusterId = "";
    private String clusterName = "";
    private String clusterTax = "";

    public SwissProtObj() {
    }

    public SwissProtObj(String uniprotID) {
        this.uniprotID = uniprotID;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterTax() {
        return clusterTax;
    }

    public void setClusterTax(String clusterTax) {
        this.clusterTax = clusterTax;
    }

    public String getUniprotID() {
        return uniprotID;
    }

    public void setUniprotID(String uniprotID) {
        this.uniprotID = uniprotID;
    }

    public String getUniprotACC() {
        return uniprotACC;
    }

    public void setUniprotACC(String uniprotACC) {
        this.uniprotACC = uniprotACC;
    }

    public String getUniprotName() {
        return uniprotName;
    }

    public void setUniprotName(String uniprotName) {
        this.uniprotName = uniprotName;
    }

    public int getSeqLength() {
        return seqLength;
    }

    public void setSeqLength(int seqLength) {
        this.seqLength = seqLength;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getTaxID() {
        return taxID;
    }

    public void setTaxID(String taxID) {
        this.taxID = taxID;
    }

    /**
     * Método para representar un objeto swiss prot en sql vaalores esperados
     * (`uniprot_id`, `uniprot_acc`, `ncbi_tax_id`, `prot_name`, `prot_seq`,
     * `prot_length`)
     *
     * @param tblName
     * @return
     */
    public String toSQLString(String tblName) {
        String sqlString = "INSERT INTO " + tblName + " VALUES ('"
                + this.uniprotID + "', '"
                + this.uniprotACC + "', '"
                + this.taxID + "', '"
                + this.uniprotName + "', '"
                + this.sequence + "', "
                + this.seqLength + ", '"
                + this.clusterId + "', '"
                + this.clusterName + "', '"
                + this.clusterTax + "');\n";
        return sqlString;
    }

    /**
     * (`uniprot_id`, `uniprot_acc`, `ncbi_tax_id`, `prot_name`, `prot_seq`,
     * `prot_length`)
     *
     * @param dbName
     * @param tblName
     * @return
     */
    public String toMongoString(String dbName, String tblName) {
        String mongoStr = dbName + "." + tblName + ".insert({"
                + "\"uniprot_id\" : \"" + this.uniprotID + "\", "
                + "\"uniprot_acc\" : \"" + this.uniprotACC + "\", "
                + "\"ncbi_tax_id\" : \"" + this.taxID + "\", "
                + "\"prot_name\" : \"" + this.uniprotName + "\", "
                + "\"prot_seq\" : \"" + this.sequence + "\", "
                + "\"prot_length\" : " + this.seqLength + "})\n";
        return mongoStr;
    }
}
