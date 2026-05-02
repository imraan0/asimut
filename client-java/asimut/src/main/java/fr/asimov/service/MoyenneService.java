package fr.asimov.service;

import fr.asimov.api.ApiClient;
import fr.asimov.model.Moyenne;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class MoyenneService {

    public static List<Moyenne> getByEleve(int eleveId) throws Exception {
        String response = ApiClient.get("/eleves/" + eleveId + "/moyennes");
        JSONArray array = new JSONArray(response);
        List<Moyenne> moyennes = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            int id = obj.getInt("id");
            int eleveIdParsed = obj.getInt("eleve_id");
            int semestreId = obj.getInt("semestre_id");
            double valeur = obj.getDouble("valeur");
            boolean valide = obj.getBoolean("valide");

            // Semestre imbriqué
            String semestreLibelle = "";
            if (!obj.isNull("semestre")) {
                JSONObject semestre = obj.getJSONObject("semestre");
                semestreLibelle = "S" + semestre.getInt("numero") + " " + semestre.optString("annee_scolaire", "");
            }

            moyennes.add(new Moyenne(id, eleveIdParsed, semestreId, valeur, valide, semestreLibelle));
        }
        return moyennes;
    }

    public static void create(int eleveId, int semestreId, double valeur) throws Exception {
        JSONObject body = new JSONObject();
        body.put("eleve_id", eleveId);
        body.put("semestre_id", semestreId);
        body.put("valeur", valeur);
        ApiClient.post("/moyennes", body);
    }

    public static void update(int moyenneId, double valeur) throws Exception {
        JSONObject body = new JSONObject();
        body.put("valeur", valeur);
        ApiClient.put("/moyennes/" + moyenneId, body);
    }

    public static Map<Integer, String> getSemestres() throws Exception {
        // Récupère les semestres depuis l'API
        String response = ApiClient.get("/semestres");

        // Si l'API ne répond pas correctement, on retourne les semestres par défaut
        try {
            JSONArray array = new JSONArray(response);
            Map<Integer, String> semestres = new LinkedHashMap<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int id = obj.getInt("id");
                String libelle = "S" + obj.getInt("numero") + " " + obj.optString("annee_scolaire", "");
                semestres.put(id, libelle);
            }
            if (!semestres.isEmpty()) return semestres;
        } catch (Exception ignored) {}

        // Fallback — semestres en dur depuis la BDD
        Map<Integer, String> fallback = new LinkedHashMap<>();
        fallback.put(1, "S1 2025-2026");
        fallback.put(2, "S2 2025-2026");
        return fallback;
    }
}