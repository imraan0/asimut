package fr.asimov.ui;

import fr.asimov.service.AuthService;
import fr.asimov.util.Session;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {

    private JPanel contentPanel;

    public DashboardFrame() {
        setTitle("Asimut — " + Session.role);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Barre latérale gauche avec les boutons de navigation
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(45, 52, 54));
        sidebar.setPreferredSize(new Dimension(200, 600));

        // Titre dans la sidebar
        JLabel titleLabel = new JLabel("Asimut");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        sidebar.add(titleLabel);

        // Boutons selon le rôle
        addMenuButtons(sidebar);

        // Bouton déconnexion en bas
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(160, 35));
        logoutButton.addActionListener(e -> {
            AuthService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
        sidebar.add(Box.createVerticalGlue()); // pousse le bouton en bas
        sidebar.add(logoutButton);
        sidebar.add(Box.createVerticalStrut(20));

        // Panel principal (contenu qui change selon le menu)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Bienvenue, rôle : " + Session.role, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        contentPanel.add(welcomeLabel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void addMenuButtons(JPanel sidebar) {
        switch (Session.role) {
            case "secretariat" -> {
                addButton(sidebar, "Élèves");
                addButton(sidebar, "Notes");
                addButton(sidebar, "Stages");
                addButton(sidebar, "Conventions");
                addButton(sidebar, "Attestations");
                addButton(sidebar, "Projets");
                addButton(sidebar, "Options");
                addButton(sidebar, "Professeurs");
                addButton(sidebar, "Parents");
            }
            case "proviseur" -> {
                addButton(sidebar, "Élèves");
                addButton(sidebar, "Notes");
                addButton(sidebar, "Conventions");
                addButton(sidebar, "Attestations");
            }
            case "professeur" -> {
                addButton(sidebar, "Mes élèves");
                addButton(sidebar, "Notes");
            }
            case "eleve" -> {
                addButton(sidebar, "Mes notes");
                addButton(sidebar, "Mon stage");
                addButton(sidebar, "Mes projets");
                addButton(sidebar, "Mes options");
            }
        }
    }

    private void addButton(JPanel sidebar, String label) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(160, 35));
        button.addActionListener(e -> showPanel(label));
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(5));
    }

    public void showPanel(String panelName) {
        contentPanel.removeAll();
        JPanel panel;
        switch (panelName) {
            case "Élèves" -> panel = new ElevePanel();
            case "Notes" -> panel = new MoyennePanel();
            case "Stages" -> panel = new StagePanel();
            case "Conventions" -> panel = new ConventionPanel();
            case "Attestations" -> panel = new AttestationPanel();
            case "Projets" -> panel = new ProjetPanel();
            case "Options" -> panel = new OptionPanelAdmin();
            case "Professeurs" -> panel = new ProfesseurPanel();
            case "Parents" -> panel = new MailPanel();
            default -> {
                JLabel placeholder = new JLabel(panelName + " — en cours de développement", SwingConstants.CENTER);
                panel = new JPanel(new BorderLayout());
                panel.add(placeholder, BorderLayout.CENTER);
            }
        }
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}