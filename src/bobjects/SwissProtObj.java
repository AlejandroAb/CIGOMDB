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
    private String ec = "";
    private String gen_name = "";
    private String pathway = "";
    private String alternative_product = "";
    private String dependencia_temp = "";
    private String dependencia_ph = "";
    public SwissProtObj() {
    }

    public String getEc() {
        return ec;
    }

    public void setEc(String ec) {
        this.ec = ec;
    }

    public String getGen_name() {
        return gen_name;
    }

    public void setGen_name(String gen_name) {
        this.gen_name = gen_name;
    }

    public String getPathway() {
        return pathway;
    }

    public void setPathway(String pathway) {
        this.pathway = pathway;
    }

    public String getAlternative_product() {
        return alternative_product;
    }

    public void setAlternative_product(String alternative_product) {
        this.alternative_product = alternative_product;
    }

    public String getDependencia_temp() {
        return dependencia_temp;
    }

    public void setDependencia_temp(String dependencia_temp) {
        this.dependencia_temp = dependencia_temp;
    }

    public String getDependencia_ph() {
        return dependencia_ph;
    }

    public void setDependencia_ph(String dependencia_ph) {
        this.dependencia_ph = dependencia_ph;
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
    public String toSQLString() {
        String sqlString = "INSERT INTO swiss_prot"
                + "(uniprot_id,uniprot_acc,ncbi_tax_id,prot_name,gene_name,prot_seq,prot_length,"
                + "cluster_id,cluster_name,cluster_ncbi_tax,ec,pathway,alternative_p,temp_dep,ph_dep) "
                + "VALUES ('"
                + this.uniprotID + "', '"
                + this.uniprotACC + "', '"
                + this.taxID + "', '"
                + this.uniprotName + "', '"
                + this.gen_name + "', '"
                + this.sequence + "', "
                + this.seqLength + ", '"
                + this.clusterId + "', '"
                + this.clusterName + "', '"
                + this.clusterTax + "', '"
                + this.ec + "', '"
                + this.pathway + "', '"
                + this.alternative_product + "', '"
                + this.dependencia_temp + "', '"
                + this.dependencia_ph+ "')";
        return sqlString;
    }
 /**
     * Método para representar un objeto swiss prot en sql vaalores esperados
     * (`uniprot_id`, `uniprot_acc`, `ncbi_tax_id`, `prot_name`, `prot_seq`,
     * `prot_length`)
     *
     * @param tblName
     * @return
     */
    public String toSQLUpdateString() {
        String sqlString = "UPDATE swiss_prot SET "                
                + "uniprot_acc = '" +this.uniprotACC + "'"
                + ",ncbi_tax_id = '" +this.taxID + "'"
                + ",prot_name = '" +this.uniprotName + "'"
                + ",gene_name = '" +this.gen_name + "'"
                + ",prot_seq = '" +this.sequence + "'"
                + ",prot_length = " +this.seqLength 
                + ",cluster_id = '" +this.clusterId + "'"
                + ",cluster_name = '" +this.clusterName + "'"
                + ",cluster_ncbi_tax = '" +this.clusterTax + "'"
                + ",ec = '" +this.ec + "'"
                + ",pathway = '" +this.pathway + "'"
                + ",alternative_p = '" +this.alternative_product + "'"
                + ",temp_dep = '" +this.dependencia_temp + "'"
                + ",ph_dep = '" +this.dependencia_ph+ "'"
                + " where uniprot_id = '" +this.uniprotID+"'";
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
