package com.miJuego.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Nivel {
    private int numeroNivel;
    private List<Enemigo> enemigosRestantes; // Enemigos activos en el mapa
    
    // Configuración del camino del nivel
    private List<float[]> waypoints;
    
    // Cola de enemigos por aparecer (oleada)
    private Queue<Enemigo> colaSpawn;
    private float spawnTimer = 0f;
    private float spawnInterval = 1.8f; // Segundos entre spawn de enemigos
    private int totalEnemigosOleada = 0;
    private int enemigosSpawnados = 0;

    public Nivel(int numeroNivel) {
        this.numeroNivel = numeroNivel;
        this.enemigosRestantes = new ArrayList<>();
        this.colaSpawn = new LinkedList<>();
        this.waypoints = new ArrayList<>();
        configurarNivel();
    }

    private void configurarNivel() {
        waypoints.clear();
        colaSpawn.clear();
        enemigosRestantes.clear();
        spawnTimer = 0f;
        enemigosSpawnados = 0;

        // Definimos caminos basados en un mundo de 20f x 15f
        switch (numeroNivel) {
            case 1:
                // ── Nivel Word ─────────────────────────────────────────────
                // Coordenadas trazadas píxel a píxel sobre la imagen concept art
                // (2752×1536) → mundo (20×15):  world = (pixel / imagen) * mundo
                //
                // Entrada desde borde derecho a la altura del punto 1
                waypoints.add(new float[]{20f,     5.225f}); // entrada borde derecho (Y = 535px)
                waypoints.add(new float[]{12.267f, 5.225f}); // punto 1: 1688x535
                waypoints.add(new float[]{10.327f, 6.777f}); // punto 2: 1421x694
                waypoints.add(new float[]{10.247f, 7.139f}); // punto 3: 1410x731
                waypoints.add(new float[]{10.443f, 7.422f}); // punto 4: 1437x760
                waypoints.add(new float[]{10.567f, 7.891f}); // punto 5: 1454x808
                waypoints.add(new float[]{10.058f, 8.428f}); // punto 6: 1384x863
                waypoints.add(new float[]{10.494f, 9.629f}); // punto 7: 1444x986
                waypoints.add(new float[]{10.102f, 10.146f});// punto 8: 1390x1039
                waypoints.add(new float[]{9.041f,  10.703f});// punto 9: 1244x1096
                waypoints.add(new float[]{7.798f,  11.650f});// punto 10: 1073x1193
                waypoints.add(new float[]{8.263f,  13.281f});// punto 11: 1137x1360
                waypoints.add(new float[]{6.265f,  14.902f});// punto 12: 862x1526
                waypoints.add(new float[]{6.265f,  15f});     // salida borde inferior (X = 862px)

                // Tutorial: 5 Pop-Ups con variantes rotativas
                spawnInterval = 2.2f;
                PopUp.Variante[] variantes = PopUp.Variante.values();
                for (int i = 0; i < 5; i++) {
                    colaSpawn.add(new PopUp("popup-" + i, variantes[i % variantes.length]));
                }
                break;

            case 2:
                // Camino largo en "U"
                waypoints.add(new float[]{0f, 3f});
                waypoints.add(new float[]{16f, 3f});
                waypoints.add(new float[]{16f, 12f});
                waypoints.add(new float[]{4f, 12f});
                waypoints.add(new float[]{4f, 7f});
                waypoints.add(new float[]{20f, 7f});
                
                // Enemigos: 6 Duendes, 4 Enemigos Comunes con Escudo
                for (int i = 0; i < 6; i++) {
                    colaSpawn.add(new Duende("lvl2-duende-" + i));
                }
                for (int i = 0; i < 4; i++) {
                    colaSpawn.add(new EnemigoComun("lvl2-comun-escudo-" + i, true)); // Con escudo eléctrico
                }
                break;

            case 3:
                // Entrada vertical superior (10,0), salida inferior (17,15)
                waypoints.add(new float[]{10f, 0f});
                waypoints.add(new float[]{10f, 5f});
                waypoints.add(new float[]{3f, 5f});
                waypoints.add(new float[]{3f, 10f});
                waypoints.add(new float[]{17f, 10f});
                waypoints.add(new float[]{17f, 15f});
                
                // Enemigos: 5 Duendes, 5 Enemigos Comunes, 3 Enemigos Múltiples
                for (int i = 0; i < 5; i++) {
                    colaSpawn.add(new Duende("lvl3-duende-" + i));
                }
                for (int i = 0; i < 5; i++) {
                    colaSpawn.add(new EnemigoComun("lvl3-comun-" + i, i % 2 == 0));
                }
                for (int i = 0; i < 3; i++) {
                    colaSpawn.add(new EnemigoMultiple("lvl3-multiple-" + i));
                }
                break;

            case 4:
                // Zigzag complejo horizontal
                waypoints.add(new float[]{0f, 2f});
                waypoints.add(new float[]{5f, 2f});
                waypoints.add(new float[]{5f, 13f});
                waypoints.add(new float[]{10f, 13f});
                waypoints.add(new float[]{10f, 2f});
                waypoints.add(new float[]{15f, 2f});
                waypoints.add(new float[]{15f, 13f});
                waypoints.add(new float[]{20f, 13f});
                
                // Enemigos: Mayor cantidad y rapidez
                for (int i = 0; i < 8; i++) {
                    colaSpawn.add(new Duende("lvl4-duende-" + i));
                }
                for (int i = 0; i < 6; i++) {
                    colaSpawn.add(new EnemigoComun("lvl4-comun-" + i, true));
                }
                for (int i = 0; i < 5; i++) {
                    colaSpawn.add(new EnemigoMultiple("lvl4-multiple-" + i));
                }
                break;

            case 5:
                // Espiral cerrado, camino de máxima duración
                waypoints.add(new float[]{0f, 1f});
                waypoints.add(new float[]{18f, 1f});
                waypoints.add(new float[]{18f, 13f});
                waypoints.add(new float[]{2f, 13f});
                waypoints.add(new float[]{2f, 5f});
                waypoints.add(new float[]{15f, 5f});
                waypoints.add(new float[]{15f, 9f});
                waypoints.add(new float[]{6f, 9f});
                waypoints.add(new float[]{6f, 7f});
                waypoints.add(new float[]{20f, 7f});
                
                // Enemigos: Oleada masiva con un jefe resistente al final
                for (int i = 0; i < 10; i++) {
                    colaSpawn.add(new Duende("lvl5-duende-" + i));
                }
                for (int i = 0; i < 8; i++) {
                    colaSpawn.add(new EnemigoComun("lvl5-comun-" + i, true));
                }
                for (int i = 0; i < 6; i++) {
                    colaSpawn.add(new EnemigoMultiple("lvl5-multiple-" + i));
                }
                
                // Creamos un enemigo especial: Boss (EnemigoComun muy fuerte y lento)
                EnemigoComun jefe = new EnemigoComun("lvl5-boss", true) {
                    @Override
                    public Color getFallbackColor() {
                        if (paralizacionTimer > 0 || ralentizarTimer > 0 || fuegoTimer > 0) {
                            return super.getFallbackColor();
                        }
                        return new Color(0, 0, 0); // Color negro imponente para el Boss
                    }
                };
                jefe.setVida(1000.0);
                jefe.rapidez = 0.7; // Muy lento
                jefe.setMonedasGeneradas(200.0);
                jefe.setScoreGenerado(150.0);
                jefe.dañoBase = 5.0; // Resta 5 vidas si cruza
                colaSpawn.add(jefe);
                break;

            default:
                // Por si hay algún nivel extra inesperado, un camino simple
                waypoints.add(new float[]{0f, 7.5f});
                waypoints.add(new float[]{20f, 7.5f});
                colaSpawn.add(new Duende("extra-duende-0"));
                break;
        }

        totalEnemigosOleada = colaSpawn.size();
    }

    public void iniciarOleada() {
        configurarNivel();
    }

    public boolean verificarFinDeNivel() {
        return colaSpawn.isEmpty() && enemigosRestantes.isEmpty();
    }

    public void updateSpawn(float deltaSeconds) {
        if (colaSpawn.isEmpty()) {
            return;
        }
        spawnTimer += deltaSeconds;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            Enemigo spawnado = colaSpawn.poll();
            if (spawnado != null) {
                // Posicionar en el primer waypoint
                float[] start = waypoints.get(0);
                spawnado.setPosicion(start[0], start[1]);
                spawnado.setWaypointIndex(1); // Va hacia el segundo waypoint
                enemigosRestantes.add(spawnado);
                enemigosSpawnados++;
            }
        }
    }

    // Devuelve true si un casillero (ix, iy) está sobre el camino
    public boolean intersectsPath(int ix, int iy) {
        float cx = ix + 0.5f;
        float cy = iy + 0.5f;

        for (int i = 0; i < waypoints.size() - 1; i++) {
            float[] p1 = waypoints.get(i);
            float[] p2 = waypoints.get(i + 1);

            if (isPointNearSegment(cx, cy, p1[0], p1[1], p2[0], p2[1], 0.4f)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPointNearSegment(float px, float py, float x1, float y1, float x2, float y2, float threshold) {
        float l2 = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
        if (l2 == 0) return Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1)) < threshold;
        
        // Proyección del punto sobre la línea
        float t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2;
        t = Math.max(0, Math.min(1, t));
        
        float projectionX = x1 + t * (x2 - x1);
        float projectionY = y1 + t * (y2 - y1);
        
        double dist = Math.sqrt((px - projectionX) * (px - projectionX) + (py - projectionY) * (py - projectionY));
        return dist < threshold;
    }

    // Getters
    public int getNumeroNivel() {
        return numeroNivel;
    }

    public List<Enemigo> getEnemigosRestantes() {
        return enemigosRestantes;
    }

    public List<float[]> getWaypoints() {
        return waypoints;
    }

    public int getEnemigosRestantesCount() {
        return colaSpawn.size() + enemigosRestantes.size();
    }
}
