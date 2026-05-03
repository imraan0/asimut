package fr.asimov.model;

public class Option {
    private int id;
    private String nom;
    private String type;

    public Option(int id, String nom, String type) {
        this.id = id;
        this.nom = nom;
        this.type = type;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getType() { return type; }

    @Override
    public String toString() {
        return nom + " (" + type + ")";
    }
}