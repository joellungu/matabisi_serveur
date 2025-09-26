package org.matabisi.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class CreditEntreprise extends PanacheEntity {
    public Long idEntreprise;
    public double solde;
}
