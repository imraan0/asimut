package fr.asimov.service;

import fr.asimov.api.ApiClient;
import fr.asimov.model.Convention;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConventionService {

    public static List<Convention> getByEleve(int eleveId) throws Exception {
        String response = ApiClient.get("/eleves/" + eleveId + "/conventions");
        JSONArray array = new JSONArray(response);
        List<Convention> conventions = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            conventions.add(parse(array.getJSONObject(i)));
        }
        return conventions;
    }

    public static void create(int eleveId, int rechercheStageId,
                              String dateDebut, String dateFin) throws Exception {
        JSONObject body = new JSONObject();
        body.put("eleve_id", eleveId);
        body.put("recherche_stage_id", rechercheStageId);
        body.put("date_debut", dateDebut);
        body.put("date_fin", dateFin);
        ApiClient.post("/conventions", body);
    }

    public static void valider(int conventionId) throws Exception {
        ApiClient.put("/conventions/" + conventionId + "/valider", new JSONObject());
    }

    public static String getPdfUrl(int conventionId) {
        return "/conventions/" + conventionId + "/pdf";
    }

    private static Convention parse(JSONObject obj) {
        int id = obj.getInt("id");
        int eleveId = obj.getInt("eleve_id");
        int rechercheStageId = obj.optInt("recherche_stage_id", 0);
        String dateDebut = obj.optString("date_debut", "");
        String dateFin = obj.optString("date_fin", "");
        boolean valide = obj.optBoolean("valide", false);

        String nomEntreprise = "";
        String nomContact = "";
        String emailContact = "";
        if (!obj.isNull("recherche_stage")) {
            JSONObject rs = obj.getJSONObject("recherche_stage");
            nomEntreprise = rs.optString("nom_entreprise", "");
            nomContact = rs.optString("nom_contact", "");
            emailContact = rs.optString("email_contact", "");
        }

        return new Convention(id, eleveId, rechercheStageId, dateDebut, dateFin,
                valide, nomEntreprise, nomContact, emailContact);
    }
}