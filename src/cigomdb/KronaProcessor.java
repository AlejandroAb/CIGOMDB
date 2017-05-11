/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cigomdb;

import bobjects.ArchivoObj;
import bobjects.Usuario;
import dao.ArchivoDAO;
import dao.KronaDAO;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.FileUtils;
import utils.MyDate;

/**
 *
 * @author Alejandro
 */
public class KronaProcessor {

    private Transacciones transacciones;
    private int nextIDArchivo = -1;
    private String runKrona = "/scratch/share/apps/KronaTools-2.7/scripts/ImportText.pl";

    public String getRunKrona() {
        return runKrona;
    }

    public void setRunKrona(String runKrona) {
        this.runKrona = runKrona;
    }

    public KronaProcessor(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public Transacciones getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public int getNextIDArchivo() {
        return nextIDArchivo;
    }

    public void setNextIDArchivo(int nextIDArchivo) {
        this.nextIDArchivo = nextIDArchivo;
    }

    /**
     * Se encarga de anotar/ relacionar el archivo de krona, con un metagenoma. En
     * caso de que no exista el archivo, este metodo tratará de crearlo, por lo
     * que puede generar el input file necesario para generar el archivo html
     * que luego es desplegado en la aplicación
     *
     * @param inputFile archivo con ids o etiquetas de metagenomas
     * @param outFile
     * @param withNoRank esta bandera es usada al momento de crear la matriz, si
     * viene true rellena los "huecos": no_order, no_genus, no_etc...con false,
     * de lo contrario lo deja en blanco
     * @param force por mas de que exista el html o la matriiz, fuerza su
     * creación
     */
    public void processKronaMetagenoma(String inputFile, String outFile, boolean withNoRank, boolean force) {
        if (nextIDArchivo == -1) {
            nextIDArchivo = transacciones.getNextIDArchivos();
            if (nextIDArchivo == -1) {
                System.err.println("ERROR No se puede determinar el siguiente ID de archivo");
            }
        }
        boolean toFile = false;
        if (outFile != null && outFile.length() > 0) {
            toFile = true;
        }
        try {
            FileWriter writer = null;
            if (toFile) {
                writer = new FileWriter(outFile, true);
            }
            KronaDAO kdao = new KronaDAO(transacciones);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line = "";
            FileUtils fu = new FileUtils();
            String htmlName = "";
            String proDataPath = "";
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0 && !line.startsWith("#")) {
                    boolean esID = false;
                    String id = line.trim();
                    String etiqueta = "";
                    try {
                        Integer.parseInt(id);
                        esID = true;//else es etiqueta
                    } catch (NumberFormatException nfe) {

                    }
                    if (!esID) {
                        etiqueta = id;
                        id = transacciones.getIdMetagenomaByLabel(etiqueta);
                    } else {
                        etiqueta = transacciones.getEtiquetaMetagenomaByLabel(id);
                    }
                    proDataPath = transacciones.getKrakenPathByMetagenomaID(id);
                    /**
                     * Para los kronas se espera que en el proc_data_path exista
                     * la karpeta krona y dentro de esta el archivo html. si no
                     * existe se crea como krona/krona.html
                     */
                    proDataPath += "krona/";
                    File kronaPath = new File(proDataPath);
                    fu.validateFile(proDataPath, true);//crea el directorio si no existe
                    boolean findHTML = false;

                    for (File f : kronaPath.listFiles()) {
                        //la bandera force hace que se creen tanto la matriz como el html por mas de que existan
                        if (f.getName().equals("krona.html") && !force) {
                            htmlName = f.getName();
                            findHTML = true;
                            System.out.println("Se encontro html en: " + f.getAbsolutePath());
                            //String idMarcador, String proDataPath, String fName, boolean isFromApp, FileWriter writer
                            addKronaFile(id, proDataPath, htmlName, false, writer);
                            break;
                        }
                    }
                    if (!findHTML) {
                        htmlName = "krona.html";
                        if (kdao.writeKronaInput(proDataPath + "matrix.krona.txt", id, withNoRank)) {
                            addMatrixFile(id, proDataPath, "matrix.krona.txt", true, writer);
                            String command = runKrona + " -o " + proDataPath + htmlName + " " + proDataPath + "matrix.krona.txt," + etiqueta;
                            if (executeKronaScript(command)) {
                                addKronaFile(id, proDataPath, htmlName, true, writer);
                            } else {
                                System.err.println("Error ejecutando: " + command);
                            }
                        }
                    }
                }
            }
            if (toFile) {
                writer.close();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MarkerLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MarkerLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Este método se encarga de crear la relación metagenoma archivo, así como
     * dar de alta el archivo en la BD
     *
     * @param idMetagenoma El id del metagenoma al cual pertenece el archivo
     * @param proDataPath el path donde se encuentraa el archivo
     * @param fName el nombre del archivo
     * @param isFromApp true si el archivo krona fue generado mediante esta
     * aplicación
     * @param writer null si va a directo a la BD o un writer válido si se
     * escribe a archivo
     * @return true si no hubo problemas
     * @throws IOException
     */
    public boolean addKronaFile(String idMetagenoma, String proDataPath, String fName, boolean isFromApp, FileWriter writer) throws IOException {
        ArchivoObj kronaFile = new ArchivoObj(nextIDArchivo);
        File f = new File(proDataPath + fName);
        nextIDArchivo++;
        kronaFile.setTipoArchivo(ArchivoObj.TIPO_KRN);
        kronaFile.setNombre(fName);
        kronaFile.setPath(proDataPath);
        kronaFile.setExtension("html");
        MyDate date = new MyDate(f.lastModified());
        kronaFile.setDate(date);
        kronaFile.setSize(f.length());
        kronaFile.setDescription("Archivo HTML generado por Krona a partir de la matriz de abundancia del metagenoma");
        kronaFile.setChecksum(FileUtils.getMD5File(proDataPath + fName));
        kronaFile.setAlcance("CIGOM");
        kronaFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        kronaFile.setDerechos("Acceso limitado a miembros del consorcio");
        kronaFile.setTags("krona, matriz, abundancia, html");
        kronaFile.setTipo("HTML");
        if (isFromApp) {
            Usuario user = new Usuario(1);//A.ABDALA
            user.setAcciones("creator");
            user.setComentarios("Se encargó del desarrollo y  ejecución de un programa automatizado para la generación de este archivo");
            kronaFile.addUser(user);
            Usuario user2 = new Usuario(20);//A. Escaobar
            user2.setAcciones("contributor");
            user2.setComentarios("Encargada de las asignaciones taxonómicas a partir de las cuales se generar la matriz de abundancia que sirve de entrada para generar este archivo");
            kronaFile.addUser(user2);
        } else {
            Usuario user = new Usuario(20);//A.ABDALA
            user.setAcciones("creator");
            user.setComentarios("Se encargó de la generación de este archivo mediante la implemntación de un pipeline para anotación taxonómica");
            kronaFile.addUser(user);
        }
        if (writer != null) {
            writer.write(kronaFile.toNewSQLString() + ";\n");
            writer.write("INSERT INTO metagenoma_archivo VALUES(" + idMetagenoma + "," + kronaFile.getIdArchivo() + ");\n");
            for (String qUsuarios : kronaFile.archivoUsuariosToSQLString()) {
                writer.write(qUsuarios + ";\n");
            }
        } else {
            ArchivoDAO adao = new ArchivoDAO(transacciones);
            adao.insertaArchivo(kronaFile, false, "", true);
            transacciones.insertaArchivoMetagenoma(idMetagenoma, kronaFile.getIdArchivo());
            for (String qUsuarios : kronaFile.archivoUsuariosToSQLString()) {
                if (!transacciones.insertaQuery(qUsuarios)) {
                    System.err.println("Error insertando relación usuario-archivo: "
                            + idMetagenoma + "(idmarcador) - " + nextIDArchivo + "(idArchivo) - q: " + qUsuarios);
                }
            }
        }

        return true;
    }

    /**
     * Este método se encarga de crear la relación marcador archivo, así como
     * dar de alta el archivo en la BD
     *
     * @param idMetagenoma El id del marcador al cual pertenece el archivo
     * @param proDataPath el path donde se encuentraa el archivo
     * @param fName el nombre del archivo
     * @param isFromApp true si el archivo krona fue generado mediante esta
     * aplicación
     * @param writer null si va a directo a la BD o un writer válido si se
     * escribe a archivo
     * @return true si no hubo problemas
     * @throws IOException
     */
    public boolean addMatrixFile(String idMetagenoma, String proDataPath, String fName, boolean isFromApp, FileWriter writer) throws IOException {
        ArchivoObj matrixFile = new ArchivoObj(nextIDArchivo);
        nextIDArchivo++;
        File f = new File(proDataPath + fName);
        matrixFile.setTipoArchivo(ArchivoObj.TIPO_MTX);
        matrixFile.setNombre(fName);
        matrixFile.setPath(proDataPath);
        matrixFile.setExtension("txt");
        MyDate date = new MyDate(f.lastModified());
        matrixFile.setDate(date);
        matrixFile.setSize(f.length());
        matrixFile.setDescription("Archivo generado a partir de las anotaciones realizadas en la base de datos");
        matrixFile.setChecksum(FileUtils.getMD5File(proDataPath + fName));
        matrixFile.setAlcance("CIGOM - bioinformática");
        matrixFile.setEditor("CIGOM, Línea de acción 4 - Degradación Natural de Hidrocarburos");
        matrixFile.setDerechos("Acceso limitado a miembros del consorcio");
        matrixFile.setTags("krona, matriz, abundancia");
        matrixFile.setTipo("txt");
        if (isFromApp) {
            Usuario user = new Usuario(1);//A.ABDALA
            user.setAcciones("creator");
            user.setComentarios("Se encargó del desarrollo y  ejecución de un programa automatizado para la generación de este archivo");
            matrixFile.addUser(user);
            Usuario user2 = new Usuario(20);//A. Escaobar
            user2.setAcciones("contributor");
            user2.setComentarios("Encargada de las asignaciones taxonómicas a partir de las cuales se generar la matriz de abundancia");
            matrixFile.addUser(user2);
        } else {
            Usuario user = new Usuario(20);//A. Escobar
            user.setAcciones("creator");
            user.setComentarios("Se encargó de la generación de este archivo mediante la implemntación de un pipeline para anotación taxonómica");
            matrixFile.addUser(user);
        }
        if (writer != null) {
            writer.write(matrixFile.toNewSQLString() + ";\n");
            writer.write("INSERT INTO metagenoma_archivo VALUES(" + idMetagenoma + "," + matrixFile.getIdArchivo() + ");\n");
            for (String qUsuarios : matrixFile.archivoUsuariosToSQLString()) {
                writer.write(qUsuarios + ";\n");
            }
        } else {
            ArchivoDAO adao = new ArchivoDAO(transacciones);
            adao.insertaArchivo(matrixFile, false, "", true);
            transacciones.insertaArchivoMetagenoma(idMetagenoma, matrixFile.getIdArchivo());
            for (String qUsuarios : matrixFile.archivoUsuariosToSQLString()) {
                if (!transacciones.insertaQuery(qUsuarios)) {
                    System.err.println("Error insertando relación usuario-archivo: "
                            + idMetagenoma + "(idmetagenoma) - " + nextIDArchivo + "(idArchivo) - q: " + qUsuarios);
                }
            }
        }

        return true;
    }

    public boolean executeKronaScript(String command) {
        try {
            // System.out.println("Rscript c:\\Users\\Alejandro\\Documents\\Cursos\\R\\Project\\release\\taxascript.R " + workingDir + fileName + " " + longitudInicial + " " + longitudFinal + " " + latitudInicial + " " + latitudFinal + " " + cellWG + " " + cellHG + " " + fDir);
            //Process proc = Runtime.getRuntime().exec("c:/Program Files/R/R-3.0.3/bin/Rscript c:/Users/Alejandro/Documents/Cursos/R/Project/release/taxascript.R " + workingDir + fileName + " " + longitudInicial + " " + longitudFinal + " " + latitudInicial + " " + latitudFinal + " " + cellW + " " + cellH + " " + fDir+" "+taxaGroups);
            //String commandLine = "c:/Program Files/R/R-3.0.3/bin/Rscript \"" + workingDir + "scripts/scriptDiversidad.R\" \"" + workingDir + "\" " + nameMatriz + " " + sc.getRealPath("") + fileNameRare + " " + sc.getRealPath("") + fileNameRenyi + " " + betaIndex + " " + sc.getRealPath("") + fileNameBeta + " " + imgExtraTitle;        

            Process proc = Runtime.getRuntime().exec(command);
            //Process proc = Runtime.getRuntime().exec("c:/Program Files/R/R-3.0.3/bin/Rscript " + scriptDir + "script_abundancia.R " + workingDir +" "+ scriptDir + " " +file + " " + imageName);
            proc.waitFor();
            InputStream inputstream = proc.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
            String line = "";
            while ((line = bufferedreader.readLine()) != null) {
                //Si el mensaje de error viene desde nuestro script de R
                //es decir un error controlado
                System.out.println(line);
                if (line.indexOf("ERROR") != -1) {
                    System.err.println(line);
                }
            }
            //
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            System.out.println("" + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
    }
}
