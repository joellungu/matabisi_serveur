package org.matabisi.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class Entreprise extends PanacheEntity {
    public byte[] logo;
    public String nom;
    public String secteur; // ex: cosmétique, télécom, boulangerie
    public String email;
    public String motDePasse;
    public int status;
}
