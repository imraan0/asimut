package fr.asimov.model;

public class Eleve {
    private int id;
    private String nom;
    private String prenom;
    private String identifiant;
    private String classe;
    private String profReferent;

    public Eleve(int id, String nom, String prenom, String identifiant,
                 String classe, String profReferent) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.identifiant = identifiant;
        this.classe = classe;
        this.profReferent = profReferent;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getIdentifiant() { return identifiant; }
    public String getClasse() { return classe; }
    public String getProfReferent() { return profReferent; }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setIdentifiant(String identifiant) { this.identifiant = identifiant; }
    public void setClasse(String classe) { this.classe = classe; }
    public void setProfReferent(String profReferent) { this.profReferent = profReferent; }

    @Override
    public String toString() {
        return prenom + " " + nom + " (" + classe + ")";
    }
}