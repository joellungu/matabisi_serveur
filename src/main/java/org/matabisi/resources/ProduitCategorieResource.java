package org.matabisi.resources;


import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.matabisi.models.Entreprise;
import org.matabisi.models.ProduitCategorie;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Path("/produit-categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProduitCategorieResource {

    // Liste toutes les catégories
    @GET
    public List<ProduitCategorie> getAll() {
        return ProduitCategorie.listAll();
    }

    // Récupère une catégorie par ID
    @GET
    @Path("{id}")
    public ProduitCategorie getById(@PathParam("id") Long id) {
        ProduitCategorie categorie = ProduitCategorie.findById(id);
        if (categorie == null) {
            throw new NotFoundException("Categorie non trouvée");
        }
        return categorie;
    }

    @GET
    @Path("all/{id}")
    public List<HashMap> getAllByIdEnt(@PathParam("id") Long id) {
        List<ProduitCategorie> produitCategories = ProduitCategorie.find("idEntreprise", id).list();
        //
        List<HashMap> ents = new LinkedList<>();
        //
        produitCategories.forEach((e) -> {
            //
            HashMap<String, Object> ent = new HashMap<String, Object>();
            //
            ent.put("idEntreprise", e.idEntreprise);
            ent.put("quantite", e.quantite);
            ent.put("routeCode", e.routeCode);
            ent.put("point", e.point);
            ent.put("status", e.status);
            ent.put("nom", e.nom);
            ent.put("id", e.id);
            //
            ents.add(ent);
        });
        //
        return ents;
    }

    @GET
    @Path("logo/{id}")
    public byte[] getLogoById(@PathParam("id") Long id) {
        ProduitCategorie entreprise = ProduitCategorie.findById(id);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise not found");
        }
        return entreprise.logo;
    }

    // Crée une nouvelle catégorie
    @POST
    @Transactional
    public Response create(ProduitCategorie categorie) {
        categorie.persist();
        return Response.status(Response.Status.CREATED).entity(categorie).build();
    }

    // Met à jour une catégorie existante
    @PUT
    @Path("{id}")
    @Transactional
    public ProduitCategorie update(@PathParam("id") Long id, ProduitCategorie data) {
        ProduitCategorie categorie = ProduitCategorie.findById(id);
        if (categorie == null) {
            throw new NotFoundException("Categorie non trouvée");
        }
        categorie.status = data.status;
        categorie.idEntreprise = data.idEntreprise;
        categorie.point = data.point;
        categorie.quantite = data.quantite;
        categorie.routeCode = data.routeCode;
        //categorie.logo = data.logo;
        return categorie;
    }


    @PUT
    @Path("status/{id}")
    @Transactional
    public Response updateStatus(@PathParam("id") Long id, int data) {
        ProduitCategorie categorie = ProduitCategorie.findById(id);
        if (categorie == null) {
            throw new NotFoundException("Categorie non trouvée");
        }
        categorie.status = data;
        return Response.ok().entity("Ok").build();
    }

    // Supprime une catégorie
    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = ProduitCategorie.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Categorie non trouvée");
        }
        return Response.noContent().build();
    }
}

