package fr.asimov.ui;

import fr.asimov.api.ApiClient;
import fr.asimov.model.Convention;
import fr.asimov.model.Eleve;
import fr.asimov.model.RechercheStage;
import fr.asimov.service.ConventionService;
import fr.asimov.service.EleveService;
import fr.asimov.service.RechercheStageService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ConventionPanel extends JPanel {

    private JTable tableEleves;
    private DefaultTableModel elevesModel;
    private JTable tableConventions;
    private DefaultTableModel conventionsModel;
    private JButton btnAjouter;
    private JButton btnPdf;
    private List<Eleve> eleves;
    private List<Convention> conventions;
    private int eleveIdSelectionne = -1;

    public ConventionPanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadEleves();
    }

    private void initComponents() {
        // Tableau élèves gauche
        String[] colonnesEleves = {"ID", "Nom", "Prénom"};
        elevesModel = new DefaultTableModel(colonnesEleves, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableEleves = new JTable(elevesModel);
        tableEleves.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEleves.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollEleves = new JScrollPane(tableEleves);
        scrollEleves.setPreferredSize(new Dimension(250, 0));
        scrollEleves.setBorder(BorderFactory.createTitledBorder("Élèves"));

        // Tableau conventions droite
        String[] colonnesConventions = {"ID", "Entreprise", "Début", "Fin", "Validée"};
        conventionsModel = new DefaultTableModel(colonnesConventions, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableConventions = new JTable(conventionsModel);
        tableConventions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableConventions.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollConventions = new JScrollPane(tableConventions);
        scrollConventions.setBorder(BorderFactory.createTitledBorder("Conventions"));

        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAjouter = new JButton("Ajouter");
        btnPdf = new JButton("Télécharger PDF");
        btnPanel.add(btnAjouter);
        btnPanel.add(btnPdf);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollEleves, scrollConventions);
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Sélection élève
        tableEleves.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableEleves.getSelectedRow();
                if (selectedRow != -1) {
                    eleveIdSelectionne = eleves.get(selectedRow).getId();
                    loadConventions(eleveIdSelectionne);
                }
            }
        });

        btnAjouter.addActionListener(e -> {
            if (eleveIdSelectionne == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un élève.");
                return;
            }
            showFormDialog();
        });

        btnPdf.addActionListener(e -> {
            int selectedRow = tableConventions.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une convention.");
                return;
            }
            Convention convention = conventions.get(selectedRow);
            new Thread(() -> {
                try {
                    byte[] pdfBytes = ApiClient.getBytes("/conventions/" + convention.getId() + "/pdf");
                    java.io.File tempFile = java.io.File.createTempFile("convention_" + convention.getId(), ".pdf");
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                        fos.write(pdfBytes);
                    }
                    SwingUtilities.invokeLater(() -> {
                        try {
                            java.awt.Desktop.getDesktop().open(tempFile);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Impossible d'ouvrir le PDF.");
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "Erreur téléchargement PDF : " + ex.getMessage())
                    );
                }
            }).start();
        });
    }

    private void loadEleves() {
        new Thread(() -> {
            try {
                eleves = EleveService.getAll();
                SwingUtilities.invokeLater(() -> {
                    elevesModel.setRowCount(0);
                    for (Eleve e : eleves) {
                        elevesModel.addRow(new Object[]{
                                e.getId(), e.getNom(), e.getPrenom()
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erreur chargement élèves : " + ex.getMessage())
                );
            }
        }).start();
    }

    private void loadConventions(int eleveId) {
        new Thread(() -> {
            try {
                conventions = ConventionService.getByEleve(eleveId);
                SwingUtilities.invokeLater(() -> {
                    conventionsModel.setRowCount(0);
                    for (Convention c : conventions) {
                        conventionsModel.addRow(new Object[]{
                                c.getId(),
                                c.getNomEntreprise(),
                                c.getDateDebut(),
                                c.getDateFin(),
                                c.isValide() ? "✅ Oui" : "❌ Non"
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erreur chargement conventions : " + ex.getMessage())
                );
            }
        }).start();
    }

    private void showFormDialog() {
        // Charge les recherches de stage validées de l'élève
        List<RechercheStage> recherches;
        try {
            recherches = RechercheStageService.getByEleve(eleveIdSelectionne);
            recherches.removeIf(r -> !r.getStatut().equals("valide"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur chargement recherches : " + ex.getMessage());
            return;
        }

        if (recherches.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune recherche de stage validée pour cet élève.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Ajouter une convention", true);
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Combobox recherches validées
        JComboBox<String> rechercheCombo = new JComboBox<>();
        int[] rechercheIds = new int[recherches.size()];
        for (int i = 0; i < recherches.size(); i++) {
            rechercheIds[i] = recherches.get(i).getId();
            rechercheCombo.addItem(recherches.get(i).getNomEntreprise());
        }

        JTextField dateDebutField = new JTextField("2026-06-01", 15);
        JTextField dateFinField = new JTextField("2026-08-31", 15);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Recherche de stage :"), gbc);
        gbc.gridx = 1; panel.add(rechercheCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Date début (YYYY-MM-DD) :"), gbc);
        gbc.gridx = 1; panel.add(dateDebutField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Date fin (YYYY-MM-DD) :"), gbc);
        gbc.gridx = 1; panel.add(dateFinField, gbc);

        JButton btnValider = new JButton("Créer");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        List<RechercheStage> finalRecherches = recherches;
        btnValider.addActionListener(ev -> {
            try {
                int rechercheId = rechercheIds[rechercheCombo.getSelectedIndex()];
                String dateDebut = dateDebutField.getText().trim();
                String dateFin = dateFinField.getText().trim();
                ConventionService.create(eleveIdSelectionne, rechercheId, dateDebut, dateFin);
                dialog.dispose();
                loadConventions(eleveIdSelectionne);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleValider(Convention convention) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Valider cette convention ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ConventionService.valider(convention.getId());
                loadConventions(eleveIdSelectionne);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur validation : " + ex.getMessage());
            }
        }
    }
}