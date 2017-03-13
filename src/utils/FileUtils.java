/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Alejandro
 */
public class FileUtils {

    /**
     * Calcula MD5checksum para un archivo
     *
     * @param inFile
     * @return
     */
    public static String getMD5File(String inFile) {
        try {
            byte[] b = Files.readAllBytes(Paths.get(inFile));
            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
            String md5 = DatatypeConverter.printHexBinary(hash);
            return md5;
        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error de IO archivo: " + inFile);
            return "";
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error de Algoritmo: MD5 " + inFile);
            return "";
        }

    }
     public boolean validateFile(String fullPath, boolean create) {
        File file = new File(fullPath);
        if (create && !file.exists()) {
            file.setWritable(true);
            return file.mkdir();
        } else {
            return file.exists();
        }
    }

}
