package org.matabisi.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.matabisi.models.CreditEntreprise;
import org.matabisi.models.Entreprise;
import org.matabisi.models.Paiement;
import org.matabisi.models.PaiementRepository;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Path("/paiement")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PaiementController {

    private final Map<String, CompletableFuture<String>> waitingRequests = new ConcurrentHashMap<>();

    @Inject
    PaiementRepository paiementRepository;

    public String lancer(String devise, String telephone, Double m, String reference) {
        System.out.println("la devise: $"+devise+"lE MONTANT: $"+m);
        //String dev = devise == "USD" ? "USD":"CDF";
        //double montant = deviseMetier.conversion(m,1L, devise=="USD");
        //
        String dev = "USD";
        double montant = m;
        //
        System.out.println("la devise: $"+devise+"lE MONTANT: $"+montant);
        //String urlPost = "http://41.243.7.46:3006/flexpay/api/rest/v1/paymentService";
        //////////////////http://41.243.7.46:3006/api/rest/v1/paymentService

        String urlPost = "https://backend.flexpay.cd/api/rest/v1/paymentService";
        String body = "{\n" +
                "  \"merchant\":\"Min_EDU-NC\"," +
                "  \"type\":1," +
                "  \"reference\": \""+reference+"\"," +
                "  \"phone\": \""+telephone+"\"," +
                "  \"amount\": \""+montant+"\"," +
                "  \"currency\":\""+dev+"\"," +
                "  \"callbackUrl\":\"https://epst-serveur-a595d15d6608.herokuapp.com/paiement/trigger\"" +
                "}";

        var requete = HttpRequest.newBuilder()
                .uri(URI.create(urlPost))
                .header("Content-Type","application/json")
                .header("Authorization","Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJcL2xvZ2luIiwicm9sZXMiOlsiTUVSQ0hBTlQiXSwiZXhwIjoxNzk0NzYxNDYzLCJzdWIiOiJlZGZiYTY0ZTYxNjM1NWMzYjdjZDJjYzZiZTA5NzMzYiJ9.1gps60CJKzY1CP8XgAEvx8ArRAfqD9v5a9PeJr4qA6c")
                //.header("Authorization","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MkkydjNXQkhUUVdpTlg4ejhQVSIsInJvbGVzIjpbIk1FUkNIQU5UIl0sImlzcyI6Ii9sb2dpbiIsImV4cCI6MTczNTY4NjAwMH0.b3H5IvM1cNtQ5I3Xz3Rf3hBO_pbgFgQ5VpdKrFUI3g0")
                //.header("Authorization","Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJcL2xvZ2luIiwicm9sZXMiOlsiTUVSQ0hBTlQiXSwiZXhwIjoxNzM3NTUyMDEwLCJzdWIiOiI4ZTE4NzJlODQwZTc5YjM5OWIxMDliMmYyNjk5YWY3YSJ9.co6sS0YEdCy3v3nja0NHvS5dYnMNmjZPJET_Ri7pB0E")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        var client = HttpClient.newHttpClient();
        try {
            var reponse = client.send(requete, HttpResponse.BodyHandlers.ofString());
            System.out.println(reponse.statusCode());
            System.out.println(reponse.body());
            return reponse.body();
        } catch (IOException e) {
            System.out.println(e);
            return "";
        } catch (InterruptedException e) {
            System.out.println(e);
            return "";
        }

        //return "";
    }

    public String checklancer(String orderNumer) {

        String urlPost = "https://backend.flexpay.cd/api/rest/v1/check/"+orderNumer;
        //////////////////http://41.243.7.46:3006/api/rest/v1/paymentService
        var requete = HttpRequest.newBuilder()
                .uri(URI.create(urlPost))
                .header("Content-Type","application/json")
                .header("Authorization","Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJcL2xvZ2luIiwicm9sZXMiOlsiTUVSQ0hBTlQiXSwiZXhwIjoxNzk0NzYxNDYzLCJzdWIiOiJlZGZiYTY0ZTYxNjM1NWMzYjdjZDJjYzZiZTA5NzMzYiJ9.1gps60CJKzY1CP8XgAEvx8ArRAfqD9v5a9PeJr4qA6c")
                .GET()
                //.POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        var client = HttpClient.newHttpClient();
        try {
            var reponse = client.send(requete, HttpResponse.BodyHandlers.ofString());
            System.out.println(reponse.statusCode());
            System.out.println(reponse.body());
            return reponse.body();
        } catch (IOException e) {
            System.out.println(e);
            return "";
        } catch (InterruptedException e) {
            System.out.println(e);
            return "";
        }

        //return "";
    }
    Toolkit toolkit;
    Timer timer;

    @Path("paie")
    @POST
    //@Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response lancerPaiment(Paiement paiement
                                  //HashMap paiement
    ) {
        //
        LocalDateTime localDateTime = LocalDateTime.now();
        //
        paiement.date = localDateTime.toString();
        //
        Paiement pt = new Paiement();
        //
        pt.date = paiement.date;
        pt.reference = paiement.reference;
        pt.amount = paiement.amount;
        pt.valider = paiement.valider;
        pt.currency = paiement.currency;
        pt.idEntreprise = paiement.idEntreprise;
        pt.phone = paiement.phone;
        //
        paiementRepository.create(pt);
        //
        //pt.persist();
        //
        System.out.println("Le id vaut: "+paiement.id);
        System.out.println("Le id vaut: "+paiement.date);
        System.out.println("Le id vaut: "+paiement.reference);
        System.out.println("Le id vaut: "+paiement.amount);
        System.out.println("Le id vaut: "+paiement.currency);
        System.out.println("Le id vaut: "+paiement.phone);
        //
        String rep = lancer(
                paiement.currency,
                paiement.phone,
                paiement.amount,
                paiement.reference
        );
        //"req-" + System.currentTimeMillis(); // Générer un ID unique pour la requête
        // Créer un CompletableFuture pour cette requête
        CompletableFuture<String> future = new CompletableFuture<>();
        waitingRequests.put(paiement.reference, future);

        try {
            //
            String result = future.get(); // Bloque jusqu'à ce que la future soit complétée
            return Response.ok(result).build();
        } catch (ExecutionException e) {
            //return Response.serverError().entity("Error: " + e.getMessage()).build();
            return Response.status(404).entity(e.getMessage()).build();
        } catch (InterruptedException e) {
            return Response.status(404).entity(e.getMessage()).build();
        } finally {
            // Nettoyer la future après utilisation
            waitingRequests.clear();
            //return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
        //return repData;
    }

    /////////////////////////////////////////////////////////////////////////////

    @POST
    @Path("/trigger")
    @Transactional
    public Response triggerRequest(String reponse) {
        //
        System.out.println("Reponse: "+reponse);
        //
        HashMap hashRep = new HashMap<>();
        //
        ObjectMapper mapper = new ObjectMapper();
        //
        try {
            JsonNode result = mapper.readTree(reponse);
            //
            // Compléter la première future en attente (ou une spécifique selon votre logique)
            Iterator iterator = waitingRequests.entrySet().iterator();
            //
            while (iterator.hasNext()) {
                //
                Map.Entry<String, CompletableFuture<String>> entry = (Map.Entry<String, CompletableFuture<String>>) iterator.next();
                //
                String requestId = entry.getKey();
                System.out.println("requestId: "+requestId);
                if (result.get("reference").asText().equals(requestId)) {
                    //
                    String reference = result.get("reference").asText();
                    //
                    Paiement paiement = Paiement.find("reference", reference).firstResult();
                    //paiement.reference;
                    //
                    CompletableFuture<String> future = entry.getValue();
                    //
                    CreditEntreprise creditEntreprise = CreditEntreprise.findById(paiement.idEntreprise);
                    if(creditEntreprise == null){
                        CreditEntreprise creditEntreprise1 = new CreditEntreprise();
                        creditEntreprise1.solde = paiement.amount;// Je vais utiliser ceci lors de l'ajout des points
                        creditEntreprise1.idEntreprise = paiement.idEntreprise;
                        creditEntreprise1.persist();
                        //
                    } else {
                        creditEntreprise.solde = creditEntreprise.solde + paiement.amount;
                    }
                    //
                    // Compléter la future pour débloquer la requête en attente
                    //
                    future.complete(reponse);
                }
                //
            }
            //
        }catch (Exception ex) {
            System.out.println("Erreur 1: "+ ex.getMessage());
            System.out.println("Erreur 2: "+ ex.getCause());
            //future.complete(reponse);
        }


        return Response.ok("Ok").build();
    }
}
