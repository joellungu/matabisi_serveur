package org.matabisi.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Recompense extends PanacheEntity {
    public String nom;
    public String description;
    public int coutPoints;
}