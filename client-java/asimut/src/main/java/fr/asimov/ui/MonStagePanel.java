package fr.asimov.ui;

import fr.asimov.model.RechercheStage;
import fr.asimov.service.RechercheStageService;
import fr.asimov.util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MonStagePanel extends JPanel {

    private JTable tableStages;
    private DefaultTableModel stagesModel;
    private JButton btnAjouter;
    private JButton btnSupprimer;
    private JLabel alerteLabel;
    private List<RechercheStage> recherches;

    public MonStagePanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadStages();
    }

    private void initComponents() {
        alerteLabel = new JLabel("");
        alerteLabel.setForeground(Color.RED);
        alerteLabel.setFont(new Font("Arial", Font.BOLD, 13));
        alerteLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        String[] colonnes = {"ID", "Entreprise", "Contact", "Email", "Statut"};
        stagesModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableStages = new JTable(stagesModel);
        tableStages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableStages.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollPane = new JScrollPane(tableStages);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mes recherches de stage"));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAjouter = new JButton("Ajouter");
        btnSupprimer = new JButton("Supprimer");
        btnPanel.add(btnAjouter);
        btnPanel.add(btnSupprimer);

        add(alerteLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        btnAjouter.addActionListener(e -> showFormDialog());
        btnSupprimer.addActionListener(e -> handleSupprimer());
    }

    private void loadStages() {
        new Thread(() -> {
            try {
                recherches = RechercheStageService.getByEleve(Session.metierId);
                boolean alerte = RechercheStageService.hasAlerte(Session.metierId);
                SwingUtilities.invokeLater(() -> {
                    stagesModel.setRowCount(0);
                    for (RechercheStage r : recherches) {
                        stagesModel.addRow(new Object[]{
                                r.getId(),
                                r.getNomEntreprise(),
                                r.getNomContact(),
                                r.getEmailContact(),
                                r.getStatut()
                        });
                    }
                    alerteLabel.setText(alerte ? "⚠️ Vous avez atteint 15 refus — contactez votre référent !" : "");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage())
                );
            }
        }).start();
    }

    private void showFormDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Ajouter une recherche", true);
        dialog.setSize(400, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField entrepriseField = new JTextField(20);
        JTextField contactField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        String[] statuts = {"non_contacte", "en_attente", "refuse", "entretien_accorde", "entretien_refuse", "valide"};
        JComboBox<String> statutCombo = new JComboBox<>(statuts);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Entreprise :"), gbc);
        gbc.gridx = 1; panel.add(entrepriseField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Contact :"), gbc);
        gbc.gridx = 1; panel.add(contactField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Email contact :"), gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Statut :"), gbc);
        gbc.gridx = 1; panel.add(statutCombo, gbc);

        JButton btnValider = new JButton("Ajouter");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                String entreprise = entrepriseField.getText().trim();
                String contact = contactField.getText().trim();
                String email = emailField.getText().trim();
                String statut = (String) statutCombo.getSelectedItem();

                if (entreprise.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Entreprise obligatoire.");
                    return;
                }
                RechercheStageService.create(Session.metierId, entreprise, contact, email, statut);
                dialog.dispose();
                loadStages();
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
                loadStages();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }
}