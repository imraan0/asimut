package fr.asimov.ui;

import fr.asimov.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;

    public LoginFrame() {
        setTitle("Asimut — Connexion");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Email :"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);

        // Mot de passe
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Bouton
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        loginButton = new JButton("Se connecter");
        panel.add(loginButton, gbc);

        // Message d'erreur
        gbc.gridy = 3;
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(errorLabel, gbc);

        add(panel);

        loginButton.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        loginButton.setEnabled(false);
        errorLabel.setText("Connexion en cours...");

        new Thread(() -> {
            boolean success = AuthService.login(email, password);
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    dispose();
                    new DashboardFrame().setVisible(true);
                } else {
                    errorLabel.setText("Email ou mot de passe incorrect.");
                    loginButton.setEnabled(true);
                }
            });
        }).start();
    }
}