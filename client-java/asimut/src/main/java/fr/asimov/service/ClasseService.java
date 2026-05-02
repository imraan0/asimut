package fr.asimov.service;

import fr.asimov.api.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClasseService {

    /**
     * Retourne une map id -> libellé pour alimenter un JComboBox.
     * Ex : { 1 -> "Niveau 6 - A (2025-2026)" }
     */
    public static Map<Integer, String> getAll() throws Exception {
        String response = ApiClient.get("/classes");
        JSONArray array = new JSONArray(response);
        Map<Integer, String> classes = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            int id = obj.getInt("id");
            String lettre = obj.optString("lettre", "");
            String annee = obj.optString("annee_scolaire", "");
            JSONObject niveau = obj.optJSONObject("niveau");
            String numNiveau = niveau != null ? niveau.optInt("numero") + "" : "?";
            String libelle = "Niveau " + numNiveau + " - " + lettre + " (" + annee + ")";
            classes.put(id, libelle);
        }
        return classes;
    }
}