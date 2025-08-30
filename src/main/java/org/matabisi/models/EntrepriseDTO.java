package org.matabisi.models;

import java.util.List;

public class EntrepriseDTO {
    public Long id;
    public String nom;
    public String secteur;
    public String email;
    public int status;
    public List<ProduitCategorieDTO> produitCategories;

    public EntrepriseDTO(Entreprise e, List<ProduitCategorie> produits) {
        this.id = e.id;
        this.nom = e.nom;
        this.secteur = e.secteur;
        this.email = e.email;
        this.status = e.status;
        this.produitCategories = produits.stream()
                .map(ProduitCategorieDTO::new)
                .toList();
    }
}
