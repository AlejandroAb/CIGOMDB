/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bobjects;

/**
 *
 * @author Alejandro
 */
public class Taxon {

    private int tax_id;
    private String taxon = "";
    private String rank = "";
    private String kingdom = "";
    private String subkingdom = "";
    private String superphylum = "";
    private String phylum = "";
    private String subphylum = "";
    private String superclass = "";
    private String infraclass = "";
    private String classe = "";
    private String subclass = "";
    private String parvorder = "";
    private String superorder = "";
    private String infraorder = "";
    private String order = "";
    private String suborder = "";
    private String superfamily = "";
    private String family = "";
    private String subfamily = "";
    private String tribe = "";
    private String subtribe = "";
    private String genus = "";
    private String subgenus = "";
    private String species = "";
    private String species_group = "";
    private String species_subgroup = "";
    private String subspecies = "";
    private String forma = "";
    private String varietas = "";
    private String no_rank = "";

    public Taxon(int tax_id) {
        this.tax_id = tax_id;
    }

    public int getTax_id() {
        return tax_id;
    }

    public void setTax_id(int tax_id) {
        this.tax_id = tax_id;
    }

    public String getTaxon() {
        return taxon;
    }

    public void setTaxon(String taxon) {
        this.taxon = taxon;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getKingdom() {
        return kingdom;
    }

    public void setKingdom(String kingdom) {
        this.kingdom = kingdom;
    }

    public String getSubkingdom() {
        return subkingdom;
    }

    public void setSubkingdom(String subkingdom) {
        this.subkingdom = subkingdom;
    }

    public String getSuperphylum() {
        return superphylum;
    }

    public void setSuperphylum(String superphylum) {
        this.superphylum = superphylum;
    }

    public String getPhylum() {
        return phylum;
    }

    public void setPhylum(String phylum) {
        this.phylum = phylum;
    }

    public String getSubphylum() {
        return subphylum;
    }

    public void setSubphylum(String subphylum) {
        this.subphylum = subphylum;
    }

    public String getSuperclass() {
        return superclass;
    }

    public void setSuperclass(String superclass) {
        this.superclass = superclass;
    }

    public String getInfraclass() {
        return infraclass;
    }

    public void setInfraclass(String infraclass) {
        this.infraclass = infraclass;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getSubclass() {
        return subclass;
    }

    public void setSubclass(String subclass) {
        this.subclass = subclass;
    }

    public String getParvorder() {
        return parvorder;
    }

    public void setParvorder(String parvorder) {
        this.parvorder = parvorder;
    }

    public String getSuperorder() {
        return superorder;
    }

    public void setSuperorder(String superorder) {
        this.superorder = superorder;
    }

    public String getInfraorder() {
        return infraorder;
    }

    public void setInfraorder(String infraorder) {
        this.infraorder = infraorder;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getSuborder() {
        return suborder;
    }

    public void setSuborder(String suborder) {
        this.suborder = suborder;
    }

    public String getSuperfamily() {
        return superfamily;
    }

    public void setSuperfamily(String superfamily) {
        this.superfamily = superfamily;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getSubfamily() {
        return subfamily;
    }

    public void setSubfamily(String subfamily) {
        this.subfamily = subfamily;
    }

    public String getTribe() {
        return tribe;
    }

    public void setTribe(String tribe) {
        this.tribe = tribe;
    }

    public String getSubtribe() {
        return subtribe;
    }

    public void setSubtribe(String subtribe) {
        this.subtribe = subtribe;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSubgenus() {
        return subgenus;
    }

    public void setSubgenus(String subgenus) {
        this.subgenus = subgenus;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getSpecies_group() {
        return species_group;
    }

    public void setSpecies_group(String species_group) {
        this.species_group = species_group;
    }

    public String getSpecies_subgroup() {
        return species_subgroup;
    }

    public void setSpecies_subgroup(String species_subgroup) {
        this.species_subgroup = species_subgroup;
    }

    public String getSubspecies() {
        return subspecies;
    }

    public void setSubspecies(String subspecies) {
        this.subspecies = subspecies;
    }

    public String getForma() {
        return forma;
    }

    public void setForma(String forma) {
        this.forma = forma;
    }

    public String getVarietas() {
        return varietas;
    }

    public void setVarietas(String varietas) {
        this.varietas = varietas;
    }

    public String getNo_rank() {
        return no_rank;
    }

    public void setNo_rank(String no_rank) {
        this.no_rank = no_rank;
    }

    public String toSQLString() {
        String query = "INSERT INTO taxon (tax_id, taxon, rank, kingdom, subkingdom, superphylum, phylum, subphylum, superclass, infraclass, class, subclass, parvorder, superorder, infraorder, orden, suborder, superfamily, family, subfamily, tribe, subtribe, genus, subgenus, species, species_group, species_subgroup, subspecies, forma, varietas, no_rank, degradadora) "
                + "VALUES (" + tax_id + ",'" + taxon + "','" + rank + "','" + kingdom + "','" + subkingdom + "','" + superphylum + "','" + phylum + "','" + subphylum + "','" + superclass + "','" + infraclass + "','" + classe + "','" + subclass + "','" + parvorder + "','" + superorder + "','" + infraorder + "','" + order + "','" + suborder + "','" + superfamily + "','" + family + "','" + subfamily + "','" + tribe + "','" + subtribe + "','" + genus + "','" + subgenus + "','" + species + "','" + species_group + "','" + species_subgroup + "','" + subspecies + "','" + forma + "','" + varietas + "','" + no_rank + "',0)";
        return query;
    }

    public void assignRank(String name, String rank) {
        switch (rank) {
            case "kingdom":
                this.setKingdom(name);
                break;
            case "subkingdom":
                this.setSubkingdom(name);
                break;
            case "superphylum":
                this.setSubphylum(name);
                break;
            case "phylum":
                this.setPhylum(name);
                break;
            case "subphylum":
                this.setSubphylum(name);
                break;
            case "superclass":
                this.setSuperclass(name);
                break;
            case "infraclass":
                this.setInfraclass(name);
                break;
            case "class":
                this.setClasse(name);
                break;
            case "subclass":
                this.setSubclass(name);
                break;
            case "parvorder":
                this.setParvorder(name);
                break;
            case "superorder":
                this.setSuperorder(name);
                break;
            case "infraorder":
                this.setInfraorder(name);
                break;
            case "order":
                this.setOrder(name);
                break;
            case "suborder":
                this.setSuborder(name);
                break;
            case "superfamily":
                this.setSuperfamily(name);
                break;
            case "family":
                this.setFamily(name);
                break;
            case "subfamily":
                this.setSubfamily(name);
                break;
            case "tribe":
                this.setTribe(name);
                break;
            case "subtribe":
                this.setSubtribe(name);
                break;
            case "genus":
                this.setGenus(name);
                break;
            case "subgenus":
                this.setSubgenus(name);
                break;
            case "species":
                this.setSpecies(name);
                break;
            case "species_group":
                this.setSpecies_group(name);
                break;
            case "species_subgroup":
                this.setSpecies_subgroup(name);
                break;
            case "subspecies":
                this.setSubspecies(name);
                break;
            case "forma":
                this.setForma(name);
                break;
            case "varietas":
                this.setVarietas(name);
                break;
            case "no_rank":
                this.no_rank += name;
                break;
        }
    }
}
