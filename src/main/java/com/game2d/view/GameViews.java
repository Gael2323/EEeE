package com.game2d.view;

/**
 * Singleton de la ventana principal del juego.
 */
public final class GameViews {

    private static final GameViews INSTANCE = new GameViews();

    private GameView view;

    private GameViews() {
    }

    public static GameViews getInstance() {
        return INSTANCE;
    }

    public GameView getView() {
        if (view == null) {
            view = new SwingGameView(
                    ImageResolvers.getInstance().getResolver(),
                    BackgroundSettings.getInstance());
        }
        return view;
    }
}
