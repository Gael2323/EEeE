package com.game2d.model;

import java.util.List;
import java.util.Optional;

/**
 * Fotografía del estado actual para dibujar un frame. Solo lectura desde la vista.
 */
public interface FrameSnapshot {

    SessionState getState();

    Float getWorldWidth();

    Float getWorldHeight();

    List<? extends Drawable> getDrawables();

    Menu getMenu();

    GameStatus getStatus();

    /**
     * Error a mostrar este frame (texto rojo en la vista). Vacío si no hay error.
     */
    default Optional<String> getErrorMessage() {
        return Optional.empty();
    }
}
