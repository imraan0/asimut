package fr.asimov.model;

public class Attestation {
    private int id;
    private int conventionId;
    private String pdfPath;
    private String dateSignature;
    private String nomEleve;
    private String nomEntreprise;
    private String dateDebut;
    private String dateFin;

    public Attestation(int id, int conventionId, String pdfPath, String dateSignature,
                       String nomEleve, String nomEntreprise, String dateDebut, String dateFin) {
        this.id = id;
        this.conventionId = conventionId;
        this.pdfPath = pdfPath;
        this.dateSignature = dateSignature;
        this.nomEleve = nomEleve;
        this.nomEntreprise = nomEntreprise;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public int getId() { return id; }
    public int getConventionId() { return conventionId; }
    public String getPdfPath() { return pdfPath; }
    public String getDateSignature() { return dateSignature; }
    public String getNomEleve() { return nomEleve; }
    public String getNomEntreprise() { return nomEntreprise; }
    public String getDateDebut() { return dateDebut; }
    public String getDateFin() { return dateFin; }
    public boolean isSigne() { return pdfPath != null && !pdfPath.isEmpty(); }
}