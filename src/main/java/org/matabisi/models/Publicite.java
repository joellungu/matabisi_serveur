package org.matabisi.models;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Publicite extends PanacheEntity {

    @Column(nullable = false)
    public String titre;

    @Column(length = 2000)
    public String description;// lien vers l'image de la publicité

    // Option 2 : Image en base de données (BLOB)
    @Lob
    @Basic(fetch = FetchType.LAZY)
    public byte[] image;

    public boolean actif = true;

    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
}
