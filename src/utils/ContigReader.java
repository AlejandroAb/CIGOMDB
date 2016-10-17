/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alejandro
 */
public class ContigReader {

    private String fileName; // path complleto al archivo con la secuencia
    Map<String, Integer> mapa;

    public ContigReader(String fileName) {
        this.fileName = fileName;
        mapa = new HashMap<>();
    }

    public boolean processContigFile() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            String linea;
            int linNum = 0;
            List<String> lines = Files.readAllLines(Paths.get("file.txt"), StandardCharsets.US_ASCII);
            
           String line32 = Files.readAllLines(Paths.get("file.txt"), StandardCharsets.US_ASCII).get(32);
          // String line33 = (String) FileUtils.readLines(file).get(31);
            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith(">")) {

                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ContigReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ContigReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
}
