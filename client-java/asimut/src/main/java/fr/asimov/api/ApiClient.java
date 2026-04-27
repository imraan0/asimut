package fr.asimov.api;

import fr.asimov.util.Session;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class ApiClient {

    private static final String BASE_URL = "https://asimut.alwaysdata.net";
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Construit le header Authorization avec le token JWT
    private static Request.Builder baseRequest(String url) {
        Request.Builder builder = new Request.Builder().url(BASE_URL + url);
        if (Session.token != null) {
            builder.addHeader("Authorization", "Bearer " + Session.token);
        }
        return builder;
    }

    // GET /endpoint
    public static String get(String endpoint) throws IOException {
        Request request = baseRequest(endpoint).get().build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    // POST /endpoint avec un body JSON
    public static String post(String endpoint, JSONObject body) throws IOException {
        RequestBody requestBody = RequestBody.create(body.toString(), JSON);
        Request request = baseRequest(endpoint).post(requestBody).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    // PUT /endpoint avec un body JSON
    public static String put(String endpoint, JSONObject body) throws IOException {
        RequestBody requestBody = RequestBody.create(body.toString(), JSON);
        Request request = baseRequest(endpoint).put(requestBody).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    // DELETE /endpoint
    public static String delete(String endpoint) throws IOException {
        Request request = baseRequest(endpoint).delete().build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}