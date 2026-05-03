package fr.asimov.model;

public class Professeur {
    private int id;
    private String nom;
    private String prenom;
    private int nbEleves;

    public Professeur(int id, String nom, String prenom, int nbEleves) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.nbEleves = nbEleves;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public int getNbEleves() { return nbEleves; }

    @Override
    public String toString() {
        return prenom + " " + nom + " (" + nbEleves + " élèves)";
    }
}