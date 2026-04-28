package fr.asimov.service;

import fr.asimov.api.ApiClient;
import fr.asimov.util.Session;
import org.json.JSONObject;

public class AuthService {

    /**
     * Envoie les credentials à l'API et stocke le token en session.
     * Retourne true si login OK, false sinon.
     */
    public static boolean login(String email, String password) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("mot_de_passe", password);

            String response = ApiClient.post("/auth/login", body);
            System.out.println("Réponse API : " + response);
            JSONObject json = new JSONObject(response);

            if (json.has("token")) {
                Session.token = json.getString("token");

                // Décode le payload du JWT (2ème partie entre les points)
                String[] parts = Session.token.split("\\.");
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                JSONObject claims = new JSONObject(payload);

                Session.role = claims.getString("role");
                Session.userId = claims.getInt("id");

                System.out.println("Role : " + Session.role);
                System.out.println("UserId : " + Session.userId);
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Déconnecte l'utilisateur en vidant la session.
     */
    public static void logout() {
        Session.token = null;
        Session.role = null;
        Session.userId = 0;
    }
}