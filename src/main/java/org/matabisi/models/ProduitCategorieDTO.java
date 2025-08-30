package org.matabisi.models;

public class ProduitCategorieDTO {

    public Long id;
    public String nom;
    public String description;
    public int quantite;
    public int status;
    public int point;
    public String routeCode;

    public ProduitCategorieDTO(ProduitCategorie p) {
        this.id = p.id;
        this.nom = p.nom;
        this.description = p.description;
        this.quantite = p.quantite;
        this.status = p.status;
        this.point = p.point;
        this.routeCode = p.routeCode;
    }
}
