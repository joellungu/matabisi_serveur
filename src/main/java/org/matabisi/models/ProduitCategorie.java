package org.matabisi.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class ProduitCategorie extends PanacheEntity {
    public byte[] logo;
    public String nom;
    public String description;
    public int quantite;
    public int status;
    public int point;
    public String routeCode;
    public Long idEntreprise;
}
