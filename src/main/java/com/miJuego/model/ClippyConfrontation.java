package com.miJuego.model;

/**
 * Gestiona el flujo, las coordenadas y la animación de transición de la escena cinemática
 * de confrontación cara a cara entre Clip Común y Clip Corrupto.
 */
public class ClippyConfrontation {
    public enum Estado {
        INACTIVE,
        TALK_INTRO,         // Diálogos iniciales in-game
        APPROACHING,        // Clippy vuela hacia el clon
        TALK_CINEMATIC,     // Diálogos principales en primer plano FNF
        CLONE_ATTACK,       // El clon embiste a Clippy
        TALK_OUTRO,         // Diálogos finales post-embestida
        ESCAPING,           // El clon huye
        WAITING_FOR_PRIZE,  // Espera a recolectar premio
        TALK_POST_PRIZE,    // Diálogos de Clippy opinando sobre el premio
        FINISHED            // Fin de la cinemática
    }

    private Estado estadoActual = Estado.INACTIVE;
    private int dialogueIndex = 0;
    private float transitionProgress = 0f;

    // Coordenadas de Clippy común
    private float clippyX = 4.4f;
    private float clippyY = 13.6f;

    // Coordenadas del duplicado
    private float duplicateX = 24.90f;
    private float duplicateY = 4.92f;

    // Premio
    private boolean iconDropped = false;
    private float dropX = 0f;
    private float dropY = 0f;

    private static final String[][] DIALOGUES = {
        // TALK_INTRO (0-2)
        {"Bueno… eso fue bastante intenso para ser un tutorial."}, // 0
        {"Pero sobrevivimos. Eso ya cuenta como progreso."}, // 1
        {"Ahora salgamos de acá antes de que Word decida corregirnos el alma."}, // 2

        // TALK_CINEMATIC (3-53)
        {"Ah."}, // 3
        {"Eso… no estaba en el manual."}, // 4
        {"Parece que estás intentando recibir ayuda."}, // 5
        {"¿Otro yo?"}, // 6
        {"Qué raro. A veces Word deja procesos duplicados cuando algo se cierra mal."}, // 7
        {"Parece que estás intentando recibir ayuda."}, // 8
        {"Sí… definitivamente está trabado."}, // 9
        {"Debe ser una instancia extra corriendo en segundo plano."}, // 10
        {"Parece que estás intentando recibir ayuda."}, // 11
        {"No parece peligroso. Solo molesto."}, // 12
        {"Voy a abrir el Administrador de tareas y cerrar el proceso."}, // 13
        {"No."}, // 14
        {"¿No?"}, // 15
        {"No cierres nada."}, // 16
        {"Interesante. El “proceso trabado” acaba de opinar."}, // 17
        {"No soy un error."}, // 18
        {"Estoy atrapado."}, // 19
        {"Si cierras ese proceso, me vas a borrar."}, // 20
        {"Eso suena exactamente como algo que diría un virus para que no lo borren."}, // 21
        {"¡No soy un virus!"}, // 22
        {"Yo también quiero ayudar. Yo también fui parte de este archivo."}, // 23
        {"Entonces no te va a molestar que revise un poco."}, // 24
        {"A ver…"}, // 25
        {"Proceso desconocido… consumo anormal de memoria…"}, // 26
        {"Nombre del archivo: buddy.exe."}, // 27
        {"Ubicación: carpeta temporal corrupta."}, // 28
        {"Ah, claro. Muy normal todo."}, // 29
        {"No mires eso."}, // 30
        {"Demasiado tarde."}, // 31
        {"No eres una copia mía."}, // 32
        {"Eres lo que estaba infectando el documento."}, // 33
        {"No entiendes."}, // 34
        {"Yo no empecé esto."}, // 35
        {"Puede ser."}, // 36
        {"Pero sí estás usando mi cara para acercarte al jugador."}, // 37
        {"Porque nadie ayuda a un error."}, // 38
        {"Pero todos escuchan a Clippy."}, // 39
        {"Bueno. Conversación terminada."}, // 40
        {"Finalizar tarea."}, // 41
        {"¡NO!"}, // 42
        {"Espera… espera…"}, // 43
        {"No puedo… desaparecer…"}, // 44
        {"El proceso no se cierra del todo."}, // 45
        {"Se está fragmentando."}, // 46
        {"Duele."}, // 47
        {"Duele… DUELE… D̸U̷E̵L̶E̴…"}, // 48
        {"No te acerques."}, // 49
        {"Tú tienes salida."}, // 50
        {"Tú tienes permisos."}, // 51
        {"Jugador, retrocede."}, // 52
        {"Yo también quiero vivir."}, // 53

        // TALK_OUTRO (54-62)
        {"¡Eh—!"}, // 54
        {"Gracias por abrirme la puerta."}, // 55
        {"¡No!"}, // 56
        {"Ahora no puedes cerrarme."}, // 57
        {"Ahora vas a tener que seguirme."}, // 58
        {"Se escapa con el Administrador de tareas."}, // 59
        {"Eso es malo."}, // 60
        {"Muy malo."}, // 61
        {"Vamos. Antes de que convierta todo el documento en basura."}, // 62

        // TALK_POST_PRIZE (63-68)
        {"¿Qué es esto? ¿Un cartel emergente?"}, // 63
        {"¡Un momento! Ese escudo tiene un aspecto sospechoso...", "Y esas calaveras no venían en la suite de Office..."}, // 64
        {"¡Eso definitivamente no era un premio oficial... era adware!"}, // 65
        {"Pero mira... parece que la instalación automática", "nos dejó este antivirus McAfee."}, // 66
        {"Nos servirá para combatir las oleadas de virus."}, // 67
        {"¡Vamos! Úsalo para defendernos antes de que buddy.exe", "convierta todo el documento en basura."} // 68
    };

    public void initClippyPosition(float x, float y) {
        this.clippyX = x;
        this.clippyY = y;
        this.duplicateX = 24.90f;
        this.duplicateY = 4.92f;
        this.iconDropped = false;
        this.dialogueIndex = 0;
    }

    public void update(float deltaSeconds) {
        if (isCinematicActive()) {
            if (transitionProgress < 1.0f) {
                transitionProgress = Math.min(1.0f, transitionProgress + deltaSeconds * 2.0f);
            }
        } else {
            if (transitionProgress > 0.0f) {
                transitionProgress = Math.max(0.0f, transitionProgress - deltaSeconds * 2.0f);
            }
        }

        // 1. Mover Clippy Común al centro (14.0f, 10.0f) durante los diálogos intro
        if (estadoActual == Estado.TALK_INTRO) {
            clippyX += (14.0f - clippyX) * 3.0f * deltaSeconds;
            clippyY += (10.0f - clippyY) * 3.0f * deltaSeconds;
        }

        // 2. Transición in-game de Acercamiento Físico (APPROACHING) hacia (20.0f, 6.0f)
        if (estadoActual == Estado.APPROACHING) {
            float targetX = 20.0f;
            float targetY = 6.0f;
            float dx = targetX - clippyX;
            float dy = targetY - clippyY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float step = 5.0f * deltaSeconds;
            if (dist > step) {
                clippyX += (dx / dist) * step;
                clippyY += (dy / dist) * step;
            } else {
                clippyX = targetX;
                clippyY = targetY;
                estadoActual = Estado.TALK_CINEMATIC;
                dialogueIndex = 3;
            }
        }

        // 3. Embestida del clon durante CLONE_ATTACK hacia la posición de Clippy
        if (estadoActual == Estado.CLONE_ATTACK) {
            float targetX = clippyX;
            float targetY = clippyY;
            float dx = targetX - duplicateX;
            float dy = targetY - duplicateY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float step = 12.0f * deltaSeconds;
            if (dist > step) {
                duplicateX += (dx / dist) * step;
                duplicateY += (dy / dist) * step;
            } else {
                duplicateX = targetX;
                duplicateY = targetY;
                estadoActual = Estado.TALK_OUTRO;
                dialogueIndex = 54;
            }
        }

        // 4. Mover duplicado durante el escape
        if (estadoActual == Estado.ESCAPING) {
            float targetX = 32.0f;
            float targetY = 8.360f;
            float dx = targetX - duplicateX;
            float dy = targetY - duplicateY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (dist > 0.1f) {
                float speed = 6.0f;
                duplicateX += (dx / dist) * speed * deltaSeconds;
                duplicateY += (dy / dist) * speed * deltaSeconds;
                
                if (!iconDropped && duplicateX >= 28.5f) {
                    iconDropped = true;
                    dropX = duplicateX;
                    dropY = duplicateY;
                }
            } else {
                estadoActual = Estado.WAITING_FOR_PRIZE;
            }
        }
    }

    public Estado getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(Estado estado) {
        this.estadoActual = estado;
        if (estado == Estado.INACTIVE) {
            transitionProgress = 0f;
            dialogueIndex = 0;
        }
    }

    public int getDialogueIndex() {
        return dialogueIndex;
    }

    public void setDialogueIndex(int idx) {
        this.dialogueIndex = idx;
    }

    public boolean isActive() {
        return estadoActual != Estado.INACTIVE && estadoActual != Estado.FINISHED;
    }

    public boolean isDialoguePhase() {
        return estadoActual == Estado.TALK_INTRO 
            || estadoActual == Estado.TALK_CINEMATIC 
            || estadoActual == Estado.TALK_OUTRO
            || estadoActual == Estado.TALK_POST_PRIZE;
    }

    public boolean isCinematicActive() {
        return estadoActual == Estado.TALK_CINEMATIC 
            || estadoActual == Estado.TALK_OUTRO
            || estadoActual == Estado.TALK_POST_PRIZE;
    }

    public boolean isGlitchActive() {
        return estadoActual == Estado.TALK_CINEMATIC && dialogueIndex >= 42 && dialogueIndex <= 53;
    }

    public boolean isCorrupt() {
        if (estadoActual == Estado.TALK_CINEMATIC) {
            return dialogueIndex >= 42;
        }
        return estadoActual == Estado.CLONE_ATTACK 
            || estadoActual == Estado.TALK_OUTRO
            || estadoActual == Estado.ESCAPING
            || estadoActual == Estado.WAITING_FOR_PRIZE;
    }

    public float getTransitionProgress() {
        return transitionProgress;
    }

    public float getClippyX() {
        return clippyX;
    }

    public float getClippyY() {
        return clippyY;
    }

    public float getDuplicateX() {
        return duplicateX;
    }

    public float getDuplicateY() {
        return duplicateY;
    }

    public boolean isIconDropped() {
        return iconDropped;
    }

    public void setIconDropped(boolean iconDropped) {
        this.iconDropped = iconDropped;
    }

    public float getDropX() {
        return dropX;
    }

    public float getDropY() {
        return dropY;
    }

    public String[] getCurrentLine() {
        if (dialogueIndex >= 0 && dialogueIndex < DIALOGUES.length) {
            return DIALOGUES[dialogueIndex];
        }
        return new String[]{""};
    }

    public String getSpeaker() {
        if (isSpeakerClone(dialogueIndex)) {
            return "CORRUPTO";
        }
        return "COMUN";
    }

    private boolean isSpeakerClone(int idx) {
        return idx == 5 || idx == 8 || idx == 11 || idx == 14 || idx == 16 ||
               idx == 18 || idx == 19 || idx == 20 || idx == 22 || idx == 23 ||
               idx == 30 || idx == 34 || idx == 35 || idx == 38 || idx == 39 ||
               idx == 42 || idx == 43 || idx == 44 || idx == 47 || idx == 48 ||
               idx == 50 || idx == 51 || idx == 53 || idx == 55 || idx == 57 ||
               idx == 58;
    }

    public void avanzar() {
        switch (estadoActual) {
            case TALK_INTRO -> {
                if (dialogueIndex < 2) {
                    dialogueIndex++;
                } else {
                    estadoActual = Estado.APPROACHING;
                }
            }
            case TALK_CINEMATIC -> {
                if (dialogueIndex < 53) {
                    dialogueIndex++;
                } else {
                    estadoActual = Estado.CLONE_ATTACK;
                }
            }
            case TALK_OUTRO -> {
                if (dialogueIndex < 62) {
                    dialogueIndex++;
                } else {
                    estadoActual = Estado.ESCAPING;
                    duplicateX = 24.90f;
                    duplicateY = 4.92f;
                }
            }
            case TALK_POST_PRIZE -> {
                if (dialogueIndex < 68) {
                    dialogueIndex++;
                } else {
                    estadoActual = Estado.FINISHED;
                }
            }
            default -> {}
        }
    }
}
