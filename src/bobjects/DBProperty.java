
package bobjects;

/**
 *
 * @author Alejandro
 */
public class DBProperty {

    private String key;
    private String value;
    private boolean numeric = false;
    private String units;
    /**
    _id: "sampID",
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
    public String getKey() {
        return key;
    }

    public String toMongoString() {
        String mongoStr = "";
        if (numeric) {
            mongoStr = "\"" + key + "\":"  + value;
        } else {
            mongoStr = "\"" + key + "\":" + "\"" + value + "\"";
        }
        return mongoStr;

    }

    public DBProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public DBProperty(String key, String value, boolean numeric) {
        this.key = key;
        this.value = value;
        this.numeric = numeric;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public void setNumeric(boolean isNumeric) {
        this.numeric = isNumeric;
    }

}
