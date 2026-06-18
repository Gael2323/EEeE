package com.game2d.model;

import com.miJuego.model.EstadoJuego;
import com.miJuego.model.Juego;
import com.miJuego.model.Jugador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TowerDefenseLogicTest {

    private Juego juego;

    @BeforeEach
    void setUp() {
        // Inicializamos el núcleo de tu juego limpio antes de cada test
        juego = new Juego();
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
        com.miJuego.model.ProgresoJuego.mcafeeUnlocked = false; // Reset static progress
    }

    // 1. TEST MONEDAS: Ver si no te deja comprar si no te alcanza el oro
    @Test
    void testNoSePuedeComprarTorreSinMonedasSuficientes() {
        // Ponemos el tipo de torre 1 (Común - Costo: 100)
        juego.setSelectedTowerType(1);

        // Forzamos al jugador a tener solo 10 monedas (insuficiente)
        juego.getJugador().setMoneda(10);

        // Tu modelo hace un 'try-catch' porque 'placeTower' tira excepción si falla el negocio.
        // Verificamos que efectivamente lance la excepción esperada al intentar plantar en (5,5)
        assertThrows(RuntimeException.class, () -> {
            juego.placeTower(15, 12);
        }, "Debería lanzar una excepción porque el jugador no tiene oro suficiente.");

        // Verificamos que la lista de torres efectivamente haya quedado vacía
        assertEquals(0, juego.getTorres().size(), "No debería haberse agregado ninguna torre.");
    }

    // 2. TEST SUPERPOSICIÓN: Ver si te deja plantar una torre encima de otra
    @Test
    void testNoSePuedePlantarTorreSobreOtra() {
        // Aseguramos que tenga oro de sobra (ej: 1000)
        juego.getJugador().setMoneda(1000);

        // Seleccionamos y plantamos la primera torre (Tipo 1: Común) en la posición (15, 12)
        juego.setSelectedTowerType(1);
        juego.placeTower(15, 12);
        assertEquals(1, juego.getTorres().size(), "La primera torre debió colocarse.");

        // Seleccionamos otra torre (Tipo 3: Cañón) e intentamos ponerla EXACTAMENTE en el mismo lugar (15, 12)
        juego.setSelectedTowerType(3);

        // Comprobamos que el juego tire excepción por intentar pisar la posición
        assertThrows(RuntimeException.class, () -> {
            juego.placeTower(15, 12);
        }, "Debería lanzar una excepción por intentar superponer torres en la misma celda.");

        // Aseguramos que la lista se mantenga en 1 sola torre y no se haya bugeado
        assertEquals(1, juego.getTorres().size(), "El mapa debería seguir teniendo una única torre.");
    }

    // 3. TEST EXTRA (VIDAS)
    @Test
    void testEstadoInicialJugadorYConsistencia() {
        Jugador jugador = juego.getJugador();

        int vidaInicial = jugador.getHealth();
        // Lo dejamos mal a proposito
        jugador.setHealth(vidaInicial - 6);

        // El test valida que el setter y getter impacten directo en el negocio
        assertEquals(vidaInicial - 6, juego.getJugador().getHealth(), "La salud del jugador debería haber bajado a la configurada.");
    }

    // 4. TEST LÍMITES POLÍGONO NIVEL 1
    @Test
    void testNoSePuedePlantarTorreFueraDelPoligonoNivel1() {
        juego.getJugador().setMoneda(1000);
        juego.setSelectedTowerType(1);

        // Posición (2, 2) está claramente fuera del polígono de la hoja (que tiene Y >= 7.3)
        assertThrows(IllegalStateException.class, () -> {
            juego.placeTower(2, 2);
        }, "Debería lanzar una excepción por estar fuera de los límites de la hoja blanca.");
    }

    @Test
    void testSePuedePlantarTorreDentroDelPoligonoNivel1() {
        juego.getJugador().setMoneda(1000);
        juego.setSelectedTowerType(1);

        // Posición (15, 12) está dentro del polígono de la hoja blanca y fuera del camino
        assertDoesNotThrow(() -> {
            juego.placeTower(15, 12);
        }, "Debería permitir plantar la torre dentro del documento.");

        assertEquals(1, juego.getTorres().size(), "Debería haber una torre colocada.");
    }

    // 5. TEST TUTORIAL INTERACTIVO DE CLIPPY
    @Test
    void testProgresoTutorialClippy() {
        // Creamos la vista dummy / null
        com.game2d.view.GameView dummyView = new com.game2d.view.GameView() {
            @Override public void render(com.game2d.model.FrameSnapshot frame) {}
            @Override public void setViewListener(com.game2d.view.ViewListener listener) {}
            @Override public void setViewportSize(int w, int h) {}
            @Override public void show() {}
            @Override public void successMessage(String msg) {}
            @Override public void errorMessage(String msg) {}
        };

        com.miJuego.model.TowerDefenseModel model = new com.miJuego.model.TowerDefenseModel(dummyView);
        com.miJuego.model.ClippyTutorial tutorial = model.getTutorial();

        // Inicialmente debe estar en INTRO_1 y spawnPaused = true
        assertTrue(tutorial.isActive(), "El tutorial debe estar activo al iniciar.");
        assertEquals(com.miJuego.model.ClippyTutorial.Estado.INTRO_1, tutorial.getEstadoActual());
        assertTrue(model.getJuego().getNivelActual().isSpawnPaused(), "El spawn debe estar pausado.");

        // Verificar que las teclas 2-8 están bloqueadas durante el tutorial activo
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("2"));
        // El tipo de torre por defecto en model al iniciar es 1 (Común), y debe seguir siendo 1
        assertEquals(1, model.getJuego().getSelectedTowerType(), "La tecla 2 no debe equipar nada durante el tutorial.");

        // Simulamos click/Space para avanzar a CINEMATIC_WALK
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        assertEquals(com.miJuego.model.ClippyTutorial.Estado.CINEMATIC_WALK, tutorial.getEstadoActual());

        // Simulamos que el corruptor llegó al final
        model.getJuego().getNivelActual().getEnemigosRestantes().clear();
        model.update(0.1f);
        assertEquals(com.miJuego.model.ClippyTutorial.Estado.CLIPPY_REACTION, tutorial.getEstadoActual());

        // Simulamos Space para avanzar desde CLIPPY_REACTION. Como parent frame es null, pasa directo a INTRO_2
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        assertEquals(com.miJuego.model.ClippyTutorial.Estado.INTRO_2, tutorial.getEstadoActual());

        // Simulamos Space para avanzar a WAIT_PLACE
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        assertEquals(com.miJuego.model.ClippyTutorial.Estado.WAIT_PLACE, tutorial.getEstadoActual());
        assertEquals(1, model.getJuego().getSelectedTowerType(), "Debería equipar automáticamente la torre Antivirus (tipo 1).");

        // Colocar una torre en WAIT_PLACE avanza automáticamente a PLACE_SUCCESS
        model.getJuego().getJugador().setMoneda(1000);
        model.getJuego().placeTower(15, 12);
        // Llamar a update para procesar el cambio
        model.update(0.1f);
        assertEquals(com.miJuego.model.ClippyTutorial.Estado.PLACE_SUCCESS, tutorial.getEstadoActual());

        // Avanzar a WAIT_START_WAVE
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        assertEquals(com.miJuego.model.ClippyTutorial.Estado.WAIT_START_WAVE, tutorial.getEstadoActual());

        // Iniciar oleada (ENTER) avanza al diálogo OUTRO primero
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Enter"));
        assertEquals(com.miJuego.model.ClippyTutorial.Estado.OUTRO, tutorial.getEstadoActual());

        // Presionar ENTER de nuevo completa el tutorial y reanuda el spawn
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Enter"));
        assertEquals(com.miJuego.model.ClippyTutorial.Estado.COMPLETED, tutorial.getEstadoActual());
        assertTrue(tutorial.isActive(), "El tutorial debe seguir activo una vez completado para que Clippy baile.");
        assertFalse(model.getJuego().getNivelActual().isSpawnPaused(), "El spawn debe haberse reanudado.");

        // Incluso después de completar el tutorial, en Nivel 1 las torres 2-8 siguen bloqueadas (tanto por tecla como por acción de la tienda)
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("2"));
        assertEquals(1, model.getJuego().getSelectedTowerType(), "Incluso tras el tutorial, las torres 2-8 deben estar bloqueadas en Nivel 1.");

        model.dispatch(com.game2d.model.SimpleGameInput.action("2"));
        assertEquals(1, model.getJuego().getSelectedTowerType(), "Las acciones de tienda para torres 2-8 deben estar bloqueadas en Nivel 1.");
    }

    @Test
    void testTowerLockGeneralizadoSegunNivel() {
        com.game2d.view.GameView dummyView = new com.game2d.view.GameView() {
            @Override public void render(com.game2d.model.FrameSnapshot frame) {}
            @Override public void setViewListener(com.game2d.view.ViewListener listener) {}
            @Override public void setViewportSize(int w, int h) {}
            @Override public void show() {}
            @Override public void successMessage(String msg) {}
            @Override public void errorMessage(String msg) {}
        };
        com.miJuego.model.TowerDefenseModel modelLvl1 = new com.miJuego.model.TowerDefenseModel(dummyView, 1);
        modelLvl1.dispatch(com.game2d.model.SimpleGameInput.keyPressed("3"));
        assertEquals(1, modelLvl1.getJuego().getSelectedTowerType(), "Torre 3 debería estar bloqueada en Nivel 1.");

        com.miJuego.model.TowerDefenseModel modelLvl2 = new com.miJuego.model.TowerDefenseModel(dummyView, 2);
        modelLvl2.setLvl2IntroActive(false);
        modelLvl2.dispatch(com.game2d.model.SimpleGameInput.keyPressed("3"));
        assertEquals(1, modelLvl2.getJuego().getSelectedTowerType(), "Torre 3 debería estar bloqueada en Nivel 2.");

        com.miJuego.model.ProgresoJuego.ieTowerUnlocked = true;
        modelLvl2.dispatch(com.game2d.model.SimpleGameInput.keyPressed("3"));
        assertEquals(7, modelLvl2.getJuego().getSelectedTowerType(), "Al presionar 3, el slot 3 debería contener a Torre 7 en Nivel 2 tras avanzar en la historia.");
    }

    @Test
    void testProgresionNivelMaximoDesbloqueado() {
        com.miJuego.model.ProgresoJuego.nivelMaximoDesbloqueado = 1;

        com.game2d.view.GameView dummyView = new com.game2d.view.GameView() {
            @Override public void render(com.game2d.model.FrameSnapshot frame) {}
            @Override public void setViewListener(com.game2d.view.ViewListener listener) {}
            @Override public void setViewportSize(int w, int h) {}
            @Override public void show() {}
            @Override public void successMessage(String msg) {}
            @Override public void errorMessage(String msg) {}
        };

        com.miJuego.model.TowerDefenseModel model = new com.miJuego.model.TowerDefenseModel(dummyView, 1);
        
        // Despausar spawn para que avance en el test y no se quede en bucle infinito
        model.getJuego().getNivelActual().setSpawnPaused(false);

        // Vaciar la cola de spawn de forma segura sin reflexión
        while (model.getJuego().getNivelActual().getEnemigosRestantesCount() > 0) {
            model.getJuego().getNivelActual().updateSpawn(100.0f);
            model.getJuego().getNivelActual().getEnemigosRestantes().clear();
        }

        // Llamar a update para activar la confrontación
        model.update(0.1f);
        assertEquals(com.miJuego.model.ClippyConfrontation.Estado.TALK_INTRO, model.getConfrontation().getEstadoActual());

        // Avanzar toda la cinemática de diálogos y movimientos automáticamente
        int safety = 0;
        while (model.getConfrontation().getEstadoActual() != com.miJuego.model.ClippyConfrontation.Estado.FINISHED && safety < 500) {
            com.miJuego.model.ClippyConfrontation.Estado current = model.getConfrontation().getEstadoActual();
            if (current == com.miJuego.model.ClippyConfrontation.Estado.TALK_INTRO ||
                current == com.miJuego.model.ClippyConfrontation.Estado.TALK_CINEMATIC ||
                current == com.miJuego.model.ClippyConfrontation.Estado.TALK_OUTRO ||
                current == com.miJuego.model.ClippyConfrontation.Estado.TALK_POST_PRIZE) {
                model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
            }
            model.update(0.1f);
            safety++;
        }

        assertEquals(2, com.miJuego.model.ProgresoJuego.nivelMaximoDesbloqueado, "El nivel máximo desbloqueado debería ser 2 tras completar el Nivel 1.");
    }

    @Test
    void testSecondClippyAppearsOnWaveCompleted() {
        com.game2d.view.GameView dummyView = new com.game2d.view.GameView() {
            @Override public void render(com.game2d.model.FrameSnapshot frame) {}
            @Override public void setViewListener(com.game2d.view.ViewListener listener) {}
            @Override public void setViewportSize(int w, int h) {}
            @Override public void show() {}
            @Override public void successMessage(String msg) {}
            @Override public void errorMessage(String msg) {}
        };
        com.miJuego.model.TowerDefenseModel model = new com.miJuego.model.TowerDefenseModel(dummyView, 1);
        
        // Al inicio, no debe estar el segundo Clippy
        boolean hasSecondClippyInitial = model.capture().getDrawables().stream()
                .anyMatch(d -> d.getId().equals("second-clippy"));
        assertFalse(hasSecondClippyInitial, "El segundo Clippy no debería mostrarse antes de terminar la oleada.");

        // Despausar spawn y vaciar la cola de enemigos
        model.getJuego().getNivelActual().setSpawnPaused(false);
        while (model.getJuego().getNivelActual().getEnemigosRestantesCount() > 0) {
            model.getJuego().getNivelActual().updateSpawn(100.0f);
            model.getJuego().getNivelActual().getEnemigosRestantes().clear();
        }

        // Ahora que la oleada terminó, el segundo Clippy debe aparecer
        boolean hasSecondClippyFinal = model.capture().getDrawables().stream()
                .anyMatch(d -> d.getId().equals("second-clippy"));
        assertTrue(hasSecondClippyFinal, "El segundo Clippy debería aparecer una vez terminada la oleada.");
    }

    @Test
    void testConfrontacionFlujoCompleto() {
        com.game2d.view.GameView dummyView = new com.game2d.view.GameView() {
            @Override public void render(com.game2d.model.FrameSnapshot frame) {}
            @Override public void setViewListener(com.game2d.view.ViewListener listener) {}
            @Override public void setViewportSize(int w, int h) {}
            @Override public void show() {}
            @Override public void successMessage(String msg) {}
            @Override public void errorMessage(String msg) {}
        };
        com.miJuego.model.ProgresoJuego.mcafeeUnlocked = false; // Reset
        com.miJuego.model.TowerDefenseModel model = new com.miJuego.model.TowerDefenseModel(dummyView, 1);
        com.miJuego.model.ClippyConfrontation confrontation = model.getConfrontation();

        assertEquals(com.miJuego.model.ClippyConfrontation.Estado.INACTIVE, confrontation.getEstadoActual());
        assertFalse(confrontation.isActive());

        // Despausar spawn y vaciar la cola de enemigos
        model.getJuego().getNivelActual().setSpawnPaused(false);
        while (model.getJuego().getNivelActual().getEnemigosRestantesCount() > 0) {
            model.getJuego().getNivelActual().updateSpawn(100.0f);
            model.getJuego().getNivelActual().getEnemigosRestantes().clear();
        }

        // update activa TALK_INTRO
        model.update(0.1f);
        assertEquals(com.miJuego.model.ClippyConfrontation.Estado.TALK_INTRO, confrontation.getEstadoActual());
        assertTrue(confrontation.isActive());
        assertEquals("COMUN", confrontation.getSpeaker());

        // Verificar diálogos iniciales de TALK_INTRO
        assertEquals(0, confrontation.getDialogueIndex());
        assertEquals("Bueno… eso fue bastante intenso para ser un tutorial.", confrontation.getCurrentLine()[0]);
        assertEquals("COMUN", confrontation.getSpeaker());

        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        assertEquals(1, confrontation.getDialogueIndex());
        assertEquals("Pero sobrevivimos. Eso ya cuenta como progreso.", confrontation.getCurrentLine()[0]);

        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        assertEquals(2, confrontation.getDialogueIndex());
        assertEquals("Ahora salgamos de acá antes de que Word decida corregirnos el alma.", confrontation.getCurrentLine()[0]);

        // Siguiente Space transiciona a APPROACHING
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        assertEquals(com.miJuego.model.ClippyConfrontation.Estado.APPROACHING, confrontation.getEstadoActual());

        // Update por ticks para que vuele hacia el clon
        int ticks = 0;
        while (confrontation.getEstadoActual() == com.miJuego.model.ClippyConfrontation.Estado.APPROACHING && ticks < 100) {
            model.update(0.1f);
            ticks++;
        }
        assertEquals(com.miJuego.model.ClippyConfrontation.Estado.TALK_CINEMATIC, confrontation.getEstadoActual());
        assertEquals(3, confrontation.getDialogueIndex());
        assertEquals("Ah.", confrontation.getCurrentLine()[0]);
        assertFalse(confrontation.isCorrupt(), "El clon no debería estar corrupto en un principio");

        // Avanzar rápido hasta que el clon empiece a corromperse (diálogo 42 "¡NO!")
        while (confrontation.getDialogueIndex() < 42 && confrontation.getEstadoActual() == com.miJuego.model.ClippyConfrontation.Estado.TALK_CINEMATIC) {
            model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        }
        assertEquals(42, confrontation.getDialogueIndex());
        assertEquals("¡NO!", confrontation.getCurrentLine()[0]);
        assertTrue(confrontation.isCorrupt(), "El clon ya debería mostrarse corrupto");
        assertTrue(confrontation.isGlitchActive(), "El glitch debería estar activo");

        // Terminar diálogos cinematográficos
        while (confrontation.getEstadoActual() == com.miJuego.model.ClippyConfrontation.Estado.TALK_CINEMATIC) {
            model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        }
        assertEquals(com.miJuego.model.ClippyConfrontation.Estado.CLONE_ATTACK, confrontation.getEstadoActual());

        // Update para que ocurra la embestida
        ticks = 0;
        while (confrontation.getEstadoActual() == com.miJuego.model.ClippyConfrontation.Estado.CLONE_ATTACK && ticks < 100) {
            model.update(0.1f);
            ticks++;
        }
        assertEquals(com.miJuego.model.ClippyConfrontation.Estado.TALK_OUTRO, confrontation.getEstadoActual());
        assertEquals(54, confrontation.getDialogueIndex());
        assertEquals("¡Eh—!", confrontation.getCurrentLine()[0]);

        // Terminar diálogos post-embestida
        while (confrontation.getEstadoActual() == com.miJuego.model.ClippyConfrontation.Estado.TALK_OUTRO) {
            model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        }
        assertEquals(com.miJuego.model.ClippyConfrontation.Estado.ESCAPING, confrontation.getEstadoActual());

        // Mover por ticks para simular el escape
        int safetyTicks = 0;
        while (confrontation.getEstadoActual() == com.miJuego.model.ClippyConfrontation.Estado.ESCAPING && safetyTicks < 100) {
            model.update(0.1f);
            safetyTicks++;
        }

        // El escape se completó. Como el dummyView ejecuta el onClose de showPrizePopup inmediatamente,
        // el estado ya transicionó a TALK_POST_PRIZE (index 63).
        assertEquals(com.miJuego.model.ClippyConfrontation.Estado.TALK_POST_PRIZE, confrontation.getEstadoActual());
        assertTrue(com.miJuego.model.ProgresoJuego.mcafeeUnlocked, "La torre McAfee debería desbloquearse al cerrarse el popup.");
        assertEquals(63, confrontation.getDialogueIndex());

        // Avanzar los diálogos de TALK_POST_PRIZE (63-68)
        while (confrontation.getEstadoActual() == com.miJuego.model.ClippyConfrontation.Estado.TALK_POST_PRIZE) {
            model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        }

        assertEquals(com.miJuego.model.ClippyConfrontation.Estado.FINISHED, confrontation.getEstadoActual());
    }

    @Test
    void testLevel2IntroCinematic() {
        com.game2d.view.GameView dummyView = new com.game2d.view.GameView() {
            @Override public void render(com.game2d.model.FrameSnapshot frame) {}
            @Override public void setViewListener(com.game2d.view.ViewListener listener) {}
            @Override public void setViewportSize(int w, int h) {}
            @Override public void show() {}
            @Override public void successMessage(String msg) {}
            @Override public void errorMessage(String msg) {}
        };
        com.miJuego.model.TowerDefenseModel model = new com.miJuego.model.TowerDefenseModel(dummyView, 2);

        // Al iniciar, la cinemática de Nivel 2 debe estar activa
        assertTrue(model.isLvl2IntroActive());
        assertEquals(1, model.getLvl2IntroStep());

        // El spawn de enemigos debe estar pausado
        assertTrue(model.getJuego().getNivelActual().isSpawnPaused());

        // Avanzamos el primer diálogo (pasa a diálogo 2)
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        assertTrue(model.isLvl2IntroActive());
        assertEquals(2, model.getLvl2IntroStep());

        // Avanzamos el segundo diálogo (pasa a diálogo 3)
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        assertTrue(model.isLvl2IntroActive());
        assertEquals(3, model.getLvl2IntroStep());

        // Avanzamos el tercer diálogo (termina la cinemática)
        model.dispatch(com.game2d.model.SimpleGameInput.keyPressed("Space"));
        assertFalse(model.isLvl2IntroActive());
        assertEquals(0, model.getLvl2IntroStep());

        // Spawn de oleada sigue pausado esperando que el jugador presione ENTER/Iniciar
        assertTrue(model.getJuego().getNivelActual().isSpawnPaused());
    }
}