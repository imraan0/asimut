package fr.asimov.service;

import fr.asimov.api.ApiClient;
import fr.asimov.model.Projet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProjetService {

    public static List<Projet> getAll() throws Exception {
        String response = ApiClient.get("/projets/all");
        JSONArray array = new JSONArray(response);
        List<Projet> projets = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            projets.add(parse(array.getJSONObject(i)));
        }
        return projets;
    }

    public static void create(int eleveId, String nom, String objectif,
                              String dateDebut, String dateFin) throws Exception {
        JSONObject body = new JSONObject();
        body.put("eleve_id", eleveId);
        body.put("nom", nom);
        body.put("objectif", objectif);
        body.put("date_debut", dateDebut);
        body.put("date_fin", dateFin);
        ApiClient.post("/projets", body);
    }

    public static void valider(int projetId) throws Exception {
        ApiClient.put("/projets/" + projetId + "/valider", new JSONObject());
    }

    public static void delete(int projetId) throws Exception {
        ApiClient.delete("/projets/" + projetId);
    }

    public static void ajouterParticipant(int eleveId, int projetId, String dateDebut) throws Exception {
        JSONObject body = new JSONObject();
        body.put("eleve_id", eleveId);
        body.put("projet_id", projetId);
        body.put("date_debut", dateDebut);
        ApiClient.post("/projets/participer", body);
    }

    public static void retirerParticipant(int projetId, int eleveId) throws Exception {
        ApiClient.delete("/projets/" + projetId + "/participer/" + eleveId);
    }

    private static Projet parse(JSONObject obj) {
        int id = obj.getInt("id");
        String nom = obj.optString("nom", "");
        String objectif = obj.optString("objectif", "");
        String dateDebut = obj.optString("date_debut", "");
        String dateFin = obj.optString("date_fin", "");
        boolean valide = obj.optBoolean("valide", false);

        List<String> participants = new ArrayList<>();
        if (obj.has("participations")) {
            JSONArray participations = obj.getJSONArray("participations");
            for (int i = 0; i < participations.length(); i++) {
                JSONObject p = participations.getJSONObject(i);
                if (!p.isNull("eleve")) {
                    JSONObject eleve = p.getJSONObject("eleve");
                    String nom2 = eleve.optString("prenom", "") + " " + eleve.optString("nom", "");
                    if (p.optBoolean("est_responsable", false)) nom2 += " (resp.)";
                    participants.add(nom2);
                }
            }
        }

        return new Projet(id, nom, objectif, dateDebut, dateFin, valide, participants);
    }
}