package fr.asimov.ui;

import fr.asimov.model.Parent;
import fr.asimov.service.MailService;
import fr.asimov.util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MailPanel extends JPanel {

    private JTable tableParents;
    private DefaultTableModel parentsModel;
    private JButton btnMailOne;
    private JButton btnMailAll;
    private List<Parent> parents;

    public MailPanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadParents();
    }

    private void initComponents() {
        String[] colonnes = {"ID", "Nom", "Prénom", "Email", "Élève"};
        parentsModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableParents = new JTable(parentsModel);
        tableParents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableParents.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scrollPane = new JScrollPane(tableParents);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnMailOne = new JButton("Envoyer un email");
        btnMailAll = new JButton("Envoyer à tous");
        btnPanel.add(btnMailOne);
        btnPanel.add(btnMailAll);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        btnMailOne.addActionListener(e -> {
            int selectedRow = tableParents.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un parent.");
                return;
            }
            showMailDialog(parents.get(selectedRow));
        });

        btnMailAll.addActionListener(e -> showMailAllDialog());
    }

    private void loadParents() {
        new Thread(() -> {
            try {
                parents = MailService.getParents();
                SwingUtilities.invokeLater(() -> {
                    parentsModel.setRowCount(0);
                    for (Parent p : parents) {
                        parentsModel.addRow(new Object[]{
                                p.getId(),
                                p.getNom(),
                                p.getPrenom(),
                                p.getEmail(),
                                p.getPrenomEleve() + " " + p.getNomEleve()
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    parents = new java.util.ArrayList<>();
                    JOptionPane.showMessageDialog(this,
                            "Impossible de charger les parents pour l'instant.\nL'envoi groupé reste disponible.");
                });
            }
        }).start();
    }

    private void showMailDialog(Parent parent) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Email à " + parent.getPrenom() + " " + parent.getNom(), true);
        dialog.setSize(450, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField sujetField = new JTextField(25);
        JTextArea messageArea = new JTextArea(6, 25);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JTextField auteurField = new JTextField("Secrétariat Asimov", 25);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Sujet :"), gbc);
        gbc.gridx = 1; panel.add(sujetField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Message :"), gbc);
        gbc.gridx = 1; panel.add(new JScrollPane(messageArea), gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Auteur :"), gbc);
        gbc.gridx = 1; panel.add(auteurField, gbc);

        JButton btnEnvoyer = new JButton("Envoyer");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btnEnvoyer, gbc);

        btnEnvoyer.addActionListener(ev -> {
            String sujet = sujetField.getText().trim();
            String message = messageArea.getText().trim();
            String auteur = auteurField.getText().trim();

            if (sujet.isEmpty() || message.isEmpty() || auteur.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tous les champs sont obligatoires.");
                return;
            }

            new Thread(() -> {
                try {
                    MailService.mailOne(parent.getId(), sujet, message, auteur);
                    SwingUtilities.invokeLater(() -> {
                        dialog.dispose();
                        JOptionPane.showMessageDialog(this, "✅ Email envoyé à " + parent.getEmail());
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage())
                    );
                }
            }).start();
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showMailAllDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Email à tous les parents", true);
        dialog.setSize(450, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField sujetField = new JTextField(25);
        JTextArea messageArea = new JTextArea(6, 25);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JTextField auteurField = new JTextField("Secrétariat Asimov", 25);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Sujet :"), gbc);
        gbc.gridx = 1; panel.add(sujetField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Message :"), gbc);
        gbc.gridx = 1; panel.add(new JScrollPane(messageArea), gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Auteur :"), gbc);
        gbc.gridx = 1; panel.add(auteurField, gbc);

        JButton btnEnvoyer = new JButton("Envoyer à tous");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btnEnvoyer, gbc);

        btnEnvoyer.addActionListener(ev -> {
            String sujet = sujetField.getText().trim();
            String message = messageArea.getText().trim();
            String auteur = auteurField.getText().trim();

            if (sujet.isEmpty() || message.isEmpty() || auteur.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tous les champs sont obligatoires.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Envoyer un email à tous les parents ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            new Thread(() -> {
                try {
                    MailService.mailAll(sujet, message, auteur);
                    SwingUtilities.invokeLater(() -> {
                        dialog.dispose();
                        JOptionPane.showMessageDialog(this, "✅ Emails envoyés à tous les parents !");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage())
                    );
                }
            }).start();
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }
}