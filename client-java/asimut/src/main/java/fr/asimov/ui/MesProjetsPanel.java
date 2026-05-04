package fr.asimov.ui;

import fr.asimov.model.Projet;
import fr.asimov.service.ProjetService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MesProjetsPanel extends JPanel {

    private JTable tableProjets;
    private DefaultTableModel projetsModel;
    private List<Projet> projets;

    public MesProjetsPanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadProjets();
    }

    private void initComponents() {
        String[] colonnes = {"ID", "Nom", "Objectif", "Début", "Fin", "Participants"};
        projetsModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableProjets = new JTable(projetsModel);
        tableProjets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableProjets.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollPane = new JScrollPane(tableProjets);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Projets disponibles"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadProjets() {
        new Thread(() -> {
            try {
                // GET /projets renvoie uniquement les projets validés pour les élèves
                String response = fr.asimov.api.ApiClient.get("/projets/mes-projets");
                org.json.JSONArray array = new org.json.JSONArray(response);
                projets = new java.util.ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    org.json.JSONObject obj = array.getJSONObject(i);
                    int id = obj.getInt("id");
                    String nom = obj.optString("nom", "");
                    String objectif = obj.optString("objectif", "");
                    String dateDebut = obj.optString("date_debut", "");
                    String dateFin = obj.optString("date_fin", "");
                    projets.add(new Projet(id, nom, objectif, dateDebut, dateFin, true, new java.util.ArrayList<>()));
                }
                SwingUtilities.invokeLater(() -> {
                    projetsModel.setRowCount(0);
                    for (Projet p : projets) {
                        projetsModel.addRow(new Object[]{
                                p.getId(), p.getNom(), p.getObjectif(),
                                p.getDateDebut(), p.getDateFin(), p.getParticipantsString()
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage())
                );
            }
        }).start();
    }
}