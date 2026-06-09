package com.miJuego.demo;

import com.game2d.controller.DefaultGameController;
import com.game2d.controller.GameCommands;
import com.game2d.model.GameModel;
import com.game2d.view.*;
import com.miJuego.model.TowerDefenseModel;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Lanza el nivel 1 (Word) desde la demo.
 *
 * <p>Configura el motor existente con la estética "desde adentro del documento"
 * y lo conecta con el flujo de la demo.</p>
 */
public final class WordLevelLauncher {

    /**
     * Arranca el nivel Word.
     */
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            // ── Fondo personalizado del documento ────────────────────────
            BackgroundSettings bg = BackgroundSettings.getInstance();
            bg.setImagePath("assets/ingame/word_document_bg.png");
            bg.setFallbackColor(new java.awt.Color(245, 245, 240)); // blanco papel

            // ── Vista del juego ───────────────────────────────────────────
            GameView view = GameViews.getInstance().getView();
            GameViewMessages.getInstance().bind(view);

            // ── Modelo ────────────────────────────────────────────────────
            // Iniciar Nivel 1
            com.miJuego.model.CameraContext.setWorldBounds(32f, 24f);
            TowerDefenseModel model = new TowerDefenseModel(view, 1);

            // ── Consola de Desarrollador ──────────────────────────────────
            com.miJuego.sandbox.DevCommandExecutor executor = new com.miJuego.sandbox.DevCommandExecutor(model.getJuego());
            com.miJuego.sandbox.DevConsoleFrame console = new com.miJuego.sandbox.DevConsoleFrame(executor);
            com.game2d.model.GameModel wrappedModel = new com.miJuego.sandbox.DevConsoleModelWrapper(model, console);

            // ── Controller con keybinds ───────────────────────────────────
            if (DemoLauncher.currentController != null) {
                DemoLauncher.currentController.stop();
            }
            DefaultGameController controller = new DefaultGameController();
            DemoLauncher.currentController = controller;
            controller.getKeyCommands()
                    .bind(KeyEvent.VK_1, "1")
                    .bind(KeyEvent.VK_2, "2")
                    .bind(KeyEvent.VK_3, "3")
                    .bind(KeyEvent.VK_4, "4")
                    .bind(KeyEvent.VK_5, "5")
                    .bind(KeyEvent.VK_6, "6")
                    .bind(KeyEvent.VK_7, "7")
                    .bind(KeyEvent.VK_8, "8")
                    .bind(KeyEvent.VK_0, "0")
                    .bind(KeyEvent.VK_NUMPAD1, "1")
                    .bind(KeyEvent.VK_NUMPAD2, "2")
                    .bind(KeyEvent.VK_NUMPAD3, "3")
                    .bind(KeyEvent.VK_NUMPAD4, "4")
                    .bind(KeyEvent.VK_NUMPAD5, "5")
                    .bind(KeyEvent.VK_NUMPAD6, "6")
                    .bind(KeyEvent.VK_NUMPAD7, "7")
                    .bind(KeyEvent.VK_NUMPAD8, "8")
                    .bind(KeyEvent.VK_NUMPAD0, "0")
                    .bind(KeyEvent.VK_U, "U")
                    .bind(KeyEvent.VK_S, "S")
                    .bind(KeyEvent.VK_N, "N")
                    .bind(KeyEvent.VK_P, GameCommands.PAUSE)
                    .bind(KeyEvent.VK_SPACE, GameCommands.PAUSE)
                    .bind(KeyEvent.VK_R, GameCommands.RESTART)
                    .bind(KeyEvent.VK_ENTER, GameCommands.START)
                    .bind(KeyEvent.VK_PAGE_UP, "ZOOM_IN")
                    .bind(KeyEvent.VK_PAGE_DOWN, "ZOOM_OUT")
                    .bind(KeyEvent.VK_ADD, "ZOOM_IN")
                    .bind(KeyEvent.VK_SUBTRACT, "ZOOM_OUT")
                    .bind(KeyEvent.VK_PLUS, "ZOOM_IN")
                    .bind(KeyEvent.VK_MINUS, "ZOOM_OUT")
                    .bind(KeyEvent.VK_BACK_QUOTE, "DEV_CONSOLE_TOGGLE")
                    .bind(KeyEvent.VK_F1, "DEV_CONSOLE_TOGGLE")
                    // ── Cámara ─────────────────────────────────────────────
                    .bind(KeyEvent.VK_LEFT,  "CAM_LEFT")
                    .bind(KeyEvent.VK_RIGHT, "CAM_RIGHT")
                    .bind(KeyEvent.VK_UP,    "CAM_UP")
                    .bind(KeyEvent.VK_DOWN,  "CAM_DOWN");

            controller.bind(wrappedModel, view);
            controller.start();
            view.show();
        });
    }

    private WordLevelLauncher() {}
}
