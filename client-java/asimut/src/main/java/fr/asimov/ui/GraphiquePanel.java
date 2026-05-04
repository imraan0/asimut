package fr.asimov.ui;

import fr.asimov.model.Eleve;
import fr.asimov.model.Moyenne;
import fr.asimov.service.EleveService;
import fr.asimov.service.MoyenneService;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GraphiquePanel extends JPanel {

    private Map<String, Double> moyennesParNiveau = new LinkedHashMap<>();
    private boolean loaded = false;

    public GraphiquePanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 400));
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                List<Eleve> eleves = EleveService.getAll();
                Map<String, List<Double>> data = new LinkedHashMap<>();

                for (Eleve eleve : eleves) {
                    String niveau = eleve.getClasse(); // ex: "Niveau 6 - A"
                    List<Moyenne> moyennes = MoyenneService.getByEleve(eleve.getId());
                    if (moyennes.isEmpty()) continue;

                    double somme = 0;
                    for (Moyenne m : moyennes) somme += m.getValeur();
                    double moyenne = somme / moyennes.size();

                    // Extraire juste le numéro du niveau
                    String key = niveau.contains("Niveau") ?
                            niveau.split("-")[0].trim() : niveau;

                    data.computeIfAbsent(key, k -> new ArrayList<>()).add(moyenne);
                }

                // Calculer la moyenne par niveau
                Map<String, Double> result = new LinkedHashMap<>();
                for (Map.Entry<String, List<Double>> entry : data.entrySet()) {
                    double avg = entry.getValue().stream()
                            .mapToDouble(Double::doubleValue).average().orElse(0);
                    result.put(entry.getKey(), Math.round(avg * 100.0) / 100.0);
                }

                SwingUtilities.invokeLater(() -> {
                    moyennesParNiveau = result;
                    loaded = true;
                    repaint();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    loaded = true;
                    repaint();
                });
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 60;
        int barWidth = 80;
        int maxHeight = height - padding * 2;

        // Titre
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Moyennes par niveau de classe", padding, 30);

        if (!loaded) {
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.drawString("Chargement...", width / 2 - 50, height / 2);
            return;
        }

        if (moyennesParNiveau.isEmpty()) {
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.drawString("Aucune donnée disponible", width / 2 - 80, height / 2);
            return;
        }

        // Axes
        g2.setColor(Color.GRAY);
        g2.drawLine(padding, padding, padding, height - padding); // axe Y
        g2.drawLine(padding, height - padding, width - padding, height - padding); // axe X

        // Graduations Y (0 à 20)
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        for (int i = 0; i <= 20; i += 5) {
            int y = height - padding - (int) (i * maxHeight / 20.0);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(padding, y, width - padding, y);
            g2.setColor(Color.GRAY);
            g2.drawString(String.valueOf(i), padding - 25, y + 4);
        }

        // Barres
        Color[] couleurs = {
                new Color(70, 130, 180),
                new Color(60, 179, 113),
                new Color(255, 165, 0),
                new Color(220, 80, 80)
        };

        int x = padding + 30;
        int idx = 0;
        for (Map.Entry<String, Double> entry : moyennesParNiveau.entrySet()) {
            double valeur = entry.getValue();
            int barHeight = (int) (valeur * maxHeight / 20.0);
            int y = height - padding - barHeight;

            // Barre
            g2.setColor(couleurs[idx % couleurs.length]);
            g2.fillRect(x, y, barWidth, barHeight);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(x, y, barWidth, barHeight);

            // Valeur au dessus
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString(String.valueOf(valeur), x + barWidth / 2 - 15, y - 5);

            // Label niveau
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.drawString(entry.getKey(), x + 5, height - padding + 15);

            x += barWidth + 40;
            idx++;
        }
    }
}