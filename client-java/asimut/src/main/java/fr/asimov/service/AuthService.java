package fr.asimov.service;

import fr.asimov.api.ApiClient;
import fr.asimov.util.Session;
import org.json.JSONArray;
import org.json.JSONObject;

public class AuthService {

    public static boolean login(String email, String password) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("mot_de_passe", password);

            String response = ApiClient.post("/auth/login", body);
            JSONObject json = new JSONObject(response);

            if (json.has("token")) {
                Session.token = json.getString("token");

                // Décode le payload du JWT
                String[] parts = Session.token.split("\\.");
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                JSONObject claims = new JSONObject(payload);

                Session.role = claims.getString("role");
                Session.userId = claims.getInt("id");

                if ("professeur".equals(Session.role)) {
                    String profResponse = ApiClient.get("/professeurs");
                    JSONArray profs = new JSONArray(profResponse);
                    for (int i = 0; i < profs.length(); i++) {
                        JSONObject prof = profs.getJSONObject(i);
                        int utilisateurId = prof.optInt("utilisateur_id", -1);
                        if (utilisateurId == Session.userId) {
                            Session.metierId = prof.getInt("id");
                            break;
                        }
                    }
                }

                if ("eleve".equals(Session.role)) {
                    String elevesResponse = ApiClient.get("/eleves");
                    JSONArray eleves = new JSONArray(elevesResponse);
                    for (int i = 0; i < eleves.length(); i++) {
                        JSONObject eleve = eleves.getJSONObject(i);
                        int utilisateurId = eleve.optInt("utilisateur_id", -1);
                        if (utilisateurId == Session.userId) {
                            Session.metierId = eleve.getInt("id");
                            break;
                        }
                    }
                }

                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void logout() {
        Session.token = null;
        Session.role = null;
        Session.userId = 0;
        Session.metierId = 0;
    }
}