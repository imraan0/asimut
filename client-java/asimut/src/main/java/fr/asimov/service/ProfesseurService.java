package fr.asimov.service;

import fr.asimov.api.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProfesseurService {

    /**
     * Retourne une map id -> libellé pour alimenter un JComboBox.
     * Ex : { 1 -> "Pierre Martin" }
     */
    public static Map<Integer, String> getAll() throws Exception {
        String response = ApiClient.get("/professeurs");
        JSONArray array = new JSONArray(response);
        Map<Integer, String> profs = new LinkedHashMap<>();

        // Option "Aucun" pour pouvoir retirer le référent
        profs.put(0, "Aucun");

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            int id = obj.getInt("id");
            String nom = obj.optString("nom", "");
            String prenom = obj.optString("prenom", "");
            profs.put(id, prenom + " " + nom);
        }
        return profs;
    }
}