/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bobjects;

/**
 * Representa un tèrmino GO en nuestra BD
 *
 * @author Alejandro
 */
public class GOObj {

    private String id;
    private String name;
    private String namespace;
    private String definition = "";
    private String is_a = "";
    private String relationship = "";
    private boolean is_obsolete = true;
    private String replace_by = ""; //consider
    private String commentario = "";
    private String url = "";

    public GOObj() {

    }

    public void reset() {
        id = "";
        name = "";
        namespace = "";
        definition = "";
        is_a = "";
        relationship = "";
        is_obsolete = false;
        replace_by = ""; //consider
        commentario = "";
        url = "";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getIs_a() {
        return is_a;
    }

    public void setIs_a(String is_a) {
        this.is_a = is_a;
    }

    public void appendIs_a(String is_a) {
        if (this.is_a.length() == 0) {
            this.is_a = is_a;
        } else {
            this.is_a += ", " + is_a;
        }
    }

    public void appendRelationship(String relationship) {
        if (this.relationship.length() == 0) {
            this.relationship = relationship;
        } else {
            this.relationship += ", " + relationship;
        }
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public boolean getIs_obsolete() {
        return is_obsolete;
    }

    public void setIs_obsolete(boolean is_obsolete) {
        this.is_obsolete = is_obsolete;
    }

    public String getReplace_by() {
        return replace_by;
    }

    public void setReplace_by(String replace_by) {
        this.replace_by = replace_by;
    }

    public void appendReplace_by(String replace_by) {
        if (this.replace_by.length() == 0) {
            this.replace_by = replace_by;
        } else {
            this.replace_by += ", " + replace_by;
        }
    }

    public String getCommentario() {
        return commentario;
    }

    public void setCommentario(String commentario) {
        this.commentario = commentario;
    }

}
