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

    private long lastClickTime = 0;
    private int lastClickX = -1;
    private int lastClickY = -1;

    // Timeout de selección de torre
    private float selectionTimeElapsed = 0.0f;
    private int lastHoverX = -1;
    private int lastHoverY = -1;

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

        // 2. Dibujar el resaltado de la celda seleccionada (Cursor de click anterior)
        if (lastClickedX >= 0 && lastClickedX < 20 && lastClickedY >= 0 && lastClickedY < 15) {
            drawables.add(new SelectionHighlightDrawable(lastClickedX, lastClickedY));
        }

        // 3. Dibujar previsualización fantasma (Ghost) de la torre equipada al hacer hover con el mouse
        int hoverX = com.miJuego.model.ActualTowerContext.getHoverX();
        int hoverY = com.miJuego.model.ActualTowerContext.getHoverY();
        if (hoverX >= 0 && hoverX < 20 && hoverY >= 0 && hoverY < 15 && juego.getEstado() == EstadoJuego.PLAYING) {
            boolean onPath = nivel.intersectsPath(hoverX, hoverY);
            boolean hasTower = false;
            for (Torre t : juego.getTorres()) {
                if (Math.round(t.getX()) == hoverX && Math.round(t.getY()) == hoverY) {
                    hasTower = true;
                    break;
                }
            }
            if (!onPath && !hasTower) {
                int selectedType = juego.getSelectedTowerType();
                if (selectedType != 0) {
                    String path = "assets/ingame/Torre_Reposo.png"; // Default reposo
                    if (selectedType == 8) {
                        path = "assets/ingame/torremc_reposo.png"; // McAfee reposo
                    } else {
                        path = "assets/ingame/torrecomun4.png"; // Todas las demás usan torrecomun mirando abajo
                    }
                    drawables.add(new SelectionCursorDrawable(hoverX, hoverY, path));
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
                lastClickedY
        );
    }

    @Override
    public void update(float deltaSeconds) {
        if (sessionState == SessionState.PAUSED) {
            return;
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
            selectionTimeElapsed = 0.0f; // Reset por click
            float wx = input.getX().orElse(0f);
            float wy = input.getY().orElse(0f);
            int ix = (int) wx;
            int iy = (int) wy;

            if (ix >= 0 && ix < 20 && iy >= 0 && iy < 15) {
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

    private void handleAction(String actionId) {
        switch (actionId) {
            case GameCommands.START -> {
                juego.restart();
                juego.setEstado(EstadoJuego.PLAYING);
                juego.getNivelActual().iniciarOleada();
                sessionState = SessionState.RUNNING;
                scoreSaved = false;

                // --- NUEVO: Seteamos la torre inicial en el HUD apenas arranca ---
                com.miJuego.model.ActualTowerContext.setNombreTorre("Común (Costo: 100)");

                view.successMessage("¡Oleada 1 Iniciada! Teclas 1-8 seleccionan torres.");
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
            // Teclas 1-7 del row superior llegan como ACTION porque están bindeadas
            case "1" -> selectTower(1, "Común (Costo: 100)");
            case "2" -> selectTower(2, "de Área (Costo: 150)");
            case "3" -> selectTower(3, "Cañón (Costo: 200)");
            case "4" -> selectTower(4, "Fuerte (Costo: 250)");
            case "5" -> selectTower(5, "de Fuego (Costo: 180)");
            case "6" -> selectTower(6, "de Hielo (Costo: 150)");
            case "7" -> selectTower(7, "Eléctrica (Costo: 220)");
            case "8" -> selectTower(8, "McAfee (Costo: 175)");
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

    private void handleKeyPress(String key) {
        if (juego.getEstado() != EstadoJuego.PLAYING) {
            if (key.equals("R") || key.equals("r") || key.equals("Start")) {
                handleAction(GameCommands.START);
            }
            return;
        }

        // Selección de tipo de torre con 1-7
        // Selección de tipo de torre ampliada
        switch (key) {
            case "1", "NumPad-1", "End" -> selectTower(1, "Común (Costo: 100)");
            case "2", "NumPad-2", "Down" -> selectTower(2, "de Área (Costo: 150)");
            case "3", "NumPad-3", "Page Down" -> selectTower(3, "Cañón (Costo: 200)");
            case "4", "NumPad-4", "Left" ->  selectTower(4, "Fuerte (Costo: 250)");
            case "5", "NumPad-5", "Clear" -> selectTower(5, "de Fuego (Costo: 180)");
            case "6", "NumPad-6", "Right" -> selectTower(6, "de Hielo (Costo: 150)");
            case "7", "NumPad-7", "Home" -> selectTower(7, "Eléctrica (Costo: 220)");
            case "8", "NumPad-8", "Up" -> selectTower(8, "McAfee (Costo: 175)");
            case "0", "NumPad-0", "Insert" -> clearTowerSelection();
            
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
        selectionTimeElapsed = 0.0f;
        
        // --- CONEXIÓN CON EL HUD ---
        com.miJuego.model.ActualTowerContext.setNombreTorre(description);
        
        view.successMessage("Seleccionada: Torre " + description);
    }

    private void clearTowerSelection() {
        juego.setSelectedTowerType(0);
        selectionTimeElapsed = 0.0f;
        com.miJuego.model.ActualTowerContext.setNombreTorre("Ninguna");
        view.successMessage("Selección cancelada");
    }

    private void clearTowerSelectionSilently() {
        juego.setSelectedTowerType(0);
        selectionTimeElapsed = 0.0f;
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