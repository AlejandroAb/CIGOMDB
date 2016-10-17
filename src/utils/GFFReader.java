/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author Alejandro
 */
public class GFFReader extends BufferedReader{

    public GFFReader(Reader in) {
        super(in);
    }

    public GFFReader(Reader in, int sz) {
        super(in, sz);
    }
    
    
    public GFFLine readGffLine() throws IOException{
        String l = readLine();
        //no lee los comentarios
        while(l != null && (l.trim().startsWith("#") || l.trim().length()<2)){
            l = readLine();
        }
        if(l == null){
            return null;
        }        
        GFFLine line = new GFFLine(l);
        return line;
        
    }
    
}
