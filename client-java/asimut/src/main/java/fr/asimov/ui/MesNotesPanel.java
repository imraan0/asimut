package fr.asimov.ui;

import fr.asimov.model.Moyenne;
import fr.asimov.service.MoyenneService;
import fr.asimov.util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MesNotesPanel extends JPanel {

    private JTable tableMoyennes;
    private DefaultTableModel moyennesModel;

    public MesNotesPanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadMoyennes();
    }

    private void initComponents() {
        String[] colonnes = {"ID", "Semestre", "Moyenne", "Validée"};
        moyennesModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableMoyennes = new JTable(moyennesModel);
        tableMoyennes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMoyennes.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollPane = new JScrollPane(tableMoyennes);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mes moyennes"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadMoyennes() {
        new Thread(() -> {
            try {
                List<Moyenne> moyennes = MoyenneService.getByEleve(Session.metierId);
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
                        JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage())
                );
            }
        }).start();
    }
}