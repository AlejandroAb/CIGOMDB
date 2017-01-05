/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import bobjects.Muestra;
import bobjects.Muestreo;
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
        //muestras de sedimento METMFI
        // String input = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\6 cruceros\\MGMF I\\Tablas Datos\\Muestreo_Sedimento.txt";
        //String output = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\6 cruceros\\MGMF I\\Tablas Datos\\Muestreo_Sedimento.sql";
        //muestras SOGOM
        //  String input = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\6 cruceros\\sogom\\Muestras.txt";
        //String output = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\6 cruceros\\sogom\\Muestras.sql";

        String database = "cigomdb";
        String user = "cigomdb";
        String host = "localhost";
        String password = "CigomWeb2016";
        String input = "";
        String output = "";
        String outFileFasta = "";//usado en marcadores
        String outFileMetaxa = "";//usado en marcadores
        String mapPrefix = "gen_id_";
        String idPrefix = "M1SE3";
        String contigIn = "";

        String gffIn = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\8 Metagenomas\\results_func\\genes_prediction\\metagolfos_FGS.gff";
        String ncIn = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\8 Metagenomas\\results_func\\genes_prediction\\metagolfos_FGS.ffn";
        String aaIn = "C:\\Users\\Alejandro\\Documents\\Projects\\pemex\\8 Metagenomas\\results_func\\genes_prediction\\metagolfos_FGS.faa";
        String names = "C:\\Users\\Alejandro\\Documents\\Projects\\taxonomydb\\taxdmp\\names.dmp";
        String nodes = "C:\\Users\\Alejandro\\Documents\\Projects\\taxonomydb\\taxdmp\\nodes.dmp";
        String uri = "";
        String url = "";
        String combined_file = "";
        String nc1_file = "";
        String nc2_file = "";
        String metatax_file = "";
        boolean insertaAmplicones = true;//--no-insert-amplicon
        boolean processOutAmplicones = false; //-poa --process_out_amplicon
        boolean processMetaxaAmplicones = false; //-mamp --metaxa_amp
        String raw_ext = "fastq"; //-rawe
        boolean toFile = false;
        int campania = -1;
        int idGenoma = -1;
        int idMetagenoma = -1;
        int metodo_medida = -1; //-mm
        String marker_meth = "";//para escoger el metodo de processamiento en modo markers 
        String delimiter = "\t";
        boolean debug = false;
        boolean startAtZero = true;
        boolean withHash = true;
        boolean validateCog = false;
        int startAtLine = 0;
        ArrayList<String> modes = new ArrayList<String>();
        modes.add("swiss");
        modes.add("genes");
        modes.add("derrotero");
        modes.add("ncbitax");
        modes.add("muestreo");
        modes.add("muestra");
        modes.add("pfam");
        modes.add("markers");
        modes.add("cog");
        modes.add("nog");
        modes.add("obo");
        modes.add("ensamble");
        modes.add("egg");
        modes.add("trinotate");
        int swissBulk = 20;
        boolean swissBatch = false;
        String mode = "";
        if (args.length < 1) {
            printHelp();
            System.exit(0);
        }
        for (int i = 0; i < args.length; i++) {
            if (i == 0 && (!args[i].equals("-h") && !args[i].equals("--help"))) {
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
                    toFile = true;
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion o - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-outseq")) {
                try {
                    outFileFasta = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion -outseq - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-outmetaxa")) {
                try {
                    outFileMetaxa = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion -outmetaxa - Se esperaba un argumento\n\n");
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
            } else if (args[i].equals("-h") || args[i].equals("--help")) {
                printHelp();
                System.exit(1);
            } else if (args[i].equals("-poa") || args[i].equals("--process_out_amplicon")) {
                processOutAmplicones = true;
            } else if (args[i].equals("-mamp") || args[i].equals("--metaxa_amp")) {
                processMetaxaAmplicones = true;
            } else if (args[i].equals("--no-insert-amplicon")) {
                insertaAmplicones = false;

            } else if (args[i].equals("--swiss-batch")) {
                swissBatch = true;

            } else if (args[i].equals("--start-at-one") || args[i].equals("-sao")) {
                startAtZero = false;

            } else if (args[i].equals("--no-hash")) {
                withHash = false;

            } else if (args[i].equals("-vc")) {//validate cog
                validateCog = true;

            } else if (args[i].equals("-rawe")) {

                try {
                    raw_ext = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion rawe - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
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
            } else if (args[i].equals("-metatax")) {
                try {
                    metatax_file = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion metatax - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-combined")) {
                try {
                    combined_file = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion combined - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-nc1")) {
                try {
                    nc1_file = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion nc1 - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-nc2")) {
                try {
                    nc2_file = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion nc2 - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-uri")) {

                try {
                    uri = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion uri - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-url")) {

                try {
                    url = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion url - Se esperaba un argumento\n\n");
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
            } else if (args[i].equals("-contig")) {
                try {
                    contigIn = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion contig - Se esperaba un argumento\n\n");
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
            } else if (args[i].equals("-line")) {
                try {
                    startAtLine = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion line - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                } catch (NumberFormatException nfe) {
                    System.out.println("Opcion line - Se esperaba un argumento numerico\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-bulk")) {
                try {
                    swissBulk = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion bulk - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                } catch (NumberFormatException nfe) {
                    System.out.println("Opcion bulk - Se esperaba un argumento numerico\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-idgenoma")) {
                try {
                    idGenoma = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion idgenoma - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                } catch (NumberFormatException nfe) {
                    System.out.println("Opcion idgenoma - Se esperaba un argumento numerico\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-idmetagenoma")) {
                try {
                    idMetagenoma = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion idmetagenoma - Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                } catch (NumberFormatException nfe) {
                    System.out.println("Opcion idmetagenoma - Se esperaba un argumento numerico\n\n");
                    printHelp();
                    System.exit(1);
                }
            } else if (args[i].equals("-mm")) {
                try {
                    metodo_medida = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion metodo de medidad - mm. Se esperaba un argumento\n\n");
                    printHelp();
                    System.exit(1);
                } catch (NumberFormatException nfe) {
                    System.out.println("Opcion  metodo de medidad - mm. Se esperaba un argumento numerico\n\n");
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
            } else if (args[i].equals("-marker_meth")) {
                try {
                    marker_meth = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    System.out.println("Opcion marker_meth - Se esperaba un argumento\n\n");
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
                if (swissBatch) {
                    swiss.loadSwissProtFromWEBBulk(debug, swissBulk);
                } else {

                    //log = swiss.loadSwissProtFromXML(input, debug);
                    log = swiss.loadSwissProtFromWEB(debug);
                    System.out.println("END:\n" + log);
                }
            } else if (mode.equals("genes")) {
                if (gffIn.length() > 0 && (aaIn.length() + ncIn.length()) > 0) {
                    GeneFuncLoader loader = new GeneFuncLoader(transacciones);
                    log += loader.loadFragileScanFiles(idPrefix, gffIn, ncIn, aaIn, mapPrefix);
                } else {
                    System.out.println("Para correr el programa gen se espera minimo un archivo gff y un archivo fasta");
                    printHelp();
                    System.exit(1);
                }
            } else if (mode.equals("ensamble")) {
                if (gffIn.length() > 0 && (aaIn.length() + ncIn.length()) > 0) {
                    if (idMetagenoma + idGenoma == -1) {
                        System.out.println("Para correr el programa ensamble se espera minimo un id de genoma o id de metagenoma");
                        printHelp();
                        System.exit(1);
                    }
                    GeneFuncLoader loader = new GeneFuncLoader(transacciones);
                    //String idPrefix, int idMetageno, int idGenoma, String gffFile, String contigFile, String nucFile, String protFile, String mapPrefix
                    loader.setDebug(debug);
                    loader.parseEnsamble(idPrefix, idMetagenoma, idGenoma, gffIn, contigIn, ncIn, aaIn, mapPrefix, startAtZero, startAtLine, withHash, toFile, output);
                } else {
                    System.out.println("Para correr el programa ensamble se espera minimo un archivo gff y un archivo fasta");
                    printHelp();
                    System.exit(1);
                }
            } else if (mode.equals("trinotate")) {
                String group = "";
                String id = "";
                if (idMetagenoma != -1) {
                    group = "metagenoma";
                    id = "" + idMetagenoma;
                } else if (idGenoma != -1) {
                    group = "genoma";
                    id = "" + idGenoma;
                } else {
                    System.out.println("Para correr el programa trinotate se espera minimo un id de genoma o id de metagenoma");
                    printHelp();
                    System.exit(1);
                }
                GeneAnnotationLoader loader = new GeneAnnotationLoader(transacciones);
                //String idPrefix, int idMetageno, int idGenoma, String gffFile, String contigFile, String nucFile, String protFile, String mapPrefix
                loader.splitTrinotateFile(input, group, id);

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
                //       log += eventMuestreo.parseFileMMFI_Muestreo(input, output, true, delimiter, campania);
                if (campania == -1) {
                    System.out.println("Para correr el programa muestreo se espera un id de campaña");
                    //printHelp();
                    System.exit(1);
                }
                if (metodo_medida == -1) {
                    System.out.println("Para correr el programa muestreo se espera un metodo de medidad");
                    //printHelp();
                    System.exit(1);
                }
                ArrayList<Muestreo> muestreos = eventMuestreo.parseFileColectas(input, output, toFile, delimiter, campania, metodo_medida, false, false);
                if (muestreos != null) {
                    System.out.println("Procesamiento de colectas");
                    for (Muestreo muestreo : muestreos) {
                        System.out.println(muestreo.getIdMuestreo() + " - " + muestreo.getEtiqueta() + " - " + muestreo.isOk() + " " + muestreo.getError());
                    }
                } else {
                    System.out.println("Error al obtener id de muestreo/colecta");
                }
            } else if (mode.equals("muestra")) {
                int idMuestra = transacciones.getMaxIDMuestra();
                //  int idMuestreo = transacciones.getMaxIDMuestreo();
                EventProcesor eventMuestreo = new EventProcesor(transacciones);
                eventMuestreo.setNextIDMuestra(idMuestra);
                //       log += eventMuestreo.parseFileMMFI_Muestreo(input, output, true, delimiter, campania);
          /*      if (campania == -1) {
                 System.out.println("Para correr el programa muestreo se espera un id de campaña");
                 //printHelp();
                 System.exit(1);
                 }
                 if (metodo_medida == -1) {
                 System.out.println("Para correr el programa muestreo se espera un metodo de medidad");
                 //printHelp();
                 System.exit(1);
                 }*/
                ArrayList<Muestra> muestras = eventMuestreo.parseFileMuestras(input, output, toFile, delimiter);
                if (muestras != null) {
                    System.out.println("Procesamiento de muestras");
                    for (Muestra muestra : muestras) {
                        System.out.println(muestra.getIdMuestra() + " - " + muestra.getEtiqueta() + " - " + muestra.isOk() + " " + muestra.getError());
                    }
                } else {
                    System.out.println("Error al obtener id de muestreo/colecta");
                }
            } else if (mode.equals("pfam")) {
                PfamProcesor pProcessor = new PfamProcesor(transacciones);
                log += pProcessor.parsePfamAClans(input, true, output);
            } else if (mode.equals("cog")) {
                COGProcessor cProcessor = new COGProcessor(transacciones);
                log += cProcessor.parseCOGNames(input, true, output, delimiter);
            } else if (mode.equals("nog")) {
                COGProcessor cProcessor = new COGProcessor(transacciones);
                log += cProcessor.parseNOGNames(input, true, output);
            } else if (mode.equals("egg")) {
                COGProcessor cProcessor = new COGProcessor(transacciones);
                cProcessor.eggNogAnnotationParser(input, validateCog);
            } else if (mode.equals("obo")) {
                if (uri.length() > 0) {
                    OBOProcessor obo = new OBOProcessor(transacciones);
                    log += obo.processOBOFile(input, uri, url, toFile, output);
                } else {
                    System.out.println("Para correr el programa obo se espera el parámetro URI (go|envo|obi...) o culquier OBO ");
                    printHelp();
                    System.exit(1);
                }
            } else if (mode.equals("markers")) {
                if (marker_meth.length() > 0) {

                    MarkerLoader loader = new MarkerLoader(transacciones);
                    if (combined_file.length() > 2) {
                        loader.setProc_combined_file(combined_file);
                    }
                    if (nc1_file.length() > 2) {
                        loader.setProc_nc1_file(nc1_file);
                    }
                    if (nc2_file.length() > 2) {
                        loader.setProc_nc2_file(nc2_file);
                    }
                    if (metatax_file.length() > 2) {
                        loader.setProc_metaxa_file(metatax_file);
                    }
                    if (marker_meth.equals("mv1")) {
                        log += loader.parseMarkerFileFormatI(input, insertaAmplicones, processOutAmplicones, processMetaxaAmplicones, raw_ext, output, outFileFasta, outFileMetaxa);
                    } else if (marker_meth.equals("mv2")) {
                        log += loader.parseMarkerFileFormatIPacbio(input, insertaAmplicones, processOutAmplicones, processMetaxaAmplicones, raw_ext);
                    } else {
                        System.out.println("Para correr el programa gen se espera el parámetro -marker_meth: <mv1>");
                    }
                } else {
                    System.out.println("Para correr el programa gen se espera el parámetro marker_meth (mv1)");
                    printHelp();
                    System.exit(1);
                }
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
                + "\n\t\ty los campos deben de tener por lo menos las letras en mayusculas para poder ser reconocidos de manera correcta.\n\t\t"
                + "Las opciones con las que trabaja son i, campania y sep");
        System.out.println("\tncbitax.\tCrea la base de datos NCBI desde cero\n\t\t Necesita los parametors names y nodes, los cuales son archivos obtenidos de ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/");
        System.out.println("\tcog\t Carga archivos de COG como:\n\t\t ftp://ftp.ncbi.nih.gov/pub/COG/COG2014/data/cognames2003-2014.tab o ftp://ftp.ncbi.nih.gov/pub/wolf/COGs/COG0303/cog.csv\n\t\t Params: input (-i) output (-o) y delimiter (-sep)");
        System.out.println("\tnog\t Carga archivos de NOG como:\n\t\t http://eggnog.embl.de/version_3.0/downloads.html -> NOG.descriptions.txt Params: input (-i) output (-o)");
        System.out.println("\tpfam\t Carga archivos de PFAM como ftp://ftp.ebi.ac.uk/pub/databases/Pfam/releases/Pfam30.0/Pfam-A.clans.tsv.gz\n\t\tParams: inout y output ");
        System.out.println("\tobo\t Carga archivos en formato OBO como ftp://ftp.geneontology.org/pub/go/www/GO.format.obo-1_4.shtml \n\t\t params: -i -o -uri(mandatory) -url");
        System.out.println("\tmarkers \tSe encarga de cargar secuencias de marcadores por muestra. tiene que entregarse el parametro marker_meth");
        System.out.println("\tmuestreo \tSe encarga de anotar archivos con datos de muestreos. Utiliza los siguientes parámetros:"
                + "\n\t\t\t-i\tArchivo de entrada con muestreos\n\t\t\t-o\tArchivo de salida\n\t\t\t-campana\tCamapaña a la cual se relacionan las muestras"
                + "\n\t\t\t-mm\tEl método de medida usado para variables fisico químicas. Valores:\n\t\t\t\t1 = Medido con CTD.\n\t\t\t\t1 = Medido con PCTester TM 35.");
        System.out.println("\tensamble \tSe encarga de cargar secuencias de de ensambles de genes. Utiliza los siguientes parámetros:"
                + "\n\t\t\t-contig\tArchivo de contigs\n\t\t\t-gff\tArchivo con las coordenadas\n\t\t\t-nc\tArchivo de nucleotidos\n\t\t\t-aa\tArchivo de proteinas"
                + "\n\t\t\t-idpre\tPrefijo para el id de los genes\n\t\t\t-mapre\tPrefijo para mapeo a la anotación funcional\n\t\t\t---start-at-one | -sao\tTrue por defecto. False empieza la numeracion del mapeo en cero y no en uno\n\t\t\t-idgenoma\tID del genoma al cual pertenecen los genes predichos\n\t\t\t-idmetagenoma\tID del metagenomma al cual pertenecen los genes predichos."
                + "\n\t\t\t-line\tLinea a patir de la cual empieza a procesar el gff\n\t\t\t--no-hash\tPor default los aechivos de contigs, nuc y prots se cargan en memoria en lugar de iterarlos para las búsquedas. Usar esta bandera para no usar hash");
        System.out.println("\trinotate  \tPrograma que se encarga de parsear archivos de anotacion de Trinotate. Es importante que el gen_map_id de la BD cuadre con el gen_id del archivo de entrada. Este programa utiliza:"
                + "\n\t\t\trinotate <-i -idmetagnoma|-idgenoma>\n\t\t\t-input\tArchivo de trinotate\n\t\t\t-idmetagenoma\tID del metagenoma para el cual se hace la anotación\n\t\t\t-idgenoma\tID del genoma para el cual se hace la anotación");
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
        System.out.println("-marker_meth\t Metodo para cargar archivos existentes de algún tipo de corrida de amplicones / marcadores \n\tMetodos:\tsogom|met1|mmf1|coat");

        //-in input file
    }
}
