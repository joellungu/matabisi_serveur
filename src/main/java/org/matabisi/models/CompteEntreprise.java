package org.matabisi.models;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class CompteEntreprise extends PanacheEntity {

    public int soldePoints = 0;
    @Column(unique = true)
    public Long idEntreprise;
}
