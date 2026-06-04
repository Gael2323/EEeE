package com.miJuego.sandbox;

import com.miJuego.model.*;

import java.util.List;
import java.util.Scanner;

/**
 * Herramienta de consola para testear mecánicas del Tower Defense sin GUI.
 *
 * <p>Uso: cambiar en build.gradle {@code mainClass = 'com.miJuego.sandbox.SandboxConsole'}
 * y correr {@code ./gradlew run} o ejecutar directamente desde el IDE.</p>
 *
 * <h2>Comandos disponibles</h2>
 * <pre>
 *   help                      — muestra todos los comandos
 *   status                    — estado actual del juego
 *   spawn duende [N]          — spawnea N Duendes (default 1)
 *   spawn comun [N] [escudo]  — spawnea N EnemigoComun (escudo opcional)
 *   spawn multiple [N]        — spawnea N EnemigoMultiple
 *   spawn boss                — spawnea el boss del nivel 5
 *   place comun X Y           — coloca TorreComun en (X,Y)
 *   place area X Y            — coloca TorreDeArea en (X,Y)
 *   place cañon X Y           — coloca Cañon en (X,Y)
 *   place fuerte X Y          — coloca TorreFuerte en (X,Y)
 *   place fuego X Y           — coloca TorreDeFuego en (X,Y)
 *   place hielo X Y           — coloca TorreDeHielo en (X,Y)
 *   place electrica X Y       — coloca TorreElectrica en (X,Y)
 *   upgrade X Y               — mejora la torre en (X,Y) sin costo
 *   sell X Y                  — vende la torre en (X,Y)
 *   give gold N               — da N monedas
 *   give lives N              — da N vidas
 *   list towers               — lista torres con posición y nivel
 *   list enemies              — lista enemigos activos con vida y efectos
 *   run N                     — simula N segundos de juego
 *   level N                   — carga el Nivel N (1-5)
 *   reset                     — reinicia todo
 *   exit                      — sale del programa
 * </pre>
 */
public class SandboxConsole {

    // El sandbox tiene su propio Juego y Nivel independiente
    private Juego juego;
    private int idCounter = 0;

    public SandboxConsole() {
        juego = new Juego();
        // Arrancamos en PLAYING directamente para poder interactuar
        juego.setEstado(EstadoJuego.PLAYING);
        juego.getNivelActual().iniciarOleada();
    }

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   TOWER DEFENSE — SANDBOX CONSOLE    ║");
        System.out.println("║   Escribe 'help' para ver comandos   ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        SandboxConsole sandbox = new SandboxConsole();
        sandbox.printStatus();
        System.out.println();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("sandbox> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            boolean shouldExit = sandbox.handleCommand(line);
            if (shouldExit) break;
            System.out.println();
        }

        System.out.println("Saliendo del Sandbox. ¡Hasta luego!");
        scanner.close();
    }

    /**
     * Parsea y ejecuta un comando. Devuelve {@code true} si hay que salir.
     */
    private boolean handleCommand(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0) return false;

        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "help" -> printHelp();
            case "status" -> printStatus();
            case "spawn" -> handleSpawn(parts);
            case "place" -> handlePlace(parts);
            case "upgrade" -> handleUpgrade(parts);
            case "sell" -> handleSell(parts);
            case "give" -> handleGive(parts);
            case "list" -> handleList(parts);
            case "run" -> handleRun(parts);
            case "level" -> handleLevel(parts);
            case "reset" -> handleReset();
            case "exit", "quit", "q" -> { return true; }
            default -> System.out.println("⚠  Comando desconocido: '" + cmd + "'. Escribe 'help' para ayuda.");
        }
        return false;
    }

    // ─── HANDLERS ────────────────────────────────────────────────────────────

    private void handleSpawn(String[] parts) {
        if (parts.length < 2) { System.out.println("Uso: spawn <tipo> [cantidad] [escudo]"); return; }

        String tipo = parts[1].toLowerCase();
        int cantidad = 1;
        if (parts.length >= 3) {
            try { cantidad = Integer.parseInt(parts[2]); }
            catch (NumberFormatException e) { System.out.println("⚠  Cantidad inválida."); return; }
        }

        List<Enemigo> enemigos = juego.getNivelActual().getEnemigosRestantes();
        List<float[]> waypoints = juego.getNivelActual().getWaypoints();
        float startX = waypoints.isEmpty() ? 0f : waypoints.get(0)[0];
        float startY = waypoints.isEmpty() ? 7f : waypoints.get(0)[1];

        int spawneados = 0;
        for (int i = 0; i < cantidad; i++) {
            Enemigo e = crearEnemigo(tipo, parts);
            if (e == null) return; // mensaje ya impreso
            e.setPosicion(startX, startY);
            e.setWaypointIndex(1);
            enemigos.add(e);
            spawneados++;
        }
        System.out.printf("✓ Spawneados %d x %s%n", spawneados, tipo);
    }

    private Enemigo crearEnemigo(String tipo, String[] parts) {
        String id = "sandbox-" + tipo + "-" + (++idCounter);
        boolean escudo = parts.length >= 4 && parts[3].equalsIgnoreCase("escudo");

        return switch (tipo) {
            case "duende"    -> new Duende(id);
            case "comun"     -> new EnemigoComun(id, escudo);
            case "multiple"  -> new EnemigoMultiple(id);
            case "boss"      -> {
                EnemigoComun boss = new EnemigoComun(id, true);
                boss.setVida(1000.0);
                boss.setRapidez(0.7);
                boss.setMonedasGeneradas(200.0);
                boss.setScoreGenerado(150.0);
                boss.setDañoBase(5.0);
                System.out.println("💀 Boss spawneado (1000 HP, muy lento)");
                yield boss;
            }
            default -> {
                System.out.println("⚠  Tipo de enemigo desconocido: " + tipo);
                System.out.println("   Tipos válidos: duende | comun | multiple | boss");
                yield null;
            }
        };
    }

    private void handlePlace(String[] parts) {
        if (parts.length < 4) { System.out.println("Uso: place <tipo> X Y"); return; }

        String tipo = parts[1].toLowerCase();
        int x, y;
        try {
            x = Integer.parseInt(parts[2]);
            y = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            System.out.println("⚠  X e Y deben ser números enteros.");
            return;
        }

        if (x < 0 || x >= 20 || y < 0 || y >= 15) {
            System.out.println("⚠  Coordenadas fuera del grid (0-19 en X, 0-14 en Y).");
            return;
        }

        // En sandbox no hay costo — le damos dinero infinito temporalmente
        int oroAntes = juego.getJugador().getMoneda();
        juego.getJugador().addMoneda(99999);

        // Seleccionar tipo de torre
        int towerType = switch (tipo) {
            case "comun"     -> 1;
            case "area"      -> 2;
            case "cañon"     -> 3;
            case "fuerte"    -> 4;
            case "fuego"     -> 5;
            case "hielo"     -> 6;
            case "electrica" -> 7;
            default          -> -1;
        };

        if (towerType == -1) {
            juego.getJugador().setMoneda(oroAntes);
            System.out.println("⚠  Tipo de torre desconocido: " + tipo);
            System.out.println("   Tipos: comun | area | cañon | fuerte | fuego | hielo | electrica");
            return;
        }

        juego.setSelectedTowerType(towerType);
        try {
            juego.placeTower(x, y);
            juego.getJugador().setMoneda(oroAntes); // restauramos el oro original
            System.out.printf("✓ Torre '%s' colocada en (%d, %d)%n", tipo, x, y);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            juego.getJugador().setMoneda(oroAntes);
            System.out.println("⚠  " + ex.getMessage());
        }
    }

    private void handleUpgrade(String[] parts) {
        if (parts.length < 3) { System.out.println("Uso: upgrade X Y"); return; }
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            // Upgrade gratis: damos oro temporal
            int oroAntes = juego.getJugador().getMoneda();
            juego.getJugador().addMoneda(99999);
            juego.upgradeTowerAt(x, y);
            juego.getJugador().setMoneda(oroAntes);
            System.out.printf("✓ Torre en (%d, %d) mejorada%n", x, y);
        } catch (NumberFormatException e) {
            System.out.println("⚠  X e Y deben ser números enteros.");
        } catch (IllegalStateException ex) {
            System.out.println("⚠  " + ex.getMessage());
        }
    }

    private void handleSell(String[] parts) {
        if (parts.length < 3) { System.out.println("Uso: sell X Y"); return; }
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            juego.sellTowerAt(x, y);
            System.out.printf("✓ Torre en (%d, %d) vendida%n", x, y);
        } catch (NumberFormatException e) {
            System.out.println("⚠  X e Y deben ser números enteros.");
        } catch (IllegalStateException ex) {
            System.out.println("⚠  " + ex.getMessage());
        }
    }

    private void handleGive(String[] parts) {
        if (parts.length < 3) { System.out.println("Uso: give gold N  |  give lives N"); return; }
        String recurso = parts[1].toLowerCase();
        int cantidad;
        try {
            cantidad = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            System.out.println("⚠  La cantidad debe ser un número entero.");
            return;
        }

        switch (recurso) {
            case "gold", "oro", "monedas" -> {
                juego.getJugador().addMoneda(cantidad);
                System.out.printf("✓ +%d monedas → Total: %d%n", cantidad, juego.getJugador().getMoneda());
            }
            case "lives", "vidas", "hp" -> {
                juego.getJugador().setHealth(juego.getJugador().getHealth() + cantidad);
                System.out.printf("✓ +%d vidas → Total: %d%n", cantidad, juego.getJugador().getHealth());
            }
            default -> System.out.println("⚠  Recurso desconocido. Usar: give gold N  |  give lives N");
        }
    }

    private void handleList(String[] parts) {
        if (parts.length < 2) { System.out.println("Uso: list towers  |  list enemies"); return; }
        switch (parts[1].toLowerCase()) {
            case "towers", "torres" -> listTowers();
            case "enemies", "enemigos" -> listEnemies();
            default -> System.out.println("⚠  Opción inválida. Usar: list towers  |  list enemies");
        }
    }

    private void listTowers() {
        List<Torre> torres = juego.getTorres();
        if (torres.isEmpty()) {
            System.out.println("  (no hay torres colocadas)");
            return;
        }
        System.out.printf("  %-5s %-4s %-4s %-16s %-6s %-6s%n", "ID", "X", "Y", "Tipo", "Nivel", "Rango");
        System.out.println("  " + "─".repeat(50));
        for (Torre t : torres) {
            System.out.printf("  %-5s %-4.0f %-4.0f %-16s %-6d %-6.1f%n",
                    t.getId(), t.getX(), t.getY(),
                    t.getTowertype(), t.getNivelMejora(), t.getRango());
        }
    }

    private void listEnemies() {
        List<Enemigo> enemigos = juego.getNivelActual().getEnemigosRestantes();
        if (enemigos.isEmpty()) {
            System.out.println("  (no hay enemigos activos)");
            return;
        }
        System.out.printf("  %-20s %-8s %-8s %-8s %-10s%n", "ID", "HP", "X", "Y", "Efectos");
        System.out.println("  " + "─".repeat(60));
        for (Enemigo e : enemigos) {
            String efectos = "";
            if (e.tieneFuego())        efectos += "[FUEGO] ";
            if (e.tieneRalentizar())   efectos += "[LENTO] ";
            if (e.tieneParalizacion()) efectos += "[PARAL] ";
            if (efectos.isEmpty())     efectos = "—";
            System.out.printf("  %-20s %-8.1f %-8.1f %-8.1f %-10s%n",
                    e.getId(), e.GetVida(), e.getX(), e.getY(), efectos.trim());
        }
    }

    /**
     * Simula N segundos de juego en pasos de 0.016 s (~60 FPS).
     * Al terminar imprime el estado resumido.
     */
    private void handleRun(String[] parts) {
        double segundos = 1.0;
        if (parts.length >= 2) {
            try { segundos = Double.parseDouble(parts[1]); }
            catch (NumberFormatException e) { System.out.println("⚠  Número de segundos inválido."); return; }
        }

        if (juego.getEstado() != EstadoJuego.PLAYING) {
            System.out.println("⚠  El juego no está en estado PLAYING. Usa 'reset' para reiniciar.");
            return;
        }

        System.out.printf("⏩ Simulando %.1f segundos...%n", segundos);
        double elapsed = 0.0;
        final double dt = 0.016;
        while (elapsed < segundos) {
            juego.update((float) dt);
            elapsed += dt;
            if (juego.getEstado() != EstadoJuego.PLAYING) break;
        }

        System.out.printf("   Completado. Estado final: %s%n", juego.getEstado());
        printStatus();
    }

    private void handleLevel(String[] parts) {
        if (parts.length < 2) { System.out.println("Uso: level N (1-5)"); return; }
        int n;
        try { n = Integer.parseInt(parts[1]); }
        catch (NumberFormatException e) { System.out.println("⚠  Número de nivel inválido."); return; }

        if (n < 1 || n > 5) { System.out.println("⚠  El nivel debe estar entre 1 y 5."); return; }

        // Recreamos el nivel directamente — el Juego no expone setNivel, pero podemos
        // usar nextLevel() repetido o hacer restart + avanzar
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
        for (int i = 1; i < n; i++) {
            juego.nextLevel();
        }
        juego.getNivelActual().iniciarOleada();
        System.out.printf("✓ Cargado Nivel %d con waypoints y oleada configurados%n", n);
        printStatus();
    }

    private void handleReset() {
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
        juego.getNivelActual().iniciarOleada();
        idCounter = 0;
        System.out.println("✓ Juego reiniciado al Nivel 1.");
        printStatus();
    }

    // ─── DISPLAY ─────────────────────────────────────────────────────────────

    private void printStatus() {
        Jugador j = juego.getJugador();
        Nivel n   = juego.getNivelActual();
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.printf ("│ Nivel: %-5d  Estado: %-18s│%n",
                n.getNumeroNivel(), juego.getEstado());
        System.out.printf ("│ Vidas: %-5d  Oro: %-6d  Score: %-8.0f│%n",
                j.getHealth(), j.getMoneda(), j.getScore());
        System.out.printf ("│ Torres: %-4d  Enemigos activos: %-9d│%n",
                juego.getTorres().size(), n.getEnemigosRestantes().size());
        System.out.println("└─────────────────────────────────────────┘");
    }

    private void printHelp() {
        System.out.println();
        System.out.println("  SPAWN");
        System.out.println("    spawn duende [N]          — spawnea N Duendes");
        System.out.println("    spawn comun [N] [escudo]  — spawnea N EnemigoComun (con escudo eléctrico opcional)");
        System.out.println("    spawn multiple [N]        — spawnea N EnemigoMultiple");
        System.out.println("    spawn boss                — spawnea el Boss (1000 HP)");
        System.out.println();
        System.out.println("  TORRES");
        System.out.println("    place <tipo> X Y   — coloca torre sin costo");
        System.out.println("                         tipos: comun | area | cañon | fuerte | fuego | hielo | electrica");
        System.out.println("    upgrade X Y        — mejora torre en (X,Y) sin costo");
        System.out.println("    sell X Y           — vende torre en (X,Y)");
        System.out.println();
        System.out.println("  RECURSOS");
        System.out.println("    give gold N        — da N monedas");
        System.out.println("    give lives N       — da N vidas");
        System.out.println();
        System.out.println("  SIMULACIÓN");
        System.out.println("    run [N]            — simula N segundos de juego (default 1)");
        System.out.println("    level N            — carga el nivel N (1-5)");
        System.out.println("    reset              — reinicia todo al nivel 1");
        System.out.println();
        System.out.println("  INFO");
        System.out.println("    status             — muestra estado actual");
        System.out.println("    list towers        — lista todas las torres");
        System.out.println("    list enemies       — lista enemigos activos");
        System.out.println();
        System.out.println("    exit / quit        — salir");
        System.out.println();
    }
}
