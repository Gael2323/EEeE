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
    private static final float WORLD_WIDTH = 20f;
    private static final float WORLD_HEIGHT = 15f;

    private final GameView view;
    private final Juego juego;
    private SessionState sessionState = SessionState.READY;
    private boolean scoreSaved = false;

    // Guardado de la última posición del cursor de click
    private int lastClickedX = -1;
    private int lastClickedY = -1;

    public TowerDefenseModel(GameView view) {
        this.view = view;
        this.juego = new Juego();
    }

    @Override
    public FrameSnapshot capture() {
        List<Drawable> drawables = new ArrayList<>();

        // 1. Dibujar el mapa completo (Pasto y Camino)
        Nivel nivel = juego.getNivelActual();
        for (int ix = 0; ix < (int) WORLD_WIDTH; ix++) {
            for (int iy = 0; iy < (int) WORLD_HEIGHT; iy++) {
                if (nivel.intersectsPath(ix, iy)) {
                    drawables.add(new PathTileDrawable("path-" + ix + "-" + iy, ix, iy));
                }
            }
        }

        // 2. Dibujar una celda seleccionada (Cursor de click anterior)
        if (lastClickedX >= 0 && lastClickedX < 20 && lastClickedY >= 0 && lastClickedY < 15) {
            drawables.add(new SelectionCursorDrawable(lastClickedX, lastClickedY));
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

        return new SimpleSnapshot(
                visualState, 
                drawables, 
                buildMenu(), 
                currentStatus()
        );
    }

    @Override
    public void update(float deltaSeconds) {
        if (sessionState == SessionState.PAUSED) {
            return;
        }

        juego.update(deltaSeconds);

        // Control de fin de juego para guardar score
        EstadoJuego estado = juego.getEstado();
        if ((estado == EstadoJuego.GAME_OVER || estado == EstadoJuego.VICTORY) && !scoreSaved) {
            scoreSaved = true;
            manejarFinDeJuego(estado);
        }
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
        });
    }

    @Override
    public void dispatch(GameInput input) {
        if (input.getKind() == InputKind.ACTION) {
            handleAction(input.getActionId().orElse(""));
            return;
        }

        if (input.getKind() == InputKind.POINTER_DOWN) {
            float wx = input.getX().orElse(0f);
            float wy = input.getY().orElse(0f);
            int ix = (int) wx;
            int iy = (int) wy;

            if (ix >= 0 && ix < 20 && iy >= 0 && iy < 15) {
                lastClickedX = ix;
                lastClickedY = iy;
                
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
                            // Intentar colocar torre del tipo seleccionado
                            juego.placeTower(ix, iy);
                            view.successMessage("Torre colocada con éxito!");
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

    private void handleAction(String actionId) {
        switch (actionId) {
            case GameCommands.START -> {
                juego.restart();
                juego.setEstado(EstadoJuego.PLAYING);
                juego.getNivelActual().iniciarOleada();
                sessionState = SessionState.RUNNING;
                scoreSaved = false;
                view.successMessage("¡Oleada 1 Iniciada! Teclas 1-7 seleccionan torres.");
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
                juego.setEstado(EstadoJuego.PLAYING);
                juego.getNivelActual().iniciarOleada();
                sessionState = SessionState.RUNNING;
                scoreSaved = false;
                view.successMessage("Partida reiniciada");
            }
            case GameCommands.RESUME -> {
                sessionState = SessionState.RUNNING;
                view.successMessage("Juego Reanudado");
            }
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

        // Selección de tipo de torre con 1-7
        switch (key) {
            case "1", "NumPad-1" -> selectTower(1, "Común (Costo: 100)");
            case "2", "NumPad-2" -> selectTower(2, "de Área (Costo: 150)");
            case "3", "NumPad-3" -> selectTower(3, "Cañón (Costo: 200)");
            case "4", "NumPad-4" -> selectTower(4, "Fuerte (Costo: 250)");
            case "5", "NumPad-5" -> selectTower(5, "de Fuego (Costo: 180)");
            case "6", "NumPad-6" -> selectTower(6, "de Hielo (Costo: 150)");
            case "7", "NumPad-7" -> selectTower(7, "Eléctrica (Costo: 220)");
            
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
            
            // Pasar de nivel
            case "N", "n" -> {
                if (juego.getNivelActual().verificarFinDeNivel()) {
                    juego.nextLevel();
                    view.successMessage("¡Nivel " + juego.getNivelActual().getNumeroNivel() + " iniciado!");
                } else {
                    view.errorMessage("Aún quedan enemigos en este nivel.");
                }
            }
            default -> {
            }
        }
    }

    private void selectTower(int type, String description) {
        juego.setSelectedTowerType(type);
        view.successMessage("Seleccionada: Torre " + description);
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

        @Override
        public String getId() { return id; }
        @Override
        public Float getX() { return x; }
        @Override
        public Float getY() { return y; }
        @Override
        public Float getWidth() { return 1.0f; }
        @Override
        public Float getHeight() { return 1.0f; }
        @Override
        public Optional<String> getImagePath() { return Optional.empty(); }
        @Override
        public Optional<URL> getImageUrl() { return Optional.empty(); }
        @Override
        public Color getFallbackColor() { return new Color(205, 175, 135); } // Color tierra/arena
        @Override
        public FallbackShape getFallbackShape() { return FallbackShape.RECTANGLE; }
        @Override
        public int getLayer() { return 0; } // Fondo de camino
    }

    private static class SelectionCursorDrawable implements Drawable {
        private final float x, y;

        public SelectionCursorDrawable(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String getId() { return "cursor"; }
        @Override
        public Float getX() { return x; }
        @Override
        public Float getY() { return y; }
        @Override
        public Float getWidth() { return 1.0f; }
        @Override
        public Float getHeight() { return 1.0f; }
        @Override
        public Optional<String> getImagePath() { return Optional.empty(); }
        @Override
        public Optional<URL> getImageUrl() { return Optional.empty(); }
        @Override
        public Color getFallbackColor() { return new Color(255, 255, 255, 100); } // Borde blanco semitransparente
        @Override
        public FallbackShape getFallbackShape() { return FallbackShape.RECTANGLE; }
        @Override
        public int getLayer() { return 2; }
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
        public Float getWorldWidth() { return WORLD_WIDTH; }
        @Override
        public Float getWorldHeight() { return WORLD_HEIGHT; }
        @Override
        public List<? extends Drawable> getDrawables() { return drawables; }
        @Override
        public Menu getMenu() { return menu; }
        @Override
        public GameStatus getStatus() { return status; }
    }
}
