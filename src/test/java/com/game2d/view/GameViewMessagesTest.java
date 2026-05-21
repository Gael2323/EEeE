package com.game2d.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameViewMessagesTest {

    private final AtomicReference<String> lastError = new AtomicReference<>();
    private final AtomicReference<String> lastSuccess = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        GameViewMessages messages = GameViewMessages.getInstance();
        messages.bind(new GameView() {
            @Override
            public void render(com.game2d.model.FrameSnapshot frame) {
            }

            @Override
            public void setViewListener(ViewListener listener) {
            }

            @Override
            public void setViewportSize(int widthPx, int heightPx) {
            }

            @Override
            public void show() {
            }

            @Override
            public void successMessage(String message) {
                lastSuccess.set(message);
            }

            @Override
            public void errorMessage(String message) {
                lastError.set(message);
            }
        });
    }

    @Test
    void errorThrowableShowsFormattedMessage() {
        GameViewMessages.getInstance().error(new IllegalArgumentException("dato inválido"));
        assertEquals("dato inválido", lastError.get());
    }

    @Test
    void successDelegatesToView() {
        GameViewMessages.getInstance().success("ok");
        assertEquals("ok", lastSuccess.get());
    }
}
