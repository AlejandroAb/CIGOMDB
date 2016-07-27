package bobjects;

/**
 * Este objeto viene a representar la medicion de una variable. Es decir la
 * entidad muestra_valor en la BD
 *
 * @author Alejandro
 */
public class Medicion {

    int idMuestra; //o id_muestreo
    int idVariable;
    int orden;
    int idMetodoMedida;
    public static int MEDIDO_CTD = 1;//1 =  medido por CTD en la BD
    private String medicion_t1;
    private String medicion_t2;
    private String medicion_t3;
    private String comentarios="";
    /**
     * Se crea un constructor solo con la variable para que en casos de
     * necesitar dar de alta una medicion a algo que aún no se da de alta,
     * primero se registre la muestra con el fin de obtener el id y luego
     * setearlo
     *
     * @param idVariable
     */
    public Medicion(int idVariable) {
        this.idVariable = idVariable;
    }

    public Medicion(int idMuestra, int idVariable) {
        this.idMuestra = idMuestra;
        this.idVariable = idVariable;
    }

    public int getIdMuestra() {
        return idMuestra;
    }

    public void setIdMuestra(int idMuestra) {
        this.idMuestra = idMuestra;
    }

    public int getIdVariable() {
        return idVariable;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public void setIdVariable(int idVariable) {
        this.idVariable = idVariable;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public int getIdMetodoMedida() {
        return idMetodoMedida;
    }

    public void setIdMetodoMedida(int idMetodoMedida) {
        this.idMetodoMedida = idMetodoMedida;
    }

    public static int getMEDIDO_CTD() {
        return MEDIDO_CTD;
    }

    public static void setMEDIDO_CTD(int MEDIDO_CTD) {
        Medicion.MEDIDO_CTD = MEDIDO_CTD;
    }

    public String getMedicion_t1() {
        return medicion_t1;
    }

    public void setMedicion_t1(String medicion_t1) {
        this.medicion_t1 = medicion_t1;
    }

    public String getMedicion_t2() {
        return medicion_t2;
    }

    public void setMedicion_t2(String medicion_t2) {
        this.medicion_t2 = medicion_t2;
    }

    public String getMedicion_t3() {
        return medicion_t3;
    }

    public void setMedicion_t3(String medicion_t3) {
        this.medicion_t3 = medicion_t3;
    }

}
