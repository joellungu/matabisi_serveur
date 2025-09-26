package org.matabisi.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.matabisi.models.*;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
        Double lon = Double.parseDouble(""+commande.get("lon"));
        Double lat = Double.parseDouble(""+commande.get("lat"));
        //Long idEntreprise = Long.parseLong(""+commande.get("idEntreprise"));
        String nomCategorie = (String) commande.get("nomCategorie");
        Long idClient = Long.parseLong(""+commande.get("idClient"));
        String telephone = (String) commande.get("telephone");
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
            Compte compte = Compte.find("cle", client.telephone+""+produit.idEntreprise).firstResult();
            if(compte == null){
                //
                Compte compte1 = new Compte();
                compte1.clientPhone = client.telephone;
                compte1.cle = client.telephone+""+produit.idEntreprise;
                compte1.soldePoints = 0;
                compte1.idEntreprise = produit.idEntreprise;
                compte1.persist();
                //
                produit.utilise = true;
                compte1.soldePoints = compte1.soldePoints + produit.valeurPoints;
                //TransactionPoints
                TransactionPoints transactionPoints = new TransactionPoints();
                transactionPoints.idProduit = produit.id;
                transactionPoints.clientPhone = client.telephone;
                transactionPoints.date = LocalDateTime.now();
                transactionPoints.type = TransactionPoints.TypeTransaction.GAIN;
                transactionPoints.valeur = produit.valeurPoints;
                transactionPoints.lon = lon;
                transactionPoints.lat = lat;
                transactionPoints.idEntreprise = produit.idEntreprise;
                transactionPoints.persist();

            } else {
                //
                System.out.println("idEntreprise: " + produit.idEntreprise);
                //

                produit.utilise = true;
                compte.soldePoints = compte.soldePoints + produit.valeurPoints;
                //TransactionPoints
                TransactionPoints transactionPoints = new TransactionPoints();
                transactionPoints.idProduit = produit.id;
                transactionPoints.clientPhone = client.telephone;
                transactionPoints.date = LocalDateTime.now();
                transactionPoints.type = TransactionPoints.TypeTransaction.GAIN;
                transactionPoints.valeur = produit.valeurPoints;
                transactionPoints.lon = lon;
                transactionPoints.lat = lat;
                transactionPoints.idEntreprise = produit.idEntreprise;
                transactionPoints.persist();
            }
            //
            HashMap params = new HashMap();
            params.put("token", "HG59P642KW9AQ2M");//HG59P642KW9AQ2M
            params.put("to", telephone);
            params.put("from", "DESS JURY");//MYLINAFOOT//CandyShop//DESS JURY
            //params.put("message", "<#> Votre code est :" + client.get("code") + "\n" + client.get("signature"));
            Entreprise entreprise = Entreprise.findById(produit.idEntreprise);
            params.put("message", "Vous venez de reçevoir "+produit.valeurPoints+" points de la part de "+entreprise.nom);
            //
            ObjectMapper obj = new ObjectMapper();
            String data = null;
            try {
                data = obj.writeValueAsString(params);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            //
            String reponse = "";
            //
            try {
                reponse = veriSMS(data);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
             //
            return Response.ok().entity("Point attribué.").build();
        }
        //return categorie;
    }



    @GET
    @Path("/filtre")
    public Response filtrerTransactions(
            @QueryParam("type") TransactionPoints.TypeTransaction type,
            @QueryParam("dateDebut") String dateDebut,
            @QueryParam("dateFin") String dateFin,
            @QueryParam("clientPhone") String clientPhone) {

        // Conversion des dates (format attendu: 2025-08-31T00:00:00)
        LocalDateTime debut = (dateDebut != null) ? LocalDateTime.parse(dateDebut) : LocalDateTime.MIN;
        LocalDateTime fin = (dateFin != null) ? LocalDateTime.parse(dateFin) : LocalDateTime.MAX;

        // Construction dynamique de la requête
        StringBuilder query = new StringBuilder("date BETWEEN ?1 AND ?2");
        List<Object> params = new ArrayList<>();
        params.add(debut);
        params.add(fin);

        if (type != null) {
            query.append(" AND type = ?").append(params.size() + 1);
            params.add(type);
        }

        if (clientPhone != null) {
            query.append(" AND clientPhone = ?").append(params.size() + 1);
            params.add(clientPhone);
        }

        // Exécution de la requête
        List<TransactionPoints> result = TransactionPoints.list(query.toString(), params.toArray());

        return Response.ok(result).build();
    }

    @GET
    @Path("entreprise/filtre")
    public Response filtrerTransactionsEnte(
            @QueryParam("idEntreprise") Long idEntreprise,
            @QueryParam("type") TransactionPoints.TypeTransaction type,
            @QueryParam("dateDebut") String dateDebut,
            @QueryParam("dateFin") String dateFin,
            @QueryParam("clientPhone") String clientPhone) {

        // Conversion des dates (format attendu: 2025-08-31T00:00:00)
        LocalDateTime debut = (dateDebut != null) ? LocalDateTime.parse(dateDebut) : LocalDateTime.MIN;
        LocalDateTime fin = (dateFin != null) ? LocalDateTime.parse(dateFin) : LocalDateTime.MAX;

        // Construction dynamique de la requête
        StringBuilder query = new StringBuilder("date BETWEEN ?1 AND ?2");
        List<Object> params = new ArrayList<>();
        params.add(debut);
        params.add(fin);
        //params.add(idEntreprise);

        if (type != null) {
            query.append(" AND type = ?").append(params.size() + 1);
            params.add(type);
        }

        if (clientPhone != null) {
            query.append(" AND clientPhone = ?").append(params.size() + 1);
            params.add(clientPhone);
        }

        // Exécution de la requête
        List<TransactionPoints> result = TransactionPoints.list(query.toString(), params.toArray());
        List<TransactionPoints> res = new LinkedList<>();
        //
        System.out.println("idEntreprise 2: "+idEntreprise);
        for(TransactionPoints r : result){
            System.out.println("idEntreprise 2: "+idEntreprise+ " _==_ "+r.idEntreprise);
            if(r.idEntreprise.equals(idEntreprise)){
                res.add(r);
            }
        };

        return Response.ok(res).build();
    }

    @POST
    @Path("/client/2mois")
    @Transactional
    public Response getTransactionsDeuxMois(String clientPhone) {
        System.out.println("Phone: "+clientPhone);
        if (clientPhone == null || clientPhone.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Le numéro de téléphone est requis")
                    .build();
        }

        LocalDateTime maintenant = LocalDateTime.now().plusDays(1); // inclut la journée courante
        LocalDateTime ilYA2Mois = LocalDateTime.now().minusMonths(2);

        List<TransactionPoints> result = TransactionPoints.list(
                "clientPhone = ?1 AND date BETWEEN ?2 AND ?3",
                clientPhone, ilYA2Mois, maintenant
        );
        //
        List<HashMap> data = new LinkedList<>();
        //
        for(TransactionPoints transactionPoints : result) {
            HashMap trans = new HashMap();
            //
            trans.put("id", transactionPoints.id);
            trans.put("valeur", transactionPoints.valeur);
            trans.put("lon", transactionPoints.lon);
            trans.put("lat", transactionPoints.lat);
            trans.put("date", transactionPoints.date);
            //
            Produit produit = Produit.findById(transactionPoints.idProduit);
            //
            if(produit != null){
                Entreprise entreprise = Entreprise.findById(produit.idEntreprise);
                ProduitCategorie produitCategorie = ProduitCategorie.find("idEntreprise",produit.idEntreprise).firstResult();
                //
                trans.put("idEntreprise", entreprise.id);
                trans.put("nom", entreprise.nom);
                trans.put("nomCategorie", produit.nomCategorie);
                trans.put("idCategorie", produitCategorie.id);

            }
            data.add(trans);
        }

        return Response.ok(data).build();
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


}
