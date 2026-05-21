package com.game2d.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Asocia teclas del teclado con ids de acción del menú/modelo.
 * El controller traduce una tecla pulsada en {@link com.game2d.model.SimpleGameInput#action(String)}
 * cuando existe un vínculo registrado aquí.
 */
public final class KeyCommandRegistry {

    private final Map<Integer, String> byKeyCode = new LinkedHashMap<>();
    private final Map<String, String> byKeyLabel = new LinkedHashMap<>();

    /**
     * Vincula una tecla por su código Swing ({@link java.awt.event.KeyEvent#VK_UP}, etc.).
     */
    public KeyCommandRegistry bind(int keyCode, String actionId) {
        byKeyCode.put(keyCode, actionId);
        return this;
    }

    /**
     * Vincula una tecla por su nombre ({@code "Up"}, {@code "W"}, {@code "Space"}, …).
     * Debe coincidir con el texto que envía la vista al pulsar la tecla.
     */
    public KeyCommandRegistry bind(String keyLabel, String actionId) {
        byKeyLabel.put(keyLabel, actionId);
        return this;
    }

    public KeyCommandRegistry unbind(int keyCode) {
        byKeyCode.remove(keyCode);
        return this;
    }

    public KeyCommandRegistry unbind(String keyLabel) {
        byKeyLabel.remove(keyLabel);
        return this;
    }

    public void clear() {
        byKeyCode.clear();
        byKeyLabel.clear();
    }

    /**
     * @return id de acción asociado a la tecla, si hay vínculo configurado
     */
    public Optional<String> resolve(int keyCode, String keyLabel) {
        return Optional.ofNullable(byKeyCode.get(keyCode))
                .or(() -> Optional.ofNullable(byKeyLabel.get(keyLabel)));
    }
}
