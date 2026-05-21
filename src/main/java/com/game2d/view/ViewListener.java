package com.game2d.view;

/**
 * Eventos de la interfaz que el controller escucha.
 */
public interface ViewListener {

    void onPointerDown(float worldX, float worldY);

    void onPointerUp(float worldX, float worldY);

    /**
     * @param keyCode código de tecla Swing ({@link java.awt.event.KeyEvent#VK_UP}, etc.)
     * @param keyLabel nombre legible de la tecla ({@code "Up"}, {@code "W"}, …)
     */
    void onKeyPressed(int keyCode, String keyLabel);

    void onActionInvoked(String actionId);
}
