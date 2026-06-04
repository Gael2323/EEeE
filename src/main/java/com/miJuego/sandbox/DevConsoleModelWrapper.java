package com.miJuego.sandbox;

import com.game2d.model.*;
import com.miJuego.model.TowerDefenseModel;

/**
 * Wrapper de {@link TowerDefenseModel} que intercepta la acción
 * {@code "DEV_CONSOLE_TOGGLE"} para mostrar/ocultar la {@link DevConsoleFrame}.
 *
 * <p>Todas las demás acciones se delegan al modelo original sin modificación.</p>
 */
public final class DevConsoleModelWrapper implements GameModel {

    private static final String ACTION_DEV_CONSOLE = "DEV_CONSOLE_TOGGLE";

    private final TowerDefenseModel delegate;
    private final DevConsoleFrame console;

    public DevConsoleModelWrapper(TowerDefenseModel delegate, DevConsoleFrame console) {
        this.delegate = delegate;
        this.console  = console;
    }

    @Override
    public FrameSnapshot capture() {
        return delegate.capture();
    }

    @Override
    public void update(float deltaSeconds) {
        delegate.update(deltaSeconds);
    }

    @Override
    public void dispatch(GameInput input) {
        // Capturamos el toggle de la consola antes de que llegue al modelo real
        if (input.getKind() == InputKind.ACTION
                && ACTION_DEV_CONSOLE.equals(input.getActionId().orElse(""))) {
            console.toggle();
            return;
        }
        delegate.dispatch(input);
    }
}
