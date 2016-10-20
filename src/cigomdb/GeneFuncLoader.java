/*
 * Esta clase esta diseñada como clase principal para procesar resultados producto
 * del análisis de metgenomas por tecnologia shotgun
 */
package cigomdb;

import bobjects.GenObj;
import bobjects.GenSeqObj;
import bobjects.Intergenic;
import dao.GenDAO;
import database.Transacciones;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.FastaReader;
import utils.GFFLine;
import utils.GFFReader;
import utils.Sequence;

/**
 * CIGOM. MAYO 2016
 *
 * @author Alejandro Abdala
 */
public class GeneFuncLoader {

    private Transacciones transacciones = null;
    private boolean debug = false;

    public GeneFuncLoader(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String loadFragileScanFiles(String idPre, String gffFile, String nucFile, String aaFile, String mapPrefix) {
        String log = "";
        GenDAO genDAO = new GenDAO(transacciones);
        int line = 0;
        try {
            BufferedReader gffReader = null;
            BufferedReader nucReader = null;
            BufferedReader aaReader = null;
            gffReader = new BufferedReader(new InputStreamReader(new FileInputStream(gffFile)));
            if (nucFile.length() > 0) {
                nucReader = new BufferedReader(new InputStreamReader(new FileInputStream(nucFile)));
            }
            if (aaFile.length() > 0) {
                aaReader = new BufferedReader(new InputStreamReader(new FileInputStream(aaFile)));
            }
            String gffLine;
            String nucLine = null;
            String aaLine = null;
            int gen_num = 0;
            while ((gffLine = gffReader.readLine()) != null) {
                line++;
                if (!gffLine.startsWith("#")) {
                    gen_num++;
                    GenObj gen = new GenObj(idPre + "" + gen_num);
                    gen.setGene_map_id(mapPrefix + "" + gen_num);
                    StringTokenizer st = new StringTokenizer(gffLine, "\t");
                    gen.setContig_id(st.nextToken());
                    st.nextToken();//FGS metodo
                    gen.setGenType(st.nextToken());
                    try {
                        int from = Integer.parseInt(st.nextToken());
                        gen.setContig_from(from);
                    } catch (NumberFormatException nfe) {
                        gen.setContig_from(0);
                        log += "Error " + line + " CONTIG_FROM gff FILE";
                    }
                    try {
                        int to = Integer.parseInt(st.nextToken());
                        gen.setContig_to(to);
                    } catch (NumberFormatException nfe) {
                        gen.setContig_from(0);
                        log += "Error " + line + " CONTIG_TO gff FILE";
                    }
                    st.nextToken();//un punto
                    gen.setGen_strand(st.nextToken());
                    st.nextToken();//0 uno o dos ver que es este campo
                    String varios = st.nextToken(); //ID=contig-100_0_1_661_+;product=predicted protein
                    StringTokenizer miscToks = new StringTokenizer(varios, ";");
                    while (miscToks.hasMoreTokens()) {
                        String field = miscToks.nextToken();
                        String fieldArr[] = field.split("=");
                        if (fieldArr.length == 2) {
                            String key = fieldArr[0];
                            String val = fieldArr[1];
                            if (key.toUpperCase().equals("ID")) {
                                gen.setContig_gen_id(val);
                            } else if (key.toLowerCase().equals("product")) {
                                gen.setGen_function(val);
                            } else {
                                gen.addProperty(key, val);

                            }
                        }
                    }
                    if (nucReader != null) {
                        if (nucLine == null) {
                            nucLine = nucReader.readLine();
                        }
                    }
                    if (aaReader != null) {
                        if (aaLine == null) {
                            aaLine = aaReader.readLine();
                        }
                    }
                    if ((">" + gen.getContig_gen_id()).equals(nucLine.trim()) && nucReader != null) {
                        String seqNuc = "";
                        while (((nucLine = nucReader.readLine()) != null) && !nucLine.startsWith(">")) {
                            seqNuc += nucLine;
                        }
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(seqNuc); //also fix de length
                        seqObj.setSeqType("NC");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    }
                    if ((">" + gen.getContig_gen_id()).equals(aaLine.trim()) && aaReader != null) {
                        String seqAmino = "";
                        while (((aaLine = aaReader.readLine()) != null) && !aaLine.startsWith(">")) {
                            seqAmino += aaLine;
                        }
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(seqAmino); //also fix de length
                        seqObj.setSeqType("AA");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    }
                    log += genDAO.almacenaGen(gen);
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, ioe);
        } catch (NoSuchElementException nsee) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, nsee);
            log += "Error token linea: " + line;
        }
        return log;

    }

    //String idPre, String gffFile, String nucFile, String aaFile, String mapPrefix
    /**
     *
     * @param idPrefix el prefijo que es usado para el id dl gen
     * @param idMetageno el metagenoma al que se relaciona
     * @param idGenoma el genoma al que esta relaccionado, puede ser a un genoma
     * o a un metagenoma pero no ambos
     * @param gffFile el archivo de coordenadas
     * @param contigFile el archivo de secuencias
     * @param nucFile el archivo de nucleotidos coorrespondiente a las
     * coordenadas y el contigfile
     * @param protFile la traducción del archivo de nucleotidos a proteinas
     * @param mapPrefix el prefijo ccon el cual luego se hara el mapping para la
     * anotacion funcional (ejemplo gen_id_)
     * @param mapStartsIn0 Hubo un caso donde el gen_id en el contig empezaa en
     * uno y en la anotacion funcional en cero esta bandera permite ese desfaz
     * @param startAtLine si se proceso un archivo grande, y este fallo y se
     * requiere re procesar desde algún punto se puede usar este parámetro
     * @param withHash lee los archivos de contigs, nucs y prots y los carga en
     * memoria en un hash, ha demostrado ser la mejor manera de procesar los
     * archivos, por esto por default est variable es true
     */
    public void parseEnsamble(String idPrefix, int idMetageno, int idGenoma, String gffFile, String contigFile, String nucFile, String protFile, String mapPrefix, boolean mapStartsIn0, int startAtLine, boolean withHash) {
        try {
            GFFReader gffReader = new GFFReader(new InputStreamReader(new FileInputStream(gffFile)));
            FastaReader contigReader = new FastaReader(new InputStreamReader(new FileInputStream(contigFile)));
            FastaReader nucReader = new FastaReader(new InputStreamReader(new FileInputStream(nucFile)));
            FastaReader protReader = new FastaReader(new InputStreamReader(new FileInputStream(protFile)));
            GenDAO genDAO = new GenDAO(transacciones);
            genDAO.setDebug(debug);
            GFFLine gffLine;
            int gen_num = 0;
            GenObj tmpGene = null;
            Sequence tmpContig = null;
            Sequence nucSeq = null;
            Sequence protSeq = null;
            Sequence contig = null;
            if (withHash) {
                nucReader.loadHash(Sequence.NUCLEOTIDOS);
                protReader.loadHash(Sequence.PROTEINAS);
                contigReader.loadHash(Sequence.NUCLEOTIDOS);
            } else {
                contig = contigReader.readSequence(Sequence.NUCLEOTIDOS);
            }
            while (gen_num < startAtLine) {
                gffLine = gffReader.readGffLine();
                gen_num++;
                if (gffLine == null) {
                    System.err.println("Fin de archivo no se proceso ningún gen. Registros en archivo: " + gen_num + " bandera startAt: " + 77);
                }
            }
            //Primero procesa el archivo GFF - todo se mueve en base a este archivo
            while ((gffLine = gffReader.readGffLine()) != null) {
                gen_num++;
                GenObj gen = new GenObj(idPrefix + "" + gen_num);
                gen.setContig_id(gffLine.getId());
                gen.setGenType(gffLine.getSeq_type());
                gen.setContig_from(gffLine.getFrom());
                gen.setContig_to(gffLine.getTo());
                gen.setGen_score(gffLine.getScore());
                gen.setGen_strand(gffLine.getStrand());
                gen.setGen_phase(gffLine.getPhase());
                gen.setIdGenoma(idGenoma);
                gen.setIdMetagenoma(idMetageno);
                gen.setGen_num(gen_num);
                if (idGenoma != -1) {
                    gen.setGen_src("GEN");
                } else {
                    gen.setGen_src("MET");
                }
                if (mapStartsIn0) {
                    gen.setGene_map_id(mapPrefix + "" + (gen_num - 1));
                } else {
                    gen.setGene_map_id(mapPrefix + "" + gen_num);
                }

                for (String key : gffLine.getAtributos().keySet()) {
                    if (key.toUpperCase().equals("ID")) {
                        gen.setContig_gen_id(gffLine.getAtrributeValue(key));
                    } else if (key.toLowerCase().equals("product")) {
                        gen.setGen_function(gffLine.getAtrributeValue(key));
                    } else if (key.toLowerCase().equals("gene_id")) {
                        gen.setContig_gen_id("gene_id_" + gffLine.getAtrributeValue(key));
                    } else {
                        gen.addProperty(key, gffLine.getAtrributeValue(key));
                    }
                }
                //aca termina de crear un gen en base a los datos del gff, ahora faltan las secuencias y las intergénicas 
                //se espera que el primer registro del gff corresponda con la primer secuencia tanto del archivo de nucs como el de prots
                if (!withHash) {
                    nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                    protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                }
                if (tmpGene == null) { //esto pasa ùnicamente para el primer gen                     
                    Intergenic cincop = new Intergenic(Intergenic.I5P);
                    cincop.setFrom(0);
                    cincop.setTo(gen.getContig_from() - 1);
                    // cincop.setSize(gen.getContig_from() - 1);
                    GenSeqObj seqObj = new GenSeqObj();
                    //esto hace posible que podamos usar la bandera startAtLine //IF WITH HASH
                    while (!withHash && contig != null && contig.getSeqId().equals(gen.getContig_id())) {
                        tmpContig = contig;
                        contig = contigReader.readSequence(Sequence.NUCLEOTIDOS);

                    }
                    if (withHash) {
                        contig = contigReader.getKey(gen.getContig_id(), false);
                    }
                    if (contig != null) {
                        if (cincop.getTo() - cincop.getFrom() < 0) {
                            cincop.setSecuenciaValidada(contig, cincop.getTo(), cincop.getFrom());
                        } else {
                            cincop.setSecuenciaValidada(contig, cincop.getFrom(), cincop.getTo());
                        }
                        seqObj.setSecuenciaValidada(contig, gen.getContig_from() - 1, gen.getContig_to());
                    } else {
                        System.err.println("No se encontró contig: " + gen.getContig_id());
                        System.exit(1);
                    }
                    /*else if (tmpContig != null && tmpContig.getSeqId().equals(gen.getContig_id())) {
                     tresp.setSecuencia(tmpContig.getSequence().substring(tresp.getFrom(), tresp.getTo()));
                     seqObj.setSequence(tmpContig.getSequence().substring(gen.getContig_from() - 1, gen.getContig_to()));
                     } */

                    gen.setInter5p(cincop);
                    seqObj.setSeqType("NC");
                    seqObj.setSeq_from(gen.getContig_from());
                    seqObj.setSeq_to(gen.getContig_to());
                    gen.addSequence(seqObj);
                } else {//del segundo gen en adelante siempre existe tmpGene
                    //valida si el gen actual esta en el mismo contig que el gen anterior
                    if (gen.getContig_id().equals(tmpGene.getContig_id())) {
                        //crea la 5p del current misma que se asigna como 3p Del anterior
                        Intergenic cincop = new Intergenic(Intergenic.I5P);
                        cincop.setFrom(tmpGene.getContig_to());
                        cincop.setTo(gen.getContig_from() - 1);
                        //    cincop.setSize(cincop.getTo() - cincop.getFrom());
                        GenSeqObj seqObj = new GenSeqObj();
                        //  String contig = "";//something getcontig_seq
                        if (contig.getSeqId().equals(gen.getContig_id())) {
                            if (cincop.getTo() - cincop.getFrom() < 0) {
                                cincop.setSecuenciaValidada(contig, cincop.getTo(), cincop.getFrom());
                            } else {
                                cincop.setSecuenciaValidada(contig, cincop.getFrom(), cincop.getTo());
                            }
                            seqObj.setSecuenciaValidada(contig, gen.getContig_from() - 1, gen.getContig_to());
                        }//este else if en teoria no tendrìa que pasar... 
                        else if (tmpContig != null && tmpContig.getSeqId().equals(gen.getContig_id())) {
                            if (cincop.getTo() - cincop.getFrom() < 0) {
                                cincop.setSecuenciaValidada(tmpContig, cincop.getTo(), cincop.getFrom());
                            } else {
                                cincop.setSecuenciaValidada(tmpContig, cincop.getFrom(), cincop.getTo());
                            }
                            seqObj.setSecuenciaValidada(tmpContig, gen.getContig_from() - 1, gen.getContig_to());
                        } else {
                            //esto no se...si se hace esto hay que trae nuevamente el primer if o todo dentro de un while
                            tmpContig = contig;
                            contig = contigReader.readSequence(Sequence.NUCLEOTIDOS);
                            System.err.println("LECTURA RARA DE CONTIG");
                        }
                        //se asigna 3p del anterior que es cinco ' del actual
                        tmpGene.setInter3p((Intergenic) cincop.clone());
                        //ANOTA TMP
                        genDAO.almacenaValidaGen(tmpGene);
                        //completamos los datos restantes del current (queda pendiente 5p)
                        gen.setInter5p(cincop);
                        seqObj.setSeqType("NC");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    } else { //ES NUEVO CONTIG
                        //como el nuevo gen, pertenece a un nuevo contig, el anterior gen tmpGene
                        //tiene su intergènica 3' de donde se quedò hasta el final del contig 
                        Intergenic tresp = new Intergenic(Intergenic.I3P);
                        tresp.setFrom(tmpGene.getContig_to());
                        if (contig.getSeqId().equals(tmpGene.getContig_id())) {
                            tresp.setTo(contig.getSequence().length());
                            /*    if (tresp.getTo() - tresp.getFrom() < 0) {
                             tresp.setSecuenciaValidada(contig,tresp.getTo(), tresp.getFrom());
                             } else {
                             tresp.setSecuenciaValidada(contig,tresp.getFrom(), tresp.getTo());
                             }*/
                            if (tresp.getTo() - tresp.getFrom() > 0) {
                                tresp.setSecuenciaValidada(contig, tresp.getFrom(), tresp.getTo());
                            } else {
                                tresp.setSecuencia("");
                            }
                        } //como aca estamos hablando del anterior gen lo mas probable es que sea en el contig y no tmpContig
                        else if (tmpContig != null && tmpContig.getSeqId().equals(tmpGene.getContig_id())) {
                            tresp.setTo(tmpContig.getSequence().length());
                            if (tresp.getTo() - tresp.getFrom() < 0) {
                                tresp.setSecuenciaValidada(tmpContig, tresp.getTo(), tresp.getFrom());
                            } else {
                                tresp.setSecuenciaValidada(tmpContig, tresp.getFrom(), tresp.getTo());
                            }
                        } else {
                            //no se si tiene mucho sentido esto...
                            tmpContig = contig;
                            contig = contigReader.readSequence(Sequence.NUCLEOTIDOS);
                        }
                        //  tresp.setSize(tresp.getTo() - tresp.getFrom());
                        tmpGene.setInter3p(tresp);
                        //ANOTA TMP GENE
                        genDAO.almacenaValidaGen(tmpGene);
                        //ACA EMPIEZA EL NUEVO CONTIG
                        Intergenic cincop = new Intergenic(Intergenic.I5P);
                        cincop.setFrom(0);
                        cincop.setTo(gen.getContig_from() - 1);
                        // cincop.setSize(gen.getContig_from() - 1);
                        GenSeqObj seqObj = new GenSeqObj();
                        if (contig.getSeqId().equals(gen.getContig_id())) {
                            if (cincop.getTo() - cincop.getFrom() < 0) {
                                cincop.setSecuenciaValidada(contig, cincop.getTo(), cincop.getFrom());
                            } else {
                                cincop.setSecuenciaValidada(contig, cincop.getFrom(), cincop.getTo());
                            }
                            seqObj.setSecuenciaValidada(contig, gen.getContig_from() - 1, gen.getContig_to());
                        }//este else if en teoria no tendrìa que pasar... 
                        else if (tmpContig != null && tmpContig.getSeqId().equals(gen.getContig_id())) {
                            if (cincop.getTo() - cincop.getFrom() < 0) {
                                cincop.setSecuenciaValidada(tmpContig, cincop.getTo(), cincop.getFrom());
                            } else {
                                cincop.setSecuenciaValidada(tmpContig, cincop.getFrom(), cincop.getTo());
                            }
                            seqObj.setSecuenciaValidada(tmpContig, gen.getContig_from() - 1, gen.getContig_to());
                        } else {
                            //Aca es mucho mas probable que pase esto, y es ca donde se va a realizar el cambio de contigs, por eso leemos nuevamente
                            tmpContig = contig;
                            if (withHash) {
                                contig = contigReader.getKey(gen.getContig_id(), false);
                            } else {
                                contig = contigReader.readSequence(Sequence.NUCLEOTIDOS);
                            }
                            if (contig != null && contig.getSeqId().equals(gen.getContig_id())) {
                                if (cincop.getTo() - cincop.getFrom() < 0) {
                                    cincop.setSecuenciaValidada(contig, cincop.getTo(), cincop.getFrom());
                                } else {
                                    cincop.setSecuenciaValidada(contig, cincop.getFrom(), cincop.getTo());
                                }
                                seqObj.setSecuenciaValidada(contig, gen.getContig_from() - 1, gen.getContig_to());
                            } else {
                                System.err.println("No se puede encontrar contig para: " + gen.getContig_id());
                            }
                        }
                        gen.setInter5p(cincop);
                        seqObj.setSeqType("NC");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    }
                }
                if (withHash) {
                    nucSeq = nucReader.getKey(gen.getContig_gen_id(), true);
                    if (nucSeq != null) {
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(nucSeq.getSequence());
                        seqObj.setSeqType("NC_2");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    } else {
                        System.err.println("No se encontró contig: " + gen.getContig_gen_id());
                    }
                } else {
                    while (nucSeq != null && !gen.getContig_gen_id().equals(nucSeq.getSeqId())) {
                        nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                        // protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                    }

                    if (nucSeq != null && gen.getContig_gen_id().equals(nucSeq.getSeqId())) {
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(nucSeq.getSequence());
                        seqObj.setSeqType("NC_2");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    } else { // es null
                        //no lo encontró así que reinicia el reader
                        nucReader = new FastaReader(new InputStreamReader(new FileInputStream(nucFile)));
                        nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                        while (nucSeq != null && !gen.getContig_gen_id().equals(nucSeq.getSeqId())) {
                            nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                            // protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                        }
                        if (nucSeq != null && gen.getContig_gen_id().equals(nucSeq.getSeqId())) {
                            GenSeqObj seqObj = new GenSeqObj();
                            seqObj.setSequence(nucSeq.getSequence());
                            seqObj.setSeqType("NC_2");
                            seqObj.setSeq_from(gen.getContig_from());
                            seqObj.setSeq_to(gen.getContig_to());
                            gen.addSequence(seqObj);
                        } else {
                            System.err.println("No se encontro NC para: " + gen.getContig_gen_id() + " - " + gen.getGenID());
                        }
                    }
                }
                if (withHash) {
                    protSeq = protReader.getKey(gen.getContig_gen_id(), true);
                    if (protSeq != null) {
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(protSeq.getSequence()); //also fix de length
                        seqObj.setSeqType("AA");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    } else {
                        System.err.print("No se encontró secuencia de proteinas para contig: " + gen.getContig_gen_id());
                    }
                } else {
                    while (protSeq != null && !gen.getContig_gen_id().equals(protSeq.getSeqId())) {
                        //nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                        protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                    }
                    if (protSeq != null && gen.getContig_gen_id().equals(protSeq.getSeqId())) {
                        GenSeqObj seqObj = new GenSeqObj();
                        seqObj.setSequence(protSeq.getSequence()); //also fix de length
                        seqObj.setSeqType("AA");
                        seqObj.setSeq_from(gen.getContig_from());
                        seqObj.setSeq_to(gen.getContig_to());
                        gen.addSequence(seqObj);
                    } else {//ES NULL
                        protReader = new FastaReader(new InputStreamReader(new FileInputStream(nucFile)));
                        protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                        while (protSeq != null && !gen.getContig_gen_id().equals(protSeq.getSeqId())) {
                            //nucSeq = nucReader.readSequenceML(Sequence.NUCLEOTIDOS);
                            protSeq = protReader.readSequenceML(Sequence.PROTEINAS);
                        }
                        if (protSeq != null && gen.getContig_gen_id().equals(protSeq.getSeqId())) {
                            GenSeqObj seqObj = new GenSeqObj();
                            seqObj.setSequence(protSeq.getSequence()); //also fix de length
                            seqObj.setSeqType("AA");
                            seqObj.setSeq_from(gen.getContig_from());
                            seqObj.setSeq_to(gen.getContig_to());
                            gen.addSequence(seqObj);
                        } else {
                            System.err.println("No se encontro AA para: " + gen.getContig_gen_id() + " - " + gen.getGenID());
                        }
                    }
                }
                tmpGene = gen;
            }
            //ANOTA EL ULTIMO GEN QUE NUNCA LLEGO A ANOTARSE
            Intergenic tresp = new Intergenic(Intergenic.I3P);
            tresp.setFrom(tmpGene.getContig_to());
            if (contig != null && contig.getSeqId().equals(tmpGene.getContig_id())) {
                tresp.setTo(contig.getSequence().length());
                if (tresp.getTo() - tresp.getFrom() > 0) {
                    tresp.setSecuencia(contig.getSequence().substring(tresp.getFrom(), tresp.getTo()));
                } else {
                    tresp.setSecuencia("");
                }

            } //como aca estamos hablando del anterior gen lo mas probable es que sea en el contig y no tmpContig
            else if (tmpContig != null && tmpContig.getSeqId().equals(tmpGene.getContig_id())) {
                tresp.setTo(tmpContig.getSequence().length());
                tresp.setSecuencia(tmpContig.getSequence().substring(tresp.getFrom(), tresp.getTo()));
            }
            //  tresp.setSize(tresp.getTo() - tresp.getFrom());
            tmpGene.setInter3p(tresp);
            //ANOTA TMP GENE
            genDAO.almacenaValidaGen(tmpGene);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("No se encontró archivo GFF: " + gffFile);
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("No se encontró archivo GFF: " + gffFile);
            System.exit(1);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(GeneFuncLoader.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error al clonar objeto");
            System.exit(1);
        }
    }
}
