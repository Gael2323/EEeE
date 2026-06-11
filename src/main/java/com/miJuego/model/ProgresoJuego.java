package com.miJuego.model;

/**
 * Clase para persistir el progreso del jugador durante la sesión.
 *
 * <p>Los campos son estáticos para simplificar el acceso cross-clase durante la sesión.
 * En el futuro, esta clase puede ser refactorizada para serializar/deserializar a archivo
 * (JSON, Properties, etc.) sin cambiar la API pública.</p>
 */
public class ProgresoJuego {

    // ── Progreso de niveles ─────────────────────────────────────────────────────
    /** El nivel máximo al que el jugador tiene acceso. Inicialmente es 1. */
    public static int nivelMaximoDesbloqueado = 1;

    /** Indica si se ha desbloqueado la torre McAfee. */
    public static boolean mcafeeUnlocked = false;

    /** Indica si se ha desbloqueado la torre Internet Explorer (Hielo). */
    public static boolean ieTowerUnlocked = false;

    /** Indica si se ha desbloqueado la torre Messenger (Eléctrica). */
    public static boolean messengerTowerUnlocked = false;

    /** Indica si se ha desbloqueado la torre Firefox (Fuego). */
    public static boolean firefoxUnlocked = false;

    // ── Estado del Hub (escritorio XP) ──────────────────────────────────────────
    /** Word / Nivel 1: siempre desbloqueado. */
    public static boolean wordUnlocked = true;

    /** Papelera de reciclaje / Nivel 2: se desbloquea al completar el Nivel 1. */
    public static boolean recycleBinUnlocked = false;

    /** Solitario / Nivel 3: bloqueado hasta completar Nivel 2. */
    public static boolean solitaireUnlocked = false;

    /** Explorador de archivos / Nivel 4: bloqueado. */
    public static boolean explorerUnlocked = false;

    /** Terminal / Nivel 5: bloqueado. */
    public static boolean terminalUnlocked = false;

    /** Galería de fotos / Nivel 6: bloqueado. */
    public static boolean galleryUnlocked = false;

    /** Centro de soporte (tienda/lore): desbloqueado desde el inicio. */
    public static boolean supportUnlocked = true;

    /** Wizard's Chronicle (Mercader): bloqueado. Se desbloquea con el Nivel 1. */
    public static boolean wizardUnlocked = false;

    // ── Flags de animaciones del Hub ────────────────────────────────────────────
    /**
     * Indica si ya se mostró la animación de Clippy corrupto escapando de Word
     * hacia la Papelera (post-Nivel 1). Si es {@code true}, el hub se muestra
     * directamente sin repetir la cinemática.
     */
    public static boolean postLevel1HubAnimationSeen = false;

    // ── Utilidades ──────────────────────────────────────────────────────────────
    /**
     * Desbloquea el ícono del hub correspondiente al nivel completado y
     * actualiza {@link #nivelMaximoDesbloqueado}.
     */
    public static void unlockAfterLevel(int completedLevel) {
        if (completedLevel >= nivelMaximoDesbloqueado) {
            nivelMaximoDesbloqueado = completedLevel + 1;
        }
        switch (completedLevel) {
            case 1 -> {
                recycleBinUnlocked = true;
                wizardUnlocked = true;
            }
            case 2 -> solitaireUnlocked = true;
            case 3 -> explorerUnlocked = true;
            case 4 -> terminalUnlocked = true;
            case 5 -> galleryUnlocked = true;
            default -> { /* futuro */ }
        }
    }

    /**
     * Devuelve el tipo de torre que debe ir en el slot de la UI dado (0 a 7).
     */
    public static int getTowerTypeForSlot(int slotIndex) {
        int[] layout = {1, 2, 3, 4, 5, 6, 7, 8};
        if (messengerTowerUnlocked) {
            layout[2] = 8;
            layout[7] = 3;
        } else if (ieTowerUnlocked) {
            layout[2] = 7;
            layout[6] = 3;
        }
        if (slotIndex >= 0 && slotIndex < 8) {
            return layout[slotIndex];
        }
        return 1;
    }

    /**
     * Determina si una torre específica está bloqueada según el nivel y los desbloqueos narrativos.
     */
    public static boolean isTowerLocked(int towerType, int levelNum) {
        if (towerType == 1) return false;
        if (towerType == 2) return !mcafeeUnlocked;
        if (towerType == 6) return !firefoxUnlocked;
        if (towerType == 7 && levelNum >= 2) return !ieTowerUnlocked;
        if (towerType == 8 && levelNum >= 2) return !messengerTowerUnlocked;

        return switch (levelNum) {
            case 1, 2 -> true; // Area (3), Avast (4), Fuerte (5), Firefox (6) bloqueadas
            case 3 -> towerType > 3; // Desbloquea Área (3)
            case 4 -> towerType > 4; // Desbloquea Avast (4)
            case 5 -> towerType > 5; // Desbloquea Fuerte (5)
            case 6 -> towerType > 6; // Desbloquea Firefox (6)
            default -> false;
        };
    }
}
