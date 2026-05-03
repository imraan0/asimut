package fr.asimov.ui;

import fr.asimov.model.Eleve;
import fr.asimov.model.Projet;
import fr.asimov.service.EleveService;
import fr.asimov.service.ProjetService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProjetPanel extends JPanel {

    private JTable tableProjets;
    private DefaultTableModel projetsModel;
    private JButton btnAjouter;
    private JButton btnValider;
    private JButton btnSupprimer;
    private JButton btnAjouterParticipant;
    private JButton btnRetirerParticipant;
    private List<Projet> projets;

    public ProjetPanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadProjets();
    }

    private void initComponents() {
        String[] colonnes = {"ID", "Nom", "Objectif", "Début", "Fin", "Validé", "Participants"};
        projetsModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableProjets = new JTable(projetsModel);
        tableProjets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableProjets.getColumnModel().getColumn(0).setMaxWidth(40);
        tableProjets.getColumnModel().getColumn(6).setPreferredWidth(200);
        JScrollPane scrollPane = new JScrollPane(tableProjets);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAjouter = new JButton("Ajouter");
        btnValider = new JButton("Valider");
        btnSupprimer = new JButton("Supprimer");
        btnAjouterParticipant = new JButton("Ajouter participant");
        btnRetirerParticipant = new JButton("Retirer participant");

        btnPanel.add(btnAjouter);
        btnPanel.add(btnValider);
        btnPanel.add(btnSupprimer);
        btnPanel.add(btnAjouterParticipant);
        btnPanel.add(btnRetirerParticipant);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        btnAjouter.addActionListener(e -> showFormDialog());
        btnValider.addActionListener(e -> handleValider());
        btnSupprimer.addActionListener(e -> handleSupprimer());
        btnAjouterParticipant.addActionListener(e -> showAjouterParticipantDialog());
        btnRetirerParticipant.addActionListener(e -> showRetirerParticipantDialog());
    }

    private void loadProjets() {
        new Thread(() -> {
            try {
                projets = ProjetService.getAll();
                SwingUtilities.invokeLater(() -> {
                    projetsModel.setRowCount(0);
                    for (Projet p : projets) {
                        projetsModel.addRow(new Object[]{
                                p.getId(),
                                p.getNom(),
                                p.getObjectif(),
                                p.getDateDebut(),
                                p.getDateFin(),
                                p.isValide() ? "✅ Oui" : "❌ Non",
                                p.getParticipantsString()
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erreur chargement projets : " + ex.getMessage())
                );
            }
        }).start();
    }

    private void showFormDialog() {
        // Charge les élèves pour choisir le responsable
        List<Eleve> eleves;
        try {
            eleves = EleveService.getAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur chargement élèves : " + ex.getMessage());
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ajouter un projet", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nomField = new JTextField(20);
        JTextField objectifField = new JTextField(20);
        JTextField dateDebutField = new JTextField("2026-06-01", 15);
        JTextField dateFinField = new JTextField("2026-08-31", 15);

        // Combobox élèves (responsable)
        JComboBox<String> eleveCombo = new JComboBox<>();
        int[] eleveIds = new int[eleves.size()];
        for (int i = 0; i < eleves.size(); i++) {
            eleveIds[i] = eleves.get(i).getId();
            eleveCombo.addItem(eleves.get(i).getPrenom() + " " + eleves.get(i).getNom());
        }

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nom :"), gbc);
        gbc.gridx = 1; panel.add(nomField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Objectif :"), gbc);
        gbc.gridx = 1; panel.add(objectifField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Date début :"), gbc);
        gbc.gridx = 1; panel.add(dateDebutField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Date fin :"), gbc);
        gbc.gridx = 1; panel.add(dateFinField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Responsable :"), gbc);
        gbc.gridx = 1; panel.add(eleveCombo, gbc);

        JButton btnValider = new JButton("Créer");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                String nom = nomField.getText().trim();
                String objectif = objectifField.getText().trim();
                String dateDebut = dateDebutField.getText().trim();
                String dateFin = dateFinField.getText().trim();
                int eleveId = eleveIds[eleveCombo.getSelectedIndex()];

                if (nom.isEmpty() || objectif.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Nom et objectif obligatoires.");
                    return;
                }

                ProjetService.create(eleveId, nom, objectif, dateDebut, dateFin);
                dialog.dispose();
                loadProjets();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleValider() {
        int selectedRow = tableProjets.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un projet.");
            return;
        }
        Projet projet = projets.get(selectedRow);
        if (projet.isValide()) {
            JOptionPane.showMessageDialog(this, "Ce projet est déjà validé.");
            return;
        }
        try {
            ProjetService.valider(projet.getId());
            loadProjets();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur validation : " + ex.getMessage());
        }
    }

    private void handleSupprimer() {
        int selectedRow = tableProjets.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un projet.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer ce projet ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ProjetService.delete(projets.get(selectedRow).getId());
                loadProjets();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur suppression : " + ex.getMessage());
            }
        }
    }

    private void showAjouterParticipantDialog() {
        int selectedRow = tableProjets.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un projet.");
            return;
        }
        Projet projet = projets.get(selectedRow);

        List<Eleve> eleves;
        try {
            eleves = EleveService.getAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur chargement élèves.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Ajouter un participant", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> eleveCombo = new JComboBox<>();
        int[] eleveIds = new int[eleves.size()];
        for (int i = 0; i < eleves.size(); i++) {
            eleveIds[i] = eleves.get(i).getId();
            eleveCombo.addItem(eleves.get(i).getPrenom() + " " + eleves.get(i).getNom());
        }

        JTextField dateDebutField = new JTextField("2026-06-01", 15);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Élève :"), gbc);
        gbc.gridx = 1; panel.add(eleveCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Date début :"), gbc);
        gbc.gridx = 1; panel.add(dateDebutField, gbc);

        JButton btnValider = new JButton("Ajouter");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                int eleveId = eleveIds[eleveCombo.getSelectedIndex()];
                String dateDebut = dateDebutField.getText().trim();
                ProjetService.ajouterParticipant(eleveId, projet.getId(), dateDebut);
                dialog.dispose();
                loadProjets();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showRetirerParticipantDialog() {
        int selectedRow = tableProjets.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un projet.");
            return;
        }
        Projet projet = projets.get(selectedRow);

        List<Eleve> eleves;
        try {
            eleves = EleveService.getAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur chargement élèves.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Retirer un participant", true);
        dialog.setSize(350, 160);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> eleveCombo = new JComboBox<>();
        int[] eleveIds = new int[eleves.size()];
        for (int i = 0; i < eleves.size(); i++) {
            eleveIds[i] = eleves.get(i).getId();
            eleveCombo.addItem(eleves.get(i).getPrenom() + " " + eleves.get(i).getNom());
        }

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Élève :"), gbc);
        gbc.gridx = 1; panel.add(eleveCombo, gbc);

        JButton btnValider = new JButton("Retirer");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                int eleveId = eleveIds[eleveCombo.getSelectedIndex()];
                ProjetService.retirerParticipant(projet.getId(), eleveId);
                dialog.dispose();
                loadProjets();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }
}