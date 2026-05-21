package com.game2d.model;

/**
 * Puntaje, oro y vidas que la vista muestra en la barra superior.
 * El alumno los actualiza en cada {@link FrameSnapshot#capture()}.
 * <p>
 * Para un valor que no use su juego (ej. Snake sin oro), puede devolver {@code -1}
 * y la vista mostrará {@code —}.
 */
public interface GameStatus {

    int getScore();

    int getGold();

    int getLives();
}
