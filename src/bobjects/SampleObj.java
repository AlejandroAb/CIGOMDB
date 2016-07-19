/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bobjects;

import java.util.ArrayList;
import utils.MyDate;

/**
 *
 * @author Alejandro
 */
public class SampleObj {
    int idRDB;
    String objectID;
    Region region;
    double profundidad;
    String samp_size; //se espera con unidades
    MyDate fecha;
    String isol_growth_condt; //Publication reference in the form of pubmed ID (pmid), digital object identifier (doi) or url for isolation and growth condition specifications of the organism/material
    String protocolo; 
    String samp_collect_device;//The method or device employed for collecting the sample: biopsy, niskin bottle, push core
    
    String lance;
    String etiqueta;//NOM-0X-EST-TIPO_LANCE-PROF_NOMINAL-TIPO_ANALISIS_ESPECIFICO --XIX-04-A1-ROSETA-500m-NUTRIENTES
    //http://bioportal.bioontology.org/ontologies/ENVO?p=classes&conceptid=root es mas completa
    //http://environmentontology.org/Browse-EnvO  es mas navegable
    String bioma;//(marine biome = ENVO:00000447)Biomes are defined based on factors such as plant structures, leaf types, plant spacing, and other factors like climate. Biome should be treated as the descriptor of the broad ecological context of a sample. Examples include: desert, taiga, deciduous woodland, or coral reef. 
    String env_feature;//(ENVO:01000686)Environmental feature level includes geographic environmental features. Compared to biome, feature is a descriptor of the more local environment.
    //ENVO_01000065 = marine oxygen minimum zone part og -> ENVO:01000686 = marine water mass
    String env_material;//(ENVO:00002149 SEA WATER)The environmental material level refers to the material that was displaced by the sample, or material in which a sample was embedded, prior to the sampling event.: 
    String env_package;//(water or sediment)//MIGS/MIMS/MIMARKS extension for reporting of measurements and observations obtained from one or more of the environments where the sample was obtained. All environmental packages listed here are further defined in separate subtables. By giving the name of the environmental package, a selection of fields can be made from the subtables and can be reported
// air, host-associated, human-associated, human-skin, human-oral, human-gut, human-vaginal, microbial mat|biofilm, miscellaneous, plant-associated, sediment, soil, wastewater|sludge, water            
    String samp_mat_process;//Any processing applied to the sample during or after retrieving the sample from environment. This field accepts OBI, for a browser of OBI (v1.0) terms please see
    //filtering of seawater, storing samples in ethanol
   // st.replaceAll("\\s","")
    ArrayList<DBProperty> medidas; //todas las medidads realziadas
  /**
   *Estructura de documento en MONGO 
   */  
      
 /*   _id: "sampID",
   label: "MG1-E1-MIN",
   vars: [
            {
                key: "pH",
                value: 7.5,
                units: "NA",                  
            },
            {
               key: "pH",
                value: 7.5,
                units: "NA",
             }
           ]
   **/
}
