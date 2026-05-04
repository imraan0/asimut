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
                    String profResponse = ApiClient.get("/professeurs/me");
                    JSONObject prof = new JSONObject(profResponse);
                    Session.metierId = prof.getInt("id");
                }

                if ("eleve".equals(Session.role)) {
                    String eleveResponse = ApiClient.get("/eleves/me");
                    JSONObject eleve = new JSONObject(eleveResponse);
                    Session.metierId = eleve.getInt("id");
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