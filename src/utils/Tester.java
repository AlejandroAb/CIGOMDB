/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import cigomdb.GeneAnnotationLoader;
import database.Transacciones;

/**
 *
 * @author Alejandro
 */
public class Tester {

    public static void main(String args[]) {
         Transacciones transacciones = new Transacciones("cigomdb", "root", "localhost", "amorphis");
        String testBlastLine = "Y898_MYCBO^Y898_MYCBO^Q:147-362,H:166-381^26.73%ID^E:4e-16^RecName: Full=Uncharacterized protein Mb0898c;^Bacteria; Actinobacteria; Actinobacteridae; Actinomycetales; Corynebacterineae; Mycobacteriaceae; Mycobacterium; Mycobacterium tuberculosis complex";
        String testPfamLine = "PF04060.8^FeS^Putative Fe-S cluster^16-49^E:8.1e-16`PF14697.1^Fer4_21^4Fe-4S dicluster domain^81-134^E:8.5e-14`PF00037.22^Fer4^4Fe-4S binding domain^82-103^E:5.6e-06`PF13237.1^Fer4_10^4Fe-4S dicluster domain^82-129^E:1.4e-06`PF12798.2^Fer4_3^4Fe-4S binding domain^88-102^E:0.013";
        GeneAnnotationLoader ga = new GeneAnnotationLoader(transacciones);
      //  ga.procesaLineaBlastTrinotate(testBlastLine, "", "");
        
        ga.procesaLineaPfamTrinotate(testPfamLine, testPfamLine);
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
        
        System.out.println("Inter1: " + seqP.substring(0, (f1-1)));
        System.out.println("gen1: " + seqP.substring(f1-1, t1));
        System.out.println("Inter2: " + seqP.substring(t1, f2-1));        
        System.out.println("gen2: " + seqP.substring(f2-1, t2));
        System.out.println("Inter2_2: " + seqP.substring(t2, seqP.length()));
    }
}
