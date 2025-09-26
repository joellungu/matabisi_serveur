package org.matabisi.models;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PaiementRepository implements PanacheRepository<Paiement> {

    // Créer un nouveau paiement
    @Transactional
    public Paiement create(Paiement paiement) {
        persist(paiement);
        return paiement;
    }

    // Trouver un paiement par son ID
    public Paiement findById(Long id) {
        return findById(id);
    }

    // Trouver tous les paiements
    public List<Paiement> findAlll() {
        return listAll();
    }

    // Trouver les paiements par l'ID de l'entreprise
    public List<Paiement> findByIdEntreprise(Long idEntreprise) {
        return find("idEntreprise", idEntreprise).list();
    }

    // Trouver les paiements par numéro de téléphone
    public List<Paiement> findByPhone(String phone) {
        return find("phone", phone).list();
    }

    // Trouver les paiements par référence
    public Paiement findByReference(String reference) {
        return find("reference", reference).firstResult();
    }

    // Trouver les paiements validés
    public List<Paiement> findValidatedPayments() {
        return find("valider", 1).list();
    }

    // Trouver les paiements non validés
    public List<Paiement> findNonValidatedPayments() {
        return find("valider", 0).list();
    }

    // Mettre à jour un paiement
    public Paiement update(Paiement paiement) {
        return getEntityManager().merge(paiement);
    }

    // Supprimer un paiement
    public void delete(Long id) {
        deleteById(id);
    }
}
