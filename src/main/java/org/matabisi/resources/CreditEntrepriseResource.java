package org.matabisi.resources;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.matabisi.models.CreditEntreprise;

import java.util.List;

@Path("/credits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CreditEntrepriseResource {

    @GET
    public List<CreditEntreprise> listAll() {
        return CreditEntreprise.listAll();
    }

    @GET
    @Path("/entreprise/{idEntreprise}/check/{nombre}")
    public boolean checkSolde(
            @PathParam("idEntreprise") Long idEntreprise,
            @PathParam("nombre") double nombre) {
        CreditEntreprise credit = CreditEntreprise.find("idEntreprise", idEntreprise).firstResult();
        if (credit == null) {
            throw new NotFoundException("Entreprise avec id " + idEntreprise + " introuvable");
        }
        return nombre <= credit.solde;
    }


    @GET
    @Path("/{id}")
    public CreditEntreprise findById(@PathParam("id") Long id) {
        return CreditEntreprise.findById(id);
    }

    @POST
    @Transactional
    public Response create(CreditEntreprise credit) {
        credit.persist();
        return Response.status(Response.Status.CREATED).entity(credit).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, CreditEntreprise updated) {
        CreditEntreprise credit = CreditEntreprise.findById(id);
        if (credit == null) {
            throw new NotFoundException("CreditEntreprise avec id " + id + " introuvable");
        }
        credit.idEntreprise = updated.idEntreprise;
        credit.solde = updated.solde;
        return Response.ok(credit).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = CreditEntreprise.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("CreditEntreprise avec id " + id + " introuvable");
        }
        return Response.noContent().build();
    }
}
