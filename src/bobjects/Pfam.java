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
public class Pfam {
    String accession;
    String id="";
    String deffinition="";
    String comments="";
    String idGO="-1";
    String clan_acc="-1";

    public Pfam(String accession) {
        this.accession = accession;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeffinition() {
        return deffinition;
    }

    public void setDeffinition(String deffinition) {
        this.deffinition = deffinition;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getIdGO() {
        return idGO;
    }

    public void setIdGO(String idGO) {
        this.idGO = idGO;
    }

    public String getClan_acc() {
        return clan_acc;
    }

    public void setClan_acc(String clan_acc) {
        this.clan_acc = clan_acc;
    }

   
    
    
}
