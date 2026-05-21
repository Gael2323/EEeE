package com.game2d.model;

import java.util.List;

/**
 * Menú de opciones que la vista muestra en la barra superior y, cuando corresponde, como ventana modal.
 */
public interface Menu {

    List<? extends Action> getActions();

    default String getTitle() {
        return "";
    }
}
