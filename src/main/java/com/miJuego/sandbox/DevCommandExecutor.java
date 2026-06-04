package com.miJuego.sandbox;

import com.miJuego.model.*;

import java.util.List;

/**
 * Interpreta y ejecuta comandos de la Dev Console sobre el {@link Juego} activo.
 *
 * <p>Todos los métodos son {@code synchronized} sobre {@code juego} para evitar
 * condiciones de carrera con el game loop de ~60 FPS.</p>
 *
 * <p>Devuelve un String con el resultado del comando para que {@link DevConsoleFrame}
 * lo imprima en su área de log.</p>
 */
public class DevCommandExecutor {

    private final Juego juego;
    private boolean godMode = false;
    private double originalSpeed = 1.0; // referencia de velocidad base de los enemigos
    private static int idCounter = 1000;

    public DevCommandExecutor(Juego juego) {
        this.juego = juego;
    }

    /**
     * Parsea y ejecuta un comando. Devuelve el mensaje de resultado.
     */
    public String execute(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) return "";
        String[] parts = rawInput.trim().split("\\s+");
        String cmd = parts[0].toLowerCase();

        return switch (cmd) {
            case "help"     -> help();
            case "status"   -> status();
            case "give"     -> give(parts);
            case "god"      -> toggleGod();
            case "spawn"    -> spawn(parts);
            case "killall"  -> killAll();
            case "place"    -> place(parts);
            case "upgrade"  -> upgrade(parts);
            case "sell"     -> sell(parts);
            case "next"     -> nextLevel();
            case "level"    -> setLevel(parts);
            case "speed"    -> setSpeed(parts);
            case "pause"    -> pauseGame();
            case "resume"   -> resumeGame();
            case "restart"  -> restart();
            case "clear"    -> "CLEAR"; // señal especial para que DevConsoleFrame limpie el área
            default         -> "⚠  Comando desconocido: '" + cmd + "'. Escribe 'help'.";
        };
    }

    // ─── COMANDOS ────────────────────────────────────────────────────────────

    private String help() {
        return """
                ╔══════════════════════════════════════════════════════╗
                ║                 DEV CONSOLE — COMANDOS              ║
                ╠══════════════════════════════════════════════════════╣
                ║  RECURSOS                                            ║
                ║    give gold <N>           — suma N monedas          ║
                ║    give lives <N>          — suma N vidas            ║
                ║    god                     — toggle vidas infinitas  ║
                ║                                                      ║
                ║  ENEMIGOS                                            ║
                ║    spawn <tipo> [N]        — spawea enemigos         ║
                ║       tipos: duende | comun | comun escudo |         ║
                ║              multiple | popup | popup-premio |       ║
                ║              popup-descarga | boss                   ║
                ║    killall                 — elimina todos           ║
                ║                                                      ║
                ║  TORRES (sin costo)                                  ║
                ║    place <tipo> <X> <Y>    — coloca torre            ║
                ║       tipos: comun | area | cañon | fuerte |         ║
                ║              fuego | hielo | electrica | mcafee      ║
                ║    upgrade <X> <Y>         — mejora gratis           ║
                ║    sell <X> <Y>            — vende torre             ║
                ║                                                      ║
                ║  NIVEL                                               ║
                ║    next                    — siguiente nivel         ║
                ║    level <N>               — salta al nivel N (1-5)  ║
                ║                                                      ║
                ║  JUEGO                                               ║
                ║    speed <X>               — velocidad (ej: 0.5, 2)  ║
                ║    pause / resume                                    ║
                ║    restart                 — reinicia todo           ║
                ║    status                  — estado actual           ║
                ║    clear                   — limpia la consola       ║
                ╚══════════════════════════════════════════════════════╝""";
    }

    private String status() {
        synchronized (juego) {
            Jugador j = juego.getJugador();
            Nivel n   = juego.getNivelActual();
            return String.format(
                """
                ── Estado actual ──────────────────────────
                  Nivel:    %d        Estado: %s
                  Vidas:    %-6d    Oro:    %-6d
                  Score:    %-10.0f God:    %s
                  Torres:   %-6d    Enemigos activos: %d
                ───────────────────────────────────────────""",
                n.getNumeroNivel(), juego.getEstado(),
                j.getHealth(), j.getMoneda(),
                j.getScore(), godMode ? "ON 🟢" : "OFF",
                juego.getTorres().size(), n.getEnemigosRestantes().size()
            );
        }
    }

    private String give(String[] parts) {
        if (parts.length < 3) return "Uso: give gold <N>  |  give lives <N>";
        String recurso = parts[1].toLowerCase();
        int cantidad;
        try { cantidad = Integer.parseInt(parts[2]); }
        catch (NumberFormatException e) { return "⚠  Cantidad inválida."; }

        synchronized (juego) {
            return switch (recurso) {
                case "gold", "oro" -> {
                    juego.getJugador().addMoneda(cantidad);
                    yield String.format("✓ +%d monedas → Total: %d", cantidad, juego.getJugador().getMoneda());
                }
                case "lives", "vidas" -> {
                    juego.getJugador().setHealth(juego.getJugador().getHealth() + cantidad);
                    yield String.format("✓ +%d vidas → Total: %d", cantidad, juego.getJugador().getHealth());
                }
                default -> "⚠  Recurso desconocido. Usa: give gold N  |  give lives N";
            };
        }
    }

    private String toggleGod() {
        godMode = !godMode;
        if (godMode) {
            // Iniciamos un hilo que cada tick restaura las vidas si caen
            Thread t = new Thread(() -> {
                while (godMode) {
                    synchronized (juego) {
                        if (juego.getJugador().getHealth() < 5) {
                            juego.getJugador().setHealth(9999);
                        }
                    }
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                }
            }, "god-mode-thread");
            t.setDaemon(true);
            t.start();
            return "✓ God Mode ACTIVADO 🟢 — vidas infinitas";
        } else {
            return "✓ God Mode DESACTIVADO ⭕";
        }
    }

    private String spawn(String[] parts) {
        if (parts.length < 2) return "Uso: spawn <tipo> [N]  — tipos: duende | comun | comun escudo | multiple/idiot | popup | popup-premio | popup-descarga | boss";

        String tipo = parts[1].toLowerCase();
        int cantidad = 1;

        // "spawn comun 3 escudo" o "spawn comun escudo"
        int escudoIndex = -1;
        for (int i = 2; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("escudo")) { escudoIndex = i; break; }
        }
        boolean conEscudo = escudoIndex >= 0;

        // El número puede estar en parts[2] si no es "escudo"
        if (parts.length >= 3 && escudoIndex != 2) {
            try { cantidad = Integer.parseInt(parts[2]); }
            catch (NumberFormatException ignored) {}
        }

        // Soporte inteligente para sub-tipos de popup (error, premio, descarga)
        if (tipo.equals("popup")) {
            String var = "";
            for (int i = 2; i < parts.length; i++) {
                String p = parts[i].toLowerCase();
                if (p.equals("error") || p.equals("premio") || p.equals("descarga") || p.equals("prize") || p.equals("download")) {
                    var = p;
                    break;
                }
            }
            if (!var.isEmpty()) {
                if (var.equals("premio") || var.equals("prize")) tipo = "popup-premio";
                else if (var.equals("descarga") || var.equals("download")) tipo = "popup-descarga";
            }
        }

        synchronized (juego) {
            List<Enemigo> enemigos = juego.getNivelActual().getEnemigosRestantes();
            List<float[]> wps     = juego.getNivelActual().getWaypoints();
            float sx = wps.isEmpty() ? 0f : wps.get(0)[0];
            float sy = wps.isEmpty() ? 7f : wps.get(0)[1];

            int n = 0;
            for (int i = 0; i < cantidad; i++) {
                Enemigo e = buildEnemigo(tipo, conEscudo);
                if (e == null) return "⚠  Tipo desconocido: " + tipo + ". Tipos: duende | comun | multiple/idiot | popup | popup-premio | popup-descarga | boss";
                e.setPosicion(sx, sy);
                e.setWaypointIndex(wps.size() > 1 ? 1 : 0);
                enemigos.add(e);
                n++;
            }
            return String.format("✓ Spawneado %d x %s%s", n, tipo, conEscudo ? " (con escudo)" : "");
        }
    }

    private Enemigo buildEnemigo(String tipo, boolean escudo) {
        String id = "dev-" + tipo + "-" + (++idCounter);
        return switch (tipo) {
            case "duende"   -> new Duende(id);
            case "comun"    -> new EnemigoComun(id, escudo);
            case "multiple", "idiot", "you_are_an_idiot", "you-are-an-idiot" -> new EnemigoMultiple(id);
            case "popup", "popup-error" -> new PopUp(id, PopUp.Variante.ERROR);
            case "popup-premio"   -> new PopUp(id, PopUp.Variante.PREMIO);
            case "popup-descarga" -> new PopUp(id, PopUp.Variante.DESCARGA);
            case "boss"     -> {
                EnemigoComun boss = new EnemigoComun(id, escudo);
                boss.setVida(1000.0);
                boss.setRapidez(0.7);
                boss.setMonedasGeneradas(200.0);
                boss.setScoreGenerado(150.0);
                boss.setDañoBase(5.0);
                yield boss;
            }
            default -> null;
        };
    }

    private String killAll() {
        synchronized (juego) {
            int count = juego.getNivelActual().getEnemigosRestantes().size();
            juego.getNivelActual().getEnemigosRestantes().clear();
            return String.format("✓ %d enemigos eliminados", count);
        }
    }

    private String place(String[] parts) {
        if (parts.length < 4) return "Uso: place <tipo> X Y";
        String tipo = parts[1].toLowerCase();
        int x, y;
        try {
            x = Integer.parseInt(parts[2]);
            y = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            return "⚠  X e Y deben ser números enteros.";
        }

        int towerType = switch (tipo) {
            case "comun"     -> 1;
            case "area"      -> 2;
            case "cañon"     -> 3;
            case "fuerte"    -> 4;
            case "fuego"     -> 5;
            case "hielo"     -> 6;
            case "electrica" -> 7;
            case "mcafee"    -> 8;
            default          -> -1;
        };
        if (towerType == -1) return "⚠  Tipo desconocido. Tipos: comun|area|cañon|fuerte|fuego|hielo|electrica|mcafee";

        synchronized (juego) {
            int oroAntes = juego.getJugador().getMoneda();
            juego.getJugador().addMoneda(99999);
            juego.setSelectedTowerType(towerType);
            EstadoJuego estadoAntes = juego.getEstado();
            if (estadoAntes != EstadoJuego.PLAYING) juego.setEstado(EstadoJuego.PLAYING);
            try {
                juego.placeTower(x, y);
                juego.getJugador().setMoneda(oroAntes);
                if (estadoAntes != EstadoJuego.PLAYING) juego.setEstado(estadoAntes);
                return String.format("✓ Torre '%s' colocada en (%d, %d) [sin costo]", tipo, x, y);
            } catch (IllegalStateException | IllegalArgumentException ex) {
                juego.getJugador().setMoneda(oroAntes);
                if (estadoAntes != EstadoJuego.PLAYING) juego.setEstado(estadoAntes);
                return "⚠  " + ex.getMessage();
            }
        }
    }

    private String upgrade(String[] parts) {
        if (parts.length < 3) return "Uso: upgrade X Y";
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            synchronized (juego) {
                int oroAntes = juego.getJugador().getMoneda();
                juego.getJugador().addMoneda(99999);
                juego.upgradeTowerAt(x, y);
                juego.getJugador().setMoneda(oroAntes);
                return String.format("✓ Torre en (%d, %d) mejorada [sin costo]", x, y);
            }
        } catch (NumberFormatException e) {
            return "⚠  X e Y deben ser números enteros.";
        } catch (IllegalStateException ex) {
            return "⚠  " + ex.getMessage();
        }
    }

    private String sell(String[] parts) {
        if (parts.length < 3) return "Uso: sell X Y";
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            synchronized (juego) {
                juego.sellTowerAt(x, y);
                return String.format("✓ Torre en (%d, %d) vendida", x, y);
            }
        } catch (NumberFormatException e) {
            return "⚠  X e Y deben ser números enteros.";
        } catch (IllegalStateException ex) {
            return "⚠  " + ex.getMessage();
        }
    }

    private String nextLevel() {
        synchronized (juego) {
            int actual = juego.getNivelActual().getNumeroNivel();
            if (actual >= 5) return "⚠  Ya estás en el último nivel (5).";
            juego.nextLevel();
            return "✓ Avanzado al Nivel " + juego.getNivelActual().getNumeroNivel();
        }
    }

    private String setLevel(String[] parts) {
        if (parts.length < 2) return "Uso: level N (1-5)";
        int n;
        try { n = Integer.parseInt(parts[1]); }
        catch (NumberFormatException e) { return "⚠  Número de nivel inválido."; }
        if (n < 1 || n > 5) return "⚠  El nivel debe estar entre 1 y 5.";

        synchronized (juego) {
            juego.restart();
            juego.setEstado(EstadoJuego.PLAYING);
            for (int i = 1; i < n; i++) juego.nextLevel();
            juego.getNivelActual().iniciarOleada();
            return "✓ Nivel " + n + " cargado";
        }
    }

    private String setSpeed(String[] parts) {
        if (parts.length < 2) return "Uso: speed <factor>  (ej: 0.5 | 1 | 2 | 5)";
        double factor;
        try { factor = Double.parseDouble(parts[1]); }
        catch (NumberFormatException e) { return "⚠  Factor inválido."; }
        if (factor <= 0 || factor > 20) return "⚠  Factor fuera de rango (0 < factor ≤ 20).";

        synchronized (juego) {
            // Modificamos la velocidad de todos los enemigos activos
            for (Enemigo e : juego.getNivelActual().getEnemigosRestantes()) {
                e.setRapidez(2.0 * factor);
            }
            return String.format("✓ Velocidad de juego: %.1fx (afecta enemigos actuales y futuros spawns quedan en %.1f rapidez)", factor, 2.0 * factor);
        }
    }

    private String pauseGame() {
        // No tenemos acceso al sessionState del modelo directamente,
        // pero podemos setear el estado del juego a START (pausa lógica)
        // La pausa real la maneja el controller; aquí solo informamos.
        return "ℹ  Para pausar usa la tecla [P] o [Espacio] en el juego.\n" +
               "   (La pausa real la controla el game loop del controller)";
    }

    private String resumeGame() {
        synchronized (juego) {
            if (juego.getEstado() == EstadoJuego.START) {
                juego.setEstado(EstadoJuego.PLAYING);
                juego.getNivelActual().iniciarOleada();
                return "✓ Juego reanudado desde START → PLAYING";
            }
            return "ℹ  Para reanudar usa la tecla [P] o [Espacio] en el juego.";
        }
    }

    private String restart() {
        synchronized (juego) {
            godMode = false;
            juego.restart();
            juego.setEstado(EstadoJuego.PLAYING);
            juego.getNivelActual().iniciarOleada();
            return "✓ Juego reiniciado al Nivel 1";
        }
    }

    public boolean isGodMode() { return godMode; }
}
