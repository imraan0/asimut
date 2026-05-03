package fr.asimov.model;

public class Convention {
    private int id;
    private int eleveId;
    private int rechercheStageId;
    private String dateDebut;
    private String dateFin;
    private boolean valide;
    private String nomEntreprise;
    private String nomContact;
    private String emailContact;

    public Convention(int id, int eleveId, int rechercheStageId, String dateDebut,
                      String dateFin, boolean valide, String nomEntreprise,
                      String nomContact, String emailContact) {
        this.id = id;
        this.eleveId = eleveId;
        this.rechercheStageId = rechercheStageId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.valide = valide;
        this.nomEntreprise = nomEntreprise;
        this.nomContact = nomContact;
        this.emailContact = emailContact;
    }

    public int getId() { return id; }
    public int getEleveId() { return eleveId; }
    public int getRechercheStageId() { return rechercheStageId; }
    public String getDateDebut() { return dateDebut; }
    public String getDateFin() { return dateFin; }
    public boolean isValide() { return valide; }
    public String getNomEntreprise() { return nomEntreprise; }
    public String getNomContact() { return nomContact; }
    public String getEmailContact() { return emailContact; }
}