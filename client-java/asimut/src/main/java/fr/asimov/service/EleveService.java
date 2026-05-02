package fr.asimov.service;

import fr.asimov.api.ApiClient;
import fr.asimov.model.Eleve;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import fr.asimov.util.Session;
import okhttp3.*;

public class EleveService {

    public static List<Eleve> getAll() throws Exception {
        String response = ApiClient.get("/eleves");
        JSONArray array = new JSONArray(response);
        List<Eleve> eleves = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Eleve eleve = parseEleve(array.getJSONObject(i));
            chargerOptions(eleve);
            eleves.add(eleve);
        }
        return eleves;
    }

    public static Eleve getById(int id) throws Exception {
        String response = ApiClient.get("/eleves/" + id);
        return parseEleve(new JSONObject(response));
    }

    public static void create(String nom, String prenom, String identifiant,
                              String email, String motDePasse, int classeId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("nom", nom);
        body.put("prenom", prenom);
        body.put("identifiant", identifiant);
        body.put("email", email);
        body.put("mot_de_passe", motDePasse);
        body.put("classe_id", classeId);
        ApiClient.post("/eleves", body);
    }

    public static void update(int id, String nom, String prenom,
                              String identifiant, int classeId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("nom", nom);
        body.put("prenom", prenom);
        body.put("identifiant", identifiant);
        body.put("classe_id", classeId);
        ApiClient.put("/eleves/" + id, body);
    }

    public static void delete(int id) throws Exception {
        ApiClient.delete("/eleves/" + id);
    }

    private static Eleve parseEleve(JSONObject obj) {
        int id = obj.optInt("id", 0);
        String nom = obj.optString("nom", "");
        String prenom = obj.optString("prenom", "");
        String identifiant = obj.optString("identifiant", "");

        // Classe : objet imbriqué — "classe" en minuscule
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

        // Prof référent : objet imbriqué avec alias "referent"
        String profReferent = "";
        if (!obj.isNull("referent")) {
            JSONObject prof = obj.optJSONObject("referent");
            if (prof != null) {
                profReferent = prof.optString("prenom", "") + " " + prof.optString("nom", "");
            }
        }

        return new Eleve(id, nom, prenom, identifiant, classe, profReferent, "", "");
    }

    public static void importCSV(String filePath) throws Exception {
        OkHttpClient client = new OkHttpClient();
        RequestBody fileBody = RequestBody.create(
                new java.io.File(filePath),
                MediaType.parse("text/csv")
        );
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fichier", new java.io.File(filePath).getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url("https://asimut.alwaysdata.net/eleves/import")
                .addHeader("Authorization", "Bearer " + Session.token)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Import CSV : " + response.body().string());
        }
    }
    public static void affecterReferent(int eleveId, int profId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("professeur_id", profId);
        ApiClient.post("/eleves/" + eleveId + "/referent", body);
    }

    public static void retirerReferent(int eleveId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("professeur_id", 0);
        ApiClient.post("/eleves/" + eleveId + "/referent", body);
    }

    public static void chargerOptions(Eleve eleve) throws Exception {
        String response = ApiClient.get("/eleves/" + eleve.getId() + "/options");
        JSONArray array = new JSONArray(response);
        if (array.length() > 0) {
            eleve.setOption1(array.getJSONObject(0).optString("nom", ""));
        }
        if (array.length() > 1) {
            eleve.setOption2(array.getJSONObject(1).optString("nom", ""));
        }
    }

}

