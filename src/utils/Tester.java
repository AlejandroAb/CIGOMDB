/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import bobjects.Muestreo;
import cigomdb.GeneAnnotationLoader;
import database.Transacciones;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Alejandro
 */
public class Tester {

    public static void main(String args[]) {        
        String reg_YYYYMMDD = "^\\d\\d\\d\\d(\\/|-|\\.)(0?[1-9]|1[0-2])(\\/|-|\\.)(0?[1-9]|[12][0-9]|3[01])(\\s*.*)$";
        String reg_YYYYMMDD2 = "^\\d\\d\\d\\d(\\/|-|\\.)(0?[0-9]|1[0-2])(\\/|-|\\.)(0?[0-9]|[12][0-9]|3[01])(\\s*.*)$";
        if ("2016-09-20T00:00".matches(reg_YYYYMMDD)) {
            System.out.println("1");
        }
          if ("0000-00-00T00:00".matches(reg_YYYYMMDD2)) {
            System.out.println("2");
        }
        System.exit(0);
        Transacciones transacciones = new Transacciones("cigomdb", "root", "localhost", "amorphis");
        String testBlastLine = "Y898_MYCBO^Y898_MYCBO^Q:147-362,H:166-381^26.73%ID^E:4e-16^RecName: Full=Uncharacterized protein Mb0898c;^Bacteria; Actinobacteria; Actinobacteridae; Actinomycetales; Corynebacterineae; Mycobacteriaceae; Mycobacterium; Mycobacterium tuberculosis complex";
        String testPfamLine = "PF04060.8^FeS^Putative Fe-S cluster^16-49^E:8.1e-16`PF14697.1^Fer4_21^4Fe-4S dicluster domain^81-134^E:8.5e-14`PF00037.22^Fer4^4Fe-4S binding domain^82-103^E:5.6e-06`PF13237.1^Fer4_10^4Fe-4S dicluster domain^82-129^E:1.4e-06`PF12798.2^Fer4_3^4Fe-4S binding domain^88-102^E:0.013";
        String testUniprot = "A0A0F6NZX8		Putative replicating factor	10493					";
        String uni[] = testUniprot.split("\t", -1);
        int i = 0;
        try {
            byte[] b = Files.readAllBytes(Paths.get("C:\\Users\\Alejandro\\Documents\\hola.txt"));
            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
            String actual = DatatypeConverter.printHexBinary(hash);
            System.out.println(actual);
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (String u : uni) {
            System.out.println("Tok " + i + ": " + u);
            i++;
        }
        String text[] = {"Usuario (Acciones)", "Usuario(Acciones)", "Nombre Apellido {Acciones}", "Nombre[Acciones]", "Nombre Apellido[Acciones]"};
        String rexEx = "[\\(\\)\\[\\]\\{\\}]";
        for (String toTest : text) {
            i = 0;
            for (String sp : toTest.split(rexEx)) {
                System.out.println(toTest + " termino " + i + " = " + sp);
                i++;
            }
        }
        System.out.println("muestreoss");
        ArrayList<Muestreo> muestreos = new ArrayList<Muestreo>();
        muestreos.add(new Muestreo(1));
        muestreos.add(new Muestreo(2));
        muestreos.add(new Muestreo(3));
        for (Muestreo muestreo : muestreos) {
            System.out.println("" + muestreo.getIdMuestreo());
            muestreo.setIdMuestreo(100);
        }
        for (Muestreo muestreo : muestreos) {
            System.out.println("" + muestreo.getIdMuestreo());
            //muestreo.setIdMuestreo(100);
        }

        //    GeneAnnotationLoader ga = new GeneAnnotationLoader(transacciones);
        //  ga.procesaLineaBlastTrinotate(testBlastLine, "", "");
        //  ga.procesaLineaPfamTrinotate(testPfamLine, testPfamLine);
        //http://stackoverflow.com/questions/15491894/regex-to-validate-date-format-dd-mm-yyyy
        String dataExp = "^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[1,3-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$";
        String YYYYMMDD = "^\\d\\d\\d\\d(\\/|-|\\.)(0?[1-9]|1[0-2])(\\/|-|\\.)(0?[1-9]|[12][0-9]|3[01])(\\s*.*)$";
        String DDMMYYYY = "^(0?[1-9]|[12][0-9]|3[01])(\\/|-|\\.)(0?[1-9]|1[0-2])(\\/|-|\\.)\\d\\d\\d\\d(\\s*.*)$";
        String time = "(00|[0-9]|1[0-9]|2[0-3]):([0-9]|[0-5][0-9]):([0-9]|[0-5][0-9])";
        String dateTimeExp = "^\\d\\d\\d\\d/(0?[1-9]|1[0-2])/(0?[1-9]|[12][0-9]|3[01]) (00|[0-9]|1[0-9]|2[0-3]):([0-9]|[0-5][0-9]):([0-9]|[0-5][0-9])$";
        //http://www.regexpal.com/1966String date = "03/09/2016";
        String date1 = "2016/09/08 18:42";
        String date2 = "2016-09-08";
        String date3 = "2016-15-20";
        String date4 = "2016/10/45";
        String date5 = "1/19/2016";
        String date6 = "01/09/2016";
        String date7 = "40/09/2016";
        String date8 = "09/40/2016";
        String date9 = "01.02.2016 LO QUE SEA";
        //String 
        System.out.println("Dates validation");
        System.out.println(date1.matches(YYYYMMDD));
        System.out.println(date2.matches(YYYYMMDD));
        System.out.println(date3.matches(YYYYMMDD));
        System.out.println(date4.matches(DDMMYYYY));
        System.out.println(date5.matches(DDMMYYYY));
        System.out.println(date6.matches(DDMMYYYY));
        System.out.println(date7.matches(DDMMYYYY));
        System.out.println(date8.matches(DDMMYYYY));
        System.out.println(date9.matches(DDMMYYYY));
        System.out.println("END Dates validation");

        String alphabet_nuc = "[ACTG]*";
        String alphabet_prot = "[ACDEFGHIKLMNPQRSTVWYBZX]*";
        String seq = "AAAAAAAAAACCCCCCCCCCGGGGGGGGGGTTTTTTTTTT";
        //012345678901234567890123
        String seqP = "ADEKKLQPPACCXZBYAHGTTQRSTXZAYZXAGTC";
        String seqNok = "FSDNFSDNUBFDSFHKFSDFPPLSMSNBDGVCXTYUQQSDGVCXD";
        System.out.println(seq.matches(alphabet_nuc));
        System.out.println(seq.matches(alphabet_prot));
        System.out.println(seqP.matches(alphabet_nuc));
        System.out.println(seqP.matches(alphabet_prot));
        System.out.println(seqNok.matches(alphabet_nuc));
        System.out.println(seqNok.matches(alphabet_prot));
        int f1 = 1;
        int t1 = 10;
        int f2 = 15;
        int t2 = 25;

        System.out.println("Inter1: " + seqP.substring(0, (f1 - 1)));
        System.out.println("gen1: " + seqP.substring(f1 - 1, t1));
        System.out.println("Inter2: " + seqP.substring(t1, f2 - 1));
        System.out.println("gen2: " + seqP.substring(f2 - 1, t2));
        System.out.println("Inter2_2: " + seqP.substring(t2, seqP.length()));
    }
}
