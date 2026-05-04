package fr.asimov.ui;

import fr.asimov.model.Option;
import fr.asimov.service.OptionService;
import fr.asimov.util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MesOptionsPanel extends JPanel {

    private JTable tableOptions;
    private DefaultTableModel optionsModel;

    public MesOptionsPanel() {
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
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mes options"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadOptions() {
        new Thread(() -> {
            try {
                List<Option> options = OptionService.getAllAsList();
                // Filtre les options de cet élève via GET /eleves/:id/options
                String response = fr.asimov.api.ApiClient.get("/eleves/" + Session.metierId + "/options");
                org.json.JSONArray array = new org.json.JSONArray(response);
                SwingUtilities.invokeLater(() -> {
                    optionsModel.setRowCount(0);
                    for (int i = 0; i < array.length(); i++) {
                        org.json.JSONObject obj = array.getJSONObject(i);
                        optionsModel.addRow(new Object[]{
                                obj.getInt("id"),
                                obj.optString("nom", ""),
                                obj.optString("type", "")
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