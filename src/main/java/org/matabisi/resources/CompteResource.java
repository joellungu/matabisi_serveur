package org.matabisi.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.matabisi.models.*;
import org.matabisi.services.PointsService;

import java.util.HashMap;
import java.util.List;

@Path("/api/Compte")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompteResource {

    @Inject
    PointsService pointsService;

    // 🔹 Create
    @POST
    @Transactional
    public Response create(Compte compte) {
        compte.persist();
        return Response.status(Response.Status.CREATED).entity(compte).build();
    }

    // 🔹 Read all
    @GET
    public List<Compte> listAll() {
        return Compte.listAll();
    }

    @GET
    @Path("points/{cle}")
    public HashMap getPoints(@PathParam("cle") String cle) {
        Compte compte = Compte.find("cle", cle).firstResult();
        if (compte == null) {
            throw new NotFoundException("Entreprise not found");
        }
        //
        HashMap pts = new HashMap();
        pts.put("points", compte.soldePoints);
        //
        return pts;
    }

    // 🔹 Read by id
    @GET
    @Path("/{id}")
    public Compte findById(@PathParam("id") Long id) {
        return Compte.findById(id);
    }

    @GET
    @Path("entreprise/{id}")
    public CompteEntreprise findCompteEntrepriseById(@PathParam("id") Long id) {
        CompteEntreprise compteEntreprise = CompteEntreprise.find("idEntreprise", id).firstResult();
        return compteEntreprise;
    }


    @GET
    @Path("client/{telephone}")
    public int compteClient(@PathParam("telephone") String telephone) {

        int points = 0;
        List<Compte> comptes = Compte.find("clientPhone",telephone).list();
        for(Compte p : comptes) {
            points = points + p.soldePoints;
        };


        return points;
    }


    // 🔹 Update
    @PUT
    @Path("/{id}")
    @Transactional
    public Compte update(@PathParam("id") Long id, Compte updatedCompte) {
        Compte compte = Compte.findById(id);
        if (compte == null) {
            throw new NotFoundException("Compte non trouvé");
        }
        compte.soldePoints = updatedCompte.soldePoints;
        compte.clientPhone = updatedCompte.clientPhone;
        return compte;
    }

    // 🔹 Delete
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = Compte.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Compte non trouvé");
        }
        return Response.noContent().build();
    }

    // --------------------------
    // GESTION DES POINTS
    // --------------------------

    @POST
    @Path("/clients/{telephone}/scan/{produitId}")
    @Transactional
    public Response scannerProduit(@PathParam("telephone") String telephone,
                                   @PathParam("produitId") Long produitId) {
        Compte compte = Compte.find("clientPhone", telephone).firstResult();
        Client client = Client.find("telephone", telephone).firstResult();
        Produit produit = Produit.findById(produitId);

        if (client == null || compte == null || produit == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            pointsService.scannerProduit(client, produit);
            return Response.ok(compte.soldePoints).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/clients/{telephone}/recompenses/{recompenseId}/acheter")
    @Transactional
    public Response acheterRecompense(@PathParam("telephone") String telephone,
                                      @PathParam("recompenseId") Long recompenseId) {
        Compte compte = Compte.find("clientPhone", telephone).firstResult();
        Client client = Client.find("telephone", telephone).firstResult();
        Recompense recompense = Recompense.findById(recompenseId);

        if (compte == null || recompense == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            pointsService.acheterRecompense(client, recompense);
            return Response.ok(compte.soldePoints).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/clients/{id}/transactions")
    public List<TransactionPoints> historique(@PathParam("id") Long clientId) {
        return TransactionPoints.list("client.id", clientId);
    }

}
