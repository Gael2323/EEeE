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
    public static com.game2d.controller.GameController currentController;

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
        frame.setResizable(true);

        MainMenuPanel menu = new MainMenuPanel();

        // ---- CALLBACKS ----
        menu.setOnJugar(() -> {
            // Crear panel de introducción
            IntroPanel intro = new IntroPanel();
            
            // Panel 1: Llegando a casa
            intro.addPage(new IntroPanel.IntroPage()
                    .imageFromResource("/assets/intro/Intro_Panel1.png")
                    .body("Hay lugares donde el tiempo simplemente decide detenerse.", 
                          "La casa de mis abuelos era uno de ellos."));
                          
            // Panel 2: En la habitación
            intro.addPage(new IntroPanel.IntroPage()
                    .imageFromResource("/assets/intro/Intro_Panel2.png")
                    .body("Entré a la antigua habitación de mi padre.", 
                          "Una cápsula intacta, olvidada en los años 90. Todo seguía exactamente donde lo dejó."));
                          
            // Panel 3: Destapando el monitor
            intro.addPage(new IntroPanel.IntroPage()
                    .imageFromResource("/assets/intro/Intro_Panel3.png")
                    .body("Entre revistas viejas y polvo, algo llamó mi atención.", 
                          "Una reliquia pesada y ruidosa que, contra toda lógica, seguía conectada."));
                          
            // Panel 4: Iniciando (Texto integrado en la imagen)
            intro.addPage(new IntroPanel.IntroPage()
                    .imageFromResource("/assets/intro/IntroPanel4.png"));
                    
            // Panel 5: Abriendo Word (Texto integrado en la imagen)
            intro.addPage(new IntroPanel.IntroPage()
                    .imageFromResource("/assets/intro/IntroPanel5.png"));
                    
            // Panel 6: (Texto integrado en la imagen)
            intro.addPage(new IntroPanel.IntroPage()
                    .imageFromResource("/assets/intro/Intro_Panel6.png"));
            
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
        // Maximizar después de setVisible con un pequeño delay para que Windows lo aplique
        javax.swing.Timer maxTimer = new javax.swing.Timer(50, e -> {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        });
        maxTimer.setRepeats(false);
        maxTimer.start();
    }

    /**
     * Regresa al menú principal desde el juego.
     *
     * <p>Llamado via reflexión desde {@link com.miJuego.model.TowerDefenseModel}
     * cuando se completa un nivel. Si el frame ya existe, lo hace visible y
     * muestra el menú; si no existe, crea uno nuevo.</p>
     */
    public static void returnToMainMenu() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.setVisible(true);
                showMainMenu();
            } else {
                showMainMenu();
            }
        });
    }

    /**
     * Muestra el hub de escritorio XP directamente (sin animación de transición).
     *
     * <p>Útil para regresar al hub desde dentro del juego si la transición
     * ya fue vista (postLevel1HubAnimationSeen == true).</p>
     *
     * @param completedLevel El nivel que acaba de terminar (para decidir si mostrar animación)
     */
    public static void showHub(int completedLevel) {
        SwingUtilities.invokeLater(() -> {
            if (frame == null) return;
            frame.setVisible(true);

            DesktopHubPanel hub = new DesktopHubPanel();
            hub.setOnWordAction(() -> {
                frame.setVisible(false);
                WordLevelLauncher.launch();
            });
            hub.setOnRecycleBinAction(() ->
                JOptionPane.showMessageDialog(frame,
                    "<html><b>Papelera – Nivel 2</b><br>Próximamente disponible.</html>",
                    "Nivel 2", JOptionPane.INFORMATION_MESSAGE)
            );
            hub.setOnTestAction(() -> {
                frame.setVisible(false);
                TestLevelLauncher.launch();
            });
            hub.setOnWizardAction(() -> {
                MerchantDialog dialog = new MerchantDialog(frame);
                dialog.setVisible(true);
            });
            switchScene(hub);
        });
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
