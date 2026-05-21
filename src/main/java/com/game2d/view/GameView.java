package com.game2d.view;

import com.game2d.model.FrameSnapshot;

/**
 * Contrato de la interfaz gráfica. La implementación concreta es {@link SwingGameView}.
 */
public interface GameView {

    void render(FrameSnapshot frame);

    void setViewListener(ViewListener listener);

    void setViewportSize(int widthPx, int heightPx);

    void show();

    /**
     * Muestra un mensaje de éxito (verde, centrado) que se desvanece y desaparece a los 3 segundos.
     */
    void successMessage(String message);

    /**
     * Muestra un mensaje de error (rojo, costado derecho) que se desvanece y desaparece a los 3 segundos.
     */
    void errorMessage(String message);

    /**
     * Muestra en pantalla un error a partir de una excepción (usa {@link ExceptionMessages#format(Throwable)}).
     */
    default void showError(Throwable error) {
        errorMessage(ExceptionMessages.format(error));
    }
}
