package com.miJuego.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Ares extends Enemigo {

    public enum EstadoAres {
        WALKING,
        APPROACHING,
        SHIELDING
    }

    private EstadoAres estadoAres = EstadoAres.WALKING;
    private TorreMessenger targetMessenger = null;
    private float shieldTimer = 0f;
    private float shieldTimeElapsed = 0f;
    private static final float DURACION_ESCUDO = 10f; // 10 segundos de escudo activo
    private float cooldownTimer = 0f;
    private static final float COOLDOWN_ESCUDO = 5.0f; // 5 segundos de cooldown
    private float scanTimer = 0f;
    private static final float SCAN_INTERVAL = 0.5f; // Escanear por TorreMessenger cada 0.5s

    public Ares(String id) {
        super(id, "Ares", 350.0, 50, 30); // Vida alta (350), oro (50), score (30)
        this.rapidez = 0.8; // Movimiento lento
        this.damageBase = 2.0;
        this.width = 1.6f;  // Tamaño tipo tanque
        this.height = 1.6f;
    }

    @Override
    public List<Enemigo> morir() {
        return new ArrayList<>();
    }

    public boolean isShieldActive() {
        return estadoAres == EstadoAres.SHIELDING;
    }

    public EstadoAres getEstadoAres() {
        return estadoAres;
    }

    public TorreMessenger getTargetMessenger() {
        return targetMessenger;
    }

    public void setTargetMessenger(TorreMessenger target) {
        this.targetMessenger = target;
    }

    public void setEstadoAres(EstadoAres estado) {
        this.estadoAres = estado;
    }

    public float getShieldTimer() {
        return shieldTimer;
    }

    public float getShieldTimeElapsed() {
        return shieldTimeElapsed;
    }

    public float getCooldownTimer() {
        return cooldownTimer;
    }

    public void activarEscudo() {
        if (estadoAres != EstadoAres.SHIELDING && cooldownTimer <= 0) {
            estadoAres = EstadoAres.SHIELDING;
            shieldTimer = DURACION_ESCUDO;
            shieldTimeElapsed = 0f;
        }
    }

    public void updateAresState(float deltaSeconds, List<Torre> torres) {
        if (estadoAres == EstadoAres.SHIELDING) {
            shieldTimeElapsed += deltaSeconds;
            shieldTimer -= deltaSeconds;
            if (shieldTimer <= 0) {
                // Escudo expira, vuelve a caminar normal con cooldown
                estadoAres = EstadoAres.WALKING;
                targetMessenger = null;
                cooldownTimer = COOLDOWN_ESCUDO;
            }
            return;
        }

        if (cooldownTimer > 0) {
            cooldownTimer -= deltaSeconds;
        }

        // Escanear TorreMessenger si está en WALKING o APPROACHING
        if (cooldownTimer <= 0) {
            scanTimer += deltaSeconds;
            if (scanTimer >= SCAN_INTERVAL) {
                scanTimer = 0f;
                findNearestMessenger(torres);
            }
        }

        if (estadoAres == EstadoAres.APPROACHING) {
            // Si la torre objetivo fue vendida o destruida, abortar
            if (targetMessenger == null || !torres.contains(targetMessenger)) {
                estadoAres = EstadoAres.WALKING;
                targetMessenger = null;
                return;
            }

            // Calcular distancia a la TorreMessenger
            float tx = targetMessenger.getX() + 0.5f;
            float ty = targetMessenger.getY() + 0.5f;
            float dx = tx - this.x;
            float dy = ty - this.y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            // Entrar en escudo cuando entra en su rango de disparo
            if (dist <= targetMessenger.getRango()) {
                estadoAres = EstadoAres.SHIELDING;
                shieldTimer = DURACION_ESCUDO;
                shieldTimeElapsed = 0f;
            }
        }
    }

    private void findNearestMessenger(List<Torre> torres) {
        TorreMessenger nearest = null;
        double minDistance = 5.0; // Radio de detección de 5 celdas
        for (Torre t : torres) {
            if (t instanceof TorreMessenger) {
                double dx = t.getX() + 0.5f - this.x;
                double dy = t.getY() + 0.5f - this.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = (TorreMessenger) t;
                }
            }
        }
        if (nearest != null) {
            targetMessenger = nearest;
            estadoAres = EstadoAres.APPROACHING;
        } else if (estadoAres == EstadoAres.APPROACHING) {
            estadoAres = EstadoAres.WALKING;
            targetMessenger = null;
        }
    }

    @Override
    public double getVelocidadActual() {
        if (estadoAres == EstadoAres.SHIELDING) {
            return 0.0; // Velocidad 0 al estar protegiendo con escudo
        }
        return super.getVelocidadActual();
    }

    @Override
    public Optional<String> getImagePath() {
        if (isShieldActive()) {
            int shieldDir = getShieldingSpriteDirection(currentOctant);
            // Cada frame dura 0.15s. Total 6 frames (0 a 5). Se detiene/congela en el frame 5.
            int frame = Math.min(5, (int) (shieldTimeElapsed / 0.15f));
            return Optional.of("assets/ingame/enemies/enemy_Ares/Enemigo_Ares_CargandoEscudo" + shieldDir + "_" + frame + ".png");
        } else {
            // Usar dirección actual del octante directamente ya que todas están disponibles
            int walkDir = currentOctant;
            int frameCount = getWalkFrameCount(walkDir);
            int frame = (int) ((System.currentTimeMillis() / 150) % frameCount);
            return Optional.of("assets/ingame/enemies/enemy_Ares/Enemigo_Ares_Caminando" + walkDir + "_" + frame + ".png");
        }
    }

    private int getShieldingSpriteDirection(int octant) {
        if (octant == 7 || octant == 0 || octant == 1) return 0; // Arriba
        if (octant == 2 || octant == 3) return 2; // Derecha
        if (octant == 4 || octant == 5) return 4; // Abajo
        return 6; // Izquierda
    }

    private int getWalkFrameCount(int uDir) {
        return switch (uDir) {
            case 0 -> 3;
            case 2 -> 8;
            case 3 -> 6;
            case 5 -> 6;
            default -> 4; // 1, 4, 6, 7
        };
    }

    @Override
    public Color getFallbackColor() {
        if (paralizacionTimer > 0 || ralentizarTimer > 0 || fuegoTimer > 0) {
            return super.getFallbackColor();
        }
        if (isShieldActive()) {
            return new Color(0, 255, 255); // Celeste brillante
        }
        return new Color(139, 0, 0); // Rojo oscuro
    }
}
