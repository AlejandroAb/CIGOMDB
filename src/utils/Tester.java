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
public class Tester {

    public static void main(String args[]) {
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
