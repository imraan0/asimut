package fr.asimov.service;

import fr.asimov.api.ApiClient;
import fr.asimov.model.RechercheStage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RechercheStageService {

    public static List<RechercheStage> getByEleve(int eleveId) throws Exception {
        String response = ApiClient.get("/stages/recherches/" + eleveId);
        JSONObject json = new JSONObject(response);
        JSONArray array = json.getJSONArray("recherches");
        List<RechercheStage> recherches = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            recherches.add(parse(array.getJSONObject(i)));
        }
        return recherches;
    }

    public static boolean hasAlerte(int eleveId) throws Exception {
        String response = ApiClient.get("/stages/recherches/" + eleveId);
        JSONObject json = new JSONObject(response);
        return json.getBoolean("alerte");
    }

    public static void create(int eleveId, String nomEntreprise, String nomContact,
                              String emailContact, String statut) throws Exception {
        JSONObject body = new JSONObject();
        body.put("nom_entreprise", nomEntreprise);
        body.put("nom_contact", nomContact);
        body.put("email_contact", emailContact);
        body.put("statut", statut);
        ApiClient.post("/stages/recherches/" + eleveId, body);
    }

    public static void update(int rechercheId, String nomEntreprise, String nomContact,
                              String emailContact, String statut, String resultat) throws Exception {
        JSONObject body = new JSONObject();
        body.put("nom_entreprise", nomEntreprise);
        body.put("nom_contact", nomContact);
        body.put("email_contact", emailContact);
        body.put("statut", statut);
        body.put("resultat", resultat);
        ApiClient.put("/stages/recherches/" + rechercheId, body);
    }

    public static void delete(int rechercheId) throws Exception {
        ApiClient.delete("/stages/recherches/" + rechercheId);
    }

    private static RechercheStage parse(JSONObject obj) {
        int id = obj.getInt("id");
        int eleveId = obj.getInt("eleve_id");
        String nomEntreprise = obj.optString("nom_entreprise", "");
        String nomContact = obj.optString("nom_contact", "");
        String emailContact = obj.optString("email_contact", "");
        int nbLettresEnvoyees = obj.optInt("nb_lettres_envoyees", 0);
        int nbLettresRecues = obj.optInt("nb_lettres_recues", 0);
        String dateEntretien = obj.isNull("date_entretien") ? "" : obj.optString("date_entretien", "");
        String resultat = obj.isNull("resultat") ? "" : obj.optString("resultat", "");
        String statut = obj.optString("statut", "");

        return new RechercheStage(id, eleveId, nomEntreprise, nomContact, emailContact,
                nbLettresEnvoyees, nbLettresRecues, dateEntretien, resultat, statut);
    }
}
