package fr.asimov.ui;

import fr.asimov.model.Convention;
import fr.asimov.model.Eleve;
import fr.asimov.model.RechercheStage;
import fr.asimov.service.ConventionService;
import fr.asimov.service.ProfesseurService;
import fr.asimov.service.RechercheStageService;
import fr.asimov.util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MesStagesPanel extends JPanel {

    private JTable tableEleves;
    private DefaultTableModel elevesModel;
    private JTable tableStages;
    private DefaultTableModel stagesModel;
    private JTable tableConventions;
    private DefaultTableModel conventionsModel;
    private JLabel alerteLabel;
    private List<Eleve> eleves;
    private List<RechercheStage> recherches;
    private List<Convention> conventions;
    private int eleveIdSelectionne = -1;

    public MesStagesPanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadEleves();
    }

    private void initComponents() {
        String[] colonnesEleves = {"ID", "Nom", "Prénom"};
        elevesModel = new DefaultTableModel(colonnesEleves, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableEleves = new JTable(elevesModel);
        tableEleves.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEleves.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollEleves = new JScrollPane(tableEleves);
        scrollEleves.setPreferredSize(new Dimension(200, 0));
        scrollEleves.setBorder(BorderFactory.createTitledBorder("Mes élèves"));

        alerteLabel = new JLabel("");
        alerteLabel.setForeground(Color.RED);
        alerteLabel.setFont(new Font("Arial", Font.BOLD, 13));
        alerteLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        String[] colonnesStages = {"ID", "Entreprise", "Statut", "Lettres env."};
        stagesModel = new DefaultTableModel(colonnesStages, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableStages = new JTable(stagesModel);
        tableStages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableStages.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollStages = new JScrollPane(tableStages);
        scrollStages.setBorder(BorderFactory.createTitledBorder("Recherches de stage"));

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

        JButton btnValiderConvention = new JButton("Valider la convention");
        JButton btnPdfConvention = new JButton("Voir PDF");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnValiderConvention);
        btnPanel.add(btnPdfConvention);

        JPanel droitePanel = new JPanel(new BorderLayout());
        JSplitPane splitStages = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollStages, scrollConventions);
        splitStages.setDividerLocation(200);
        droitePanel.add(alerteLabel, BorderLayout.NORTH);
        droitePanel.add(splitStages, BorderLayout.CENTER);
        droitePanel.add(btnPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollEleves, droitePanel);
        splitPane.setDividerLocation(200);

        add(splitPane, BorderLayout.CENTER);

        tableEleves.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableEleves.getSelectedRow();
                if (selectedRow != -1) {
                    eleveIdSelectionne = eleves.get(selectedRow).getId();
                    loadStages(eleveIdSelectionne);
                    loadConventions(eleveIdSelectionne);
                }
            }
        });

        btnValiderConvention.addActionListener(e -> {
            int selectedRow = tableConventions.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une convention.");
                return;
            }
            Convention convention = conventions.get(selectedRow);
            if (convention.isValide()) {
                JOptionPane.showMessageDialog(this, "Convention déjà validée.");
                return;
            }
            try {
                ConventionService.valider(convention.getId());
                loadConventions(eleveIdSelectionne);
                JOptionPane.showMessageDialog(this, "✅ Convention validée !");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        });

        btnPdfConvention.addActionListener(e -> {
            int selectedRow = tableConventions.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une convention.");
                return;
            }
            Convention convention = conventions.get(selectedRow);
            new Thread(() -> {
                try {
                    byte[] pdfBytes = fr.asimov.api.ApiClient.getBytes(
                            "/conventions/" + convention.getId() + "/pdf");
                    java.io.File tempFile = java.io.File.createTempFile(
                            "convention_" + convention.getId(), ".pdf");
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
                            JOptionPane.showMessageDialog(this, "Erreur PDF : " + ex.getMessage())
                    );
                }
            }).start();
        });
    }

    private void loadEleves() {
        new Thread(() -> {
            try {
                eleves = ProfesseurService.getEleves(Session.metierId);
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

    private void loadStages(int eleveId) {
        new Thread(() -> {
            try {
                recherches = RechercheStageService.getByEleve(eleveId);
                boolean alerte = RechercheStageService.hasAlerte(eleveId);
                SwingUtilities.invokeLater(() -> {
                    stagesModel.setRowCount(0);
                    for (RechercheStage r : recherches) {
                        stagesModel.addRow(new Object[]{
                                r.getId(),
                                r.getNomEntreprise(),
                                r.getStatut(),
                                r.getNbLettresEnvoyees()
                        });
                    }
                    alerteLabel.setText(alerte ? "⚠️ Alerte : cet élève a atteint 15 refus !" : "");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erreur stages : " + ex.getMessage())
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
                        JOptionPane.showMessageDialog(this, "Erreur conventions : " + ex.getMessage())
                );
            }
        }).start();
    }
}