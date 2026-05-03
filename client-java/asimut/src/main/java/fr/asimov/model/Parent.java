package fr.asimov.model;

public class Parent {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String nomEleve;
    private String prenomEleve;

    public Parent(int id, String nom, String prenom, String email,
                  String nomEleve, String prenomEleve) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.nomEleve = nomEleve;
        this.prenomEleve = prenomEleve;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getNomEleve() { return nomEleve; }
    public String getPrenomEleve() { return prenomEleve; }

    @Override
    public String toString() {
        return prenom + " " + nom + " (parent de " + prenomEleve + " " + nomEleve + ")";
    }
}