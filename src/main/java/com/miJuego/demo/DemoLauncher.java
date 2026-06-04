package com.miJuego.demo;

import javax.swing.*;
import java.awt.*;

/**
 * Punto de entrada de la demo del Tower Defense.
 *
 * <p>Flujo: Menú Principal → Intro → Tutorial (Word) → Fin demo.</p>
 *
 * <h2>Cómo correrlo</h2>
 * <p>Cambiar en {@code build.gradle}:</p>
 * <pre>
 *   mainClass = 'com.miJuego.demo.DemoLauncher'
 * </pre>
 */
public final class DemoLauncher {

    private static JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DemoLauncher::showMainMenu);
    }

    /** Muestra el menú principal. */
    public static void showMainMenu() {
        if (frame != null) {
            frame.dispose();
        }

        frame = new JFrame("Tower Defense");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        MainMenuPanel menu = new MainMenuPanel();

        // ---- CALLBACKS ----
        menu.setOnJugar(() -> {
            // Crear panel de introducción
            IntroPanel intro = new IntroPanel();
            intro.addPage(new IntroPanel.IntroPage()
                    .title("Introducción")
                    .imageFromResource("/assets/intro/intro_placeholder.png"));
            // Al terminar la intro → escena interactiva de Word
            intro.setOnFinished(() -> {
                WordIntroPanel wordIntro = new WordIntroPanel();
                wordIntro.setOnFinished(() -> {
                    // Cinemática terminó → arranca el nivel real
                    DemoLauncher.getFrame().setVisible(false);
                    WordLevelLauncher.launch();
                });
                DemoLauncher.switchScene(wordIntro);
                SwingUtilities.invokeLater(wordIntro::requestFocusInWindow);
            });
            DemoLauncher.switchScene(intro);
            SwingUtilities.invokeLater(intro::requestFocusInWindow);
        });

        menu.setOnOpciones(() -> {
            // TODO: Pantalla de opciones / controles
            JOptionPane.showMessageDialog(frame,
                    "Controles:\n" +
                    "• Teclas 1-7: Seleccionar torre\n" +
                    "• Click: Colocar torre\n" +
                    "• U: Mejorar torre\n" +
                    "• S: Vender torre\n" +
                    "• N: Siguiente nivel\n" +
                    "• P / Espacio: Pausar\n" +
                    "• R: Reiniciar\n" +
                    "• Enter: Iniciar oleada",
                    "Opciones", JOptionPane.INFORMATION_MESSAGE);
        });

        menu.setOnSalir(() -> {
            System.exit(0);
        });

        frame.add(menu);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** Cambia la escena actual por un nuevo panel. */
    public static void switchScene(JPanel newScene) {
        if (frame == null) return;
        frame.getContentPane().removeAll();
        frame.add(newScene);
        frame.revalidate();
        frame.repaint();
    }

    /** Devuelve el frame principal para transiciones. */
    public static JFrame getFrame() {
        return frame;
    }

    private DemoLauncher() {}
}
