package com.game2d.model;

/**
 * Estado general de la sesión. El alumno decide cuándo cambiar cada valor
 * dentro de su implementación de {@link GameModel}.
 */
public enum SessionState {
    READY,
    RUNNING,
    PAUSED,
    FINISHED
}
