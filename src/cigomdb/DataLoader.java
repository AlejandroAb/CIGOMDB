/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import database.Transacciones;
import java.util.ArrayList;

/**
 *
 * @author Alejandro
 */
public class DataLoader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //args for derrotero: derrotero -i "C://Users//Alejandro//Documents//Projects//pemex//6 cruceros//MGMF I//derrotero.txt" -campania 2 
        //args para muestreo agua MMF 01: -i C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\6 cruceros\\MGMF I\\Tablas Datos\\Muestreo_Agua.txt
        //-o C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\6 cruceros\\MGMF I\\Tablas Datos\\Muestreo_Agua.sql
        //parte de args para swissprot: -i "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\4 db\\swissprot\\uniref100.5k.xml";
        String database = "cigomdb";
        String user = "root";
        String host = "localhost";
        String password = "amorphis";
        String input = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\6 cruceros\\MGMF I\\Tablas Datos\\Muestreo_Sedimento.txt";
        String output = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\6 cruceros\\MGMF I\\Tablas Datos\\Muestreo_Sedimento.sql";
        String mapPrefix = "gen_id_";
        String idPrefix = "M1SE3";
        String gffIn = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\8 Metagenomas\\results_func\\genes_prediction\\metagolfos_FGS.gff";
        String ncIn = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\8 Metagenomas\\results_func\\genes_prediction\\metagolfos_FGS.ffn";
        String aaIn = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\8 Metagenomas\\results_func\\genes_prediction\\metagolfos_FGS.faa";
        String names = "C:\\Users\\Alejandro\\Documents\\Projects\\taxonomydb\\taxdmp\\names.dmp";
        String nodes = "C:\\Users\\Alejandro\\Documents\\Projects\\taxonomydb\\taxdmp\\nodes.dmp";
        int campania = -1;
        String delimiter = "\t";
        boolean debug = false;
        ArrayList<String> modes = new ArrayList<String>();
        modes.add("swiss");
        modes.add("genes");
        modes.add("derrotero");
        modes.add("ncbitax");
        modes.add("muestreo");
        String mode = "";
        for (int i = 0; i < args.length; i++) {
            if (i == 0 && (!args[i].equals("-h") && !args[i].equals("-help"))) {
                mode = args[i];
                if (!modes.contains(mode)) {
                    System.out.println("Opcion no valida\n\n");
                    printHelp();
                    System.exit(1);
                }
            }
            if (args[i].equals("-i")) {
                try {
                    input = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion i - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-o")) {
                try {
                    output = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion o - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-db")) {
                try {
                    database = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion db - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-u")) {
                try {
                    user = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion user - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-host")) {
                try {
                    host = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion host - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-pass")) {
                try {
                    password = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion pass - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-d")) {
                debug = true;
            } else if (args[i].equals("-h") || args[i].equals("-help")) {
                printHelp();
                System.exit(1);
            } else if (args[i].equals("-idpre")) {

                try {
                    idPrefix = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion idpre - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-gff")) {

                try {
                    gffIn = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion gff - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-nc")) {
                try {
                    ncIn = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion nc - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-aa")) {
                try {
                    aaIn = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion aa - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-mapre")) {
                try {
                    mapPrefix = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion mapre - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-campania")) {
                try {
                    campania = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion campania - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                } catch (NumberFormatException nfe) {
                    System.out.println("Opcion campania - Se esperaba un argumento numerico\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-sep")) {
                try {
                    delimiter = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion -sep - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-nodes")) {
                try {
                    nodes = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion nodes - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-names")) {
                try {
                    names = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion names - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            }
        }
        Transacciones transacciones = new Transacciones(database, user, host, password);
        transacciones.setDebug(debug);
        if (transacciones.testConnection()) {
            String log = "";
            long start = System.currentTimeMillis();
            if (mode.equals("swiss")) {
                SwissProt swiss = new SwissProt(transacciones);
                log = swiss.loadSwissProtFromXML(input, debug);
                System.out.println("END: " + log);
            } else if (mode.equals("genes")) {
                if (gffIn.length() > 0 && (aaIn.length() + ncIn.length()) > 0) {
                    GeneFuncLoader loader = new GeneFuncLoader(transacciones);
                    log += loader.loadFragileScanFiles(idPrefix, gffIn, ncIn, aaIn, mapPrefix);
                } else {
                    System.out.println("Para correr el programa gen se espera minimo un archivo gff y un archivo fasta");
                    printHelp();
                    System.exit(1);
                }
            } else if (mode.equals("derrotero")) {
                DerroteroLoader derrotero = new DerroteroLoader(transacciones);
                derrotero.parseMatrizDerrotero(campania, input, delimiter);
            } else if (mode.equals("ncbitax")) {
                NCBITaxCreator ncbi = new NCBITaxCreator(transacciones);
                log += ncbi.createTaxaListFromNCBI(nodes, names);
                //System.out.println(log);
            } else if (mode.equals("muestreo")) {
                int idMuestra = transacciones.getMaxIDMuestra();
                int idMuestreo = transacciones.getMaxIDMuestreo();
                EventProcesor eventMuestreo = new EventProcesor(transacciones);
                eventMuestreo.setNextIDMuestreo(idMuestreo);
                eventMuestreo.setNextIDMuestra(idMuestra);
                log += eventMuestreo.parseFileMMFI_Muestreo(input, output, true, delimiter, campania);
            }
            long end = System.currentTimeMillis() - start;
            System.out.println(end / 1000 + " s.");
            System.out.println("\nLog and Messages\n" + log);
        } else {
            System.out.println("No hay conexion con la BD.\nAsegurese de introducir de manera correcta los parametros de conexion.\nDataLoader -h para mas ayuda.");
        }
    }

    private static void printHelp() {
        System.out.println("*********CIGOM DATABASE LOADER***************");
        System.out.println("**@author:  Alejandro Abdala              **");
        System.out.println("********************************************");
        System.out.println("uso:DataLoader <mode> <opt>");
        System.out.println("#modes:");
        System.out.println("\tswiss.\tCarga la bd de swissprot a partir del xml");
        System.out.println("\t\tEl único parámetro que toma este programa es -i. Input file con XML swissprot");
        System.out.println("\tgen.\tCarga a la bd resultados de prediccion de genes");
        System.out.println("\n\t\tEsta opción necesita los siguientes parametros: ");
        System.out.println("\n\t\t\t -idpre\t Prefijo (+ numero consecutivo) para el ID que tendrá cada gen predicho.\n\t\t\t\t Se espera que cumpla con alguna nomencaltura"
                + "\n\t\t\t -gff\t Archivo gff que tiene las coordenadas de cada gen en el archivo de contigs "
                + "\n\t\t\t -nc\t Archivo fasta con los genes predichos - secuencias de nucleótidos"
                + "\n\t\t\t -aa\t Archivo fasta con las proteínas predichas - secuencias de aminoácidos"
                + "\n\t\t\t -mapre\t Prefijo para mapear futras predicciones. Default = gene_id_#");
        System.out.println("\tderrotero.\tCarga archivos tabulares de derrotero. "
                + "\n\t\tSe espera que el archivo tenga en el header por lo menos los siguientes campos:\n\t\t"
                + "ESTacion,LATitud, LONgitud,fecha PLANneada,	fecha EJECutada,COMentarios,TIPO de muestra.\n\t\t"
                + "Para que el archivo pueda ser procesado de manera correcta la primera linea tiene que ser el header del archivo"
                + " y los campos deben de tener por lo menos las letras en mayusculas para poder ser reconocidos de manera correcta.\n\t\t"
                + "Las opciones con las que trabaja son i, campania y sep");
        System.out.println("\tncbitax.\tCrea la base de datos NCBI desde cero\n\t\t Necesita los parametors names y nodes, los cuales son archivos obtenidos de ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/");
        System.out.println("\n--------------------------------------------------");
        System.out.println("\n             --------Options--------");
        System.out.println("-d\t debug = true");
        System.out.println("-db\t database");
        System.out.println("-h\t help menu");
        System.out.println("-host\t db host");
        System.out.println("-i\t set inputfile");
        System.out.println("-pass\t db password");
        System.out.println("-u\t db user");
        System.out.println("-campania\t camapania por si se utiliza el derrotero");
        System.out.println("-sep\t separador para formatear archivos. Def: tab(\\t)");
        System.out.println("-names\t Nombre del archivo con la información de nombres taxonomicos names.dmp de NCBI");
        System.out.println("-nodes\t Nombre del archivo con la información de nodos taxonomicos nodes.dmp de NCBI");
        //-in input file

    }
}
