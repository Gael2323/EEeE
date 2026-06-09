package com.miJuego.model;

import com.game2d.controller.DefaultGameController;
import com.game2d.controller.GameCommands;
import com.game2d.model.GameModel;
import com.game2d.view.GameView;
import com.game2d.view.GameViewMessages;
import com.game2d.view.GameViews;
import com.game2d.view.BackgroundSettings;

import java.awt.Color;
import java.awt.event.KeyEvent;

public final class TowerDefenseMain {

    public static void main(String[] args) {
        // Cambiar el color de fondo a verde bosque para representar el pasto
        BackgroundSettings.getInstance().setFallbackColor(new Color(34, 139, 34));

        GameView view = GameViews.getInstance().getView();
        GameViewMessages.getInstance().bind(view);
        GameModel model = new TowerDefenseModel(view);

        DefaultGameController controller = new DefaultGameController();
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
                // ── Cámara ──────────────────────────────────────────────────
                .bind(KeyEvent.VK_LEFT,  "CAM_LEFT")
                .bind(KeyEvent.VK_RIGHT, "CAM_RIGHT")
                .bind(KeyEvent.VK_UP,    "CAM_UP")
                .bind(KeyEvent.VK_DOWN,  "CAM_DOWN");

        controller.bind(model, view);
        controller.start();
    }

    private TowerDefenseMain() {
    }
}
