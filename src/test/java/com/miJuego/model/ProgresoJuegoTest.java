package com.miJuego.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProgresoJuegoTest {

    @BeforeEach
    void setUp() {
        resetProgreso();
    }

    @AfterEach
    void tearDown() {
        resetProgreso();
    }

    private void resetProgreso() {
        ProgresoJuego.nivelMaximoDesbloqueado = 1;
        ProgresoJuego.mcafeeUnlocked = false;
        ProgresoJuego.ieTowerUnlocked = false;
        ProgresoJuego.messengerTowerUnlocked = false;
        ProgresoJuego.firefoxUnlocked = false;
        ProgresoJuego.wordUnlocked = true;
        ProgresoJuego.recycleBinUnlocked = false;
        ProgresoJuego.solitaireUnlocked = false;
        ProgresoJuego.explorerUnlocked = false;
        ProgresoJuego.terminalUnlocked = false;
        ProgresoJuego.galleryUnlocked = false;
        ProgresoJuego.supportUnlocked = true;
        ProgresoJuego.wizardUnlocked = false;
        ProgresoJuego.postLevel1HubAnimationSeen = false;
    }

    @Test
    void testUnlockAfterLevel1() {
        ProgresoJuego.unlockAfterLevel(1);
        
        assertEquals(2, ProgresoJuego.nivelMaximoDesbloqueado, "El nivel máximo desbloqueado debería ser 2");
        assertTrue(ProgresoJuego.recycleBinUnlocked, "La papelera debería estar desbloqueada");
        assertTrue(ProgresoJuego.wizardUnlocked, "El mago (Mercader) debería estar desbloqueado");
        assertFalse(ProgresoJuego.solitaireUnlocked, "El solitario no debería estar desbloqueado aún");
    }

    @Test
    void testUnlockAfterLevel2() {
        ProgresoJuego.unlockAfterLevel(2);
        
        assertEquals(3, ProgresoJuego.nivelMaximoDesbloqueado, "El nivel máximo desbloqueado debería ser 3");
        assertTrue(ProgresoJuego.solitaireUnlocked, "El solitario debería estar desbloqueado");
    }

    @Test
    void testTowerLockedLogic() {
        // Nivel 1: Torre común (1) desbloqueada, el resto de las genéricas bloqueadas
        assertFalse(ProgresoJuego.isTowerLocked(1, 1), "La torre común siempre está desbloqueada");
        assertTrue(ProgresoJuego.isTowerLocked(3, 1), "La torre de área debe estar bloqueada en nivel 1");
        
        // Nivel 3: Desbloquea torre 3 (Area)
        assertFalse(ProgresoJuego.isTowerLocked(3, 3), "La torre de área debe estar desbloqueada en nivel 3");
        assertTrue(ProgresoJuego.isTowerLocked(4, 3), "La torre de Avast debe estar bloqueada en nivel 3");

        // Nivel 4: Desbloquea torre 4 (Avast)
        assertFalse(ProgresoJuego.isTowerLocked(4, 4), "La torre de Avast debe estar desbloqueada en nivel 4");
    }

    @Test
    void testSpecialTowerLockedLogic() {
        // Torres especiales como McAfee (2) y Firefox (6)
        assertTrue(ProgresoJuego.isTowerLocked(2, 1), "McAfee debe estar bloqueada inicialmente");
        ProgresoJuego.mcafeeUnlocked = true;
        assertFalse(ProgresoJuego.isTowerLocked(2, 1), "McAfee debe estar desbloqueada si mcafeeUnlocked es true");

        assertTrue(ProgresoJuego.isTowerLocked(6, 1), "Firefox debe estar bloqueada inicialmente");
        ProgresoJuego.firefoxUnlocked = true;
        assertFalse(ProgresoJuego.isTowerLocked(6, 1), "Firefox debe estar desbloqueada si firefoxUnlocked es true");
    }

    @Test
    void testGetTowerTypeForSlot() {
        // Default layout: {1, 2, 3, 4, 5, 6, 7, 8}
        assertEquals(1, ProgresoJuego.getTowerTypeForSlot(0));
        assertEquals(3, ProgresoJuego.getTowerTypeForSlot(2));

        // Activar ieTowerUnlocked
        ProgresoJuego.ieTowerUnlocked = true;
        assertEquals(7, ProgresoJuego.getTowerTypeForSlot(2), "Con IE desbloqueado, el slot 2 debe ser 7");
        assertEquals(3, ProgresoJuego.getTowerTypeForSlot(6), "Con IE desbloqueado, el slot 6 debe ser 3");

        // Activar messengerTowerUnlocked
        ProgresoJuego.ieTowerUnlocked = false;
        ProgresoJuego.messengerTowerUnlocked = true;
        assertEquals(8, ProgresoJuego.getTowerTypeForSlot(2), "Con Messenger desbloqueado, el slot 2 debe ser 8");
        assertEquals(3, ProgresoJuego.getTowerTypeForSlot(7), "Con Messenger desbloqueado, el slot 7 debe ser 3");
    }
}
