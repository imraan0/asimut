package fr.asimov.model;

public class RechercheStage {
    private int id;
    private int eleveId;
    private String nomEntreprise;
    private String nomContact;
    private String emailContact;
    private int nbLettresEnvoyees;
    private int nbLettresRecues;
    private String dateEntretien;
    private String resultat;
    private String statut;

    public RechercheStage(int id, int eleveId, String nomEntreprise, String nomContact,
                          String emailContact, int nbLettresEnvoyees, int nbLettresRecues,
                          String dateEntretien, String resultat, String statut) {
        this.id = id;
        this.eleveId = eleveId;
        this.nomEntreprise = nomEntreprise;
        this.nomContact = nomContact;
        this.emailContact = emailContact;
        this.nbLettresEnvoyees = nbLettresEnvoyees;
        this.nbLettresRecues = nbLettresRecues;
        this.dateEntretien = dateEntretien;
        this.resultat = resultat;
        this.statut = statut;
    }

    public int getId() { return id; }
    public int getEleveId() { return eleveId; }
    public String getNomEntreprise() { return nomEntreprise; }
    public String getNomContact() { return nomContact; }
    public String getEmailContact() { return emailContact; }
    public int getNbLettresEnvoyees() { return nbLettresEnvoyees; }
    public int getNbLettresRecues() { return nbLettresRecues; }
    public String getDateEntretien() { return dateEntretien; }
    public String getResultat() { return resultat; }
    public String getStatut() { return statut; }

    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }
    public void setNomContact(String nomContact) { this.nomContact = nomContact; }
    public void setEmailContact(String emailContact) { this.emailContact = emailContact; }
    public void setStatut(String statut) { this.statut = statut; }
}