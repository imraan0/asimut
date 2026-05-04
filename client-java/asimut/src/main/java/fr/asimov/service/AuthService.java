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
                    try {
                        String eleveResponse = ApiClient.get("/eleves/me");
                        JSONObject eleve = new JSONObject(eleveResponse);
                        Session.metierId = eleve.getInt("id");
                    } catch (Exception e) {
                        // Fallback temporaire jusqu'au redémarrage AlwaysData
                        if (Session.userId == 4) Session.metierId = 1;  // imran.isik@asimov.fr
                        if (Session.userId == 6) Session.metierId = 2;  // eleve2@asimov.fr
                        if (Session.userId == 7) Session.metierId = 3;
                        if (Session.userId == 8) Session.metierId = 4;
                        if (Session.userId == 9) Session.metierId = 5;
                        if (Session.userId == 11) Session.metierId = 7;
                        if (Session.userId == 16) Session.metierId = 12;
                        if (Session.userId == 17) Session.metierId = 13;
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