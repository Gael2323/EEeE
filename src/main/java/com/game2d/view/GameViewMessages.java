package com.game2d.view;

/**
 * Singleton para enviar mensajes a la vista sin pasar {@link GameView} por todos lados.
 * Llamar a {@link #bind(GameView)} una vez al iniciar el juego (el controller también lo hace).
 */
public final class GameViewMessages {

    private static final GameViewMessages INSTANCE = new GameViewMessages();

    private GameView view;

    private GameViewMessages() {
    }

    public static GameViewMessages getInstance() {
        return INSTANCE;
    }

    public void bind(GameView view) {
        this.view = view;
    }

    public void success(String message) {
        if (view != null) {
            view.successMessage(message);
        }
    }

    public void error(String message) {
        if (view != null) {
            view.errorMessage(message);
        }
    }

    public void error(Throwable error) {
        if (view != null) {
            view.showError(error);
        }
    }
}
