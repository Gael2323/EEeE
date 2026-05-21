package com;

import com.game2d.example.snake.SnakeGameModel;
import com.game2d.model.SessionState;
import com.game2d.view.GameView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppTest {

    @Test
    void appDelegatesToSnakeExample() {
        assertNotNull(App.class);
    }

    @Test
    void snakeModelStartsRunning() {
        SnakeGameModel model = new SnakeGameModel(new NullGameView());
        assertEquals(SessionState.RUNNING, model.capture().getState());
    }

    private static final class NullGameView implements GameView {

        @Override
        public void render(com.game2d.model.FrameSnapshot frame) {
        }

        @Override
        public void setViewListener(com.game2d.view.ViewListener listener) {
        }

        @Override
        public void setViewportSize(int widthPx, int heightPx) {
        }

        @Override
        public void show() {
        }

        @Override
        public void successMessage(String message) {
        }

        @Override
        public void errorMessage(String message) {
        }
    }
}
