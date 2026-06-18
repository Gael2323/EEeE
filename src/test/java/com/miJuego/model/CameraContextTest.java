package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CameraContextTest {

    @BeforeEach
    void setUp() {
        // Reset a estado "por defecto" para evitar cruces
        CameraContext.setWorldBounds(32f, 24f);
        CameraContext.setTargetViewport(20f, 15f);
        CameraContext.snapTo(0f, 0f);
        CameraContext.triggerShake(0f, 0f); // apagar shake
    }

    @Test
    void testInitialValuesAndBounds() {
        assertEquals(32f, CameraContext.getWorldW());
        assertEquals(24f, CameraContext.getWorldH());
        assertEquals(20f, CameraContext.VIEWPORT_W);
        assertEquals(15f, CameraContext.VIEWPORT_H);
    }

    @Test
    void testMoveCameraRight() {
        CameraContext.moveRight();
        // CameraContext.STEP = 2f
        assertEquals(2f, CameraContext.getTargetCameraX(), 0.01f);
        
        // El movimiento real tiene interpolación, snapTo actualiza directamente
        CameraContext.snapTo(2f, 0f);
        assertEquals(2f, CameraContext.getCameraX(), 0.01f);
    }

    @Test
    void testMoveCameraClampsToWorldBounds() {
        CameraContext.snapTo(100f, 100f);
        // Debe clipearse a: WorldW - ViewportW = 32 - 20 = 12
        // WorldH - ViewportH = 24 - 15 = 9
        assertEquals(12f, CameraContext.getTargetCameraX(), 0.01f);
        assertEquals(9f, CameraContext.getTargetCameraY(), 0.01f);
        
        CameraContext.snapTo(-50f, -50f);
        // Debe clipearse a 0
        assertEquals(0f, CameraContext.getTargetCameraX(), 0.01f);
        assertEquals(0f, CameraContext.getTargetCameraY(), 0.01f);
    }

    @Test
    void testShakeCamera() {
        CameraContext.snapTo(10f, 10f);
        CameraContext.triggerShake(1.0f, 5.0f);
        
        // No verificamos el valor exacto de getCameraX() por el offset random, pero vemos que la lógica pasa
        float currentX = CameraContext.getCameraX();
        // currentX podría estar corrido por el shake, targetX sigue en 10
        assertEquals(10f, CameraContext.getTargetCameraX(), 0.01f);
    }



    @Test
    void testZoomInAndOut() {
        // Default targetViewport = 20,15 -> ZoomIndex = 1
        CameraContext.zoomIn();
        // ZoomIn (indice 0) -> target 10, 7.5
        assertEquals(10f, CameraContext.getTargetViewportW(), 0.01f);
        assertEquals(7.5f, CameraContext.getTargetViewportH(), 0.01f);
        
        CameraContext.zoomOut();
        // Vuelve a 20, 15
        assertEquals(20f, CameraContext.getTargetViewportW(), 0.01f);
        assertEquals(15f, CameraContext.getTargetViewportH(), 0.01f);
    }
}
