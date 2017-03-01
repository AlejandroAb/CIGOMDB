/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alejandro
 */
public class TestFiles {
    public static void main(String args[]){
        testFilesFromFile(args[0]);
    }
    public static void testFilesFromFile(String input) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(input));
            String linea;
            while ((linea = reader.readLine()) != null) {
                String filePath = linea;
                File tmpFile = new File(filePath);
                boolean findPath = false;
                if (tmpFile.exists()) {
                    System.out.print("OK");
                    findPath = true;
                } else {
                    System.out.print("NOK");
                    //le quitamos el último "/"
                    String tmpPath = filePath.substring(0, filePath.length() - 1);
                    int index = tmpPath.lastIndexOf("/");
                    while (!findPath && index > 0) {
                        tmpPath = tmpPath.substring(0, index - 1);
                        tmpFile = new File(tmpPath);
                        if (tmpFile.exists()) {
                            findPath = true;
                        } else {
                            index = tmpPath.lastIndexOf("/");
                        }
                    }
                }
                if (findPath) {
                    for (File f : tmpFile.listFiles()) {
                        if (f.getName().contains("extended")) {
                            System.out.print("\t" + f.getName());
                        }
                    }
                    String metaxaDir = tmpFile.getAbsolutePath();
                    metaxaDir = metaxaDir.endsWith("/") ? metaxaDir + "metaxa" : metaxaDir + "/metaxa";
                    File metaxaFile = new File(metaxaDir);
                    if (metaxaFile.exists()) {
                        System.out.print("\tMETAXA-OK");
                        for (File f : metaxaFile.listFiles()) {
                            if (f.getName().contains("taxonomy")) {
                                System.out.print("\t" + f.getName());
                            }
                        }
                    } else {
                        System.out.print("\tMETAXA-NOK");
                    }
                } else {
                    System.err.println("No existe nada para: " + linea);
                }
                System.out.println();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(TestFiles.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }
}
