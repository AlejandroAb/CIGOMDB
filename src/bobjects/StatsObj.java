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
public class StatsObj {

    private int idStats;
    private String reads = "-1";
    private String bases = "-1";
    private String long_avg = "-1";
    private String gc_prc = "-1";
    private String qc_avg = "-1";
    private String ns_prc = "-1";
    private String q20 = "-1";
    private String q30 = "-1";
    private String combined_prc = "-1";

    public StatsObj(int idStats) {
        this.idStats = idStats;
    }

    public int getIdStats() {
        return idStats;
    }

    public void setIdStats(int idStats) {
        this.idStats = idStats;
    }

    public String getReads() {
        return reads;
    }

    public void setReads(String reads) {
        this.reads = reads;
    }

    public String getBases() {
        return bases;
    }

    public void setBases(String bases) {
        this.bases = bases;
    }

    public String getLong_avg() {
        return long_avg;
    }

    public void setLong_avg(String long_avg) {
        this.long_avg = long_avg;
    }

    public String getGc_prc() {
        return gc_prc;
    }

    public void setGc_prc(String gc_prc) {
        this.gc_prc = gc_prc;
    }

    public String getQc_avg() {
        return qc_avg;
    }

    public void setQc_avg(String qc_avg) {
        this.qc_avg = qc_avg;
    }

    public String getNs_prc() {
        return ns_prc;
    }

    public void setNs_prc(String ns_prc) {
        this.ns_prc = ns_prc;
    }

    public String getQ20() {
        return q20;
    }

    public void setQ20(String q20) {
        this.q20 = q20;
    }

    public String getQ30() {
        return q30;
    }

    public void setQ30(String q30) {
        this.q30 = q30;
    }

    public String getCombined_prc() {
        return combined_prc;
    }

    public void setCombined_prc(String combined_prc) {
        this.combined_prc = combined_prc;
    }

    public String toSQLString() {
        String sql = "INSERT INTO stats(idstats,lecturas,bases,long_avg,gc_prc,qc_avg,ns_prc,q20,q30,combined_prc)"
                + "VALUES(" + this.idStats + "," + this.reads + "," + this.bases + "," + this.long_avg + ","
                + this.gc_prc + "," + this.qc_avg + "," + this.ns_prc + "," + this.q20 + "," + this.q30 + "," 
                + this.combined_prc +");";
        return sql;
    }
}
