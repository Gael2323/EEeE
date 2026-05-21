package com.game2d.model;

import java.util.Optional;

/**
 * Evento de entrada que el controller reenvía al modelo.
 */
public interface GameInput {

    InputKind getKind();

    default Optional<Float> getX() {
        return Optional.empty();
    }

    default Optional<Float> getY() {
        return Optional.empty();
    }

    default Optional<String> getKeyCode() {
        return Optional.empty();
    }

    default Optional<String> getActionId() {
        return Optional.empty();
    }
}
