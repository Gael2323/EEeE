package com.game2d.model;

/**
 * Contrato del juego. Lo implementan los alumnos con la lógica de su juego.
 */
public interface GameModel {

    FrameSnapshot capture();

    void update(float deltaSeconds);

    void dispatch(GameInput input);
}
