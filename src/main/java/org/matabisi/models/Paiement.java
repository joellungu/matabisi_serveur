package org.matabisi.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import jakarta.persistence.Entity;

@Entity
public class Paiement extends PanacheEntity {

    public Long idEntreprise;
    public String phone;
    public String reference;
    public int valider;
    public Double amount;
    public String currency;
    public String date;

}

