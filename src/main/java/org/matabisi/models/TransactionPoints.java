package org.matabisi.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class TransactionPoints extends PanacheEntity {

    public enum TypeTransaction { GAIN, DEPENSE }

    @Enumerated(EnumType.STRING)
    public TypeTransaction type;

    public int valeur;
    public LocalDateTime date = LocalDateTime.now();

    public String clientPhone;

    public Long idProduit; // seulement si transaction liée à un scan produit
}