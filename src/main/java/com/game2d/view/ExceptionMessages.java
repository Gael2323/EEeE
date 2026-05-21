package com.game2d.view;

/**
 * Texto legible para mostrar errores en pantalla.
 */
public final class ExceptionMessages {

    private ExceptionMessages() {
    }

    public static String format(Throwable error) {
        if (error == null) {
            return "Error desconocido";
        }
        String message = error.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }
        return error.getClass().getSimpleName();
    }
}
