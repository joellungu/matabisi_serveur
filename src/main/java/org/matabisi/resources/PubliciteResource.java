package org.matabisi.resources;


import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.matabisi.models.Publicite;
import org.matabisi.models.PubliciteDTO;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

@Path("/publicites")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PubliciteResource {
    private PubliciteDTO toDTO(Publicite pub) {
        PubliciteDTO dto = new PubliciteDTO();
        dto.id = pub.id;
        dto.titre = pub.titre;
        dto.description = pub.description;
        dto.actif = pub.actif;
        dto.createdAt = pub.createdAt;
        dto.updatedAt = pub.updatedAt;
        return dto;
    }

    @GET
    public List<PubliciteDTO> getAll() {
        return Publicite.<Publicite>listAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @GET
    @Path("/{id}")
    public Response getOne(@PathParam("id") Long id) {
        Publicite pub = Publicite.findById(id);
        if (pub == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(toDTO(pub)).build();
    }

    @POST
    @Transactional
    public Response create(Publicite pub) {
        pub.createdAt = LocalDateTime.now();
        pub.updatedAt = LocalDateTime.now();
        pub.persist();
        return Response.status(Response.Status.CREATED).entity(toDTO(pub)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, Publicite updated) {
        Publicite pub = Publicite.findById(id);
        if (pub == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        pub.titre = updated.titre;
        pub.description = updated.description;
        if (updated.image != null) {
            pub.image = updated.image;
        }
        pub.actif = updated.actif;
        pub.updatedAt = LocalDateTime.now();
        return Response.ok(toDTO(pub)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = Publicite.deleteById(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Route dédiée pour l’image
    @GET
    @Path("/{id}/image")
    @Produces({"image/png", "image/jpeg"})
    @Transactional
    public Response getImage(@PathParam("id") Long id) {
        Publicite pub = Publicite.findById(id);
        if (pub == null || pub.image == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(new ByteArrayInputStream(pub.image)).build();
    }
}
