package com.miJuego.sandbox;

import com.game2d.controller.DefaultGameController;
import com.game2d.controller.GameCommands;
import com.game2d.model.GameModel;
import com.game2d.view.BackgroundSettings;
import com.game2d.view.GameView;
import com.game2d.view.GameViewMessages;
import com.game2d.view.GameViews;
import com.miJuego.model.TowerDefenseModel;

import java.awt.Color;
import java.awt.event.KeyEvent;

/**
 * Punto de entrada del modo <b>Dev Console</b>.
 *
 * <p>Arranca el Tower Defense completo y además abre una ventana flotante
 * de consola de desarrollador (estilo "~" de Counter-Strike) que permite
 * manipular el estado del juego en tiempo real.</p>
 *
 * <h2>Cómo correrlo</h2>
 * <p>Cambiar en {@code build.gradle}:</p>
 * <pre>
 *   application {
 *       mainClass = 'com.miJuego.sandbox.DevConsoleMain'
 *   }
 * </pre>
 * <p>y luego {@code ./gradlew run}. O ejecutar directamente desde el IDE.</p>
 *
 * <h2>Teclas del juego (igual que siempre)</h2>
 * <ul>
 *   <li>{@code 1-7} — seleccionar tipo de torre</li>
 *   <li>Click — colocar torre seleccionada</li>
 *   <li>{@code U} — mejorar torre, {@code S} — vender torre</li>
 *   <li>{@code N} — siguiente nivel, {@code P / Espacio} — pausar</li>
 *   <li>{@code R} — reiniciar, {@code Enter} — iniciar oleada</li>
 * </ul>
 *
 * <h2>Dev Console</h2>
 * <ul>
 *   <li>{@code ~} (tilde) — abrir/cerrar la consola de desarrollador</li>
 *   <li>{@code F1}        — alternativa para abrir/cerrar la consola</li>
 * </ul>
 */
public final class DevConsoleMain {

    /** Identificador de acción para toggle de la dev console. */
    private static final String ACTION_DEV_CONSOLE = "DEV_CONSOLE_TOGGLE";

    public static void main(String[] args) {
        // ── 1. Configurar fondo del juego ─────────────────────────────────────
        BackgroundSettings.getInstance().setFallbackColor(new Color(34, 139, 34));

        // ── 2. Inicializar vista y modelo ─────────────────────────────────────
        GameView view = GameViews.getInstance().getView();
        GameViewMessages.getInstance().bind(view);
        TowerDefenseModel model = new TowerDefenseModel(view);

        // ── 3. Crear la Dev Console (ventana flotante) ────────────────────────
        DevCommandExecutor executor = new DevCommandExecutor(model.getJuego());
        DevConsoleFrame console = new DevConsoleFrame(executor);

        // ── 4. Configurar el controller con todas las teclas del juego ────────
        DefaultGameController controller = new DefaultGameController();
        controller.getKeyCommands()
                // Torres
                .bind(KeyEvent.VK_1, "1")
                .bind(KeyEvent.VK_2, "2")
                .bind(KeyEvent.VK_3, "3")
                .bind(KeyEvent.VK_4, "4")
                .bind(KeyEvent.VK_5, "5")
                .bind(KeyEvent.VK_6, "6")
                .bind(KeyEvent.VK_7, "7")
                .bind(KeyEvent.VK_8, "8")
                .bind(KeyEvent.VK_0, "0")
                .bind(KeyEvent.VK_NUMPAD8, "8")
                .bind(KeyEvent.VK_NUMPAD0, "0")
                // Acciones sobre torre seleccionada
                .bind(KeyEvent.VK_U, "U")
                .bind(KeyEvent.VK_S, "S")
                // Navegación de nivel
                .bind(KeyEvent.VK_N, "N")
                // Control de juego
                .bind(KeyEvent.VK_P,     GameCommands.PAUSE)
                .bind(KeyEvent.VK_SPACE,  GameCommands.PAUSE)
                .bind(KeyEvent.VK_R,      GameCommands.RESTART)
                .bind(KeyEvent.VK_ENTER,  GameCommands.START)
                // ── Dev Console toggle ────────────────────────────────────────
                .bind(KeyEvent.VK_BACK_QUOTE, ACTION_DEV_CONSOLE)  // tecla ~
                .bind(KeyEvent.VK_F1,         ACTION_DEV_CONSOLE); // alternativa F1

        // ── 5. Interceptar el toggle de la consola en el modelo ───────────────
        //    Usamos un GameModel wrapper que captura la acción antes de delegarla.
        GameModel wrappedModel = new DevConsoleModelWrapper(model, console);

        // ── 6. Arrancar el controller ─────────────────────────────────────────
        controller.bind(wrappedModel, view);
        controller.start();

        // ── 7. La consola arranca oculta. Se abre con ~ o F1. ─────────────────
        // (No llamamos console.setVisible(true) aquí — el usuario la abre cuando quiere)
    }

    private DevConsoleMain() {}
}
