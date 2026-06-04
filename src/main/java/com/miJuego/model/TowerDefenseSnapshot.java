package com.miJuego.model;

import com.game2d.model.Drawable;
import com.game2d.model.FrameSnapshot;
import com.game2d.model.GameStatus;
import com.game2d.model.Menu;
import com.game2d.model.SessionState;

import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Captura enriquecida del estado del juego en un frame determinado.
 * Pasa información detallada (como la referencia al Juego y las coordenadas de click)
 * de forma desacoplada desde el modelo hacia las vistas del HUD.
 */
public class TowerDefenseSnapshot implements FrameSnapshot {

    private final SessionState state;
    private final List<? extends Drawable> drawables;
    private final Menu menu;
    private final GameStatus status;

    // Información rica de estado
    private final Juego juego;
    private final int lastClickedX;
    private final int lastClickedY;

    public TowerDefenseSnapshot(SessionState state, List<? extends Drawable> drawables, Menu menu,
                                GameStatus status, Juego juego, int lastClickedX, int lastClickedY) {
        this.state = state;
        this.drawables = drawables;
        this.menu = menu;
        this.status = status;
        this.juego = juego;
        this.lastClickedX = lastClickedX;
        this.lastClickedY = lastClickedY;
    }

    @Override
    public SessionState getState() {
        return state;
    }

    @Override
    public Float getWorldWidth() {
        return 20f;
    }

    @Override
    public Float getWorldHeight() {
        return 15f;
    }

    @Override
    public List<? extends Drawable> getDrawables() {
        return drawables;
    }

    @Override
    public Menu getMenu() {
        return menu;
    }

    @Override
    public GameStatus getStatus() {
        return status;
    }

    // Getters de datos ricos
    public Juego getJuego() {
        return juego;
    }

    public int getLastClickedX() {
        return lastClickedX;
    }

    public int getLastClickedY() {
        return lastClickedY;
    }
}
