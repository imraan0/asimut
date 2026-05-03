package fr.asimov.service;

import fr.asimov.api.ApiClient;
import fr.asimov.model.Parent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MailService {

    public static List<Parent> getParents() throws Exception {
        String response = ApiClient.get("/parents");
        JSONArray array = new JSONArray(response);
        List<Parent> parents = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            int id = obj.getInt("id");
            String nom = obj.optString("nom", "");
            String prenom = obj.optString("prenom", "");
            String email = obj.optString("email", "");
            String nomEleve = "";
            String prenomEleve = "";
            if (!obj.isNull("eleve")) {
                JSONObject eleve = obj.getJSONObject("eleve");
                nomEleve = eleve.optString("nom", "");
                prenomEleve = eleve.optString("prenom", "");
            }
            parents.add(new Parent(id, nom, prenom, email, nomEleve, prenomEleve));
        }
        return parents;
    }

    public static void mailOne(int parentId, String sujet, String message, String auteur) throws Exception {
        JSONObject body = new JSONObject();
        body.put("sujet", sujet);
        body.put("message", message);
        body.put("auteur", auteur);
        ApiClient.post("/parents/" + parentId + "/mail", body);
    }

    public static void mailAll(String sujet, String message, String auteur) throws Exception {
        JSONObject body = new JSONObject();
        body.put("sujet", sujet);
        body.put("message", message);
        body.put("auteur", auteur);
        ApiClient.post("/parents/mail", body);
    }
}