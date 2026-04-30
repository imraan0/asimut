package fr.asimov.ui;

import fr.asimov.model.Eleve;
import fr.asimov.service.EleveService;
import fr.asimov.util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ElevePanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAjouter;
    private JButton btnModifier;
    private JButton btnSupprimer;
    private JButton btnImportCSV;
    private List<Eleve> eleves;

    public ElevePanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadEleves();
    }

    private void initComponents() {
        // Tableau
        String[] colonnes = {"ID", "Nom", "Prénom", "Identifiant", "Classe", "Prof référent"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tableau non éditable directement
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50); // colonne ID étroite
        JScrollPane scrollPane = new JScrollPane(table);

        // Barre de boutons en bas
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAjouter = new JButton("Ajouter");
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        btnImportCSV = new JButton("Importer CSV");

        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);

        // Import CSV uniquement pour le secrétariat
        if ("secretariat".equals(Session.role)) {
            btnPanel.add(btnImportCSV);
        }

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Actions
        btnAjouter.addActionListener(e -> showFormDialog(null));
        btnModifier.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un élève.");
                return;
            }
            showFormDialog(eleves.get(selectedRow));
        });
        btnSupprimer.addActionListener(e -> handleSupprimer());
        btnImportCSV.addActionListener(e -> handleImportCSV());
    }

    // Charge les élèves depuis l'API et remplit le tableau
    private void loadEleves() {
        new Thread(() -> {
            try {
                eleves = EleveService.getAll();
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0); // vide le tableau
                    for (Eleve e : eleves) {
                        tableModel.addRow(new Object[]{
                                e.getId(),
                                e.getNom(),
                                e.getPrenom(),
                                e.getIdentifiant(),
                                e.getClasse(),
                                e.getProfReferent()
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

    // Formulaire ajout/modification
    private void showFormDialog(Eleve eleve) {
        boolean isEdit = eleve != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Modifier un élève" : "Ajouter un élève", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nomField = new JTextField(isEdit ? eleve.getNom() : "", 20);
        JTextField prenomField = new JTextField(isEdit ? eleve.getPrenom() : "", 20);
        JTextField identifiantField = new JTextField(isEdit ? eleve.getIdentifiant() : "", 20);
        JTextField classeIdField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JPasswordField mdpField = new JPasswordField(20);

        String[][] fields = {
                {"Nom :", null}, {"Prénom :", null},
                {"Identifiant :", null}, {"Classe ID :", null},
                {"Email :", null}, {"Mot de passe :", null}
        };

        JTextField[] inputs = {nomField, prenomField, identifiantField, classeIdField, emailField};

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nom :"), gbc);
        gbc.gridx = 1; panel.add(nomField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Prénom :"), gbc);
        gbc.gridx = 1; panel.add(prenomField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Identifiant :"), gbc);
        gbc.gridx = 1; panel.add(identifiantField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Classe ID :"), gbc);
        gbc.gridx = 1; panel.add(classeIdField, gbc);

        if (!isEdit) {
            gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Email :"), gbc);
            gbc.gridx = 1; panel.add(emailField, gbc);
            gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Mot de passe :"), gbc);
            gbc.gridx = 1; panel.add(mdpField, gbc);
        }

        JButton btnValider = new JButton(isEdit ? "Modifier" : "Ajouter");
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                String nom = nomField.getText().trim();
                String prenom = prenomField.getText().trim();
                String identifiant = identifiantField.getText().trim();
                int classeId = Integer.parseInt(classeIdField.getText().trim());

                if (isEdit) {
                    EleveService.update(eleve.getId(), nom, prenom, identifiant, classeId);
                } else {
                    String email = emailField.getText().trim();
                    String mdp = new String(mdpField.getPassword());
                    EleveService.create(nom, prenom, identifiant, email, mdp, classeId);
                }

                dialog.dispose();
                loadEleves(); // rafraîchit la liste
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Classe ID doit être un nombre.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleSupprimer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un élève.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer cet élève ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                EleveService.delete(eleves.get(selectedRow).getId());
                loadEleves();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur suppression : " + ex.getMessage());
            }
        }
    }

    private void handleImportCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sélectionner un fichier CSV");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                EleveService.importCSV(path);
                loadEleves();
                JOptionPane.showMessageDialog(this, "Import réussi !");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur import : " + ex.getMessage());
            }
        }
    }
}