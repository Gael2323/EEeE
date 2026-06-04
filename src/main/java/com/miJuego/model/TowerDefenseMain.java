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
                .bind(KeyEvent.VK_ENTER, GameCommands.START);

        controller.bind(model, view);
        controller.start();
    }

    private TowerDefenseMain() {
    }
}
