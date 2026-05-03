package fr.asimov.ui;

import fr.asimov.model.Eleve;
import fr.asimov.service.ClasseService;
import fr.asimov.service.EleveService;
import fr.asimov.service.OptionService;
import fr.asimov.service.ProfesseurService;
import fr.asimov.util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

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
        String[] colonnes = {"ID", "Nom", "Prénom", "Identifiant", "Classe", "Prof référent", "Option 1", "Option 2"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAjouter = new JButton("Ajouter");
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        btnImportCSV = new JButton("Importer CSV");

        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);
        if ("secretariat".equals(Session.role)) {
            btnPanel.add(btnImportCSV);
        }

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

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

    private void loadEleves() {
        new Thread(() -> {
            try {
                eleves = EleveService.getAll();
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    for (Eleve e : eleves) {
                        tableModel.addRow(new Object[]{
                                e.getId(),
                                e.getNom(),
                                e.getPrenom(),
                                e.getIdentifiant(),
                                e.getClasse(),
                                e.getProfReferent(),
                                e.getOption1(),
                                e.getOption2()
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

    private void showFormDialog(Eleve eleve) {
        boolean isEdit = eleve != null;

        Map<Integer, String> classes;
        Map<Integer, String> profs;
        Map<Integer, String> options;
        try {
            classes = ClasseService.getAll();
            profs = ProfesseurService.getAll();
            options = OptionService.getAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur chargement données : " + ex.getMessage());
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Modifier un élève" : "Ajouter un élève", true);
        dialog.setSize(420, isEdit ? 420 : 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nomField = new JTextField(isEdit ? eleve.getNom() : "", 20);
        JTextField prenomField = new JTextField(isEdit ? eleve.getPrenom() : "", 20);
        JTextField identifiantField = new JTextField(isEdit ? eleve.getIdentifiant() : "", 20);
        JTextField emailField = new JTextField(20);
        JPasswordField mdpField = new JPasswordField(20);

        // Combobox classes
        JComboBox<String> classeCombo = new JComboBox<>();
        int[] classeIds = new int[classes.size()];
        int idx = 0;
        for (Map.Entry<Integer, String> entry : classes.entrySet()) {
            classeIds[idx++] = entry.getKey();
            classeCombo.addItem(entry.getValue());
        }

        // Combobox profs
        JComboBox<String> profCombo = new JComboBox<>();
        int[] profIds = new int[profs.size()];
        int idxProf = 0;
        for (Map.Entry<Integer, String> entry : profs.entrySet()) {
            profIds[idxProf++] = entry.getKey();
            profCombo.addItem(entry.getValue());
        }

        // Combobox options (avec "Aucune" en premier)
        int[] optionIds = new int[options.size() + 1];
        String[] optionLabels = new String[options.size() + 1];
        optionIds[0] = 0;
        optionLabels[0] = "Aucune";
        int idxOpt = 1;
        for (Map.Entry<Integer, String> entry : options.entrySet()) {
            optionIds[idxOpt] = entry.getKey();
            optionLabels[idxOpt] = entry.getValue();
            idxOpt++;
        }
        JComboBox<String> option1Combo = new JComboBox<>(optionLabels);
        JComboBox<String> option2Combo = new JComboBox<>(optionLabels);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nom :"), gbc);
        gbc.gridx = 1; panel.add(nomField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Prénom :"), gbc);
        gbc.gridx = 1; panel.add(prenomField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Identifiant :"), gbc);
        gbc.gridx = 1; panel.add(identifiantField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Classe :"), gbc);
        gbc.gridx = 1; panel.add(classeCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Prof référent :"), gbc);
        gbc.gridx = 1; panel.add(profCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Option 1 :"), gbc);
        gbc.gridx = 1; panel.add(option1Combo, gbc);
        gbc.gridx = 0; gbc.gridy = 6; panel.add(new JLabel("Option 2 :"), gbc);
        gbc.gridx = 1; panel.add(option2Combo, gbc);

        if (!isEdit) {
            gbc.gridx = 0; gbc.gridy = 7; panel.add(new JLabel("Email :"), gbc);
            gbc.gridx = 1; panel.add(emailField, gbc);
            gbc.gridx = 0; gbc.gridy = 8; panel.add(new JLabel("Mot de passe :"), gbc);
            gbc.gridx = 1; panel.add(mdpField, gbc);
        }

        JButton btnValider = new JButton(isEdit ? "Modifier" : "Ajouter");
        gbc.gridx = 0; gbc.gridy = isEdit ? 7 : 9;
        gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                String nom = nomField.getText().trim();
                String prenom = prenomField.getText().trim();
                String identifiant = identifiantField.getText().trim();
                int classeId = classeIds[classeCombo.getSelectedIndex()];
                int profId = profIds[profCombo.getSelectedIndex()];
                int opt1Id = optionIds[option1Combo.getSelectedIndex()];
                int opt2Id = optionIds[option2Combo.getSelectedIndex()];

                // Vérification : les deux options ne peuvent pas être identiques
                if (opt1Id != 0 && opt1Id == opt2Id) {
                    JOptionPane.showMessageDialog(dialog, "Les deux options doivent être différentes.");
                    return;
                }

                if (isEdit) {
                    EleveService.update(eleve.getId(), nom, prenom, identifiant, classeId);

                    // Référent
                    if (profId == 0) {
                        EleveService.retirerReferent(eleve.getId());
                    } else {
                        EleveService.affecterReferent(eleve.getId(), profId);
                    }

                    // Options
                    if (opt1Id != 0) OptionService.affecter(eleve.getId(), opt1Id);
                    if (opt2Id != 0) OptionService.affecter(eleve.getId(), opt2Id);

                } else {
                    String email = emailField.getText().trim();
                    String mdp = new String(mdpField.getPassword());
                    EleveService.create(nom, prenom, identifiant, email, mdp, classeId);
                }

                dialog.dispose();
                loadEleves();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        JScrollPane formScroll = new JScrollPane(panel);
        dialog.add(formScroll);
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
            if (!path.endsWith(".csv")) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un fichier CSV.");
                return;
            }
            try {
                String rapport = EleveService.importCSV(path);
                org.json.JSONObject json = new org.json.JSONObject(rapport);
                int importes = json.getInt("importes");
                org.json.JSONArray erreurs = json.getJSONArray("erreurs");

                StringBuilder msg = new StringBuilder();
                msg.append("✅ ").append(importes).append(" élève(s) importé(s).");
                if (erreurs.length() > 0) {
                    msg.append("\n❌ ").append(erreurs.length()).append(" erreur(s) :");
                    for (int i = 0; i < erreurs.length(); i++) {
                        org.json.JSONObject err = erreurs.getJSONObject(i);
                        msg.append("\n  - Ligne ").append(err.getInt("ligne"))
                                .append(" : ").append(err.getString("raison"));
                    }
                }
                loadEleves();
                JOptionPane.showMessageDialog(this, msg.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur import : " + ex.getMessage());
            }
        }
    }
}