package com.game2d.example.snake;

import com.game2d.controller.GameCommands;
import com.game2d.model.SessionState;
import com.game2d.model.SimpleGameInput;
import com.game2d.view.GameView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnakeGameModelTest {

    private SnakeGameModel model;

    @BeforeEach
    void setUp() {
        model = new SnakeGameModel(new NoOpView());
    }

    @Test
    void startsRunning() {
        assertEquals(SessionState.RUNNING, model.capture().getState());
    }

    @Test
    void pauseStillProvidesMenuForApi() {
        model.dispatch(SimpleGameInput.action(GameCommands.PAUSE));
        assertEquals(SessionState.PAUSED, model.capture().getState());
        assertEquals(2, model.capture().getMenu().getActions().size());
    }

    @Test
    void restartWhilePausedResetsScore() {
        model.dispatch(SimpleGameInput.action(GameCommands.PAUSE));
        model.dispatch(SimpleGameInput.action(GameCommands.RESTART));
        assertEquals(SessionState.PAUSED, model.capture().getState());
        assertEquals(0, model.capture().getStatus().getScore());
        assertEquals("Pausado", model.capture().getMenu().getTitle());
    }

    @Test
    void statusShowsScoreAndUnusedStatsAsDash() {
        model.dispatch(SimpleGameInput.action(GameCommands.PAUSE));
        assertEquals(0, model.capture().getStatus().getScore());
        assertEquals(-1, model.capture().getStatus().getGold());
        assertEquals(-1, model.capture().getStatus().getLives());
    }

    @Test
    void wrapDoesNotEndGame() {
        model.dispatch(SimpleGameInput.action(GameCommands.START));
        for (int i = 0; i < 30; i++) {
            model.dispatch(SimpleGameInput.action(GameCommands.MOVE_LEFT));
            model.update(1f);
        }
        assertEquals(SessionState.RUNNING, model.capture().getState());
    }

    private static final class NoOpView implements GameView {

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
