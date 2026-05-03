package fr.asimov.ui;

import fr.asimov.model.Eleve;
import fr.asimov.model.Option;
import fr.asimov.service.EleveService;
import fr.asimov.service.OptionService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class OptionPanelAdmin extends JPanel {

    private JTable tableOptions;
    private DefaultTableModel optionsModel;
    private JButton btnAjouter;
    private JButton btnSupprimer;
    private JButton btnAffecter;
    private JButton btnRetirer;
    private List<Option> options;

    public OptionPanelAdmin() {
        setLayout(new BorderLayout());
        initComponents();
        loadOptions();
    }

    private void initComponents() {
        String[] colonnes = {"ID", "Nom", "Type"};
        optionsModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableOptions = new JTable(optionsModel);
        tableOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableOptions.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollPane = new JScrollPane(tableOptions);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAjouter = new JButton("Ajouter une option");
        btnSupprimer = new JButton("Supprimer");
        btnAffecter = new JButton("Affecter à un élève");
        btnRetirer = new JButton("Retirer d'un élève");

        btnPanel.add(btnAjouter);
        btnPanel.add(btnSupprimer);
        btnPanel.add(btnAffecter);
        btnPanel.add(btnRetirer);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        btnAjouter.addActionListener(e -> showAjouterDialog());
        btnSupprimer.addActionListener(e -> handleSupprimer());
        btnAffecter.addActionListener(e -> showAffecterDialog());
        btnRetirer.addActionListener(e -> showRetirerDialog());
    }

    private void loadOptions() {
        new Thread(() -> {
            try {
                Map<Integer, String> map = OptionService.getAll();
                options = OptionService.getAllAsList();
                SwingUtilities.invokeLater(() -> {
                    optionsModel.setRowCount(0);
                    for (Option o : options) {
                        optionsModel.addRow(new Object[]{
                                o.getId(), o.getNom(), o.getType()
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erreur chargement options : " + ex.getMessage())
                );
            }
        }).start();
    }

    private void showAjouterDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ajouter une option", true);
        dialog.setSize(350, 180);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nomField = new JTextField(20);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"langue", "technique"});

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nom :"), gbc);
        gbc.gridx = 1; panel.add(nomField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Type :"), gbc);
        gbc.gridx = 1; panel.add(typeCombo, gbc);

        JButton btnValider = new JButton("Créer");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                String nom = nomField.getText().trim();
                String type = (String) typeCombo.getSelectedItem();
                if (nom.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Nom obligatoire.");
                    return;
                }
                OptionService.create(nom, type);
                dialog.dispose();
                loadOptions();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleSupprimer() {
        int selectedRow = tableOptions.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une option.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer cette option ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                OptionService.delete(options.get(selectedRow).getId());
                loadOptions();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur suppression : " + ex.getMessage());
            }
        }
    }

    private void showAffecterDialog() {
        int selectedRow = tableOptions.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une option.");
            return;
        }
        Option option = options.get(selectedRow);

        List<Eleve> eleves;
        try {
            eleves = EleveService.getAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur chargement élèves.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Affecter à un élève", true);
        dialog.setSize(350, 150);
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

        JButton btnValider = new JButton("Affecter");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                int eleveId = eleveIds[eleveCombo.getSelectedIndex()];
                OptionService.affecter(eleveId, option.getId());
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Option affectée !");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showRetirerDialog() {
        int selectedRow = tableOptions.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une option.");
            return;
        }
        Option option = options.get(selectedRow);

        List<Eleve> eleves;
        try {
            eleves = EleveService.getAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur chargement élèves.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Retirer d'un élève", true);
        dialog.setSize(350, 150);
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
                OptionService.retirer(eleveId, option.getId());
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Option retirée !");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }
}