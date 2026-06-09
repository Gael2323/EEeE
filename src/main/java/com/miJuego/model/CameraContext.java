package com.miJuego.model;

/**
 * Contexto global de la cámara/viewport del juego.
 *
 * El mundo lógico es de 32×24 celdas, pero el viewport muestra solo una
 * porción de él (VIEWPORT_W × VIEWPORT_H). Las flechitas mueven la cámara.
 */
public final class CameraContext {

    /** Cuántas celdas muestra el viewport horizontalmente. */
    public static volatile float VIEWPORT_W = 20f;

    /** Cuántas celdas muestra el viewport verticalmente. */
    public static volatile float VIEWPORT_H = 15f;

    /** Límites del mundo lógico. */
    private static volatile float WORLD_W = 32f;
    private static volatile float WORLD_H = 24f;

    public static float getWorldW() { return WORLD_W; }
    public static float getWorldH() { return WORLD_H; }

    public static void setWorldBounds(float w, float h) {
        WORLD_W = w;
        WORLD_H = h;
    }

    /** Posición (en celdas) de la esquina superior-izquierda del viewport. */
    private static volatile float cameraX = 0f;
    private static volatile float cameraY = 0f;

    /** Valores objetivo para interpolación suave */
    private static volatile float targetCameraX = 0f;
    private static volatile float targetCameraY = 0f;
    private static volatile float targetViewportW = 20f;
    private static volatile float targetViewportH = 15f;

    /** Cuántas celdas se mueve la cámara por pulsación. */
    private static final float STEP = 2f;

    private static int zoomIndex = 1; // 0: Cerca, 1: Normal, 2: Lejos, 3: Completo
    private static final float[][] ZOOM_SIZES = {
        {10f, 7.5f},  // Cerca (Zoom In)
        {20f, 15f},   // Normal (Default)
        {24f, 18f},   // Lejos
        {32f, 24f},   // Completo original (Muestra toda la grilla 32x24)
        {48f, 36f},   // Muy lejos
        {64f, 48f},   // Completo extendido (Muestra toda la grilla 64x48)
        {96f, 72f},   // Vista panorámica
        {128f, 96f}   // Vista alejada máxima (El mapa ocupará la mitad de la pantalla)
    };

    public static float getCameraX() { return cameraX; }
    public static float getCameraY() { return cameraY; }
    public static float getTargetCameraX() { return targetCameraX; }
    public static float getTargetCameraY() { return targetCameraY; }
    public static float getTargetViewportW() { return targetViewportW; }
    public static float getTargetViewportH() { return targetViewportH; }

    public static void moveLeft()  { setCameraX(targetCameraX - STEP); }
    public static void moveRight() { setCameraX(targetCameraX + STEP); }
    public static void moveUp()    { setCameraY(targetCameraY - STEP); }
    public static void moveDown()  { setCameraY(targetCameraY + STEP); }

    public static void zoomIn() {
        for (int i = ZOOM_SIZES.length - 1; i >= 0; i--) {
            if (ZOOM_SIZES[i][0] < targetViewportW - 0.1f) {
                zoomIndex = i;
                updateZoom();
                return;
            }
        }
        if (zoomIndex > 0) {
            zoomIndex = 0;
            updateZoom();
        }
    }

    public static void zoomOut() {
        for (int i = 0; i < ZOOM_SIZES.length; i++) {
            if (ZOOM_SIZES[i][0] > targetViewportW + 0.1f) {
                zoomIndex = i;
                updateZoom();
                return;
            }
        }
        if (zoomIndex < ZOOM_SIZES.length - 1) {
            zoomIndex = ZOOM_SIZES.length - 1;
            updateZoom();
        }
    }

    private static void updateZoom() {
        float centerX = targetCameraX + targetViewportW / 2f;
        float centerY = targetCameraY + targetViewportH / 2f;

        targetViewportW = ZOOM_SIZES[zoomIndex][0];
        targetViewportH = ZOOM_SIZES[zoomIndex][1];

        setCameraX(centerX - targetViewportW / 2f);
        setCameraY(centerY - targetViewportH / 2f);
    }

    public static void reset() {
        zoomIndex = 1;
        targetViewportW = 20f;
        targetViewportH = 15f;
        VIEWPORT_W = 20f;
        VIEWPORT_H = 15f;

        targetCameraX = 0f;
        targetCameraY = 0f;
        cameraX = 0f;
        cameraY = 0f;
    }

    public static void snapTo(float x, float y) {
        setCameraX(x);
        setCameraY(y);
        cameraX = targetCameraX;
        cameraY = targetCameraY;
    }

    public static void setCameraX(float x) {
        if (targetViewportW > WORLD_W) {
            targetCameraX = (WORLD_W - targetViewportW) / 2f;
        } else {
            targetCameraX = Math.max(0f, Math.min(WORLD_W - targetViewportW, x));
        }
    }

    public static void setCameraY(float y) {
        if (targetViewportH > WORLD_H) {
            targetCameraY = (WORLD_H - targetViewportH) / 2f;
        } else {
            targetCameraY = Math.max(0f, Math.min(WORLD_H - targetViewportH, y));
        }
    }

    public static void setTargetViewport(float w, float h) {
        targetViewportW = Math.max(8f, w);
        targetViewportH = Math.max(6f, h);
    }

    public static void tick(float dt) {
        float lerpSpeed = 10f; // Velocidad de interpolación
        cameraX = cameraX + (targetCameraX - cameraX) * lerpSpeed * dt;
        cameraY = cameraY + (targetCameraY - cameraY) * lerpSpeed * dt;
        VIEWPORT_W = VIEWPORT_W + (targetViewportW - VIEWPORT_W) * lerpSpeed * dt;
        VIEWPORT_H = VIEWPORT_H + (targetViewportH - VIEWPORT_H) * lerpSpeed * dt;

        // Limitar la posición actual para no salirse de los límites lógicos
        if (VIEWPORT_W > WORLD_W) {
            cameraX = (WORLD_W - VIEWPORT_W) / 2f;
        } else {
            if (cameraX + VIEWPORT_W > WORLD_W) cameraX = WORLD_W - VIEWPORT_W;
            if (cameraX < 0f) cameraX = 0f;
        }

        if (VIEWPORT_H > WORLD_H) {
            cameraY = (WORLD_H - VIEWPORT_H) / 2f;
        } else {
            if (cameraY + VIEWPORT_H > WORLD_H) cameraY = WORLD_H - VIEWPORT_H;
            if (cameraY < 0f) cameraY = 0f;
        }
    }

    private CameraContext() {}
}
