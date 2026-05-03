package fr.asimov.ui;

import fr.asimov.model.Attestation;
import fr.asimov.model.Convention;
import fr.asimov.model.Eleve;
import fr.asimov.service.AttestationService;
import fr.asimov.service.ConventionService;
import fr.asimov.service.EleveService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AttestationPanel extends JPanel {

    private JTable tableEleves;
    private DefaultTableModel elevesModel;
    private JTable tableAttestations;
    private DefaultTableModel attestationsModel;
    private JButton btnVoirPdf;
    private JButton btnUpload;
    private List<Eleve> eleves;
    private List<Attestation> attestations = new ArrayList<>();
    private int eleveIdSelectionne = -1;

    public AttestationPanel() {
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

        // Tableau attestations droite
        String[] colonnesAttestations = {"ID", "Entreprise", "Élève", "Début", "Fin", "Signé"};
        attestationsModel = new DefaultTableModel(colonnesAttestations, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableAttestations = new JTable(attestationsModel);
        tableAttestations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableAttestations.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollAttestations = new JScrollPane(tableAttestations);
        scrollAttestations.setBorder(BorderFactory.createTitledBorder("Attestations"));

        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnVoirPdf = new JButton("Voir PDF");
        btnUpload = new JButton("Uploader PDF signé");
        btnPanel.add(btnVoirPdf);
        btnPanel.add(btnUpload);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollEleves, scrollAttestations);
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Sélection élève
        tableEleves.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableEleves.getSelectedRow();
                if (selectedRow != -1) {
                    eleveIdSelectionne = eleves.get(selectedRow).getId();
                    loadAttestations(eleveIdSelectionne);
                }
            }
        });

        btnVoirPdf.addActionListener(e -> {
            int selectedRow = tableAttestations.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une attestation.");
                return;
            }
            Attestation attestation = attestations.get(selectedRow);
            new Thread(() -> {
                try {
                    byte[] pdfBytes = AttestationService.getPdf(attestation.getId());
                    File tempFile = File.createTempFile("attestation_" + attestation.getId(), ".pdf");
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                        fos.write(pdfBytes);
                    }
                    SwingUtilities.invokeLater(() -> {
                        try {
                            Desktop.getDesktop().open(tempFile);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Impossible d'ouvrir le PDF.");
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage())
                    );
                }
            }).start();
        });

        btnUpload.addActionListener(e -> {
            int selectedRow = tableAttestations.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une attestation.");
                return;
            }
            Attestation attestation = attestations.get(selectedRow);
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Sélectionner le PDF signé");
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                new Thread(() -> {
                    try {
                        AttestationService.uploadPdf(attestation.getId(), file);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "PDF uploadé avec succès !");
                            loadAttestations(eleveIdSelectionne);
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(this, "Erreur upload : " + ex.getMessage())
                        );
                    }
                }).start();
            }
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

    private void loadAttestations(int eleveId) {
        new Thread(() -> {
            try {
                // On passe par les conventions pour trouver les attestations
                List<Convention> conventions = ConventionService.getByEleve(eleveId);
                List<Attestation> result = new ArrayList<>();
                for (Convention c : conventions) {
                    if (c.isValide()) {
                        try {
                            Attestation a = AttestationService.getById(c.getId());
                            result.add(a);
                        } catch (Exception ignored) {}
                    }
                }
                attestations = result;
                SwingUtilities.invokeLater(() -> {
                    attestationsModel.setRowCount(0);
                    for (Attestation a : attestations) {
                        attestationsModel.addRow(new Object[]{
                                a.getId(),
                                a.getNomEntreprise(),
                                a.getNomEleve(),
                                a.getDateDebut(),
                                a.getDateFin(),
                                a.isSigne() ? "✅ Oui" : "❌ Non"
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erreur chargement attestations : " + ex.getMessage())
                );
            }
        }).start();
    }
}