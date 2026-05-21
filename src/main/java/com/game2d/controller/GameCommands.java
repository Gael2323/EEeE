package com.game2d.controller;

/**
 * Ids de acción sugeridos para usar en el menú, en {@link KeyCommandRegistry} y en {@code dispatch}.
 * Los alumnos pueden definir otros ids libremente; estas constantes evitan errores de tipeo.
 */
public final class GameCommands {

    public static final String START = "start";
    public static final String PAUSE = "pause";
    public static final String RESUME = "resume";
    public static final String RESTART = "restart";
    public static final String QUIT = "quit";

    public static final String MOVE_UP = "move_up";
    public static final String MOVE_DOWN = "move_down";
    public static final String MOVE_LEFT = "move_left";
    public static final String MOVE_RIGHT = "move_right";

    private GameCommands() {
    }
}
