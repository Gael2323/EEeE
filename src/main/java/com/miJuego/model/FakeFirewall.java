package com.miJuego.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * FakeFirewall — Counter de las torres de hielo.
 *
 * Comportamiento (igual al patrón de Ares con TorreMessenger):
 *  - WALKING    : sigue waypoints normalmente.
 *  - APPROACHING: detecta una TorreDeHielo cercana y se acerca en línea recta.
 *  - BURNING    : entra en el rango de ataque de la torre, se detiene y ejecuta
 *                 la animación de quemarse hasta el último frame, luego alterna
 *                 entre el penúltimo y el último indefinidamente.
 *
 * Además, en todos los estados limpia ralentizado/parálisis de aliados cercanos
 * cada SCAN_LIMPIEZA segundos.
 */
public class FakeFirewall extends Enemigo {

    // ─── Estados ─────────────────────────────────────────────────────────────
    public enum EstadoFakeFirewall {
        WALKING,
        APPROACHING,
        BURNING
    }

    // ─── Constantes ──────────────────────────────────────────────────────────
    private static final float DETECCION_ICE   = 6.0f;  // Radio de búsqueda de torre de hielo
    private static final float RADIO_LIMPIEZA  = 2.5f;  // Radio del aura pasiva de limpieza
    private static final float SCAN_ICE        = 0.5f;  // Frecuencia de escaneo de torres
    private static final float SCAN_LIMPIEZA   = 1.0f;  // Frecuencia de limpieza de aliados
    private static final float SEG_POR_FRAME   = 0.15f; // Duración de cada frame de burning
    private static final float SEG_ALTERNADO   = 0.35f; // Duración de cada frame al alternar

    // ─── Estado interno ──────────────────────────────────────────────────────
    private EstadoFakeFirewall estado = EstadoFakeFirewall.WALKING;
    private TorreDeHielo       targetIceTower = null;   // Torre de hielo objetivo

    private float burningElapsed = 0f; // Tiempo acumulado en estado BURNING (para animación)
    private float scanIceTimer   = 0f; // Acumulador para escaneo de torres
    private float scanLimpiezaTimer = 0f; // Acumulador para limpieza de aliados

    // ─── Constructor ─────────────────────────────────────────────────────────
    public FakeFirewall(String id) {
        super(id, "FakeFirewall", 200.0, 35, 20);
        this.rapidez  = 1.8;
        this.damageBase = 2.0;
        this.width    = 1.3f;
        this.height   = 1.3f;
    }

    // ─── API pública ─────────────────────────────────────────────────────────

    public EstadoFakeFirewall getEstado()        { return estado; }
    public TorreDeHielo       getTargetIceTower(){ return targetIceTower; }
    public float              getBurningElapsed() { return burningElapsed; }
    public boolean            isBurning()         { return estado == EstadoFakeFirewall.BURNING; }

    /** Activa el estado BURNING (llamar desde Juego.java cuando entra en rango). */
    public void activarBurning() {
        if (estado != EstadoFakeFirewall.BURNING) {
            estado         = EstadoFakeFirewall.BURNING;
            burningElapsed = 0f;
        }
    }

    // ─── Actualización de estado ──────────────────────────────────────────────

    /**
     * Llamar en cada tick del game loop.
     * Gestiona la IA de detección, el cooldown de limpieza y la animación.
     *
     * @param deltaSeconds  tiempo desde el último frame
     * @param todosEnemigos lista viva de enemigos (para limpiar aliados)
     * @param torres        lista de torres activas (para detectar torres de hielo)
     */
    public void updateFakeFirewallState(float deltaSeconds,
                                        List<Enemigo> todosEnemigos,
                                        List<Torre> torres) {
        // ── Limpieza pasiva de aliados (siempre, en cualquier estado) ──────
        scanLimpiezaTimer += deltaSeconds;
        if (scanLimpiezaTimer >= SCAN_LIMPIEZA) {
            scanLimpiezaTimer = 0f;
            limpiarAliadosCercanos(todosEnemigos);
        }

        // ── Estado BURNING: solo acumula el timer de animación ─────────────
        if (estado == EstadoFakeFirewall.BURNING) {
            burningElapsed += deltaSeconds;
            // Si la torre objetivo fue destruida, volver a caminar
            if (targetIceTower == null || !torres.contains(targetIceTower)) {
                estado         = EstadoFakeFirewall.WALKING;
                targetIceTower = null;
                burningElapsed = 0f;
            }
            return;
        }

        // ── Escaneo de torres de hielo ─────────────────────────────────────
        scanIceTimer += deltaSeconds;
        if (scanIceTimer >= SCAN_ICE) {
            scanIceTimer = 0f;
            escanearTorresHielo(torres);
        }

        // ── APPROACHING: verificar si la torre fue destruida o ya estamos en rango ──
        if (estado == EstadoFakeFirewall.APPROACHING) {
            if (targetIceTower == null || !torres.contains(targetIceTower)) {
                estado         = EstadoFakeFirewall.WALKING;
                targetIceTower = null;
                return;
            }
            // Si ya entramos en el rango de ataque de la torre → activar BURNING
            float dx   = targetIceTower.getX() + 0.5f - this.x;
            float dy   = targetIceTower.getY() + 0.5f - this.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist <= targetIceTower.getRango()) {
                activarBurning();
            }
        }
    }

    /** Limpia ralentizado y parálisis de aliados dentro de RADIO_LIMPIEZA. */
    public void limpiarAliadosCercanos(List<Enemigo> todos) {
        float rangeSq = RADIO_LIMPIEZA * RADIO_LIMPIEZA;
        for (Enemigo e : todos) {
            if (e == this || e.isDead()) continue;
            float dx = e.getX() - this.x;
            float dy = e.getY() - this.y;
            if (dx * dx + dy * dy <= rangeSq) {
                if (e.ralentizarTimer > 0) {
                    e.ralentizarTimer  = 0f;
                    e.ralentizarFactor = 1.0;
                }
                if (e.paralizacionTimer > 0) {
                    e.paralizacionTimer = 0f;
                }
            }
        }
    }

    // ─── Velocidad ───────────────────────────────────────────────────────────

    @Override
    public double getVelocidadActual() {
        if (estado == EstadoFakeFirewall.BURNING) return 0.0; // Quieto mientras se quema
        if (paralizacionTimer > 0)               return 0.0;
        return rapidez; // Inmune al ralentizado
    }

    // ─── Animación ───────────────────────────────────────────────────────────

    @Override
    public Optional<String> getImagePath() {
        if (estado == EstadoFakeFirewall.BURNING) {
            return getBurningSprite();
        }
        // Normal: sprite según dirección actual (0-7 octantes)
        return Optional.of("assets/ingame/enemies/Fake_Firewall/Fake_Firewall" + currentOctant + ".png");
    }

    private Optional<String> getBurningSprite() {
        int dir         = currentOctant; // Dirección congelada al momento de detenerse
        int totalFrames = getBurningFrameCount(dir);
        float fullAnimTime = totalFrames * SEG_POR_FRAME;

        int step;
        if (burningElapsed < fullAnimTime) {
            // Fase 1: recorre todos los frames en orden
            step = (int) (burningElapsed / SEG_POR_FRAME);
            step = Math.min(step, totalFrames - 1);
        } else {
            // Fase 2: alterna entre el penúltimo y el último frame
            int alternating = (int) ((burningElapsed - fullAnimTime) / SEG_ALTERNADO) % 2;
            step = (totalFrames - 1) - alternating; // 0 → último, 1 → penúltimo
        }

        int frame = mapBurningFrame(dir, step);
        return Optional.of("assets/ingame/enemies/Fake_Firewall/Fake_Firewall_Quemandose" + dir + "_" + frame + ".png");
    }

    /**
     * Frames reales por dirección (assets confirmados):
     *  dir 0 → 0,1,2,3        (4 frames)
     *  dir 1 → 0,_,2,3,4      (4 frames reales — falta 1_1)
     *  dir 2 → 0,1,2,3        (4 frames)
     *  dir 3 → 0,1,2,3,4      (5 frames)
     *  dir 4 → 0,1,2,3,4      (5 frames)
     *  dir 5 → 0,1,2,3        (4 frames)
     *  dir 6 → 0,1,2,3        (4 frames)
     *  dir 7 → 0,1,_,3,4      (4 frames reales — falta 7_2)
     */
    private int getBurningFrameCount(int dir) {
        return switch (dir) {
            case 3, 4 -> 5;
            default   -> 4;
        };
    }

    /** Mapea el paso lógico al número de frame real en el archivo, saltando los faltantes. */
    private int mapBurningFrame(int dir, int step) {
        if (dir == 1) {
            // Assets: _0, _2, _3, _4  (falta _1)
            return switch (step) { case 0 -> 0; case 1 -> 2; case 2 -> 3; default -> 4; };
        }
        if (dir == 7) {
            // Assets: _0, _1, _3, _4  (falta _2)
            return switch (step) { case 0 -> 0; case 1 -> 1; case 2 -> 3; default -> 4; };
        }
        return step;
    }

    // ─── Color de fallback ───────────────────────────────────────────────────

    @Override
    public Color getFallbackColor() {
        if (fuegoTimer > 0)   return super.getFallbackColor();
        if (isBurning())      return new Color(255, 120, 20); // Naranja intenso al quemarse
        return new Color(255, 80, 20); // Naranja-rojo base
    }

    // ─── Muerte ──────────────────────────────────────────────────────────────

    @Override
    public List<Enemigo> morir() {
        return new ArrayList<>();
    }

    // ─── Búsqueda interna de torre de hielo ─────────────────────────────────

    private void escanearTorresHielo(List<Torre> torres) {
        TorreDeHielo nearest  = null;
        double       minDist  = DETECCION_ICE;

        for (Torre t : torres) {
            if (t instanceof TorreDeHielo ice) {
                float dx   = ice.getX() + 0.5f - this.x;
                float dy   = ice.getY() + 0.5f - this.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = ice;
                }
            }
        }

        if (nearest != null) {
            targetIceTower = nearest;
            // Si ya estamos dentro del rango de ataque, activar burning directamente
            float dx   = nearest.getX() + 0.5f - this.x;
            float dy   = nearest.getY() + 0.5f - this.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist <= nearest.getRango()) {
                activarBurning();
            } else {
                estado = EstadoFakeFirewall.APPROACHING;
            }
        } else if (estado == EstadoFakeFirewall.APPROACHING) {
            // La torre fue destruida mientras se acercaba
            estado         = EstadoFakeFirewall.WALKING;
            targetIceTower = null;
        }
    }
}
