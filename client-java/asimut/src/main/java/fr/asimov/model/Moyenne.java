package fr.asimov.model;

public class Moyenne {
    private int id;
    private int eleveId;
    private int semestreId;
    private double valeur;
    private boolean valide;
    private String semestreLibelle;

    public Moyenne(int id, int eleveId, int semestreId, double valeur, boolean valide, String semestreLibelle) {
        this.id = id;
        this.eleveId = eleveId;
        this.semestreId = semestreId;
        this.valeur = valeur;
        this.valide = valide;
        this.semestreLibelle = semestreLibelle;
    }

    public int getId() { return id; }
    public int getEleveId() { return eleveId; }
    public int getSemestreId() { return semestreId; }
    public double getValeur() { return valeur; }
    public boolean isValide() { return valide; }
    public String getSemestreLibelle() { return semestreLibelle; }

    public void setValeur(double valeur) { this.valeur = valeur; }
    public void setValide(boolean valide) { this.valide = valide; }
}