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

    /**
     * Muestra la ventana emergente de premio de adware falso para el nivel 1.
     */
    default void showPrizePopup(Runnable onClose) {
        onClose.run();
    }

    /**
     * Muestra la segunda ventana emergente oficial que confirma la instalación involuntaria.
     */
    default void showPrizeResolutionPopup(Runnable onClose) {
        onClose.run();
    }

    /**
     * Inicia la transición post-nivel hacia el Hub/Escritorio XP.
     *
     * <p>Hace un fade-out sobre la vista actual usando el GlassPane del JFrame,
     * luego muestra el escritorio XP con la animación correspondiente al nivel completado.</p>
     *
     * @param completedLevel el número de nivel que acaba de terminar
     */
    default void showPostLevelHub(int completedLevel) {
        // Default no-op; SwingGameView lo sobrescribe con el fade+hub real
    }
}

