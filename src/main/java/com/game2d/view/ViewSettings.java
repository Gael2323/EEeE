package com.game2d.view;

/**
 * Ajustes internos de la vista. El menú ({@link com.game2d.model.Menu}) sigue en el modelo
 * para que los alumnos lo implementen; la UI del menú está desactivada porque interrumpe el flujo del juego.
 */
public final class ViewSettings {

    /** Si es {@code true}, se muestran botones en la barra y ventana modal del menú. */
    public static final boolean MENU_UI_ENABLED = false;

    private ViewSettings() {
    }
}
