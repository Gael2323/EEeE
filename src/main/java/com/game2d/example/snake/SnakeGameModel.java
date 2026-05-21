package com.game2d.example.snake;

import com.game2d.controller.GameCommands;
import com.game2d.model.Action;
import com.game2d.model.Drawable;
import com.game2d.model.FallbackShape;
import com.game2d.model.FrameSnapshot;
import com.game2d.model.GameInput;
import com.game2d.model.GameStatus;
import com.game2d.model.GameModel;
import com.game2d.model.InputKind;
import com.game2d.model.Menu;
import com.game2d.model.Renderable;
import com.game2d.model.SessionState;
import com.game2d.view.GameView;

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Snake de ejemplo para ver el plug-and-play con MVC.
 * Los alumnos pueden leer este código como referencia; su entrega va en su propio paquete.
 */
public final class SnakeGameModel implements GameModel {

    private static final float WORLD_SIZE = 20f;
    private static final float CELL_SIZE = 1f;
    private static final float SECONDS_PER_STEP = 0.18f;

    private final GameView view;
    private final Random random = new Random();
    private final LinkedList<int[]> snake = new LinkedList<>();

    private SessionState state = SessionState.RUNNING;
    private int directionX = 1;
    private int directionY = 0;
    private int pendingDirectionX = 1;
    private int pendingDirectionY = 0;
    private int[] food;
    private int score;
    private float moveTimer;

    public SnakeGameModel(GameView view) {
        this.view = view;
        resetGame();
        state = SessionState.RUNNING;
    }

    @Override
    public FrameSnapshot capture() {
        List<Drawable> drawables = new ArrayList<>();
        int index = 0;
        for (int[] segment : snake) {
            boolean head = index == 0;
            drawables.add(new CellDrawable(
                    "snake-" + index,
                    segment[0],
                    segment[1],
                    head ? new Color(0, 100, 0) : new Color(80, 200, 80)));
            index++;
        }
        if (food != null) {
            drawables.add(new CellDrawable("food", food[0], food[1], Color.RED));
        }
        return new SimpleSnapshot(state, drawables, buildMenu(), currentStatus());
    }

    @Override
    public void update(float deltaSeconds) {
        if (state != SessionState.RUNNING) {
            return;
        }
        directionX = pendingDirectionX;
        directionY = pendingDirectionY;

        moveTimer += deltaSeconds;
        if (moveTimer < SECONDS_PER_STEP) {
            return;
        }
        moveTimer = 0f;
        step();
    }

    @Override
    public void dispatch(GameInput input) {
        if (input.getKind() == InputKind.ACTION) {
            handleAction(input.getActionId().orElse(""));
            return;
        }
        if (input.getKind() == InputKind.KEY_PRESSED) {
            handleKey(input.getKeyCode().orElse(""));
        }
    }

    private void handleAction(String actionId) {
        switch (actionId) {
            case GameCommands.START -> startGame();
            case GameCommands.RESUME -> resumeGame();
            case GameCommands.PAUSE -> togglePause();
            case GameCommands.RESTART -> restartGame();
            case GameCommands.MOVE_UP -> queueDirection(0, -1);
            case GameCommands.MOVE_DOWN -> queueDirection(0, 1);
            case GameCommands.MOVE_LEFT -> queueDirection(-1, 0);
            case GameCommands.MOVE_RIGHT -> queueDirection(1, 0);
            default -> {
            }
        }
    }

    private void handleKey(String key) {
        switch (key) {
            case "Up" -> queueDirection(0, -1);
            case "Down" -> queueDirection(0, 1);
            case "Left" -> queueDirection(-1, 0);
            case "Right" -> queueDirection(1, 0);
            default -> {
            }
        }
    }

    private void startGame() {
        resumeGame();
    }

    private void resumeGame() {
        if (state == SessionState.PAUSED) {
            state = SessionState.RUNNING;
        }
    }

    private void togglePause() {
        if (state == SessionState.RUNNING) {
            state = SessionState.PAUSED;
        } else if (state == SessionState.PAUSED) {
            state = SessionState.RUNNING;
        }
    }

    private void restartGame() {
        resetGame();
        state = SessionState.PAUSED;
        view.successMessage("Partida reiniciada · Puntaje: 0");
    }

    private void resetGame() {
        snake.clear();
        int mid = Math.round(WORLD_SIZE / 2f);
        snake.add(new int[] {mid - 1, mid});
        snake.add(new int[] {mid, mid});
        snake.add(new int[] {mid + 1, mid});
        directionX = 1;
        directionY = 0;
        pendingDirectionX = 1;
        pendingDirectionY = 0;
        score = 0;
        moveTimer = 0f;
        spawnFood();
    }

    private void step() {
        int[] head = snake.getFirst();
        int newX = wrap(head[0] + directionX);
        int newY = wrap(head[1] + directionY);

        snake.addFirst(new int[] {newX, newY});
        boolean ate = food != null && newX == food[0] && newY == food[1];
        if (ate) {
            score++;
            view.successMessage("+1 · Puntaje: " + score);
            spawnFood();
        } else {
            snake.removeLast();
        }
    }

    private int wrap(int coordinate) {
        int size = (int) WORLD_SIZE;
        return Math.floorMod(coordinate, size);
    }

    private void spawnFood() {
        int attempts = 0;
        do {
            food = new int[] {random.nextInt((int) WORLD_SIZE), random.nextInt((int) WORLD_SIZE)};
            attempts++;
        } while (isOnSnake(food[0], food[1]) && attempts < 500);
    }

    private boolean isOnSnake(int x, int y) {
        for (int[] segment : snake) {
            if (segment[0] == x && segment[1] == y) {
                return true;
            }
        }
        return false;
    }

    private void queueDirection(int dx, int dy) {
        if (state != SessionState.RUNNING) {
            return;
        }
        if (dx == -directionX && dy == -directionY) {
            return;
        }
        pendingDirectionX = dx;
        pendingDirectionY = dy;
    }

    private Menu buildMenu() {
        List<Action> actions = new ArrayList<>();
        if (state == SessionState.RUNNING) {
            actions.add(button(GameCommands.PAUSE, "Pausar", true));
        } else if (state == SessionState.PAUSED) {
            actions.add(button(GameCommands.RESUME, "Continuar", true));
            actions.add(button(GameCommands.RESTART, "Reiniciar", true));
        }
        String title = state == SessionState.PAUSED ? "Pausado" : "Snake";
        return new SimpleMenu(title, actions);
    }

    private GameStatus currentStatus() {
        return new SimpleGameStatus(score, -1, -1);
    }

    private static SimpleAction button(String id, String label, boolean enabled) {
        return new SimpleAction(id, label, enabled);
    }

    private static final class SimpleGameStatus implements GameStatus {

        private final int score;
        private final int gold;
        private final int lives;

        private SimpleGameStatus(int score, int gold, int lives) {
            this.score = score;
            this.gold = gold;
            this.lives = lives;
        }

        @Override
        public int getScore() {
            return score;
        }

        @Override
        public int getGold() {
            return gold;
        }

        @Override
        public int getLives() {
            return lives;
        }
    }

    private static final class SimpleSnapshot implements FrameSnapshot {

        private final SessionState state;
        private final List<? extends Drawable> drawables;
        private final Menu menu;
        private final GameStatus status;

        private SimpleSnapshot(SessionState state, List<? extends Drawable> drawables, Menu menu,
                               GameStatus status) {
            this.state = state;
            this.drawables = drawables;
            this.menu = menu;
            this.status = status;
        }

        @Override
        public SessionState getState() {
            return state;
        }

        @Override
        public Float getWorldWidth() {
            return WORLD_SIZE;
        }

        @Override
        public Float getWorldHeight() {
            return WORLD_SIZE;
        }

        @Override
        public List<? extends Drawable> getDrawables() {
            return drawables;
        }

        @Override
        public Menu getMenu() {
            return menu;
        }

        @Override
        public GameStatus getStatus() {
            return status;
        }
    }

    private static final class SimpleMenu implements Menu {

        private final String title;
        private final List<? extends Action> actions;

        private SimpleMenu(String title, List<? extends Action> actions) {
            this.title = title;
            this.actions = actions;
        }

        @Override
        public List<? extends Action> getActions() {
            return actions;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    private static final class SimpleAction implements Action {

        private final String id;
        private final String label;
        private final boolean enabled;

        private SimpleAction(String id, String label, boolean enabled) {
            this.id = id;
            this.label = label;
            this.enabled = enabled;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public Optional<String> getImagePath() {
            return Optional.empty();
        }

        @Override
        public Optional<URL> getImageUrl() {
            return Optional.empty();
        }
    }

    private static final class CellDrawable implements Drawable {

        private final String id;
        private final float x;
        private final float y;
        private final Color color;

        private CellDrawable(String id, float x, float y, Color color) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Float getX() {
            return x;
        }

        @Override
        public Float getY() {
            return y;
        }

        @Override
        public Float getWidth() {
            return CELL_SIZE;
        }

        @Override
        public Float getHeight() {
            return CELL_SIZE;
        }

        @Override
        public Optional<String> getImagePath() {
            return Optional.empty();
        }

        @Override
        public Optional<URL> getImageUrl() {
            return Optional.empty();
        }

        @Override
        public Color getFallbackColor() {
            return color;
        }

        @Override
        public FallbackShape getFallbackShape() {
            return FallbackShape.RECTANGLE;
        }
    }
}
