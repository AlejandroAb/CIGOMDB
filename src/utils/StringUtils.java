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

    public String scapeSQL(String text) {
        String scapedText = text.replace("\\", "\\\\").replace("'", "\\'");
        return scapedText;

    }

    /**
     * Esyte método obtiene el reverso complementario de una secuencia. Si se
     * ingresa GGTCAT se obtiene ATGACC
     *
     * @param cadena cadena a invertir
     * @return inverso complementario de la cadena
     */
    public String reversoComplementarioNuc(String cadena) {
        String invertida = "";
        if (cadena != null) {
            for (int i = cadena.length() - 1; i >= 0; i--) {
                char base = cadena.charAt(i);
                if (base == 'A') {
                    base = 'T';
                } else if (base == 'T') {
                    base = 'A';
                } else if (base == 'C') {
                    base = 'G';
                } else if (base == 'G') {
                    base = 'C';
                } else if (base == 'N') {
                    base = 'N';
                } else {
                    System.err.println("Caracter No Esperado. SUtils.reversoComplementarioNuc: " + base + "\nSecuencia: " + cadena);
                }
                invertida += base;
            }
        }
        return invertida;
    }

}
