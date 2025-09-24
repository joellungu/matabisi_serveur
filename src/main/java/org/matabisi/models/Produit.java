package org.matabisi.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class Produit extends PanacheEntity {

    public String nomCategorie;
    @Column(unique = true)
    public String codeUnique; // QR ou code alphanumérique
    public int valeurPoints;
    public boolean utilise = false;
    public Long idEntreprise;
    public LocalDateTime updatedAt;
}
