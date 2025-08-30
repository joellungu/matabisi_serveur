package org.matabisi.services;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.matabisi.models.Entreprise;

@ApplicationScoped
public class EntrepriseRepository implements PanacheRepository<Entreprise> {

    public Entreprise findByEmail(String email) {
        return find("email", email).firstResult();
    }
}