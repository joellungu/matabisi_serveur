package org.matabisi.resources;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.matabisi.models.Client;
import org.matabisi.models.Compte;
import org.matabisi.models.Produit;

import java.util.HashMap;

@Path("api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionPointsResource {
    //
    @GET
    @Path("{id}")
    public Produit getById(@PathParam("id") Long id) {
        Produit categorie = Produit.findById(id);
        if (categorie == null) {
            throw new NotFoundException("Categorie non trouvée");
        }
        return categorie;
    }

    @POST
    @Transactional
    public Response transaction(HashMap commande) {
        //
        String codeUnique = (String) commande.get("codeUnique");
        Long lon = Long.parseLong(""+commande.get("lon"));
        Long lat = Long.parseLong(""+commande.get("lat"));
        String nomCategorie = (String) commande.get("nomCategorie");
        Long idClient = Long.parseLong(""+commande.get("idClient"));
        //
        Produit produit = Produit.find("codeUnique = ?1 and nomCategorie = ?2", codeUnique, nomCategorie).firstResult();
        if (produit == null) {
            throw new NotFoundException("Produit non trouvé.");
        } else if (produit.utilise) {
            throw new NotFoundException("Produit déjà utilisé.");
        } else {
            //
            Client client = Client.findById(idClient);
            //
            Compte compte = Compte.find("clientPhone", client.telephone).firstResult();
            //
            produit.utilise = true;
            compte.soldePoints = compte.soldePoints + produit.valeurPoints;
            //
            return Response.ok().entity("Point attribué.").build();
        }
        //return categorie;
    }


}
