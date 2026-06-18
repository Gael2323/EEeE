package com.miJuego.model;

import com.miJuego.model.*;
import com.game2d.model.*;
import com.game2d.view.GameView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TowerDefenseModelTest {

    private TowerDefenseModel model;
    private DummyGameView view;

    private static class DummyGameView implements GameView {
        @Override public void render(FrameSnapshot frame) {}
        @Override public void setViewListener(com.game2d.view.ViewListener listener) {}
        @Override public void setViewportSize(int widthPx, int heightPx) {}
        @Override public void show() {}
        @Override public void successMessage(String message) {}
        @Override public void errorMessage(String message) {}
    }

    @BeforeEach
    void setUp() {
        view = new DummyGameView();
        // Inicializamos el modelo para el Nivel 1
        model = new TowerDefenseModel(view, 1);
        ProgresoJuego.mcafeeUnlocked = false; // Reset
    }

    @Test
    void testInitialStateLevel1() {
        // En nivel 1, el juego arranca con el spawn en pausa para el tutorial
        assertTrue(model.getTutorial().isActive(), "El tutorial de Clippy debe estar activo al iniciar el nivel 1");
        assertFalse(model.getConfrontation().isActive(), "La confrontación no debe estar activa al inicio");
    }

    @Test
    void testSkipTutorialShortcut() {
        // Ejecutamos la acción "K" (Saltar Nivel / Ir a confrontación)
        GameInput inputK = new GameInput() {
            @Override
            public InputKind getKind() {
                return InputKind.KEY_PRESSED;
            }
            @Override
            public java.util.Optional<String> getKeyCode() {
                return java.util.Optional.of("K");
            }
            @Override
            public java.util.Optional<Float> getX() { return java.util.Optional.empty(); }
            @Override
            public java.util.Optional<Float> getY() { return java.util.Optional.empty(); }
        };

        model.dispatch(inputK);

        // Verificamos que el tutorial se detuvo y comenzó la confrontación
        assertFalse(model.getTutorial().isActive(), "El tutorial debería desactivarse al presionar K");
        assertTrue(model.getConfrontation().isActive(), "La confrontación debería estar activa");
        assertEquals(ClippyConfrontation.Estado.TALK_INTRO, model.getConfrontation().getEstadoActual(), "Debería arrancar en TALK_INTRO");
    }

    @Test
    void testSkipConfrontationShortcut() {
        // Primero vamos a la confrontación
        GameInput inputK = new GameInput() {
            @Override
            public InputKind getKind() { return InputKind.KEY_PRESSED; }
            @Override
            public java.util.Optional<String> getKeyCode() { return java.util.Optional.of("K"); }
            @Override
            public java.util.Optional<Float> getX() { return java.util.Optional.empty(); }
            @Override
            public java.util.Optional<Float> getY() { return java.util.Optional.empty(); }
        };
        model.dispatch(inputK);
        assertTrue(model.getConfrontation().isActive());

        // Ahora presionamos "L" para skippear la cinemática de confrontación
        GameInput inputL = new GameInput() {
            @Override
            public InputKind getKind() { return InputKind.KEY_PRESSED; }
            @Override
            public java.util.Optional<String> getKeyCode() { return java.util.Optional.of("L"); }
            @Override
            public java.util.Optional<Float> getX() { return java.util.Optional.empty(); }
            @Override
            public java.util.Optional<Float> getY() { return java.util.Optional.empty(); }
        };

        model.dispatch(inputL);

        // Verificamos resultados
        assertEquals(ClippyConfrontation.Estado.FINISHED, model.getConfrontation().getEstadoActual(), "La confrontación debe terminar");
        assertTrue(ProgresoJuego.mcafeeUnlocked, "Se debería desbloquear la torre McAfee al finalizar");
    }
    
    @Test
    void testLevel2IntroState() {
        // Probamos que el nivel 2 levante la intro
        TowerDefenseModel lvl2Model = new TowerDefenseModel(view, 2);
        assertTrue(lvl2Model.isLvl2IntroActive(), "La intro del Nivel 2 debe estar activa al inicio");
        assertEquals(1, lvl2Model.getLvl2IntroStep(), "El paso de intro del nivel 2 inicial debe ser 1");
    }
}
