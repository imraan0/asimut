package fr.asimov.ui;

import fr.asimov.model.Eleve;
import fr.asimov.model.Moyenne;
import fr.asimov.service.EleveService;
import fr.asimov.service.MoyenneService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MoyennePanel extends JPanel {

    private JTable tableEleves;
    private DefaultTableModel elevesModel;
    private JTable tableMoyennes;
    private DefaultTableModel moyennesModel;
    private JButton btnAjouter;
    private JButton btnModifier;
    private List<Eleve> eleves;
    private List<Moyenne> moyennes;

    public MoyennePanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadEleves();
    }

    private void initComponents() {
        // Tableau élèves (gauche)
        String[] colonnesEleves = {"ID", "Nom", "Prénom", "Classe"};
        elevesModel = new DefaultTableModel(colonnesEleves, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableEleves = new JTable(elevesModel);
        tableEleves.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEleves.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollEleves = new JScrollPane(tableEleves);
        scrollEleves.setPreferredSize(new Dimension(300, 0));
        scrollEleves.setBorder(BorderFactory.createTitledBorder("Élèves"));

        // Tableau moyennes (droite)
        String[] colonnesMoyennes = {"ID", "Semestre", "Valeur", "Validée"};
        moyennesModel = new DefaultTableModel(colonnesMoyennes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableMoyennes = new JTable(moyennesModel);
        tableMoyennes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMoyennes.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollMoyennes = new JScrollPane(tableMoyennes);
        scrollMoyennes.setBorder(BorderFactory.createTitledBorder("Moyennes"));

        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAjouter = new JButton("Ajouter une moyenne");
        btnModifier = new JButton("Modifier la valeur");
        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);

        // Layout principal : élèves à gauche, moyennes à droite
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollEleves, scrollMoyennes);
        splitPane.setDividerLocation(300);

        add(splitPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Quand on clique sur un élève → charge ses moyennes
        tableEleves.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableEleves.getSelectedRow();
                if (selectedRow != -1) {
                    loadMoyennes(eleves.get(selectedRow).getId());
                }
            }
        });

        btnAjouter.addActionListener(e -> {
            int selectedRow = tableEleves.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un élève.");
                return;
            }
            showAjouterDialog(eleves.get(selectedRow).getId());
        });

        btnModifier.addActionListener(e -> {
            int selectedEleve = tableEleves.getSelectedRow();
            int selectedMoyenne = tableMoyennes.getSelectedRow();
            if (selectedEleve == -1 || selectedMoyenne == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un élève et une moyenne.");
                return;
            }
            Moyenne moyenne = moyennes.get(selectedMoyenne);
            if (moyenne.isValide()) {
                JOptionPane.showMessageDialog(this, "Cette moyenne est validée par le proviseur, elle ne peut plus être modifiée.");
                return;
            }
            showModifierDialog(moyenne);
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
                                e.getId(),
                                e.getNom(),
                                e.getPrenom(),
                                e.getClasse()
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

    private void loadMoyennes(int eleveId) {
        new Thread(() -> {
            try {
                moyennes = MoyenneService.getByEleve(eleveId);
                SwingUtilities.invokeLater(() -> {
                    moyennesModel.setRowCount(0);
                    for (Moyenne m : moyennes) {
                        moyennesModel.addRow(new Object[]{
                                m.getId(),
                                m.getSemestreLibelle(),
                                m.getValeur(),
                                m.isValide() ? "✅ Oui" : "❌ Non"
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erreur chargement moyennes : " + ex.getMessage())
                );
            }
        }).start();
    }

    private void showAjouterDialog(int eleveId) {
        Map<Integer, String> semestres;
        try {
            semestres = MoyenneService.getSemestres();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur chargement semestres : " + ex.getMessage());
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ajouter une moyenne", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> semestreCombo = new JComboBox<>();
        int[] semestreIds = new int[semestres.size()];
        int idx = 0;
        for (Map.Entry<Integer, String> entry : semestres.entrySet()) {
            semestreIds[idx++] = entry.getKey();
            semestreCombo.addItem(entry.getValue());
        }

        JTextField valeurField = new JTextField(10);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Semestre :"), gbc);
        gbc.gridx = 1; panel.add(semestreCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Valeur :"), gbc);
        gbc.gridx = 1; panel.add(valeurField, gbc);

        JButton btnValider = new JButton("Ajouter");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                double valeur = Double.parseDouble(valeurField.getText().trim().replace(",", "."));
                if (valeur < 0 || valeur > 20) {
                    JOptionPane.showMessageDialog(dialog, "La valeur doit être entre 0 et 20.");
                    return;
                }
                int semestreId = semestreIds[semestreCombo.getSelectedIndex()];
                MoyenneService.create(eleveId, semestreId, valeur);
                dialog.dispose();
                loadMoyennes(eleveId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Valeur invalide.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showModifierDialog(Moyenne moyenne) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Modifier la moyenne", true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField valeurField = new JTextField(String.valueOf(moyenne.getValeur()), 10);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nouvelle valeur :"), gbc);
        gbc.gridx = 1; panel.add(valeurField, gbc);

        JButton btnValider = new JButton("Modifier");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                double valeur = Double.parseDouble(valeurField.getText().trim().replace(",", "."));
                if (valeur < 0 || valeur > 20) {
                    JOptionPane.showMessageDialog(dialog, "La valeur doit être entre 0 et 20.");
                    return;
                }
                MoyenneService.update(moyenne.getId(), valeur);
                dialog.dispose();
                loadMoyennes(moyenne.getEleveId());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Valeur invalide.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }
}