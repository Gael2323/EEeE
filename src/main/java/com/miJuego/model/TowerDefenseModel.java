package com.miJuego.model;

import com.game2d.controller.GameCommands;
import com.game2d.model.*;
import com.game2d.view.GameView;
import com.game2d.view.GameViewMessages;

import javax.swing.JOptionPane;
import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TowerDefenseModel implements GameModel {
    private static float getWorldWidth() { return com.miJuego.model.CameraContext.getWorldW(); }
    private static float getWorldHeight() { return com.miJuego.model.CameraContext.getWorldH(); }

    private final GameView view;
    private final Juego juego;
    private SessionState sessionState = SessionState.READY;
    private boolean scoreSaved = false;
    private boolean prizePopupOpened = false;

    // Guardado de la última posición del cursor de click
    private int lastClickedX = -1;
    private int lastClickedY = -1;

    private long lastClickTime = 0;
    private int lastClickX = -1;
    private int lastClickY = -1;

    // Timeout de selección de torre
    private float selectionTimeElapsed = 0.0f;
    private int lastHoverX = -1;
    private int lastHoverY = -1;

    // Tutorial de Clippy interactivo
    private final ClippyTutorial tutorial;

    // Confrontación cinemática de Clippy
    private ClippyConfrontation confrontation;
    
    private com.miJuego.model.TorreAvast torreEsperandoCoordenadas = null;

    private javax.sound.sampled.Clip bgMusic;

    public TowerDefenseModel(GameView view) {
        this(view, 1);
    }

    public TowerDefenseModel(GameView view, int levelNum) {
        this.view = view;
        this.juego = new Juego();
        this.juego.restart(levelNum);
        this.tutorial = new ClippyTutorial();
        this.confrontation = new ClippyConfrontation();

        this.prizePopupOpened = false;

        // Si es el Nivel 1, iniciamos el juego en estado PLAYING pero pausamos el spawn para el tutorial
        if (levelNum == 1) {
            juego.setEstado(EstadoJuego.PLAYING);
            juego.getNivelActual().setSpawnPaused(true);
        } else {
            juego.setEstado(EstadoJuego.PLAYING);
            juego.getNivelActual().setSpawnPaused(true);
            view.successMessage("¡Presione ENTER para comenzar la primera oleada!");
        }
    }

    public ClippyTutorial getTutorial() {
        return tutorial;
    }

    public ClippyConfrontation getConfrontation() {
        return confrontation;
    }

    @Override
    public FrameSnapshot capture() {
        List<Drawable> drawables = new ArrayList<>();

        // 1. Dibujar el mapa completo (Pasto y Camino)
        Nivel nivel = juego.getNivelActual();
        for (int ix = 0; ix < (int) getWorldWidth(); ix++) {
            for (int iy = 0; iy < (int) getWorldHeight(); iy++) {
                if (nivel.intersectsPath(ix, iy)) {
                    drawables.add(new PathTileDrawable("path-" + ix + "-" + iy, ix, iy));
                }
            }
        }

        // 2. Dibujar el resaltado de la celda seleccionada (Cursor de click anterior)
        if (lastClickedX >= 0 && lastClickedX < getWorldWidth() && lastClickedY >= 0 && lastClickedY < getWorldHeight()) {
            drawables.add(new SelectionHighlightDrawable(lastClickedX, lastClickedY));
        }

        // 3. Dibujar previsualización fantasma (Ghost) de la torre equipada al hacer hover con el mouse
        int hoverX = com.miJuego.model.ActualTowerContext.getHoverX();
        int hoverY = com.miJuego.model.ActualTowerContext.getHoverY();
        if (hoverX >= 0 && hoverX < getWorldWidth() && hoverY >= 0 && hoverY < getWorldHeight() && juego.getEstado() == EstadoJuego.PLAYING) {
            int selectedType = juego.getSelectedTowerType();
            if (selectedType != 0) {
                boolean onPath = nivel.intersectsPath(hoverX, hoverY);
                boolean hasTower = false;
                for (Torre t : juego.getTorres()) {
                    if (Math.round(t.getX()) == hoverX && Math.round(t.getY()) == hoverY) {
                        hasTower = true;
                        break;
                    }
                }
                if (!onPath && !hasTower && nivel.isValidPlacementArea(hoverX, hoverY)) {
                    // Celda válida: verde XP translúcido
                    drawables.add(new HoverHighlightDrawable(hoverX, hoverY, new Color(50, 200, 100, 90)));

                    String path = "assets/ingame/Torre_Reposo.png"; // Default reposo
                    if (selectedType == 2) {
                        path = "assets/ingame/torremc_reposo.png"; // McAfee reposo
                    } else if (selectedType == 3) {
                        path = "assets/ingame/torre_area_reposo.png";
                    } else {
                        path = "assets/ingame/torrecomun4.png"; // Todas las demás usan torrecomun mirando abajo
                    }
                    drawables.add(new SelectionCursorDrawable(hoverX, hoverY, path));
                } else {
                    // Celda inválida: rojo XP translúcido
                    drawables.add(new HoverHighlightDrawable(hoverX, hoverY, new Color(240, 60, 60, 95)));
                }
            }
        }

        // 3. Dibujar torres
        drawables.addAll(juego.getTorres());

        // 4. Dibujar enemigos
        drawables.addAll(nivel.getEnemigosRestantes());

        // 5. Dibujar proyectiles (balas)
        drawables.addAll(juego.getBalas());

        // 6. Dibujar efectos visuales temporales (explosiones y rayos)
        int fxId = 0;
        for (Juego.VisualEffect fx : juego.getEfectosVisuales()) {
            drawables.add(new VisualEffectDrawable("fx-" + (fxId++), fx));
        }

        // 7. Dibujar tutorial de Clippy si está activo
        if (nivel.getNumeroNivel() == 1 && tutorial.isActive()) {
            drawables.add(new ClippyTutorialDrawable(
                    tutorial.getEstadoActual(),
                    tutorial.getLines(),
                    tutorial.getExpression(),
                    tutorial.getActualX(),
                    tutorial.getActualY()
            ));
        }

        // Dibujar a los personajes de la confrontación en la grilla si está activa (estilo Final Fantasy)
        if (nivel.getNumeroNivel() == 1 && nivel.verificarFinDeNivel()) {
            if (confrontation.getEstadoActual() != ClippyConfrontation.Estado.INACTIVE &&
                confrontation.getEstadoActual() != ClippyConfrontation.Estado.FINISHED) {
                
                if (confrontation.getEstadoActual() != ClippyConfrontation.Estado.WAITING_FOR_PRIZE) {
                    // Dibujar Clip Común en la grilla
                    drawables.add(new SecondClippyDrawable(confrontation.getClippyX(), confrontation.getClippyY(), false));
                    
                    // Dibujar al duplicado / clon corrupto en la grilla
                    float x = confrontation.getDuplicateX();
                    float y = confrontation.getDuplicateY();
                    boolean isCorrupt = confrontation.isCorrupt();
                    drawables.add(new SecondClippyDrawable(x, y, isCorrupt));
                }
            } else {
                drawables.add(new SecondClippyDrawable(24.90f, 4.92f, false));
            }
        }

        // Dibujar la confrontación cinemática si está activa
        if (confrontation.isActive()) {
            drawables.add(new ClippyConfrontationDrawable(confrontation));
        }



        // Traducir EstadoJuego de dominio a SessionState de MVC
        SessionState visualState = switch (juego.getEstado()) {
            case START -> SessionState.READY;
            case PLAYING -> SessionState.RUNNING;
            case GAME_OVER, VICTORY -> SessionState.FINISHED;
        };

        // Si pausamos el juego de forma intermedia
        if (sessionState == SessionState.PAUSED && visualState == SessionState.RUNNING) {
            visualState = SessionState.PAUSED;
        }

        return new TowerDefenseSnapshot(
                visualState,
                drawables,
                buildMenu(),
                currentStatus(),
                juego,
                lastClickedX,
                lastClickedY,
                tutorial.isActive()
        );
    }

    @Override
    public void update(float deltaSeconds) {
        if (sessionState == SessionState.PAUSED) {
            return;
        }

        // Actualizar el tutorial de Clippy si está activo
        if (juego.getNivelActual().getNumeroNivel() == 1 && tutorial.isActive()) {
            tutorial.update(deltaSeconds);
            // Si el tutorial está esperando a que el jugador coloque la primera torre
            // y el jugador ya colocó al menos una torre, avanzar al paso de éxito
            if (tutorial.getEstadoActual() == ClippyTutorial.Estado.WAIT_PLACE && !juego.getTorres().isEmpty()) {
                tutorial.setEstadoActual(ClippyTutorial.Estado.PLACE_SUCCESS);
                view.successMessage("¡Torre Antivirus colocada con éxito!");
            }
        }

        // Actualizar la confrontación cinemática si está activa
        if (confrontation.isActive()) {
            confrontation.update(deltaSeconds);

            // Auto-trigger del popup de premio al escapar el clon
            if (juego.getNivelActual().getNumeroNivel() == 1 &&
                confrontation.getEstadoActual() == ClippyConfrontation.Estado.WAITING_FOR_PRIZE &&
                !prizePopupOpened) {
                
                prizePopupOpened = true;
                view.showPrizePopup(() -> {
                    view.showPrizeResolutionPopup(() -> {
                        confrontation.setEstadoActual(ClippyConfrontation.Estado.TALK_POST_PRIZE);
                        confrontation.setDialogueIndex(63);
                        confrontation.setIconDropped(false);
                        com.miJuego.model.ProgresoJuego.mcafeeUnlocked = true;
                    });
                });
            }

            // Paneo de cámara hacia el centro de forma suave si no es la cinemática de primer plano
            if (!confrontation.isCinematicActive()) {
                float targetCamX = 11.45f;
                float targetCamY = 1.46f;
                
                // Si está escapando o esperando el premio, seguir al duplicado moviendo la cámara a la derecha
                if (confrontation.getEstadoActual() == ClippyConfrontation.Estado.ESCAPING ||
                    confrontation.getEstadoActual() == ClippyConfrontation.Estado.WAITING_FOR_PRIZE) {
                    targetCamX = 14.0f;
                }
                
                float currentCamX = CameraContext.getCameraX();
                float currentCamY = CameraContext.getCameraY();
                float lerpSpeed = 2.0f;
                CameraContext.setCameraX(currentCamX + (targetCamX - currentCamX) * lerpSpeed * deltaSeconds);
                CameraContext.setCameraY(currentCamY + (targetCamY - currentCamY) * lerpSpeed * deltaSeconds);
            }
        }

        juego.update(deltaSeconds);

        // Control de tiempo de expiración de selección de torre (10 segundos)
        if (juego.getSelectedTowerType() != 0) {
            int currentHoverX = com.miJuego.model.ActualTowerContext.getHoverX();
            int currentHoverY = com.miJuego.model.ActualTowerContext.getHoverY();
            if (currentHoverX != lastHoverX || currentHoverY != lastHoverY) {
                lastHoverX = currentHoverX;
                lastHoverY = currentHoverY;
                selectionTimeElapsed = 0.0f; // Resetear si el mouse se mueve
            } else {
                selectionTimeElapsed += deltaSeconds;
                if (selectionTimeElapsed >= 10.0f) {
                    clearTowerSelectionSilently();
                }
            }
        } else {
            selectionTimeElapsed = 0.0f;
        }

        // Control de nivel terminado
        if (juego.getEstado() == EstadoJuego.PLAYING) {
            if (juego.getNivelActual().verificarFinDeNivel()) {
                if (juego.getNivelActual().getNumeroNivel() == 1) {
                    if (confrontation.getEstadoActual() == ClippyConfrontation.Estado.INACTIVE) {
                        confrontation.initClippyPosition(tutorial.getActualX(), tutorial.getActualY());
                        tutorial.setActive(false);
                        stopMusic(); // Cortar la música principal
                        confrontation.setEstadoActual(ClippyConfrontation.Estado.TALK_INTRO);
                    } else if (confrontation.getEstadoActual() == ClippyConfrontation.Estado.FINISHED) {
                        handleLevelCompleted();
                    }
                } else {
                    handleLevelCompleted();
                }
            } else if (juego.getNivelActual().verificarFinDeOleada()) {
                // Avanzar a la siguiente oleada del mismo nivel
                juego.getNivelActual().prepararSiguienteOleada();
                view.successMessage("¡Oleada superada! Presione ENTER para iniciar la oleada " 
                    + juego.getNivelActual().getOleadaActual() + " de " 
                    + juego.getNivelActual().getMaximaOleadas() + ".");
            }
        }

        // Control de fin de juego para guardar score
        EstadoJuego estado = juego.getEstado();
        if ((estado == EstadoJuego.GAME_OVER || estado == EstadoJuego.VICTORY) && !scoreSaved) {
            scoreSaved = true;
            manejarFinDeJuego(estado);
        }
    }

    private boolean levelCompletedProcessed = false;

    private void handleLevelCompleted() {
        if (levelCompletedProcessed) return;
        levelCompletedProcessed = true;

        stopMusic();

        int completedLevel = juego.getNivelActual().getNumeroNivel();

        // Actualizar el progreso persistente de forma centralizada
        ProgresoJuego.unlockAfterLevel(completedLevel);

        // Iniciar la transición al hub escritorio XP
        // Esto hace fade-out sobre el nivel actual y luego muestra el hub
        view.showPostLevelHub(completedLevel);
    }

    private boolean isTowerLocked(int towerType) {
        return com.miJuego.model.ProgresoJuego.isTowerLocked(towerType, juego.getNivelActual().getNumeroNivel());
    }

    private void manejarFinDeJuego(EstadoJuego estado) {
        // Ejecutar en hilo de Swing para evitar problemas de concurrencia en JOptionPane
        java.awt.EventQueue.invokeLater(() -> {
            String mensaje = estado == EstadoJuego.VICTORY
                    ? "¡FELICITACIONES! Has ganado el juego. Ingrese su nombre para el TOP 10:"
                    : "Game Over. Has perdido. Ingrese su nombre para el TOP 10:";

            String name = JOptionPane.showInputDialog(null, mensaje, "Fin de Partida", JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.isBlank()) {
                name = "Jugador";
            }

            int finalScore = (int) juego.getJugador().getScore();
            Scoreboard.save(name, finalScore);

            // Mostrar scoreboard TOP 10
            List<Scoreboard.Entry> top10 = Scoreboard.load();
            StringBuilder sb = new StringBuilder("=== TOP 10 MEJORES JUGADORES ===\n\n");
            int rank = 1;
            for (Scoreboard.Entry entry : top10) {
                sb.append(rank).append(". ").append(entry.toString()).append("\n");
                rank++;
            }

            JOptionPane.showMessageDialog(null, sb.toString(), "Tabla de Posiciones", JOptionPane.INFORMATION_MESSAGE);

            // Reiniciar estado listo
            juego.setEstado(EstadoJuego.START);
            scoreSaved = false;
            stopMusic();
        });
    }

    private void avanzarTutorial() {
        ClippyTutorial.Estado est = tutorial.getEstadoActual();
        if (est == ClippyTutorial.Estado.INTRO_1) {
            tutorial.setEstadoActual(ClippyTutorial.Estado.INTRO_2);
        } else if (est == ClippyTutorial.Estado.INTRO_2) {
            // Forzar la selección de la torre antivirus (tipo 1) y avanzar
            juego.setSelectedTowerType(1);
            com.miJuego.model.ActualTowerContext.setNombreTorre("Común (Costo: 100)");
            tutorial.setEstadoActual(ClippyTutorial.Estado.WAIT_PLACE);
        } else if (est == ClippyTutorial.Estado.PLACE_SUCCESS) {
            tutorial.setEstadoActual(ClippyTutorial.Estado.WAIT_START_WAVE);
        } else if (est == ClippyTutorial.Estado.OUTRO) {
            tutorial.setEstadoActual(ClippyTutorial.Estado.COMPLETED);
            juego.getNivelActual().setSpawnPaused(false);
            sessionState = SessionState.RUNNING;
            view.successMessage("¡Que comience la defensa! Las ventanas emergentes se aproximan.");
            
            if (bgMusic == null && juego.getNivelActual().getNumeroNivel() == 1) {
                startMusic();
            }
        }
    }

    @Override
    public void dispatch(GameInput input) {
        // Atajos para saltear nivel o cinemática
        if (input.getKind() == InputKind.KEY_PRESSED) {
            String key = input.getKeyCode().orElse("");
            if (key.equalsIgnoreCase("K") && juego.getNivelActual().getNumeroNivel() == 1 && !confrontation.isActive()) {
                tutorial.setActive(false);
                juego.getNivelActual().getEnemigosRestantes().clear();
                confrontation.initClippyPosition(tutorial.getActualX(), tutorial.getActualY());
                stopMusic(); // Cortar la música principal
                confrontation.setEstadoActual(ClippyConfrontation.Estado.TALK_INTRO);
                view.successMessage("Nivel salteado: Iniciando confrontación.");
                return;
            }
            if (key.equalsIgnoreCase("L") && confrontation.isActive()) {
                confrontation.setEstadoActual(ClippyConfrontation.Estado.FINISHED);
                com.miJuego.model.ProgresoJuego.mcafeeUnlocked = true;
                handleLevelCompleted();
                view.successMessage("Cinemática salteada: Nivel completado.");
                return;
            }
        }

        // Si está esperando que se abra/cierre el popup de adware
        if (juego.getNivelActual().getNumeroNivel() == 1 && confrontation.getEstadoActual() == ClippyConfrontation.Estado.WAITING_FOR_PRIZE) {
            return; // Bloquea interacción del nivel ordinaria mientras la ventana emergente esté activa
        }

        // Interceptar inputs durante la confrontación cinemática (fase de diálogos)
        if (confrontation.isActive()) {
            if (confrontation.isDialoguePhase()) {
                if (input.getKind() == InputKind.POINTER_DOWN) {
                    confrontation.avanzar();
                    return;
                }
                if (input.getKind() == InputKind.KEY_PRESSED) {
                    String key = input.getKeyCode().orElse("");
                    if (key.equalsIgnoreCase("Space") || key.equalsIgnoreCase("Space Bar") || key.equalsIgnoreCase("Enter")) {
                        confrontation.avanzar();
                        return;
                    }
                }
                if (input.getKind() == InputKind.ACTION) {
                    String actionId = input.getActionId().orElse("");
                    if (actionId.equals(GameCommands.PAUSE) || actionId.equals(GameCommands.START)) {
                        confrontation.avanzar();
                        return;
                    }
                }
            }
            return; // Bloquear cualquier otro input mientras la cinemática/escape esté activo
        }

        // Intercepción del tutorial interactivo de Clippy
        if (juego.getNivelActual().getNumeroNivel() == 1 && tutorial.isActive()) {
            ClippyTutorial.Estado est = tutorial.getEstadoActual();
            boolean esConversacional = (est == ClippyTutorial.Estado.INTRO_1 ||
                                        est == ClippyTutorial.Estado.INTRO_2 ||
                                        est == ClippyTutorial.Estado.PLACE_SUCCESS ||
                                        est == ClippyTutorial.Estado.OUTRO);

            if (esConversacional) {
                if (input.getKind() == InputKind.KEY_PRESSED) {
                    String key = input.getKeyCode().orElse("");
                    if (key.equalsIgnoreCase("Space") || key.equalsIgnoreCase("Space Bar") || key.equalsIgnoreCase("Enter")) {
                        avanzarTutorial();
                        return;
                    }
                } else if (input.getKind() == InputKind.ACTION) {
                    String actionId = input.getActionId().orElse("");
                    if (actionId.equals(GameCommands.PAUSE) || actionId.equals(GameCommands.START)) {
                        avanzarTutorial();
                        return;
                    }
                } else if (input.getKind() == InputKind.POINTER_DOWN) {
                    avanzarTutorial();
                    return;
                }
            }

            // Bloquear acciones de inicio de oleada si no está listo
            if (input.getKind() == InputKind.ACTION && input.getActionId().orElse("").equals(GameCommands.START)) {
                if (est == ClippyTutorial.Estado.WAIT_START_WAVE) {
                    tutorial.setEstadoActual(ClippyTutorial.Estado.OUTRO);
                } else if (est != ClippyTutorial.Estado.OUTRO) {
                    view.errorMessage("Primero debes seguir las instrucciones de Clippy.");
                }
                return;
            }

            if (input.getKind() == InputKind.KEY_PRESSED) {
                String key = input.getKeyCode().orElse("");
                if (key.equalsIgnoreCase("Enter")) {
                    if (est == ClippyTutorial.Estado.WAIT_START_WAVE) {
                        tutorial.setEstadoActual(ClippyTutorial.Estado.OUTRO);
                    } else if (est != ClippyTutorial.Estado.OUTRO) {
                        view.errorMessage("Primero debes seguir las instrucciones de Clippy.");
                    }
                    return;
                }
            }
        }

        if (input.getKind() == InputKind.ACTION) {
            handleAction(input.getActionId().orElse(""));
            return;
        }

        if (input.getKind() == InputKind.POINTER_DOWN) {
            selectionTimeElapsed = 0.0f; // Reset por click
            float wx = input.getX().orElse(0f);
            float wy = input.getY().orElse(0f);
            int ix = (int) wx;
            int iy = (int) wy;

            if (torreEsperandoCoordenadas != null) {
                torreEsperandoCoordenadas.setTargetCoordinates(wx, wy);
                view.successMessage("Blanco fijado en coordenadas: " + wx + ", " + wy);
                torreEsperandoCoordenadas = null;
                return;
            }

            if (ix >= 0 && ix < getWorldWidth() && iy >= 0 && iy < getWorldHeight()) {
                long currentTime = System.currentTimeMillis();
                boolean isDoubleClick = (ix == lastClickX && iy == lastClickY && (currentTime - lastClickTime) < 400);

                lastClickedX = ix;
                lastClickedY = iy;
                lastClickX = ix;
                lastClickY = iy;
                lastClickTime = currentTime;

                // Si la partida está corriendo, intentamos colocar o seleccionar torre
                if (juego.getEstado() == EstadoJuego.PLAYING) {
                    try {
                        // Buscar si hay torre existente para seleccionar
                        boolean seleccionada = false;
                        for (Torre t : juego.getTorres()) {
                            if (Math.round(t.getX()) == ix && Math.round(t.getY()) == iy) {
                                view.successMessage("Torre " + t.getTowertype() + " (Lvl " + t.getNivelMejora() + ") seleccionada. [U] Mejorar, [S] Vender.");
                                seleccionada = true;
                                break;
                            }
                        }

                        if (!seleccionada) {
                            if (isDoubleClick) {
                                // Intentar colocar torre del tipo seleccionado
                                juego.placeTower(ix, iy);
                                view.successMessage("Torre colocada con éxito!");
                                // Desaparecer la casilla de selección azul tras colocación exitosa
                                lastClickedX = -1;
                                lastClickedY = -1;
                                // Limpiar el estado de click anterior para no encadenar triples clicks
                                lastClickX = -1;
                                lastClickY = -1;
                                lastClickTime = 0;
                            } else {
                                view.successMessage("Celda seleccionada. Haz doble click para colocar la torre.");
                            }
                        }
                    } catch (IllegalStateException | IllegalArgumentException ex) {
                        // Reenviar a la vista de error
                        view.errorMessage(ex.getMessage());
                    }
                }
            }
            return;
        }

        if (input.getKind() == InputKind.KEY_PRESSED) {
            String key = input.getKeyCode().orElse("");
            handleKeyPress(key);
        }
    }

    public void handleAction(String actionId) {
        if (sessionState == SessionState.FINISHED) {
            if (actionId.equals(GameCommands.RESTART)) {
                juego.restart();
                juego.setEstado(EstadoJuego.PLAYING);
                juego.getNivelActual().iniciarOleada();
                sessionState = SessionState.RUNNING;
                scoreSaved = false;
                view.successMessage("Partida reiniciada");
            } else if (actionId.equals(GameCommands.START)) {
                juego.restart();
                juego.setEstado(EstadoJuego.PLAYING);
                juego.getNivelActual().iniciarOleada();
                sessionState = SessionState.RUNNING;
                scoreSaved = false;
                view.successMessage("Partida reiniciada");
            }
            return;
        }

        switch (actionId) {
            case GameCommands.START -> {
                if (juego.getEstado() == EstadoJuego.PLAYING) {
                    if (juego.getNivelActual().isSpawnPaused()) {
                        juego.getNivelActual().iniciarOleada();
                        sessionState = SessionState.RUNNING;
                        view.successMessage("¡Oleada " + juego.getNivelActual().getOleadaActual() + " Iniciada! Teclas 1-8 seleccionan torres.");
                        
                        if (bgMusic == null && juego.getNivelActual().getNumeroNivel() == 1) {
                            startMusic();
                        }
                    }
                } else {
                    juego.restart();
                    juego.setEstado(EstadoJuego.PLAYING);
                    juego.getNivelActual().iniciarOleada();
                    sessionState = SessionState.RUNNING;
                    scoreSaved = false;

                    // --- NUEVO: Seteamos la torre inicial en el HUD apenas arranca ---
                    com.miJuego.model.ActualTowerContext.setNombreTorre("Común (Costo: 100)");

                    view.successMessage("¡Oleada 1 Iniciada! Teclas 1-8 seleccionan torres.");
                    
                    if (bgMusic == null && juego.getNivelActual().getNumeroNivel() == 1) {
                        startMusic();
                    }
                }
            }
            case GameCommands.PAUSE -> {
                if (sessionState == SessionState.RUNNING) {
                    sessionState = SessionState.PAUSED;
                    view.successMessage("Juego Pausado");
                } else if (sessionState == SessionState.PAUSED) {
                    sessionState = SessionState.RUNNING;
                    view.successMessage("Juego Reanudado");
                }
            }
            case GameCommands.RESTART -> {
                juego.restart();
                prizePopupOpened = false;
                // Si es el nivel 1, volvemos a pausar spawn e iniciar el tutorial
                if (juego.getNivelActual().getNumeroNivel() == 1) {
                    juego.setEstado(EstadoJuego.PLAYING);
                    juego.getNivelActual().setSpawnPaused(true);
                    tutorial.setEstadoActual(ClippyTutorial.Estado.INTRO_1);
                    tutorial.setActive(true);
                } else {
                    juego.setEstado(EstadoJuego.PLAYING);
                    juego.getNivelActual().iniciarOleada();
                }
                sessionState = SessionState.RUNNING;
                scoreSaved = false;
                view.successMessage("Partida reiniciada");
            }
            case GameCommands.RESUME -> {
                sessionState = SessionState.RUNNING;
                view.successMessage("Juego Reanudado");
            }
            // Teclas 1-7 del row superior llegan como ACTION porque están bindeadas
            case "1" -> selectTowerBySlot(0);
            case "2" -> selectTowerBySlot(1);
            case "3" -> selectTowerBySlot(2);
            case "4" -> selectTowerBySlot(3);
            case "5" -> selectTowerBySlot(4);
            case "6" -> selectTowerBySlot(5);
            case "7" -> selectTowerBySlot(6);
            case "8" -> selectTowerBySlot(7);
            case "0" -> clearTowerSelection();
            // U y S también están bindeadas
            case "U" -> {
                if (lastClickedX >= 0 && lastClickedY >= 0) {
                    try {
                        juego.upgradeTowerAt(lastClickedX, lastClickedY);
                        view.successMessage("¡Torre mejorada con éxito!");
                    } catch (IllegalStateException ex) {
                        view.errorMessage(ex.getMessage());
                    }
                }
            }
            case "S" -> {
                if (lastClickedX >= 0 && lastClickedY >= 0) {
                    try {
                        juego.sellTowerAt(lastClickedX, lastClickedY);
                        view.successMessage("Torre vendida con éxito.");
                        lastClickedX = -1;
                        lastClickedY = -1;
                    } catch (IllegalStateException ex) {
                        view.errorMessage(ex.getMessage());
                    }
                }
            }
            case "N" -> {
                if (juego.getNivelActual().verificarFinDeNivel()) {
                    if (juego.getNivelActual().getNumeroNivel() == 1) {
                        view.errorMessage("No hay más niveles desbloqueados automáticamente. Regrese al menú principal.");
                    } else {
                        juego.nextLevel();
                        view.successMessage("¡Nivel " + juego.getNivelActual().getNumeroNivel() + " iniciado!");
                    }
                } else {
                    view.errorMessage("Aún quedan enemigos en este nivel.");
                }
            }
            case "ZOOM_IN" -> {
                com.miJuego.model.CameraContext.zoomIn();
                view.successMessage("Zoom In: Viewport " + (int)com.miJuego.model.CameraContext.VIEWPORT_W + "x" + (int)com.miJuego.model.CameraContext.VIEWPORT_H);
            }
            case "ZOOM_OUT" -> {
                com.miJuego.model.CameraContext.zoomOut();
                view.successMessage("Zoom Out: Viewport " + (int)com.miJuego.model.CameraContext.VIEWPORT_W + "x" + (int)com.miJuego.model.CameraContext.VIEWPORT_H);
            }
            // ── Navegación de cámara (flechas del teclado) ───────────────────
            case "CAM_LEFT"  -> com.miJuego.model.CameraContext.moveLeft();
            case "CAM_RIGHT" -> com.miJuego.model.CameraContext.moveRight();
            case "CAM_UP"    -> com.miJuego.model.CameraContext.moveUp();
            case "CAM_DOWN"  -> com.miJuego.model.CameraContext.moveDown();
            default -> {
            }
        }
    }

    private void handleKeyPress(String key) {
        if (juego.getEstado() != EstadoJuego.PLAYING) {
            if (key.equals("R") || key.equals("r") || key.equals("Start")) {
                handleAction(GameCommands.START);
            }
            return;
        }

        // Selección de tipo de torre con 1-7 (mapeado a slots visuales)

        // Selección de tipo de torre con 1-7
        // Selección de tipo de torre ampliada
        switch (key) {
            case "1", "NumPad-1", "End"       -> selectTowerBySlot(0);
            case "2", "NumPad-2"              -> selectTowerBySlot(1);
            case "3", "NumPad-3", "PageDown"  -> selectTowerBySlot(2);
            case "4", "NumPad-4"              -> selectTowerBySlot(3);
            case "5", "NumPad-5", "Clear"     -> selectTowerBySlot(4);
            case "6", "NumPad-6"              -> selectTowerBySlot(5);
            case "7", "NumPad-7", "Home"      -> selectTowerBySlot(6);
            case "8", "NumPad-8"              -> selectTowerBySlot(7);
            case "0", "NumPad-0", "Insert"    -> clearTowerSelection();

            // ── Navegación de cámara con flechitas ───────────────────────────────
            case "CAM_LEFT",  "Left"  -> com.miJuego.model.CameraContext.moveLeft();
            case "CAM_RIGHT", "Right" -> com.miJuego.model.CameraContext.moveRight();
            case "CAM_UP",    "Up"    -> com.miJuego.model.CameraContext.moveUp();
            case "CAM_DOWN",  "Down"  -> com.miJuego.model.CameraContext.moveDown();
            
            // Acciones sobre torre seleccionada
            case "U", "u" -> {
                if (lastClickedX >= 0 && lastClickedY >= 0) {
                    try {
                        juego.upgradeTowerAt(lastClickedX, lastClickedY);
                        view.successMessage("¡Torre mejorada con éxito!");
                    } catch (IllegalStateException ex) {
                        view.errorMessage(ex.getMessage());
                    }
                }
            }
            case "S", "s" -> {
                if (lastClickedX >= 0 && lastClickedY >= 0) {
                    try {
                        juego.sellTowerAt(lastClickedX, lastClickedY);
                        view.successMessage("Torre vendida con éxito.");
                        lastClickedX = -1;
                        lastClickedY = -1;
                    } catch (IllegalStateException ex) {
                        view.errorMessage(ex.getMessage());
                    }
                }
            }
            case "M", "m" -> {
                if (lastClickedX >= 0 && lastClickedY >= 0) {
                    for (Torre t : juego.getTorres()) {
                        if (Math.round(t.getX()) == lastClickedX && Math.round(t.getY()) == lastClickedY) {
                            if (t instanceof com.miJuego.model.TorreAvast avast) {
                                avast.setManualTargetingMode(!avast.isManualTargetingMode());
                                view.successMessage("Modo de Avast cambiado a: " + (avast.isManualTargetingMode() ? "Área" : "Enemigo"));
                            }
                            break;
                        }
                    }
                }
            }
            case "T", "t" -> {
                if (lastClickedX >= 0 && lastClickedY >= 0) {
                    for (Torre t : juego.getTorres()) {
                        if (Math.round(t.getX()) == lastClickedX && Math.round(t.getY()) == lastClickedY) {
                            if (t instanceof com.miJuego.model.TorreAvast avast && avast.isManualTargetingMode()) {
                                torreEsperandoCoordenadas = avast;
                                view.successMessage("Avast: Haz click en el mapa para fijar el blanco.");
                            }
                            break;
                        }
                    }
                }
            }

            // Pasar de nivel
            case "N", "n" -> {
                if (juego.getNivelActual().verificarFinDeNivel()) {
                    if (juego.getNivelActual().getNumeroNivel() == 1) {
                        view.errorMessage("No hay más niveles desbloqueados automáticamente. Regrese al menú principal.");
                    } else {
                        juego.nextLevel();
                        view.successMessage("¡Nivel " + juego.getNivelActual().getNumeroNivel() + " iniciado!");
                    }
                } else {
                    view.errorMessage("Aún quedan enemigos en este nivel.");
                }
            }
            default -> {
            }
        }
    }

    private void selectTowerBySlot(int slotIndex) {
        int type = com.miJuego.model.ProgresoJuego.getTowerTypeForSlot(slotIndex);
        if (isTowerLocked(type)) {
            view.errorMessage("Esta torre está bloqueada en el nivel " + juego.getNivelActual().getNumeroNivel() + ".");
            return;
        }
        String description = switch (type) {
            case 1 -> "Común (Costo: 100)";
            case 2 -> "McAfee (Costo: 80)";
            case 3 -> "de Área (Costo: 150)";
            case 4 -> "Cañón (Costo: 200)";
            case 5 -> "Fuerte (Costo: 250)";
            case 6 -> "de Fuego (Costo: 180)";
            case 7 -> "de Hielo (Costo: 175)";
            case 8 -> "Eléctrica (Costo: 220)";
            default -> "Torre";
        };
        selectTower(type, description);
    }

    private void selectTower(int type, String description) {
        juego.setSelectedTowerType(type);
        selectionTimeElapsed = 0.0f;
        // Limpiar casilla de selección azul al equipar/seleccionar una nueva torre de la tienda
        lastClickedX = -1;
        lastClickedY = -1;
        
        // --- CONEXIÓN CON EL HUD ---
        com.miJuego.model.ActualTowerContext.setNombreTorre(description);
        
        view.successMessage("Seleccionada: Torre " + description);
    }

    private void clearTowerSelection() {
        juego.setSelectedTowerType(0);
        selectionTimeElapsed = 0.0f;
        // Limpiar casilla de selección azul al cancelar selección
        lastClickedX = -1;
        lastClickedY = -1;
        com.miJuego.model.ActualTowerContext.setNombreTorre("Ninguna");
        view.successMessage("Selección cancelada");
    }

    private void clearTowerSelectionSilently() {
        juego.setSelectedTowerType(0);
        selectionTimeElapsed = 0.0f;
        // Limpiar casilla de selección azul silenciosamente
        lastClickedX = -1;
        lastClickedY = -1;
        com.miJuego.model.ActualTowerContext.setNombreTorre("Ninguna");
    }

    private Menu buildMenu() {
        List<Action> actions = new ArrayList<>();
        if (juego.getEstado() == EstadoJuego.START) {
            actions.add(new SimpleAction(GameCommands.START, "Iniciar Juego", true));
        } else if (sessionState == SessionState.RUNNING) {
            actions.add(new SimpleAction(GameCommands.PAUSE, "Pausar", true));
            actions.add(new SimpleAction(GameCommands.RESTART, "Reiniciar", true));
        } else if (sessionState == SessionState.PAUSED) {
            actions.add(new SimpleAction(GameCommands.RESUME, "Continuar", true));
            actions.add(new SimpleAction(GameCommands.RESTART, "Reiniciar", true));
        }
        return new SimpleMenu(juego.getEstado() == EstadoJuego.START ? "Menu" : "Pausa", actions);
    }

    private GameStatus currentStatus() {
        Jugador j = juego.getJugador();
        return new GameStatus() {
            @Override
            public int getScore() {
                return (int) j.getScore();
            }

            @Override
            public int getGold() {
                return j.getMoneda();
            }

            @Override
            public int getLives() {
                return j.getHealth();
            }
        };
    }

    /** Expone el Juego para herramientas de desarrollo (DevConsole, SandboxConsole). */
    public Juego getJuego() {
        return juego;
    }

    private void startMusic() {
        try {
            java.net.URL url = getClass().getResource("/assets/ingame/Level1_Word_Theme.mp3");
            if (url != null) {
                javax.sound.sampled.AudioInputStream in = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
                javax.sound.sampled.AudioFormat baseFormat = in.getFormat();
                javax.sound.sampled.AudioFormat decodedFormat = new javax.sound.sampled.AudioFormat(
                        javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );
                javax.sound.sampled.AudioInputStream din = javax.sound.sampled.AudioSystem.getAudioInputStream(decodedFormat, in);
                bgMusic = javax.sound.sampled.AudioSystem.getClip();
                bgMusic.open(din);
                bgMusic.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if (bgMusic != null && bgMusic.isRunning()) {
            bgMusic.stop();
            bgMusic.close();
            bgMusic = null;
        }
    }

    // --- Drawables Auxiliares ---

    private static class PathTileDrawable implements Drawable {
        private final String id;
        private final float x;
        private final float y;

        public PathTileDrawable(String id, float x, float y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        @Override public String getId() { return id; }
        @Override public Float getX() { return x; }
        @Override public Float getY() { return y; }
        // Ligeramente mayor a 1.0f para cubrir los gaps de sub-pixel entre tiles
        @Override public Float getWidth()  { return 1.02f; }
        @Override public Float getHeight() { return 1.02f; }
        @Override public Optional<String> getImagePath() { return Optional.empty(); }
        @Override public Optional<URL> getImageUrl() { return Optional.empty(); }
        // Totalmente transparente — el camino ya está pintado en la imagen de fondo
        @Override public Color getFallbackColor() { return new Color(0, 0, 0, 0); }
        @Override public FallbackShape getFallbackShape() { return FallbackShape.RECTANGLE; }
        @Override public int getLayer() { return 0; }
    }

    private static class HoverHighlightDrawable implements Drawable {
        private final float x, y;
        private final Color color;

        public HoverHighlightDrawable(float x, float y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        @Override public String getId() { return "hover-highlight"; }
        @Override public Float getX() { return x; }
        @Override public Float getY() { return y; }
        @Override public Float getWidth() { return 1.0f; }
        @Override public Float getHeight() { return 1.0f; }
        @Override public Optional<String> getImagePath() { return Optional.empty(); }
        @Override public Optional<URL> getImageUrl() { return Optional.empty(); }
        @Override public Color getFallbackColor() { return color; }
        @Override public FallbackShape getFallbackShape() { return FallbackShape.RECTANGLE; }
        @Override public int getLayer() { return 10; } // Justo debajo del cursor fantasma (layer 11)
    }

    private static class SelectionHighlightDrawable implements Drawable {
        private final float x, y;

        public SelectionHighlightDrawable(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override public String getId() { return "selection-highlight"; }
        @Override public Float getX() { return x; }
        @Override public Float getY() { return y; }
        @Override public Float getWidth() { return 1.0f; }
        @Override public Float getHeight() { return 1.0f; }
        @Override public Optional<String> getImagePath() { return Optional.empty(); }
        @Override public Optional<URL> getImageUrl() { return Optional.empty(); }
        @Override public Color getFallbackColor() { return new Color(51, 153, 255, 90); } // Celeste Windows XP semitransparente
        @Override public FallbackShape getFallbackShape() { return FallbackShape.RECTANGLE; }
        @Override public int getLayer() { return 12; } // Encima de todo para marcar claramente la celda seleccionada
    }

    private static class SelectionCursorDrawable implements Drawable {
        private final float x, y;
        private final String imagePath;

        public SelectionCursorDrawable(float x, float y, String imagePath) {
            this.x = x;
            this.y = y;
            this.imagePath = imagePath;
        }

        @Override
        public String getId() { return "cursor"; }
        @Override
        public Float getX() { return x; }
        @Override
        public Float getY() { return y; }
        @Override
        public Float getWidth() { return 2.0f; }
        @Override
        public Float getHeight() { return 2.0f; }
        @Override
        public Optional<String> getImagePath() { return Optional.ofNullable(imagePath); }
        @Override
        public Optional<URL> getImageUrl() { return Optional.empty(); }
        @Override
        public Color getFallbackColor() { return new Color(255, 255, 255, 100); } // Borde blanco semitransparente
        @Override
        public FallbackShape getFallbackShape() { return FallbackShape.RECTANGLE; }
        @Override
        public int getLayer() { return 11; } // Dibujado encima de las torres colocadas (que tienen layer 10)
    }

    private static class VisualEffectDrawable implements Drawable {
        private final String id;
        private final Juego.VisualEffect fx;

        public VisualEffectDrawable(String id, Juego.VisualEffect fx) {
            this.id = id;
            this.fx = fx;
        }

        @Override
        public String getId() { return id; }
        @Override
        public Float getX() { return fx.x1 - fx.size / 2; }
        @Override
        public Float getY() { return fx.y1 - fx.size / 2; }
        @Override
        public Float getWidth() { return fx.size; }
        @Override
        public Float getHeight() { return fx.size; }
        @Override
        public Optional<String> getImagePath() { return Optional.empty(); }
        @Override
        public Optional<URL> getImageUrl() { return Optional.empty(); }
        @Override
        public Color getFallbackColor() {
            if ("explosion".equals(fx.type)) {
                return new Color(255, 69, 0, 180); // Naranja semitransparente
            }
            return new Color(0, 191, 255, 180); // Celeste eléctrico
        }
        @Override
        public FallbackShape getFallbackShape() { return FallbackShape.ELLIPSE; }
        @Override
        public int getLayer() { return 9; }
    }

    // --- Clases menú auxiliares ---

    private static class SimpleMenu implements Menu {
        private final String title;
        private final List<? extends Action> actions;

        public SimpleMenu(String title, List<? extends Action> actions) {
            this.title = title;
            this.actions = actions;
        }

        @Override
        public List<? extends Action> getActions() { return actions; }
        @Override
        public String getTitle() { return title; }
    }

    private static class SimpleAction implements Action {
        private final String id;
        private final String label;
        private final boolean enabled;

        public SimpleAction(String id, String label, boolean enabled) {
            this.id = id;
            this.label = label;
            this.enabled = enabled;
        }

        @Override
        public String getId() { return id; }
        @Override
        public String getLabel() { return label; }
        @Override
        public boolean isEnabled() { return enabled; }
        @Override
        public Optional<String> getImagePath() { return Optional.empty(); }
        @Override
        public Optional<URL> getImageUrl() { return Optional.empty(); }
    }

    private static class SimpleSnapshot implements FrameSnapshot {
        private final SessionState state;
        private final List<? extends Drawable> drawables;
        private final Menu menu;
        private final GameStatus status;

        public SimpleSnapshot(SessionState state, List<? extends Drawable> drawables, Menu menu, GameStatus status) {
            this.state = state;
            this.drawables = drawables;
            this.menu = menu;
            this.status = status;
        }

        @Override
        public SessionState getState() { return state; }
        @Override
        public Float getWorldWidth() { return com.miJuego.model.CameraContext.getWorldW(); }
        @Override
        public Float getWorldHeight() { return com.miJuego.model.CameraContext.getWorldH(); }
        @Override
        public List<? extends Drawable> getDrawables() { return drawables; }
        @Override
        public Menu getMenu() { return menu; }
        @Override
        public GameStatus getStatus() { return status; }
    }
}