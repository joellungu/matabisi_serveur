package org.matabisi.resources;


import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.matabisi.models.CompteEntreprise;
import org.matabisi.models.Produit;
import org.matabisi.models.ProduitCategorie;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Path("api/produit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProduitResource {

    @GET
    public List<Produit> getAll() {
        return Produit.listAll();
    }

    // Récupère une catégorie par ID
    @GET
    @Path("{id}")
    public Produit getById(@PathParam("id") Long id) {
        Produit categorie = Produit.findById(id);
        if (categorie == null) {
            throw new NotFoundException("Categorie non trouvée");
        }
        return categorie;
    }

    @GET
    @Path("all/{id}/{nomCategorie}")
    public List<HashMap> getAllByIdEnt(@PathParam("id") Long id, @PathParam("nomCategorie") String nomCategorie,
                                       @QueryParam("page") @DefaultValue("0") int page) {
        PanacheQuery<Produit> query = Produit.find("idEntreprise = ?1 and utilise = ?2 and nomCategorie = ?3", id, false, nomCategorie);
        //
        int pageSize = 20;
        //PanacheQuery<Produit> query = Produit.findAll();
        List<Produit> produitCategories = query.list();
        query.page(page, pageSize);
        System.out.println("La taille: "+produitCategories.size());
        //
        List<HashMap> ents = new LinkedList<>();
        //
        produitCategories.forEach((e) -> {
            //
            HashMap<String, Object> ent = new HashMap<String, Object>();
            //
            ent.put("idEntreprise", e.idEntreprise);
            ent.put("codeUnique", e.codeUnique);
            ent.put("utilise", e.utilise);
            ent.put("valeurPoints", e.valeurPoints);
            ent.put("nomCategorie", e.nomCategorie);
            ent.put("id", e.id);
            //
            ents.add(ent);
        });
        //
        return ents;
    }

    @POST
    @Path("{idEntreprise}")
    @Transactional
    public Response create(@PathParam("idEntreprise") Long idEntreprise, List<Produit> produits) {
        //
        CompteEntreprise compteEntreprise = CompteEntreprise.findById(idEntreprise);
        //
        int v = 0;
        for (Produit produit : produits) {
            //
            v = v + produit.valeurPoints;
            //
            produit.persist();
        };
        //
        if (compteEntreprise != null) {
            //
            compteEntreprise.soldePoints = compteEntreprise.soldePoints + v;
            //categorie.persist();
        }
        return Response.status(Response.Status.CREATED).entity(produits).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response supprimer(@PathParam("id") Long id) {
        Produit.deleteById(id);
        //categorie.persist();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/count")
    public long countProduits() {
        return Produit.count();
    }
}
