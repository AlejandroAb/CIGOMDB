/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

/**
 *
 * @author Alejandro
 */
public class StringUtils {

    public StringUtils() {
    }
    public String scapeSQL(String text){
        String scapedText = text.replace("\\", "\\\\").replace("'", "\\'");
        return scapedText;
        
    }
    
}
