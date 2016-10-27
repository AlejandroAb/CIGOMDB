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
public class EGGObj {

    private String idEGG;//COG4862	KTN	
    private String description;//Negative regulator of genetic competence, sporulation and motility
    private String fun;//KTN
    private String cog_n_fun[];
    private String prots;
    private String species;
    public EGGObj(String idEGG) {
        this.idEGG = idEGG;
    }

    public String getProts() {
        return prots;
    }

    public void setProts(String prots) {
        this.prots = prots;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

   

    public String getIdEGG() {
        return idEGG;
    }

    public void setIdCOG(String idEGG) {
        this.idEGG = idEGG;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFun() {
        return fun;
    }

    public void setFun(String fun) {
        this.fun = fun;
        this.cog_n_fun = new String[fun.length()];
        int i = 0;
        for (char f : fun.toCharArray()) {
            cog_n_fun[i] = "" + f;
            i++;
        }
    }

    public String[] getCog_n_fun() {
        return cog_n_fun;
    }

}
