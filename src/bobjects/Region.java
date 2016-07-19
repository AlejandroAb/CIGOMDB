/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bobjects;

import utils.MyCoord;
import static utils.JsonUtils.printJson;
import static utils.JsonUtils.toJsonString;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Arrays;
import org.bson.Document;

/**
 *
 * @author Alejandro
 */
public class Region {

    private String name;
    private MyCoord latitud;
    private MyCoord longitud;
    private String gaz;
    private String estacion;

    public Region(String name, MyCoord latitud, MyCoord longitud) {
        this.name = name;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String getEstacion() {
        return estacion;
    }

    public void setEstacion(String estacion) {
        this.estacion = estacion;
    }

    public Document toJSONDoc() {

        //"type": "Point", "coordinates": [100.0, 0.0]
        Document geoLoc = new Document()
                .append("type", "Point")
                .append("coordinates", Arrays.asList(latitud.getCoordenadas(), longitud.getCoordenadas()));
        //+ "new Document(\"_id\", \"user1\").append(\"interests\", Arrays.asList(\"basketball\", \"drumming\"));
        Document region = new Document()
                .append("name", name)
                .append("age", 25)
                .append("loc", geoLoc);
        return region;
    }

    public void printJSONString() {

        //"type": "Point", "coordinates": [100.0, 0.0]
        Document geoLoc = new Document()
                .append("type", "Point")
                .append("coordinates", Arrays.asList(latitud.getCoordenadas(), longitud.getCoordenadas()));
        //+ "new Document(\"_id\", \"user1\").append(\"interests\", Arrays.asList(\"basketball\", \"drumming\"));
        Document region = new Document()
                .append("name", name)
                .append("age", 25)
                .append("loc", geoLoc);
        printJson(region);
        //return region;
    }

    public String toJSONString() {

        //"type": "Point", "coordinates": [100.0, 0.0]
        Document geoLoc = new Document()
                .append("type", "Point")
                .append("coordinates", Arrays.asList(latitud.getCoordenadas(), longitud.getCoordenadas()));
        //+ "new Document(\"_id\", \"user1\").append(\"interests\", Arrays.asList(\"basketball\", \"drumming\"));
        Document region = new Document()
                .append("name", name)
                .append("age", 25)
                .append("loc", geoLoc);
        return toJsonString(region);
        //return region;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyCoord getLatitud() {
        return latitud;
    }

    public void setLatitud(MyCoord latitud) {
        this.latitud = latitud;
    }

    public MyCoord getLongitud() {
        return longitud;
    }

    public void setLongitud(MyCoord longitud) {
        this.longitud = longitud;
    }

    public String getGaz() {
        return gaz;
    }

    public void setGaz(String gaz) {
        this.gaz = gaz;
    }

}
