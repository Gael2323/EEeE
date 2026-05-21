package com.game2d.example.snake;

import com.game2d.controller.DefaultGameController;
import com.game2d.controller.GameCommands;
import com.game2d.model.GameModel;
import com.game2d.view.GameView;
import com.game2d.view.GameViewMessages;
import com.game2d.view.GameViews;

import java.awt.event.KeyEvent;

/**
 * Punto de entrada del Snake de ejemplo. Ejecutar esta clase para probar el MVC.
 */
public final class SnakeMain {

    public static void main(String[] args) {
        GameView view = GameViews.getInstance().getView();
        GameViewMessages.getInstance().bind(view);
        GameModel model = new SnakeGameModel(view);

        DefaultGameController controller = new DefaultGameController();
        controller.getKeyCommands()
                .bind(KeyEvent.VK_UP, GameCommands.MOVE_UP)
                .bind(KeyEvent.VK_DOWN, GameCommands.MOVE_DOWN)
                .bind(KeyEvent.VK_LEFT, GameCommands.MOVE_LEFT)
                .bind(KeyEvent.VK_RIGHT, GameCommands.MOVE_RIGHT)
                .bind(KeyEvent.VK_W, GameCommands.MOVE_UP)
                .bind(KeyEvent.VK_S, GameCommands.MOVE_DOWN)
                .bind(KeyEvent.VK_A, GameCommands.MOVE_LEFT)
                .bind(KeyEvent.VK_D, GameCommands.MOVE_RIGHT)
                .bind(KeyEvent.VK_P, GameCommands.PAUSE)
                .bind(KeyEvent.VK_SPACE, GameCommands.PAUSE)
                .bind(KeyEvent.VK_R, GameCommands.RESTART);

        controller.bind(model, view);
        controller.start();
    }

    private SnakeMain() {
    }
}
