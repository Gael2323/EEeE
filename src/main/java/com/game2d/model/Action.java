package com.game2d.model;

/**
 * Botón del menú (barra superior o ventana modal).
 * El alumno define qué acciones existen y cuáles están habilitadas en cada momento.
 */
public interface Action extends Renderable {

    String getId();

    String getLabel();

    boolean isEnabled();
}
