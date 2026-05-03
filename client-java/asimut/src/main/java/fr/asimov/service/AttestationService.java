package fr.asimov.service;

import fr.asimov.api.ApiClient;
import fr.asimov.model.Attestation;
import org.json.JSONObject;

import java.io.File;

public class AttestationService {

    public static Attestation getById(int id) throws Exception {
        String response = ApiClient.get("/attestations/" + id);
        return parse(new JSONObject(response));
    }

    public static byte[] getPdf(int id) throws Exception {
        return ApiClient.getBytes("/attestations/" + id + "/pdf");
    }

    public static void uploadPdf(int id, File file) throws Exception {
        ApiClient.uploadFile("/attestations/" + id + "/upload", file);
    }

    private static Attestation parse(JSONObject obj) {
        int id = obj.getInt("id");
        int conventionId = obj.getInt("convention_id");
        String pdfPath = obj.isNull("pdf_path") ? "" : obj.optString("pdf_path", "");
        String dateSignature = obj.isNull("date_signature") ? "" : obj.optString("date_signature", "");

        String nomEleve = "";
        String nomEntreprise = "";
        String dateDebut = "";
        String dateFin = "";

        if (!obj.isNull("convention")) {
            JSONObject convention = obj.getJSONObject("convention");
            dateDebut = convention.optString("date_debut", "");
            dateFin = convention.optString("date_fin", "");

            if (!convention.isNull("eleve")) {
                JSONObject eleve = convention.getJSONObject("eleve");
                nomEleve = eleve.optString("prenom", "") + " " + eleve.optString("nom", "");
            }
            if (!convention.isNull("recherche_stage")) {
                JSONObject rs = convention.getJSONObject("recherche_stage");
                nomEntreprise = rs.optString("nom_entreprise", "");
            }
        }

        return new Attestation(id, conventionId, pdfPath, dateSignature,
                nomEleve, nomEntreprise, dateDebut, dateFin);
    }
}