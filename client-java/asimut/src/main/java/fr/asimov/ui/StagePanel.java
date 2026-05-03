package fr.asimov.ui;

import fr.asimov.model.Eleve;
import fr.asimov.model.RechercheStage;
import fr.asimov.service.EleveService;
import fr.asimov.service.RechercheStageService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StagePanel extends JPanel {

    private JTable tableEleves;
    private DefaultTableModel elevesModel;
    private JTable tableStages;
    private DefaultTableModel stagesModel;
    private JLabel alerteLabel;
    private JButton btnAjouter;
    private JButton btnModifier;
    private JButton btnSupprimer;
    private List<Eleve> eleves;
    private List<RechercheStage> recherches;
    private int eleveIdSelectionne = -1;

    public StagePanel() {
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

        // Alerte 15 refus
        alerteLabel = new JLabel("");
        alerteLabel.setForeground(Color.RED);
        alerteLabel.setFont(new Font("Arial", Font.BOLD, 13));
        alerteLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Tableau recherches droite
        String[] colonnesStages = {"ID", "Entreprise", "Contact", "Email", "Lettres env.", "Lettres reç.", "Statut"};
        stagesModel = new DefaultTableModel(colonnesStages, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableStages = new JTable(stagesModel);
        tableStages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableStages.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollStages = new JScrollPane(tableStages);
        scrollStages.setBorder(BorderFactory.createTitledBorder("Recherches de stage"));

        // Panel droite = alerte + tableau
        JPanel droitePanel = new JPanel(new BorderLayout());
        droitePanel.add(alerteLabel, BorderLayout.NORTH);
        droitePanel.add(scrollStages, BorderLayout.CENTER);

        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAjouter = new JButton("Ajouter");
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollEleves, droitePanel);
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Sélection élève → charge ses recherches
        tableEleves.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableEleves.getSelectedRow();
                if (selectedRow != -1) {
                    eleveIdSelectionne = eleves.get(selectedRow).getId();
                    loadRecherches(eleveIdSelectionne);
                }
            }
        });

        btnAjouter.addActionListener(e -> {
            if (eleveIdSelectionne == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un élève.");
                return;
            }
            showFormDialog(null);
        });

        btnModifier.addActionListener(e -> {
            int selectedRow = tableStages.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une recherche.");
                return;
            }
            showFormDialog(recherches.get(selectedRow));
        });

        btnSupprimer.addActionListener(e -> handleSupprimer());
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

    private void loadRecherches(int eleveId) {
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
                                r.getNomContact(),
                                r.getEmailContact(),
                                r.getNbLettresEnvoyees(),
                                r.getNbLettresRecues(),
                                r.getStatut()
                        });
                    }
                    if (alerte) {
                        alerteLabel.setText("⚠️ Alerte : cet élève a atteint 15 refus !");
                    } else {
                        alerteLabel.setText("");
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erreur chargement stages : " + ex.getMessage())
                );
            }
        }).start();
    }

    private void showFormDialog(RechercheStage recherche) {
        boolean isEdit = recherche != null;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Modifier une recherche" : "Ajouter une recherche", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField entrepriseField = new JTextField(isEdit ? recherche.getNomEntreprise() : "", 20);
        JTextField contactField = new JTextField(isEdit ? recherche.getNomContact() : "", 20);
        JTextField emailField = new JTextField(isEdit ? recherche.getEmailContact() : "", 20);
        JTextField resultatField = new JTextField(isEdit ? recherche.getResultat() : "", 20);

        String[] statuts = {"non_contacte", "en_attente", "refuse", "entretien_accorde", "entretien_refuse", "valide"};
        JComboBox<String> statutCombo = new JComboBox<>(statuts);
        if (isEdit) {
            statutCombo.setSelectedItem(recherche.getStatut());
        }

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Entreprise :"), gbc);
        gbc.gridx = 1; panel.add(entrepriseField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Contact :"), gbc);
        gbc.gridx = 1; panel.add(contactField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Email contact :"), gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Statut :"), gbc);
        gbc.gridx = 1; panel.add(statutCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Résultat :"), gbc);
        gbc.gridx = 1; panel.add(resultatField, gbc);

        JButton btnValider = new JButton(isEdit ? "Modifier" : "Ajouter");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                String entreprise = entrepriseField.getText().trim();
                String contact = contactField.getText().trim();
                String email = emailField.getText().trim();
                String statut = (String) statutCombo.getSelectedItem();
                String resultat = resultatField.getText().trim();

                if (entreprise.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Le nom de l'entreprise est obligatoire.");
                    return;
                }

                if (isEdit) {
                    RechercheStageService.update(recherche.getId(), entreprise, contact, email, statut, resultat);
                } else {
                    RechercheStageService.create(eleveIdSelectionne, entreprise, contact, email, statut);
                }

                dialog.dispose();
                loadRecherches(eleveIdSelectionne);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleSupprimer() {
        int selectedRow = tableStages.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une recherche.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer cette recherche ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                RechercheStageService.delete(recherches.get(selectedRow).getId());
                loadRecherches(eleveIdSelectionne);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur suppression : " + ex.getMessage());
            }
        }
    }
}