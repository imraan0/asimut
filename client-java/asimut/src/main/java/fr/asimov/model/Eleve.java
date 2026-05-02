package fr.asimov.model;

public class Eleve {
    private int id;
    private String nom;
    private String prenom;
    private String identifiant;
    private String classe;
    private String profReferent;
    private String option1;
    private String option2;

    public Eleve(int id, String nom, String prenom, String identifiant,
                 String classe, String profReferent, String option1, String option2) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.identifiant = identifiant;
        this.classe = classe;
        this.profReferent = profReferent;
        this.option1 = option1;
        this.option2 = option2;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getIdentifiant() { return identifiant; }
    public String getClasse() { return classe; }
    public String getProfReferent() { return profReferent; }
    public String getOption1() { return option1; }
    public String getOption2() { return option2; }

    public void setOption1(String option1) { this.option1 = option1; }
    public void setOption2(String option2) { this.option2 = option2; }
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