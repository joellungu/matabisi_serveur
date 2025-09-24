package org.matabisi.resources;


import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import io.vertx.ext.web.FileUpload;
import org.jboss.resteasy.reactive.RestForm;
import org.matabisi.models.*;
import org.matabisi.services.EntrepriseRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Path("/api/Entreprise")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EntrepriseResource {

    @Inject
    EntrepriseRepository entrepriseRepository;


    // CREATE
    @POST
    @Transactional
    public Response create(Entreprise entreprise) {
        // Hash du mot de passe
        Entreprise entreprise1 = Entreprise.find("email", entreprise.email).firstResult();
        if(entreprise1 != null){
            entreprise1.nom = entreprise.nom;
            entreprise1.logo = entreprise.logo;
            entreprise1.email = entreprise.email;
            entreprise1.secteur = entreprise.secteur;
            return Response.ok().entity(entreprise1).build();
        } else {
            entreprise.motDePasse = BcryptUtil.bcryptHash(entreprise.motDePasse);
            entrepriseRepository.persist(entreprise);
            //
            try {
                CompteEntreprise compte = new CompteEntreprise();
                compte.idEntreprise = entreprise.id;
                compte.soldePoints = 0;
                compte.persist();
            } catch (Exception e) {
                System.out.println("Ce compte exite déjà.");
            }
            //
        }
        return Response.status(Response.Status.CREATED).entity(entreprise).build();
    }
    ///

    /*
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(@RestForm String nom,
                           @RestForm String secteur,
                           @RestForm String email,
                           @RestForm String motDePasse,
                           @RestForm FileUpload logo) throws IOException {
        Entreprise e = new Entreprise();
        e.nom = nom;
        e.secteur = secteur;
        e.email = email;
        e.motDePasse = BcryptUtil.bcryptHash(motDePasse);
        //e.logo = logo.;
        if (logo != null) {
            e.logo = Files.readAllBytes(Paths.get(logo.uploadedFileName()));
        }
        //
        e.persist(); // Panache persiste automatiquement

        return Response.status(Response.Status.CREATED).entity(e).build();
    }
    */

    /* READ ALL
    @GET
    public List<Entreprise> listAll() {
        return entrepriseRepository.listAll();
    }
    */

    @GET
    public List<EntrepriseDTO> getEntreprisesAvecProduits() {
        List<Entreprise> entreprises = Entreprise.listAll();

        return entreprises.stream()
                .map(e -> {
                    List<ProduitCategorie> produits = ProduitCategorie.list("idEntreprise = ?1 and status = ?2", e.id, 1);
                    return new EntrepriseDTO(e, produits);
                })
                .toList();
    }

    @GET
    @Path("/all")
    @RolesAllowed("admin")
    @Transactional
    public Response getAllEntreprises() {
        List<Entreprise> entreprises = entrepriseRepository.listAll();
        List<HashMap> ents = new LinkedList<>();
        //
        entreprises.forEach((e) -> {
            //
            HashMap<String, Object> ent = new HashMap<String, Object>();
            //
            ent.put("email", e.email);
            ent.put("secteur", e.secteur);
            ent.put("status", e.status);
            ent.put("nom", e.nom);
            ent.put("id", e.id);
            //
            ents.add(ent);
        });
        //
        return Response.ok().entity(ents).build();
    }

    // READ ONE
    @GET
    @Path("/{id}")
    public Entreprise getById(@PathParam("id") Long id) {
        Entreprise entreprise = entrepriseRepository.findById(id);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise not found");
        }
        return entreprise;
    }

    @GET
    @Path("logo/{id}")
    public byte[] getLogoById(@PathParam("id") Long id) {
        Entreprise entreprise = entrepriseRepository.findById(id);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise not found");
        }
        return entreprise.logo;
    }

    // UPDATE
    @PUT
    @Path("/{id}")
    @Transactional
    public String update(@PathParam("id") Long id, Entreprise data) {
        Entreprise entreprise = entrepriseRepository.findById(id);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise not found");
        }

        if(data.logo == null) {
            //
            if (data.motDePasse != null && !data.motDePasse.isBlank()) {
                entreprise.motDePasse = BcryptUtil.bcryptHash(data.motDePasse);
            }
            //
            entreprise.nom = data.nom;
            entreprise.secteur = data.secteur;
            entreprise.email = data.email;
            entreprise.fraisRetraitUSD = data.fraisRetraitUSD;
        } else {
            //
            if (data.motDePasse != null && !data.motDePasse.isBlank()) {
                entreprise.motDePasse = BcryptUtil.bcryptHash(data.motDePasse);
            }
            //
            entreprise.logo = data.logo;
            entreprise.nom = data.nom;
            entreprise.secteur = data.secteur;
            entreprise.email = data.email;
            entreprise.fraisRetraitUSD = data.fraisRetraitUSD;
        }

        //entreprise.logo = data.logo;
        return "Mise à jour éffectué";
    }

    @PUT
    @Path("status/{id}")
    @Transactional
    public Response updateStatus(@PathParam("id") Long id, int data) {
        Entreprise categorie = Entreprise.findById(id);
        if (categorie == null) {
            throw new NotFoundException("Categorie non trouvée");
        }
        categorie.status = data;
        return Response.ok().entity("Ok").build();
    }

    // DELETE
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Entreprise entreprise = entrepriseRepository.findById(id);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise not found");
        }
        CompteEntreprise.delete("idEntreprise", id);
        entrepriseRepository.delete(entreprise);
        return Response.noContent().build();
    }
}
