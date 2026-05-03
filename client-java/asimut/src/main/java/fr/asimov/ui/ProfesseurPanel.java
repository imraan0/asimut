package fr.asimov.ui;

import fr.asimov.model.Eleve;
import fr.asimov.model.Professeur;
import fr.asimov.service.EleveService;
import fr.asimov.service.ProfesseurService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ProfesseurPanel extends JPanel {

    private JTable tableProfs;
    private DefaultTableModel profsModel;
    private JTable tableEleves;
    private DefaultTableModel elevesModel;
    private JButton btnAffecterManuel;
    private JButton btnAffecterAuto;
    private List<Professeur> professeurs;
    private int profIdSelectionne = -1;

    public ProfesseurPanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadProfesseurs();
    }

    private void initComponents() {
        // Tableau professeurs gauche
        String[] colonnesProfs = {"ID", "Nom", "Prénom", "Nb élèves"};
        profsModel = new DefaultTableModel(colonnesProfs, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableProfs = new JTable(profsModel);
        tableProfs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableProfs.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollProfs = new JScrollPane(tableProfs);
        scrollProfs.setPreferredSize(new Dimension(300, 0));
        scrollProfs.setBorder(BorderFactory.createTitledBorder("Professeurs"));

        // Tableau élèves référents droite
        String[] colonnesEleves = {"ID", "Nom", "Prénom", "Classe"};
        elevesModel = new DefaultTableModel(colonnesEleves, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableEleves = new JTable(elevesModel);
        tableEleves.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEleves.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollEleves = new JScrollPane(tableEleves);
        scrollEleves.setBorder(BorderFactory.createTitledBorder("Élèves référents"));

        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAffecterManuel = new JButton("Affecter un référent");
        btnAffecterAuto = new JButton("Affectation automatique");
        btnPanel.add(btnAffecterManuel);
        btnPanel.add(btnAffecterAuto);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollProfs, scrollEleves);
        splitPane.setDividerLocation(300);

        add(splitPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Sélection prof → charge ses élèves
        tableProfs.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableProfs.getSelectedRow();
                if (selectedRow != -1) {
                    profIdSelectionne = professeurs.get(selectedRow).getId();
                    loadElevesReferents(profIdSelectionne);
                }
            }
        });

        btnAffecterManuel.addActionListener(e -> showAffecterManuelDialog());
        btnAffecterAuto.addActionListener(e -> showAffecterAutoDialog());
    }

    private void loadProfesseurs() {
        new Thread(() -> {
            try {
                professeurs = ProfesseurService.getAllAsList();
                SwingUtilities.invokeLater(() -> {
                    profsModel.setRowCount(0);
                    for (Professeur p : professeurs) {
                        profsModel.addRow(new Object[]{
                                p.getId(), p.getNom(), p.getPrenom(), p.getNbEleves()
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erreur chargement professeurs : " + ex.getMessage())
                );
            }
        }).start();
    }

    private void loadElevesReferents(int profId) {
        new Thread(() -> {
            try {
                List<Eleve> eleves = ProfesseurService.getEleves(profId);
                SwingUtilities.invokeLater(() -> {
                    elevesModel.setRowCount(0);
                    for (Eleve e : eleves) {
                        elevesModel.addRow(new Object[]{
                                e.getId(), e.getNom(), e.getPrenom(), e.getClasse()
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

    private void showAffecterManuelDialog() {
        List<Eleve> eleves;
        Map<Integer, String> profs;
        try {
            List<Eleve> tousLesEleves = EleveService.getAll();
            eleves = tousLesEleves.stream()
                    .filter(e -> e.getProfReferent() == null || e.getProfReferent().isBlank())
                    .collect(java.util.stream.Collectors.toList());
            profs = ProfesseurService.getAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur chargement données.");
            return;
        }

        if (eleves.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tous les élèves ont déjà un référent !");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Affecter un référent", true);
        dialog.setSize(400, 200);
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

        JComboBox<String> profCombo = new JComboBox<>();
        int[] profIds = new int[profs.size()];
        int idx = 0;
        int selectedProfIndex = 0;
        for (Map.Entry<Integer, String> entry : profs.entrySet()) {
            profIds[idx] = entry.getKey();
            profCombo.addItem(entry.getValue());
            if (entry.getKey() == profIdSelectionne) selectedProfIndex = idx;
            idx++;
        }
        profCombo.setSelectedIndex(selectedProfIndex);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Élève :"), gbc);
        gbc.gridx = 1; panel.add(eleveCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Professeur référent :"), gbc);
        gbc.gridx = 1; panel.add(profCombo, gbc);

        JButton btnValider = new JButton("Affecter");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnValider, gbc);

        btnValider.addActionListener(ev -> {
            try {
                int eleveId = eleveIds[eleveCombo.getSelectedIndex()];
                int profId = profIds[profCombo.getSelectedIndex()];
                EleveService.affecterReferent(eleveId, profId);
                dialog.dispose();
                loadProfesseurs();
                if (profIdSelectionne != -1) loadElevesReferents(profIdSelectionne);
                JOptionPane.showMessageDialog(this, "Référent affecté !");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showAffecterAutoDialog() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Affecter automatiquement un référent à tous les élèves sans référent ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                try {
                    List<Eleve> eleves = EleveService.getAll();
                    int count = 0;
                    for (Eleve e : eleves) {
                        if (e.getProfReferent() == null || e.getProfReferent().isBlank()) {
                            EleveService.affecterReferentAuto(e.getId());
                            count++;
                        }
                    }
                    int finalCount = count;
                    SwingUtilities.invokeLater(() -> {
                        loadProfesseurs();
                        if (profIdSelectionne != -1) loadElevesReferents(profIdSelectionne);
                        JOptionPane.showMessageDialog(this,
                                "✅ " + finalCount + " élève(s) affecté(s) automatiquement.");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage())
                    );
                }
            }).start();
        }
    }
}