package fr.asimov.ui;

import fr.asimov.model.Eleve;
import fr.asimov.service.ProfesseurService;
import fr.asimov.util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MesElevesPanel extends JPanel {

    private JTable tableEleves;
    private DefaultTableModel elevesModel;
    private List<Eleve> eleves;

    public MesElevesPanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadEleves();
    }

    private void initComponents() {
        String[] colonnes = {"ID", "Nom", "Prénom", "Classe"};
        elevesModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableEleves = new JTable(elevesModel);
        tableEleves.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEleves.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollPane = new JScrollPane(tableEleves);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mes élèves référents"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadEleves() {
        new Thread(() -> {
            try {
                eleves = ProfesseurService.getEleves(Session.metierId);
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
                        JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage())
                );
            }
        }).start();
    }
}