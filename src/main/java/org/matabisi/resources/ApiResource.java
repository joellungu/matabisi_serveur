package org.matabisi.resources;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.matabisi.models.*;
import org.matabisi.services.PointsService;

import java.util.List;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "API Resource", description = "Gestion des utilisateurs")
public class ApiResource {



    // --------------------------
    // ENTREPRISES & PRODUITS
    // --------------------------

    @POST
    @Path("/entreprises")
    @Transactional
    public Entreprise createEntreprise(Entreprise entreprise) {
        entreprise.persist();
        return entreprise;
    }

    @POST
    @Path("/entreprises/{id}/produits")
    @Transactional
    public Produit addProduit(@PathParam("id") Long entrepriseId, Produit produit) {
        Entreprise entreprise = Entreprise.findById(entrepriseId);
        if (entreprise == null) {
            throw new WebApplicationException("Entreprise introuvable", 404);
        }
        produit.idEntreprise = entreprise.id;
        produit.persist();
        return produit;
    }



    // --------------------------
    // RECOMPENSES
    // --------------------------

    @POST
    @Path("/recompenses")
    @Transactional
    public Recompense createRecompense(Recompense recompense) {
        recompense.persist();
        return recompense;
    }

    @GET
    @Path("/recompenses")
    public List<Recompense> listRecompenses() {
        return Recompense.listAll();
    }
}