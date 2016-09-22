/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bobjects;

/**
 * Clase para representar NOGs  
 * @author Alejandro
 */
public class NOGObj {
    private String id_NOG;
    private String nog_description;

    public NOGObj(String id_NOG) {
        this.id_NOG = id_NOG;
    }

    public String getId_NOG() {
        return id_NOG;
    }

    public void setId_NOG(String id_NOG) {
        this.id_NOG = id_NOG;
    }

    public String getNog_description() {
        return nog_description;
    }

    public void setNog_description(String nog_description) {
        this.nog_description = nog_description;
    }
    
}
