package fr.asimov.model;

import java.util.List;

public class Projet {
    private int id;
    private String nom;
    private String objectif;
    private String dateDebut;
    private String dateFin;
    private boolean valide;
    private List<String> participants;

    public Projet(int id, String nom, String objectif, String dateDebut,
                  String dateFin, boolean valide, List<String> participants) {
        this.id = id;
        this.nom = nom;
        this.objectif = objectif;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.valide = valide;
        this.participants = participants;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getObjectif() { return objectif; }
    public String getDateDebut() { return dateDebut; }
    public String getDateFin() { return dateFin; }
    public boolean isValide() { return valide; }
    public List<String> getParticipants() { return participants; }
    public String getParticipantsString() { return String.join(", ", participants); }
}