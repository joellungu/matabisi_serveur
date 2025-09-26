package org.matabisi.resources;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.matabisi.models.Entreprise;
import org.matabisi.services.EntrepriseRepository;

import java.time.Duration;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    EntrepriseRepository entrepriseRepository;

    // DTO pour login
    public static class LoginRequest {
        public String email;
        public String motDePasse;
    }

    public static class LoginResponse {
        public Long id;
        public String token;
        public String nom;
        public String email;
        public String secteur;
        public int fraisRetraitUSD;
    }

    @POST
    @Path("/login")
    @Transactional
    public Response login(LoginRequest request) {
        Entreprise entreprise = entrepriseRepository.findByEmail(request.email);
        if (entreprise == null) {
            return loginAdmin(request);
        }

        // Vérification mot de passe
        if (!BcryptUtil.matches(request.motDePasse, entreprise.motDePasse)) {
            throw new NotAuthorizedException("Email ou mot de passe incorrect");
        }

        // Génération du JWT
        String token = Jwt.issuer("https://matabisi.com")
                .subject(entreprise.email)
                .claim("nom", entreprise.nom)
                .claim("secteur", entreprise.secteur)
                .groups("entreprise") // role
                .expiresIn(Duration.ofHours(24)) // durée de validité du token
                .sign();

        LoginResponse response = new LoginResponse();
        response.id = entreprise.id;
        response.token = token;
        response.nom = entreprise.nom;
        response.email = entreprise.email;
        response.secteur = entreprise.secteur;
        response.fraisRetraitUSD = entreprise.fraisRetraitUSD;

        return Response.ok(response).build();
    }

    public Response loginAdmin(LoginRequest request) {
        // Vérifie si l'email et le mot de passe correspondent
        if (!"admin@gmail.com".equals(request.email) || !"admin1234".equals(request.motDePasse)) {
            throw new NotAuthorizedException("Identifiants admin invalides");
        }

        // Génération du JWT admin
        String token = Jwt.issuer("https://matabisi.com")
                .subject("admin") // identifiant du token
                .claim("nom", "Super Admin")
                .claim("email", request.email)
                .groups("admin") // rôle admin
                .expiresIn(Duration.ofHours(12)) // durée plus courte pour admin
                .sign();

        LoginResponse response = new LoginResponse();
        response.token = token;
        response.nom = "Super Admin";
        response.email = request.email;
        response.secteur = "Administration";

        return Response.ok(response).build();
    }

    @POST
    @Path("/update-password")
    @Transactional
    public Response updatePassword(UpdatePasswordRequest request) {
        // Récupérer l'email à partir du token JWT
        String email = jwt.getSubject();
        System.out.println("L'email: "+email);
        System.out.println("L'email: "+request.newPassword);

        Entreprise entreprise = Entreprise.find("email", email).firstResult();
        if (entreprise == null) {
            System.out.println("L'email: "+email);
            System.out.println("L'email: "+request.newPassword);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!BcryptUtil.matches(request.getOldPassword(), entreprise.motDePasse)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Ancien mot de passe incorrect").build();
        }

        String newPasswordHash = BcryptUtil.bcryptHash(request.getNewPassword());
        entreprise.motDePasse = newPasswordHash;

        entreprise.persist();

        return Response.ok().build();
    }

    public static class UpdatePasswordRequest {
        private String oldPassword;
        private String newPassword;

        // Getters and setters
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

}
