package fr.asimov.service;

import fr.asimov.api.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;
import fr.asimov.model.Eleve;
import fr.asimov.model.Professeur;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Professeur> getAllAsList() throws Exception {
        String response = ApiClient.get("/professeurs");
        JSONArray array = new JSONArray(response);
        List<Professeur> profs = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            profs.add(new Professeur(
                    obj.getInt("id"),
                    obj.optString("nom", ""),
                    obj.optString("prenom", ""),
                    obj.optInt("nb_eleves", 0)
            ));
        }
        return profs;
    }

    public static List<Eleve> getEleves(int profId) throws Exception {
        String response = ApiClient.get("/professeurs/" + profId + "/eleves");
        JSONArray array = new JSONArray(response);
        List<Eleve> eleves = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            int id = obj.getInt("id");
            String nom = obj.optString("nom", "");
            String prenom = obj.optString("prenom", "");
            String identifiant = obj.optString("identifiant", "");
            String classe = "";
            if (!obj.isNull("classe")) {
                JSONObject classeObj = obj.optJSONObject("classe");
                if (classeObj != null) {
                    String lettre = classeObj.optString("lettre", "");
                    JSONObject niveauObj = classeObj.optJSONObject("niveau");
                    String niveau = niveauObj != null ? niveauObj.optInt("numero") + "" : "";
                    classe = "Niveau " + niveau + " - " + lettre;
                }
            }
            eleves.add(new Eleve(id, nom, prenom, identifiant, classe, "", "", ""));
        }
        return eleves;
    }
}