package com.miJuego.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BossPeedy extends Enemigo {

    public enum BossState {
        WALKING,
        FLYING, // Volando hacia una torre (invulnerable al suelo o muy rápido)
        SPINNING,    // Dando la vuelta 360° sobre la torre
        LANDING,     // Cayendo en picada (superherolanding)
        RECOMPOSING, // Recomponiéndose en el suelo
        STUNNING // Ejecutando animación de aturdir
    }

    private BossState estadoBoss = BossState.WALKING;
    
    private float destX;
    private float destY;
    
    private float habilidadTimer = 0f;
    private float tiempoParaHabilidad = 6.0f; // Cada 6 segundos usa una habilidad
    private float animTimer = 0f;
    
    private Torre targetTower = null;
    private float lastX, lastY; // Para saber hacia donde mira

    private Random random = new Random();

    public BossPeedy(String id) {
        // Stats masivas: 3000 de vida
        super(id, "Boss Peedy", 3000.0, 500, 5000);
        this.rapidez = 1.2; // Un poco más de rapidez
        this.damageBase = 0.0; // No hace damage al jugador directamente
        this.width = 3.0f; // Boss gigante
        this.height = 3.0f;
        elegirDestinoAleatorio();
    }

    @Override
    public List<Enemigo> morir() {
        return new ArrayList<>(); // No se divide
    }

    public BossState getEstadoBoss() {
        return estadoBoss;
    }

    public void setEstadoBoss(BossState estadoBoss) {
        this.estadoBoss = estadoBoss;
    }

    public float getDestX() {
        return destX;
    }

    public float getDestY() {
        return destY;
    }

    public void elegirDestinoAleatorio() {
        // El mapa lógico del juego es 64x48
        this.destX = 4f + random.nextFloat() * 56f; // Entre 4 y 60
        this.destY = 4f + random.nextFloat() * 40f; // Entre 4 y 44
    }

    private int activeHabilidadType = 0; // 0: stun, 1: teleport
    private boolean habilidadEjecutada = false;

    public void updateHabilidades(float deltaSeconds, List<Torre> torres, List<Enemigo> enemigos, List<Juego.VisualEffect> fxList) {
        // Si está congelado/ralentizado muy fuerte, no avanza sus timers
        if (this.paralizacionTimer > 0) return;

        if (estadoBoss == BossState.WALKING) {
            habilidadTimer += deltaSeconds;
            if (habilidadTimer >= tiempoParaHabilidad) {
                habilidadTimer = 0f;
                decidirSiguienteAccion(torres, enemigos);
            }
        } else if (estadoBoss == BossState.STUNNING) {
            animTimer += deltaSeconds;
            int frame = getStunningFrame();
            if (frame >= 3 && !habilidadEjecutada) {
                habilidadEjecutada = true;
                if (activeHabilidadType == 0) {
                    ejecutarAturdirTorres(torres, fxList);
                } else {
                    ejecutarTeleportarEnemigos(enemigos, fxList);
                }
            }
            if (frame >= 8) { // Terminado (después de 2.55s)
                estadoBoss = BossState.WALKING;
                elegirDestinoAleatorio();
            }
        } else if (estadoBoss == BossState.FLYING) {
            // El movimiento real se procesa en Juego.java
        } else if (estadoBoss == BossState.SPINNING) {
            animTimer += deltaSeconds;
            if (animTimer >= 0.8f) { // 0.8 segundos de giro 360
                estadoBoss = BossState.LANDING;
                animTimer = 0f;
                habilidadEjecutada = false; // Resetear para la fase de impacto
            }
        } else if (estadoBoss == BossState.LANDING) {
            animTimer += deltaSeconds;
            if (animTimer >= 0.50f && !habilidadEjecutada) {
                habilidadEjecutada = true;
                // Destruir la torre aquí
                if (targetTower != null) {
                    fxList.add(new Juego.VisualEffect(targetTower.getX() + 0.5f, targetTower.getY() + 0.5f, targetTower.getX() + 0.5f, targetTower.getY() + 0.5f, 1.0f, "explosion", 3.0f));
                    fxList.add(new Juego.VisualEffect(targetTower.getX() + 0.5f - 0.5f, targetTower.getY() + 0.5f - 0.5f, targetTower.getX() + 0.5f - 0.5f, targetTower.getY() + 0.5f - 0.5f, 0.5f, "explosion", 1.5f));
                    fxList.add(new Juego.VisualEffect(targetTower.getX() + 0.5f + 0.5f, targetTower.getY() + 0.5f + 0.5f, targetTower.getX() + 0.5f + 0.5f, targetTower.getY() + 0.5f + 0.5f, 0.5f, "explosion", 1.5f));
                    if (torres.contains(targetTower)) {
                        torres.remove(targetTower);
                    }
                }
                CameraContext.triggerShake(0.5f, 1.2f); // Fuerte sacudida
            }
            if (animTimer >= 1.50f) { // 1.50 segundos de landing total
                estadoBoss = BossState.RECOMPOSING;
                animTimer = 0f;
            }
        } else if (estadoBoss == BossState.RECOMPOSING) {
            animTimer += deltaSeconds;
            if (animTimer >= 0.5f) { // 0.5 segundos de re-incorporación
                estadoBoss = BossState.WALKING;
                terminarVuelo();
                elegirDestinoAleatorio();
            }
        }
        
        lastX = this.x;
        lastY = this.y;
    }

    public void resetAnimTimer() {
        this.animTimer = 0f;
    }

    private void iniciarPicada(List<Torre> torres) {
        estadoBoss = BossState.FLYING;
        // Encontrar la torre con la mayor amenaza para el malware
        Torre best = null;
        double maxThreat = -1.0;
        for (Torre t : torres) {
            double threat = 50.0;
            if (t instanceof TorreInternetExplorer || t instanceof TorreDeHielo || t instanceof TorreElectrica) {
                threat = 100.0; // Prioridad alta por aturdimiento/congelación
            } else if (t instanceof TorreMcAfee || t instanceof TorreFirefox || t instanceof TorreDeArea) {
                threat = 85.0; // Alto DPS
            } else if (t instanceof TorreMessenger || t instanceof TorreAvast) {
                threat = 70.0; // DPS medio/soporte
            } else if (t instanceof TorreFuerte) {
                threat = 60.0;
            }
            if (threat > maxThreat) {
                maxThreat = threat;
                best = t;
            }
        }
        targetTower = best;
    }

    private void decidirSiguienteAccion(List<Torre> torres, List<Enemigo> enemigos) {
        // 1. Calcular Utilidades
        double scoreStun = 0;
        double scoreTeleport = 0;
        double scorePicada = 0;
        double scoreVueloRandom = 30.0; // Utilidad base de relocalización

        // A. Utilidad de Aturdir (STUN)
        double rangoStunSq = 4.0 * 4.0;
        int activeTowersInRange = 0;
        int supportTowersInRange = 0;
        for (Torre t : torres) {
            if (t.stunTimer <= 0) {
                double dx = t.getX() - this.getX();
                double dy = t.getY() - this.getY();
                if (dx * dx + dy * dy <= rangoStunSq) {
                    activeTowersInRange++;
                    if (t instanceof TorreInternetExplorer || t instanceof TorreDeHielo || t instanceof TorreElectrica) {
                        supportTowersInRange++;
                    }
                }
            }
        }
        scoreStun = activeTowersInRange * 25.0 + supportTowersInRange * 15.0;

        // B. Utilidad de Teleportar enemigos (Remolino de bugs)
        double rangoTeleportSq = 6.0 * 6.0;
        int eligibleEnemies = 0;
        int highValueEnemies = 0;
        for (Enemigo e : enemigos) {
            if (e != this && !e.isDead()) {
                double dx = e.getX() - this.getX();
                double dy = e.getY() - this.getY();
                if (dx * dx + dy * dy <= rangoTeleportSq) {
                    eligibleEnemies++;
                    if (e.getNodosVisitados() <= 8) {
                        highValueEnemies++;
                    }
                }
            }
        }
        scoreTeleport = eligibleEnemies * 20.0 + highValueEnemies * 10.0;

        // C. Utilidad de picada (Destruir torre)
        if (!torres.isEmpty()) {
            scorePicada = 45.0;
            double maxThreat = -1.0;
            for (Torre t : torres) {
                double threat = 50.0;
                if (t instanceof TorreInternetExplorer || t instanceof TorreDeHielo || t instanceof TorreElectrica) {
                    threat = 100.0;
                } else if (t instanceof TorreMcAfee || t instanceof TorreFirefox || t instanceof TorreDeArea) {
                    threat = 85.0;
                } else if (t instanceof TorreMessenger || t instanceof TorreAvast) {
                    threat = 70.0;
                } else if (t instanceof TorreFuerte) {
                    threat = 60.0;
                }
                if (threat > maxThreat) {
                    maxThreat = threat;
                }
            }
            scorePicada += (maxThreat - 50.0) * 0.5;
        }

        // D. Utilidad de Vuelo Random (relocalización táctica)
        double rangoDangerSq = 5.0 * 5.0;
        int dangerTowers = 0;
        for (Torre t : torres) {
            double dx = t.getX() - this.getX();
            double dy = t.getY() - this.getY();
            if (dx * dx + dy * dy <= rangoDangerSq) {
                dangerTowers++;
            }
        }
        if (dangerTowers >= 3) {
            scoreVueloRandom += 35.0;
        }

        // 2. Selección probabilística (Ruleta Ponderada)
        double total = scoreStun + scoreTeleport + scorePicada + scoreVueloRandom;
        if (total <= 0) {
            iniciarVueloRandom();
            return;
        }

        double r = random.nextDouble() * total;
        double sum = 0.0;

        sum += scoreStun;
        if (r < sum) {
            iniciarAturdir();
            return;
        }

        sum += scoreTeleport;
        if (r < sum) {
            iniciarTeleport();
            return;
        }

        sum += scorePicada;
        if (r < sum) {
            iniciarPicada(torres);
            return;
        }

        iniciarVueloRandom();
    }

    private void iniciarVueloRandom() {
        estadoBoss = BossState.FLYING;
        targetTower = null;
        elegirDestinoAleatorio();
    }

    private void iniciarAturdir() {
        estadoBoss = BossState.STUNNING;
        animTimer = 0f;
        activeHabilidadType = 0;
        habilidadEjecutada = false;
    }

    private void ejecutarAturdirTorres(List<Torre> torres, List<Juego.VisualEffect> fxList) {
        // Aturdir torres en rango de 4 celdas
        double rangoStunSq = 4.0 * 4.0;
        for (Torre t : torres) {
            double dx = t.getX() - this.getX();
            double dy = t.getY() - this.getY();
            if (dx * dx + dy * dy <= rangoStunSq) {
                t.stun(3.0f); // 3 segundos de stun
            }
        }
        
        // Efecto visual de grito
        fxList.add(new Juego.VisualEffect(this.x, this.y, this.x, this.y, 1.0f, "explosion", 4.0f));
    }

    private void iniciarTeleport() {
        estadoBoss = BossState.STUNNING;
        animTimer = 0f;
        activeHabilidadType = 1;
        habilidadEjecutada = false;
    }

    private void ejecutarTeleportarEnemigos(List<Enemigo> todosLosEnemigos, List<Juego.VisualEffect> fxList) {
        // Buscamos enemigos elegibles (que no sean el boss y no estén muertos)
        List<Enemigo> elegibles = new ArrayList<>();
        for (Enemigo e : todosLosEnemigos) {
            if (e != this && !e.isDead()) {
                elegibles.add(e);
            }
        }
        if (elegibles.isEmpty()) return;

        // Elegir hasta 3 enemigos
        int cant = Math.min(3, elegibles.size());
        Random rand = new Random();
        for (int k = 0; k < cant; k++) {
            int idx = rand.nextInt(elegibles.size());
            Enemigo e = elegibles.remove(idx);

            // Efecto visual de remolino inicial en la posición del enemigo
            fxList.add(new Juego.VisualEffect(e.getX(), e.getY(), e.getX(), e.getY(), 0.5f, "explosion", 1.5f));

            // Avanzar el enemigo 3 nodos hacia adelante
            WaypointNode node = e.getTargetNode();
            for (int i = 0; i < 3; i++) {
                if (node != null && !node.siguientes.isEmpty()) {
                    node = node.siguientes.get(rand.nextInt(node.siguientes.size()));
                }
            }
            if (node != null) {
                e.setPosicion(node.x, node.y);
                e.setTargetNode(node);
                e.setNodosVisitados(e.getNodosVisitados() + 3);
                // Efecto visual de llegada
                fxList.add(new Juego.VisualEffect(node.x, node.y, node.x, node.y, 0.8f, "explosion", 2.0f));
            }
        }
    }

    public Torre getTargetTower() {
        return targetTower;
    }

    public void terminarVuelo() {
        estadoBoss = BossState.WALKING;
        targetTower = null;
    }

    public int getStunningFrame() {
        if (animTimer < 0.15f) return 0;
        if (animTimer < 0.30f) return 1;
        if (animTimer < 0.45f) return 2;
        if (animTimer < 1.95f) return 3; // El sprite 3 dura 1.5s
        if (animTimer < 2.10f) return 4;
        if (animTimer < 2.25f) return 5;
        if (animTimer < 2.40f) return 6;
        if (animTimer < 2.55f) return 7;
        return 8; // Terminado
    }

    public float getHeightOffset() {
        if (estadoBoss == BossState.SPINNING) {
            float progress = animTimer / 0.8f;
            return 3.0f * progress;
        } else if (estadoBoss == BossState.LANDING) {
            if (animTimer < 0.25f) {
                // Elevación rápida de 3.0f a 8.0f
                float progress = animTimer / 0.25f;
                return 3.0f + 5.0f * progress;
            } else if (animTimer < 0.50f) {
                // Caída en picada de 8.0f a 0.0f
                float progress = (animTimer - 0.25f) / 0.25f;
                return 8.0f * (1.0f - progress);
            } else {
                // Impacto en el suelo
                return 0f;
            }
        }
        return 0f;
    }

    @Override
    public Float getX() {
        if (estadoBoss == BossState.SPINNING) {
            if (targetTower != null) {
                float progress = animTimer / 0.8f;
                double angle = progress * 2 * Math.PI;
                double radius = 1.5 * Math.sin(progress * Math.PI);
                return (float) (targetTower.getX() + 0.5f + radius * Math.cos(angle));
            }
        } else if (estadoBoss == BossState.LANDING) {
            if (targetTower != null) {
                return targetTower.getX() + 0.5f;
            }
        }
        return this.x;
    }

    @Override
    public Float getY() {
        if (estadoBoss == BossState.SPINNING) {
            if (targetTower != null) {
                float progress = animTimer / 0.8f;
                double angle = progress * 2 * Math.PI;
                double radius = 1.5 * Math.sin(progress * Math.PI);
                float altitude = 3.0f * progress;
                return (float) (targetTower.getY() + 0.5f + radius * Math.sin(angle) - altitude);
            }
        } else if (estadoBoss == BossState.LANDING) {
            if (targetTower != null) {
                float altitude = getHeightOffset();
                return targetTower.getY() + 0.5f - altitude;
            }
        }
        return this.y;
    }

    @Override
    public Float getWidth() {
        float baseWidth = this.width;
        if (estadoBoss == BossState.SPINNING || estadoBoss == BossState.LANDING) {
            float ho = getHeightOffset();
            return baseWidth * (1.0f + 0.3f * (ho / 8.0f));
        }
        return baseWidth;
    }

    @Override
    public Float getHeight() {
        float baseHeight = this.height;
        if (estadoBoss == BossState.SPINNING || estadoBoss == BossState.LANDING) {
            float ho = getHeightOffset();
            return baseHeight * (1.0f + 0.3f * (ho / 8.0f));
        }
        return baseHeight;
    }

    @Override
    public int getLayer() {
        if (estadoBoss == BossState.LANDING) {
            float impactTime = animTimer - 0.50f;
            if (impactTime >= 0f && impactTime < 0.08f) {
                return 5; // Dibujado por debajo del efecto visual (layer 9) en el impacto inicial
            }
        }
        return 11; // Dibujado por encima del efecto visual (layer 9) y torres (layer 10)
    }

    // --- SPRITES ---
    
    @Override
    public Optional<String> getImagePath() {
        String path = "assets/ingame/enemies/boss_Peedy/";

        if (estadoBoss == BossState.FLYING) {
            // Calcular dirección de vuelo basada en targetTower o coordenadas de destino libre
            float tx = (targetTower != null) ? targetTower.getX() + 0.5f : destX;
            float ty = (targetTower != null) ? targetTower.getY() + 0.5f : destY;
            float dx = tx - this.x;
            float dy = ty - this.y;
            double angle = Math.atan2(dy, dx);
            if (angle < 0) angle += 2 * Math.PI;
            int octant = (int) Math.round(angle / (Math.PI / 4)) % 8;
            int flyOctant = (octant + 2) % 8;

            int[] flyingFrames = {6, 5, 7, 5, 5, 1, 6, 5};
            int fMax = flyingFrames[flyOctant];
            int frameVolando = (int) ((System.currentTimeMillis() / 80) % fMax);
            return Optional.of(path + "Peedy_Volando" + flyOctant + "_" + frameVolando + ".png");
        }
        
        // Calcular dirección basada en coordenadas de destino libre
        float dx = destX - this.x;
        float dy = destY - this.y;
        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * Math.PI;
        int octant = (int) Math.round(angle / (Math.PI / 4)) % 8;
        int uDir = (octant + 2) % 8;

        if (estadoBoss == BossState.STUNNING) {
            int frame = getStunningFrame();
            if (frame > 7) frame = 7;
            return Optional.of(path + "peedy_Stunning_" + frame + ".png");
        } else if (estadoBoss == BossState.SPINNING) {
            // 360 loop rápido sobre la torre usando las 8 direcciones de vuelo sucesivamente basadas en el ángulo tangente
            double tangentAngle = (animTimer / 0.8f) * 2 * Math.PI + Math.PI / 2;
            if (tangentAngle < 0) tangentAngle += 2 * Math.PI;
            int spinAngleOctant = (int) Math.round(tangentAngle / (Math.PI / 4)) % 8;
            int spinOctant = (spinAngleOctant + 2) % 8;
            int[] flyingFrames = {6, 5, 7, 5, 5, 1, 6, 5};
            int fMax = flyingFrames[spinOctant];
            int frameVolando = (int) ((System.currentTimeMillis() / 80) % fMax);
            return Optional.of(path + "Peedy_Volando" + spinOctant + "_" + frameVolando + ".png");
        } else if (estadoBoss == BossState.LANDING) {
            if (animTimer < 0.25f) {
                // Elevación: volando hacia arriba (octante 0)
                int frameVolando = (int) ((System.currentTimeMillis() / 80) % 6);
                return Optional.of(path + "Peedy_Volando0_" + frameVolando + ".png");
            } else if (animTimer < 0.50f) {
                // Caída: picada con sprite 0
                return Optional.of(path + "peedy_cayendo_picada_superherolanding_0.png");
            } else {
                // Impacto:
                // - Sprite 1 (durante los primeros 0.08s)
                // - Sprite 2 (desde 0.08s hasta 0.25s tras impacto, es decir, animTimer < 0.75f)
                // - Sprite 3 (desde 0.25s hasta 1.00s tras impacto, es decir, animTimer < 1.50f)
                float impactTime = animTimer - 0.50f;
                if (impactTime < 0.08f) {
                    return Optional.of(path + "peedy_cayendo_picada_superherolanding_1.png");
                } else if (impactTime < 0.25f) {
                    return Optional.of(path + "peedy_cayendo_picada_superherolanding_2.png");
                } else {
                    return Optional.of(path + "peedy_cayendo_picada_superherolanding_3.png");
                }
            }
        } else if (estadoBoss == BossState.RECOMPOSING) {
            // Animación recomponiéndose del superherolanding
            int frame = (int) (animTimer / 0.1f) % 5;
            return Optional.of(path + "peedy_recomponiendose_del_superherolanding_" + frame + ".png");
        } else {
            // Caminando
            int[] walkingFrames = {8, 7, 9, 6, 7, 5, 8, 7};
            int fCount = walkingFrames[uDir];
            int frameCaminando = (int) ((System.currentTimeMillis() / 120) % fCount);
            
            String prefix = (uDir == 1) ? "Peedy_Caminando_" : "peedy_Caminando_";
            String filename = prefix + uDir + "_" + frameCaminando + ".png";
            return Optional.of(path + filename);
        }
    }
}
