package com.game2d.model;

/**
 * Elemento visible en el mundo del juego (personaje, bloque, torre, comida, etc.).
 */
public interface Drawable extends Renderable {

    String getId();

    Float getX();

    Float getY();

    Float getWidth();

    Float getHeight();

    default Float getRotationRadians() {
        return 0f;
    }

    /** Menor valor = se dibuja más atrás. */
    default int getLayer() {
        return 0;
    }
}
