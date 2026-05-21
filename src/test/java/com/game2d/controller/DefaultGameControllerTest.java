package com.game2d.controller;

import com.game2d.model.Drawable;
import com.game2d.model.FrameSnapshot;
import com.game2d.model.GameInput;
import com.game2d.model.GameModel;
import com.game2d.model.InputKind;
import com.game2d.model.Menu;
import com.game2d.model.SessionState;
import com.game2d.view.GameView;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultGameControllerTest {

    @Test
    void dispatchOnActionInvoked() {
        RecordingModel model = new RecordingModel();
        RecordingView view = new RecordingView();
        DefaultGameController controller = new DefaultGameController();
        controller.bind(model, view);

        controller.onActionInvoked("start");

        assertEquals("start", model.lastActionId.get());
        assertEquals(1, view.renderCount.get());
    }

    @Test
    void keyBindingDispatchesAction() {
        RecordingModel model = new RecordingModel();
        DefaultGameController controller = new DefaultGameController();
        controller.bind(model, new RecordingView());
        controller.getKeyCommands().bind(KeyEvent.VK_UP, GameCommands.MOVE_UP);

        controller.onKeyPressed(KeyEvent.VK_UP, "Up");

        assertEquals(InputKind.ACTION, model.lastInput.get().getKind());
        assertEquals(GameCommands.MOVE_UP, model.lastInput.get().getActionId().orElseThrow());
    }

    @Test
    void unmappedKeyDispatchesKeyPressed() {
        RecordingModel model = new RecordingModel();
        DefaultGameController controller = new DefaultGameController();
        controller.bind(model, new RecordingView());

        controller.onKeyPressed(KeyEvent.VK_Z, "Z");

        assertEquals(InputKind.KEY_PRESSED, model.lastInput.get().getKind());
        assertEquals("Z", model.lastInput.get().getKeyCode().orElseThrow());
    }

    @Test
    void updateOnlyWhenRunning() {
        RecordingModel model = new RecordingModel();
        model.state = SessionState.READY;
        DefaultGameController controller = new DefaultGameController();
        controller.bind(model, new RecordingView());

        controller.tickForTest();

        assertEquals(0, model.updateCount.get());

        model.state = SessionState.RUNNING;
        controller.tickForTest();

        assertEquals(1, model.updateCount.get());
    }

    private static final class RecordingModel implements GameModel {

        private final AtomicReference<String> lastActionId = new AtomicReference<>();
        private final AtomicReference<GameInput> lastInput = new AtomicReference<>();
        private final AtomicInteger updateCount = new AtomicInteger();
        private SessionState state = SessionState.READY;

        @Override
        public FrameSnapshot capture() {
            return new FrameSnapshot() {
                @Override
                public SessionState getState() {
                    return state;
                }

                @Override
                public Float getWorldWidth() {
                    return 10f;
                }

                @Override
                public Float getWorldHeight() {
                    return 10f;
                }

                @Override
                public List<? extends Drawable> getDrawables() {
                    return List.of();
                }

                @Override
                public Menu getMenu() {
                    return () -> List.of();
                }

                @Override
                public com.game2d.model.GameStatus getStatus() {
                    return new com.game2d.model.GameStatus() {
                        @Override
                        public int getScore() {
                            return 0;
                        }

                        @Override
                        public int getGold() {
                            return 0;
                        }

                        @Override
                        public int getLives() {
                            return 0;
                        }
                    };
                }
            };
        }

        @Override
        public void update(float deltaSeconds) {
            updateCount.incrementAndGet();
        }

        @Override
        public void dispatch(GameInput input) {
            lastInput.set(input);
            input.getActionId().ifPresent(lastActionId::set);
        }
    }

    private static final class RecordingView implements GameView {

        private final AtomicInteger renderCount = new AtomicInteger();

        @Override
        public void render(com.game2d.model.FrameSnapshot frame) {
            renderCount.incrementAndGet();
        }

        @Override
        public void setViewListener(com.game2d.view.ViewListener listener) {
        }

        @Override
        public void setViewportSize(int widthPx, int heightPx) {
        }

        @Override
        public void successMessage(String message) {
        }

        @Override
        public void errorMessage(String message) {
        }

        @Override
        public void show() {
        }
    }
}
