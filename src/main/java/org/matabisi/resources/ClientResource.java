package org.matabisi.resources;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.matabisi.models.Client;
import org.matabisi.models.Compte;
import org.matabisi.models.Entreprise;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

@Path("/api/Client")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "API Resource", description = "Gestion des utilisateurs")
public class ClientResource {

    // --------------------------
    // CLIENTS & COMPTES
    // --------------------------

    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2).build();

    @POST
    @Path("/login")
    @Transactional
    public Response enoiSMS(HashMap<String, String> client) throws IOException, URISyntaxException, InterruptedException {
        //String message = "<#> Votre code est :"+code+"\n"+signature;
        HashMap params = new HashMap();
        params.put("token", "HG59P642KW9AQ2M");//HG59P642KW9AQ2M
        params.put("to", client.get("telephone"));
        params.put("from", "DESS JURY");//MYLINAFOOT//CandyShop//DESS JURY
        params.put("message", "<#> Votre code est :" + client.get("code") + "\n" + client.get("signature"));
        //
        //String textRep2 = envoiOTP(client.get("telephone"), client.get("code"), client.get("signature"));
        //
        //System.out.println("textRep2: "+textRep2);
        //
        ObjectMapper obj = new ObjectMapper();
        String data = obj.writeValueAsString(params);
        String reponse = veriSMS(data);
        ObjectMapper mapper = new ObjectMapper();
        //
        String nomComplet = "";
        Client client1 = Client.find("telephone", client.get("telephone")).firstResult();
        //
        if (client1 != null) {
            nomComplet = client1.nom;
        }

        // Transformer le JSON en Map
        var map = mapper.readValue(reponse, java.util.Map.class);
        //
        HashMap repData = new HashMap();
        repData.put("status", map.get("status"));
        repData.put("nomComplet", nomComplet);
        //
        return Response.ok().entity(repData).build();
    }

    @POST
    @Path("/clients")
    @Transactional
    public Client createClient(Client client) {
        Client client1 = Client.find("telephone", client.telephone).firstResult();
        if (client1 != null) {
            client1.nom = client.nom;
            // créer un compte lié automatiquement
            List<Entreprise> entrepriseList = Entreprise.listAll();
            //
            System.out.println("Le entrepriseList: "+entrepriseList.size());
            //
            entrepriseList.forEach((e) -> {
                //
                HashMap<String, Object> params = new HashMap<>();
                params.put("idEntreprise", e.id);
                params.put("clientPhone", client1.telephone);

                Compte compte = Compte.find("idEntreprise =: idEntreprise and clientPhone =: clientPhone", params).firstResult();
                //
                System.out.println("Le compte: "+compte.clientPhone);
                 //
                if(compte == null){
                    Compte compte1 = new Compte();
                    compte1.clientPhone = client1.telephone;
                    compte1.cle = client1.telephone+""+e.id;
                    compte1.soldePoints = 0;
                    compte1.idEntreprise = e.id;
                    compte1.persist();
                }
            });
            //
            return client1;
        } else {
            client.persist();
            // créer un compte lié automatiquement
            List<Entreprise> entrepriseList = Entreprise.listAll();
            //
            entrepriseList.forEach((e) -> {
                //
                try {
                    Compte compte = new Compte();
                    compte.clientPhone = client1.telephone;
                    compte.cle = client1.telephone+""+e.id;
                    compte.soldePoints = 0;
                    compte.idEntreprise = e.id;
                    compte.persist();
                } catch (Exception ee) {
                    System.out.println("Ce compte exite déjà. "+ee.getMessage());
                }
            });

            return client;
        }
    }

    @GET
    @Path("/clients/{id}/solde")
    public Response getSolde(@PathParam("telephone") String telephone) {
        Compte compte = Compte.find("clientPhone", telephone).firstResult();
        if (compte == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(compte.soldePoints).build();
    }

    @GET
    @Path("nombre")
    public Response getNombre() {
        long nombre = Client.count();
        //
        return Response.ok(nombre).build();
    }


    private String veriSMS(String data) throws URISyntaxException, IOException, InterruptedException {
        //https://test.new.rawbankillico.com:4003/RAWAPIGateway/ecommerce/payment/770013/000007316065
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.keccel.com/sms/v2/message.asp"))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                //.POST(HttpRequest.BodyPublishers.ofString("{\r\n\t\"mobilenumber\": \""+telephone+"\",\r\n\t\"trancurrency\":\""+devise+"\",\r\n\t\"amounttransaction\": \""+montant+"\",\r\n\t\"merchantid\": \"brnch0000000000000801\",\r\n\t\"invoiceid\":\"123456715\",\r\n\t\"terminalid\":\"123456789012\",\r\n\t\"encryptkey\": \"AX8dsXSKqWlJqRhpnCeFJ03CzqMsCisQVUNSymXKqeiaQdHf8eQSyITvCD6u3CLZJBebnxj5LbdosC/4OvUtNbAUbaIgBKMC5MpXGRXZdfAlGsVRfHTmjaGDe1RIiHKP\",\r\n\t\"securityparams\":{\r\n\t\t\"gpslatitude\": \"24.864190\",\r\n\t\t\"gpslongitude\": \"67.090420\"\r\n\t}\r\n}"))
                .build();
        //
        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        //
        System.out.println("La reponse 2: "+response.body());
        return response.body();
    }

    private String envoiOTP(String telephone, String code, String signature) {
        //
        String senderId = "FlowRewards";
        String message = "<#> Votre code est :"+code+"\n"+signature;
        String mobileNumbers = telephone;
        String apiKey = "Vdl79bvbbgqMA7mQE%2BhIV3Abcr16MGCDPtrHyJq%2BtHE%3D&ClientId=df72090a-8614-4ff6-8be4-5ada54816646";

        // Construction de l'URL avec encodage
        String baseUrl = "http://164.68.101.225:6005/api/v2/SendSMS";
        String fullUrl = String.format("%s?SenderId=%s&Message=%s&MobileNumbers=%s&ApiKey=%s",
                baseUrl,
                URLEncoder.encode(senderId, StandardCharsets.UTF_8),
                URLEncoder.encode(message, StandardCharsets.UTF_8),
                URLEncoder.encode(mobileNumbers, StandardCharsets.UTF_8),
                apiKey
        );
        //Random random = new Random();
        // Génère un nombre entre 1000000 et 9999999 (inclus)
        //int number = 1_000_000 + random.nextInt(9_000_000);
        //
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(fullUrl))
                    .GET()
                    .build();
            //
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            //
            System.out.println("GET status: " + response.statusCode());
            System.out.println("GET body:\n" + response.body());
            //
            return (String) response.body();
            //
        } catch (Exception e) {
            System.out.println("Erreur:\n" + e.toString());
            return "status: erreur";
        }
    }
}
