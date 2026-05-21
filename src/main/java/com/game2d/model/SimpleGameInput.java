package com.game2d.model;

import java.util.Optional;

/**
 * Implementación concreta de {@link GameInput} usada por el controller.
 * Los alumnos no necesitan usar esta clase salvo que quieran probar su modelo aislado.
 */
public final class SimpleGameInput implements GameInput {

    private final InputKind kind;
    private final Float x;
    private final Float y;
    private final String keyCode;
    private final String actionId;

    private SimpleGameInput(InputKind kind, Float x, Float y, String keyCode, String actionId) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.keyCode = keyCode;
        this.actionId = actionId;
    }

    public static SimpleGameInput pointerDown(float x, float y) {
        return new SimpleGameInput(InputKind.POINTER_DOWN, x, y, null, null);
    }

    public static SimpleGameInput pointerUp(float x, float y) {
        return new SimpleGameInput(InputKind.POINTER_UP, x, y, null, null);
    }

    public static SimpleGameInput keyPressed(String keyCode) {
        return new SimpleGameInput(InputKind.KEY_PRESSED, null, null, keyCode, null);
    }

    public static SimpleGameInput action(String actionId) {
        return new SimpleGameInput(InputKind.ACTION, null, null, null, actionId);
    }

    @Override
    public InputKind getKind() {
        return kind;
    }

    @Override
    public Optional<Float> getX() {
        return Optional.ofNullable(x);
    }

    @Override
    public Optional<Float> getY() {
        return Optional.ofNullable(y);
    }

    @Override
    public Optional<String> getKeyCode() {
        return Optional.ofNullable(keyCode);
    }

    @Override
    public Optional<String> getActionId() {
        return Optional.ofNullable(actionId);
    }
}
