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
public class COGObj {
    private String idCOG;//COG4862	KTN	
    private String cog_description;//Negative regulator of genetic competence, sporulation and motility
    private String cog_fun;//KTN
    private String cog_n_fun[];
    public COGObj(String idCOG) {
        this.idCOG = idCOG;
    }

    
    public String getIdCOG() {
        return idCOG;
    }

    public void setIdCOG(String idCOG) {
        this.idCOG = idCOG;
    }

    public String getCog_description() {
        return cog_description;
    }

    public void setCog_description(String cog_description) {
        this.cog_description = cog_description;
    }

    public String getCog_fun() {
        return cog_fun;
    }

    public void setCog_fun(String cog_fun) {
        this.cog_fun = cog_fun;
        this.cog_n_fun = new String[cog_fun.length()];
        int i=0;
        for(char fun : cog_fun.toCharArray()){
            cog_n_fun[i] = ""+fun;
            i++;
        }
    }

    public String[] getCog_n_fun() {
        return cog_n_fun;
    }
    
    
    
}
