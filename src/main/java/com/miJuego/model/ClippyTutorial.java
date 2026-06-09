package com.miJuego.model;

/**
 * Gestiona los estados y la posición dinámica (ajustada al viewport de la cámara)
 * del tutorial interactivo in-game con Clippy en el Nivel 1.
 */
public class ClippyTutorial {
    public enum Estado {
        INTRO_1,           // Alerta sobre buddy.exe
        INTRO_2,           // Explicación de cómo equipar torre
        WAIT_PLACE,        // Esperando colocación de la primera torre
        PLACE_SUCCESS,     // Éxito y feedback del cursor
        WAIT_START_WAVE,   // Explicación de iniciar la oleada
        OUTRO,             // Diálogo de cierre
        COMPLETED          // Finalizado, Clippy desaparece
    }

    private Estado estadoActual = Estado.INTRO_1;
    private boolean active = true;

    // Coordenadas lógicas en el mundo de juego (inicializadas cerca de la esquina inferior derecha inicial)
    private float actualX = 14.2f;
    private float actualY = 9.8f;

    // Control de retraso de movimiento de Clippy respecto al desplazamiento de cámara
    private float lastCamX = 0f;
    private float lastCamY = 0f;
    private float moveDelayTimer = 0f;
    private static final float DELAY_TIME = 0.5f; // 0.5 segundos de retraso

    private float danceTimer = 0f;

    public ClippyTutorial() {
        this.lastCamX = CameraContext.getCameraX();
        this.lastCamY = CameraContext.getCameraY();
    }

    public Estado getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(Estado estado) {
        this.estadoActual = estado;
        // Clippy sigue activo para poder bailar en la esquina asignada
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            this.estadoActual = Estado.COMPLETED;
        }
    }

    public float getActualX() {
        if (estadoActual == Estado.COMPLETED) {
            // Wobble horizontal
            return actualX + (float) Math.sin(danceTimer * 5.0f) * 0.15f;
        }
        return actualX;
    }

    public float getActualY() {
        if (estadoActual == Estado.COMPLETED) {
            // Wobble vertical (salto)
            return actualY + (float) (Math.abs(Math.sin(danceTimer * 10.0f)) * -0.15f);
        }
        return actualY;
    }

    /**
     * Actualiza la posición de Clippy haciéndolo volar de manera suave
     * para que permanezca siempre en la esquina inferior derecha del viewport de la cámara
     * o en su área de baile si el tutorial ya terminó.
     */
    public void update(float deltaSeconds) {
        if (!active) return;

        if (estadoActual == Estado.COMPLETED) {
            // Posición de baile fija ajustada arriba a la izquierda para centrar en la nota adhesiva
            float targetX = 4.4f;
            float targetY = 13.6f;

            // Lerp de desplazamiento suave
            float speed = 3.0f;
            actualX += (targetX - actualX) * speed * deltaSeconds;
            actualY += (targetY - actualY) * speed * deltaSeconds;

            danceTimer += deltaSeconds;
            return;
        }

        // Obtener el viewport actual de la cámara
        float camX = CameraContext.getCameraX();
        float camY = CameraContext.getCameraY();
        float vpW = CameraContext.VIEWPORT_W;
        float vpH = CameraContext.VIEWPORT_H;

        // Si el jugador desplaza la cámara, reiniciamos el temporizador de retraso
        if (camX != lastCamX || camY != lastCamY) {
            lastCamX = camX;
            lastCamY = camY;
            moveDelayTimer = DELAY_TIME;
        }

        // Posición objetivo en la esquina inferior derecha del viewport, limitada a un tamaño estándar
        // de viewport de 16x12 para que no vuele fuera de la hoja cuando el jugador se aleja con el zoom.
        float targetX = camX + Math.min(vpW, 16.0f) - 1.8f;
        float targetY = camY + Math.min(vpH, 12.0f) - 2.2f;

        // Si el retraso está activo, decrementamos el temporizador; de lo contrario, volamos con lerp
        if (moveDelayTimer > 0f) {
            moveDelayTimer -= deltaSeconds;
        } else {
            // Interpolación lineal suave (lerp) para que vuele de manera fluida y elegante
            float speed = 4.5f;
            actualX += (targetX - actualX) * speed * deltaSeconds;
            actualY += (targetY - actualY) * speed * deltaSeconds;
        }
    }

    /**
     * Retorna las líneas de texto del globo clásico correspondientes al estado actual.
     */
    public String[] getLines() {
        return switch (estadoActual) {
            case INTRO_1 -> new String[]{
                "¡Hola! El virus buddy.exe está",
                "enviando ventanas emergentes y",
                "letras 'A' a dañar el documento.",
                "¡Tenemos que defendernos!"
            };
            case INTRO_2 -> new String[]{
                "Para hacerlo, primero debes",
                "comprar y colocar un Antivirus.",
                "Haz clic en el panel izquierdo",
                "o presiona la tecla 1 para equiparlo."
            };
            case WAIT_PLACE -> new String[]{
                "Haz DOBLE CLIC en la hoja blanca",
                "(fuera del camino rojo) para",
                "colocar la Torre Antivirus."
            };
            case PLACE_SUCCESS -> new String[]{
                "¡Excelente! Las torres atacan",
                "solas. Si el cursor se pone verde",
                "la celda es válida. Si se pone rojo,",
                "el camino o los límites lo impiden."
            };
            case WAIT_START_WAVE -> new String[]{
                "¡Todo listo! Haz clic en el botón",
                "'Iniciar Oleada' en el lateral",
                "o presiona la tecla Enter para",
                "comenzar la defensa del archivo."
            };
            case OUTRO -> new String[]{
                "Ese fue un buen trabajo,",
                "voy a quedarme por aquí",
                "por si necesitas una mano."
            };
            default -> new String[0];
        };
    }

    /**
     * Retorna el nombre de la expresión de Clippy según el estado actual.
     */
    public String getExpression() {
        return switch (estadoActual) {
            case INTRO_1 -> "preocupado";
            case INTRO_2 -> "leyendo";
            case WAIT_PLACE -> "solicitando";
            case PLACE_SUCCESS -> "feliz";
            case WAIT_START_WAVE -> "aprobando";
            case OUTRO -> "feliz";
            default -> "neutro";
        };
    }
}
