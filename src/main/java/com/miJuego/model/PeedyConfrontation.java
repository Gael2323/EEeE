package com.miJuego.model;

public class PeedyConfrontation {
    public enum Estado {
        INACTIVE,
        TALK_INTRO,         // Clippy says he removed internet drivers
        WALK_TO_SPAWN,      // Clippy walks to the spawn area
        TALK_SUSPICIOUS,    // Clippy gets suspicious and tells player to come
        PEEDY_LANDING,      // Peedy superhero landing
        TALK_PEEDY,         // Peedy talks, Clippy backs away
        FINISHED            // Cinematic ends, wave 6 starts
    }

    private Estado estadoActual = Estado.INACTIVE;
    private int dialogueIndex = 0;

    // Coordenadas de Clippy
    private float clippyX = 55.0f;
    private float clippyY = 26.5f;

    // Coordenadas de Peedy
    private float peedyX = 12.6f;
    private float peedyY = -10.0f; // Empieza fuera de la pantalla arriba
    
    // Tiempos para animaciones
    private float landingTimer = 0f;

    private static final String[][] DIALOGUES = {
        // TALK_INTRO (0-1)
        {"¡Listo! Logré eliminar los drivers de red y bloquear los puertos de internet."}, // 0
        {"Deberíamos estar seguros por ah--"}, // 1
        
        // TALK_SUSPICIOUS (2-3)
        {"Qué extraño... hay una fluctuación enorme en el sector de la papelera."}, // 2
        {"Acércate un poco, creo que algo grande intenta salir..."}, // 3
        
        // TALK_PEEDY (4-8)
        {"¡CRAAAAAACK!"}, // 4 (Peedy)
        {"¿Pensaron que desconectar el WiFi los salvaría?"}, // 5 (Peedy)
        {"¡Yo soy el jefe final! ¡Y esta papelera es MI nido!"}, // 6 (Peedy)
        {"¡Retrocedan, rápido!"}, // 7 (Clippy asustado)
        {"¡Acabemos con este pajarraco!"} // 8 (Clippy determinado)
    };

    public void initClippyPosition(float startX, float startY) {
        this.clippyX = startX;
        this.clippyY = startY;
        this.peedyX = 12.6f;
        this.peedyY = -10.0f;
        this.landingTimer = 0f;
    }

    public void update(float deltaSeconds) {
        if (estadoActual == Estado.INACTIVE || estadoActual == Estado.FINISHED) return;

        if (estadoActual == Estado.WALK_TO_SPAWN) {
            float targetX = 18.0f;
            float targetY = 15.0f;
            float dx = targetX - clippyX;
            float dy = targetY - clippyY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float step = 10.0f * deltaSeconds;
            
            if (dist > step) {
                clippyX += (dx / dist) * step;
                clippyY += (dy / dist) * step;
            } else {
                clippyX = targetX;
                clippyY = targetY;
                estadoActual = Estado.TALK_SUSPICIOUS;
                dialogueIndex = 2;
            }
        }
        
        if (estadoActual == Estado.PEEDY_LANDING) {
            landingTimer += deltaSeconds;
            float targetY = 11.2f;
            
            if (landingTimer < 1.0f) {
                // Cayendo en picada
                peedyY += (targetY - peedyY) * 5.0f * deltaSeconds;
            } else if (landingTimer < 3.0f) {
                // Recomponiendose
                peedyY = targetY;
            } else {
                estadoActual = Estado.TALK_PEEDY;
                dialogueIndex = 4;
            }
        }
        
        // Clippy retrocede por el susto durante los primeros dialogos de Peedy
        if (estadoActual == Estado.TALK_PEEDY && dialogueIndex >= 4 && dialogueIndex <= 6) {
            float targetX = 25.0f;
            float targetY = 20.0f;
            clippyX += (targetX - clippyX) * 2.0f * deltaSeconds;
            clippyY += (targetY - clippyY) * 2.0f * deltaSeconds;
        }
    }

    public void advanceDialogue() {
        if (!isDialoguePhase()) return;

        if (estadoActual == Estado.TALK_INTRO) {
            if (dialogueIndex == 1) {
                estadoActual = Estado.WALK_TO_SPAWN;
            } else {
                dialogueIndex++;
            }
        } else if (estadoActual == Estado.TALK_SUSPICIOUS) {
            if (dialogueIndex == 3) {
                estadoActual = Estado.PEEDY_LANDING;
            } else {
                dialogueIndex++;
            }
        } else if (estadoActual == Estado.TALK_PEEDY) {
            if (dialogueIndex == 8) {
                estadoActual = Estado.FINISHED;
            } else {
                dialogueIndex++;
            }
        }
    }

    public String[] getCurrentLines() {
        if (!isDialoguePhase()) return new String[0];
        if (dialogueIndex >= 0 && dialogueIndex < DIALOGUES.length) {
            return DIALOGUES[dialogueIndex];
        }
        return new String[0];
    }

    public boolean isDialoguePhase() {
        return estadoActual == Estado.TALK_INTRO 
            || estadoActual == Estado.TALK_SUSPICIOUS 
            || estadoActual == Estado.TALK_PEEDY;
    }

    // Getters y setters
    public Estado getEstadoActual() { return estadoActual; }
    public void setEstadoActual(Estado e) { 
        this.estadoActual = e; 
        if (e == Estado.INACTIVE) dialogueIndex = 0;
        else if (e == Estado.TALK_INTRO) dialogueIndex = 0;
        else if (e == Estado.TALK_SUSPICIOUS) dialogueIndex = 2;
        else if (e == Estado.TALK_PEEDY) dialogueIndex = 4;
    }
    public int getDialogueIndex() { return dialogueIndex; }
    public float getClippyX() { return clippyX; }
    public float getClippyY() { return clippyY; }
    public float getPeedyX() { return peedyX; }
    public float getPeedyY() { return peedyY; }
    public float getLandingTimer() { return landingTimer; }
    
    public boolean isActive() {
        return estadoActual != Estado.INACTIVE && estadoActual != Estado.FINISHED;
    }
}
