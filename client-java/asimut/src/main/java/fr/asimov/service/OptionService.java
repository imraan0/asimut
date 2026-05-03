package fr.asimov.service;

import fr.asimov.api.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;
import fr.asimov.model.Option;
import java.util.ArrayList;
import java.util.List;

public class OptionService {

    /**
     * Retourne une map id -> libellé pour alimenter un JComboBox.
     * Ex : { 1 -> "Mathématiques expertes (technique)" }
     */
    public static Map<Integer, String> getAll() throws Exception {
        String response = ApiClient.get("/options");
        JSONArray array = new JSONArray(response);
        Map<Integer, String> options = new LinkedHashMap<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            int id = obj.getInt("id");
            String nom = obj.optString("nom", "");
            String type = obj.optString("type", "");
            options.put(id, nom + " (" + type + ")");
        }
        return options;
    }

    public static void affecter(int eleveId, int optionId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("eleve_id", eleveId);
        body.put("option_id", optionId);
        ApiClient.post("/options/affecter", body);
    }

    public static void retirer(int eleveId, int optionId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("eleve_id", eleveId);
        body.put("option_id", optionId);
        ApiClient.delete("/options/retirer", body);
    }

    public static List<Option> getAllAsList() throws Exception {
        String response = ApiClient.get("/options");
        JSONArray array = new JSONArray(response);
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            options.add(new Option(
                    obj.getInt("id"),
                    obj.optString("nom", ""),
                    obj.optString("type", "")
            ));
        }
        return options;
    }

    public static void create(String nom, String type) throws Exception {
        JSONObject body = new JSONObject();
        body.put("nom", nom);
        body.put("type", type);
        ApiClient.post("/options", body);
    }

    public static void delete(int id) throws Exception {
        ApiClient.delete("/options/" + id);
    }
}