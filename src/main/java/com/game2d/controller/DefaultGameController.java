package com.game2d.controller;

import com.game2d.model.GameModel;
import com.game2d.model.SessionState;
import com.game2d.model.SimpleGameInput;
import com.game2d.view.GameView;
import com.game2d.view.ViewListener;

import javax.swing.Timer;

/**
 * Controller por defecto: timer de actualización, reenvío de entradas al modelo y repintado.
 * <p>
 * Comandos y teclas: use {@link #getKeyCommands()} para asociar teclas a ids de acción
 * (los mismos que devuelve el {@link com.game2d.model.Menu} en cada {@link com.game2d.model.Action}).
 * Si una tecla no está registrada, el modelo recibe {@link com.game2d.model.InputKind#KEY_PRESSED}
 * con el nombre de la tecla.
 */
public final class DefaultGameController implements GameController, ViewListener {

    private static final int TICK_MS = 16;

    private final KeyCommandRegistry keyCommands = new KeyCommandRegistry();

    private GameModel model;
    private GameView view;
    private Timer timer;

    public KeyCommandRegistry getKeyCommands() {
        return keyCommands;
    }

    @Override
    public void bind(GameModel model, GameView view) {
        this.model = model;
        this.view = view;
        view.setViewListener(this);
        com.game2d.view.GameViewMessages.getInstance().bind(view);
    }

    @Override
    public void start() {
        if (model == null || view == null) {
            throw new IllegalStateException("Debe llamar a bind(model, view) antes de start()");
        }
        view.show();
        renderFrame();

        timer = new Timer(TICK_MS, e -> tick());
        timer.start();
    }

    @Override
    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    void tickForTest() {
        tick();
    }

    private void tick() {
        SessionState state = model.capture().getState();
        if (state == SessionState.RUNNING) {
            model.update(TICK_MS / 1000f);
        }
        renderFrame();
    }

    @Override
    public void onPointerDown(float worldX, float worldY) {
        runModelAction(() -> model.dispatch(SimpleGameInput.pointerDown(worldX, worldY)));
    }

    @Override
    public void onPointerUp(float worldX, float worldY) {
        runModelAction(() -> model.dispatch(SimpleGameInput.pointerUp(worldX, worldY)));
    }

    @Override
    public void onKeyPressed(int keyCode, String keyLabel) {
        runModelAction(() -> keyCommands.resolve(keyCode, keyLabel).ifPresentOrElse(
                actionId -> model.dispatch(SimpleGameInput.action(actionId)),
                () -> model.dispatch(SimpleGameInput.keyPressed(keyLabel))
        ));
    }

    @Override
    public void onActionInvoked(String actionId) {
        runModelAction(() -> model.dispatch(SimpleGameInput.action(actionId)));
    }

    private void runModelAction(Runnable action) {
        try {
            action.run();
            renderFrame();
        } catch (RuntimeException ex) {
            view.showError(ex);
        }
    }

    private void renderFrame() {
        try {
            var frame = model.capture();
            frame.getErrorMessage().ifPresent(view::errorMessage);
            view.render(frame);
        } catch (RuntimeException ex) {
            view.showError(ex);
        }
    }
}
