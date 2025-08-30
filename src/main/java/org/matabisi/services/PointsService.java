package org.matabisi.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.matabisi.models.*;

@ApplicationScoped
public class PointsService {

    @Transactional
    public void scannerProduit(Client client, Produit produit) {
        if (produit.utilise) {
            throw new IllegalStateException("Ce produit a déjà été utilisé !");
        }

        produit.utilise = true;
        produit.persist();

        Compte compte = Compte.find("clientPhone", client.telephone).firstResult();

        // créditer le compte
        compte.soldePoints += produit.valeurPoints;
        compte.persist();

        // enregistrer la transaction
        TransactionPoints tx = new TransactionPoints();
        tx.type = TransactionPoints.TypeTransaction.GAIN;
        tx.valeur = produit.valeurPoints;
        tx.clientPhone = client.telephone;
        tx.idProduit = produit.id;
        tx.persist();
    }

    @Transactional
    public void acheterRecompense(Client client, Recompense recompense) {

        Compte compte = Compte.find("clientPhone", client.telephone).firstResult();

        if (compte.soldePoints < recompense.coutPoints) {
            throw new IllegalStateException("Solde insuffisant !");
        }

        compte.soldePoints -= recompense.coutPoints;
        compte.persist();

        TransactionPoints tx = new TransactionPoints();
        tx.type = TransactionPoints.TypeTransaction.DEPENSE;
        tx.valeur = recompense.coutPoints;
        tx.clientPhone = client.telephone;
        tx.persist();
    }
}