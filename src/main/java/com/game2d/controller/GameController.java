package com.game2d.controller;

import com.game2d.model.GameModel;
import com.game2d.view.GameView;

/**
 * Une el modelo con la vista y maneja el ciclo de juego.
 */
public interface GameController {

    void bind(GameModel model, GameView view);

    void start();

    void stop();
}
