package com.miJuego.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Juego {
    private Nivel nivelActual;
    private Jugador jugador;
    private EstadoJuego estado;

    // Elementos de juego
    private final List<Torre> torres;
    private final List<Bala> balas;
    
    // Configuración para el bucle y acciones
    private int selectedTowerType = 1; // 1: Comun, 2: Area, 3: Cañon, 4: Fuerte, 5: Fuego, 6: Hielo, 7: Electrica
    private static int idCounter = 0;
    
    // Lista de efectos visuales temporales (láseres, explosiones)
    public static class VisualEffect {
        public float x1, y1, x2, y2;
        public float duration; // en segundos
        public String type; // "laser", "explosion"
        public float size;
        
        public VisualEffect(float x1, float y1, float x2, float y2, float duration, String type, float size) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.duration = duration;
            this.type = type;
            this.size = size;
        }
    }
    private final List<VisualEffect> efectosVisuales;

    public Juego() {
        this.torres = new ArrayList<>();
        this.balas = new ArrayList<>();
        this.efectosVisuales = new ArrayList<>();
        restart();
    }

    public void restart() {
        this.jugador = new Jugador(0, 20, 150); // 20 vidas, 150 monedas
        this.nivelActual = new Nivel(1);
        this.torres.clear();
        this.balas.clear();
        this.efectosVisuales.clear();
        this.estado = EstadoJuego.START;
        this.selectedTowerType = 1;
    }

    public void nextLevel() {
        int nextLvl = nivelActual.getNumeroNivel() + 1;
        if (nextLvl <= 5) {
            this.nivelActual = new Nivel(nextLvl);
            this.balas.clear();
            this.efectosVisuales.clear();
            this.estado = EstadoJuego.PLAYING;
            this.nivelActual.iniciarOleada();
            // Le damos oro de bonificación
            jugador.addMoneda(100);
        } else {
            this.estado = EstadoJuego.VICTORY;
        }
    }

    public void update(float deltaSeconds) {
        if (estado != EstadoJuego.PLAYING) {
            return;
        }

        // 1. Spawner de enemigos
        nivelActual.updateSpawn(deltaSeconds);

        // 2. Mover y actualizar enemigos
        List<Enemigo> enemigos = nivelActual.getEnemigosRestantes();
        List<Enemigo> nuevosEnemigos = new ArrayList<>();
        Iterator<Enemigo> enemyIterator = enemigos.iterator();
        List<float[]> waypoints = nivelActual.getWaypoints();

        while (enemyIterator.hasNext()) {
            Enemigo enemigo = enemyIterator.next();
            
            // Actualizar efectos de estado (fuego, hielo, parálisis)
            enemigo.actualizarEfectosYDaño(deltaSeconds);

            // Verificar si murió por daño de fuego/efectos en este tick
            if (enemigo.isDead()) {
                enemyIterator.remove();
                handleEnemyDeath(enemigo, nuevosEnemigos);
                continue;
            }

            // Movimiento
            int wpIdx = enemigo.getWaypointIndex();
            if (wpIdx < waypoints.size()) {
                float[] wp = waypoints.get(wpIdx);
                float dx = wp[0] - enemigo.getX();
                float dy = wp[1] - enemigo.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                double speed = enemigo.getVelocidadActual();

                if (dist <= speed * deltaSeconds) {
                    // Llegó al waypoint
                    enemigo.setPosicion(wp[0], wp[1]);
                    enemigo.avanzarWaypoint();
                    
                    // Si ya pasó el último waypoint, entra a la base
                    if (enemigo.getWaypointIndex() >= waypoints.size()) {
                        enemyIterator.remove();
                        jugador.decreaseHealth((int) enemigo.dañorBase());
                        if (jugador.getHealth() <= 0) {
                            estado = EstadoJuego.GAME_OVER;
                        }
                    }
                } else {
                    // Avanzar hacia el waypoint
                    float newX = enemigo.getX() + (float) (dx / dist * speed * deltaSeconds);
                    float newY = enemigo.getY() + (float) (dy / dist * speed * deltaSeconds);
                    enemigo.setPosicion(newX, newY);
                }
            }
        }
        // Agregar los enemigos que se dividieron por muerte
        enemigos.addAll(nuevosEnemigos);

        // 3. Actualizar proyectiles (balas)
        Iterator<Bala> bIt = balas.iterator();
        while (bIt.hasNext()) {
            Bala bala = bIt.next();
            bala.update(deltaSeconds);
            if (bala.isHit()) {
                bIt.remove();
                
                // Efecto splash adicional si es cañón
                if (bala.getSourceTower() instanceof Cañon) {
                    Cañon cañon = (Cañon) bala.getSourceTower();
                    efectosVisuales.add(new VisualEffect(
                        bala.getX(), bala.getY(), 0, 0, 
                        0.25f, "explosion", (float) cañon.getAreaAGolpear()
                    ));
                    
                    double rangeSq = cañon.getAreaAGolpear() * cañon.getAreaAGolpear();
                    for (Enemigo e : enemigos) {
                        if (e != bala.getTarget()) {
                            double dx = e.getX() - bala.getX();
                            double dy = e.getY() - bala.getY();
                            if (dx * dx + dy * dy <= rangeSq) {
                                e.setVida(e.GetVida() - 15.0 * cañon.getNivelMejora()); // Daño splash
                            }
                        }
                    }
                }
            }
        }

        // 4. Torres buscando objetivos y disparando
        for (Torre torre : torres) {
            torre.updateCooldown(deltaSeconds);
            if (torre.canShoot()) {
                shootTower(torre, enemigos);
            }
        }

        // 5. Actualizar efectos visuales temporales
        Iterator<VisualEffect> fxIt = efectosVisuales.iterator();
        while (fxIt.hasNext()) {
            VisualEffect fx = fxIt.next();
            fx.duration -= deltaSeconds;
            if (fx.duration <= 0) {
                fxIt.remove();
            }
        }

        // 6. Verificar si el nivel se terminó
        if (nivelActual.verificarFinDeNivel()) {
            if (nivelActual.getNumeroNivel() == 5) {
                estado = EstadoJuego.VICTORY;
            } else {
                // Esperamos que el usuario pulse 'N' para pasar al siguiente nivel
            }
        }
    }

    private void handleEnemyDeath(Enemigo enemigo, List<Enemigo> nuevosEnemigos) {
        // Otorgar oro y puntaje
        jugador.addMoneda(enemigo.GetMonedasGeneradas());
        jugador.addScore(enemigo.GetScoreGenerado());
        
        // Efecto muerte (ej. dividir para el EnemigoMultiple)
        List<Enemigo> split = enemigo.morir();
        if (split != null && !split.isEmpty()) {
            nuevosEnemigos.addAll(split);
        }
    }

    private void shootTower(Torre torre, List<Enemigo> enemigos) {
        if (enemigos.isEmpty()) {
            return;
        }

        // Encontrar enemigos en rango
        List<Enemigo> targetsInRange = new ArrayList<>();
        double rangeSq = torre.getRango() * torre.getRango();
        for (Enemigo e : enemigos) {
            double dx = e.getX() - torre.getX();
            double dy = e.getY() - torre.getY();
            if (dx * dx + dy * dy <= rangeSq) {
                targetsInRange.add(e);
            }
        }

        if (targetsInRange.isEmpty()) {
            return;
        }

        // Heurística de selección de objetivos
        if (torre instanceof TorreDeArea) {
            TorreDeArea ta = (TorreDeArea) torre;
            // Daña a todos los enemigos en rango hasta su límite
            int count = 0;
            int max = ta.getCantidadEnemigosDañadoMax();
            for (Enemigo e : targetsInRange) {
                if (count >= max) break;
                // Dispara
                balas.add(new Bala("bala-" + (++idCounter), torre, e, 10.0));
                count++;
            }
            ta.setCantidadEnemigosDañado(count);
            torre.resetCooldown();
        } 
        else if (torre instanceof TorreFuerte) {
            // Prioriza al que tiene más vida
            Enemigo strongTarget = targetsInRange.get(0);
            for (Enemigo e : targetsInRange) {
                if (e.GetVida() > strongTarget.GetVida()) {
                    strongTarget = e;
                }
            }
            ((TorreFuerte) torre).setObjetivo(strongTarget);
            balas.add(new Bala("bala-" + (++idCounter), torre, strongTarget, 80.0));
            torre.resetCooldown();
        } 
        else if (torre instanceof TorreComun) {
            // Prioriza el que está más avanzado en el camino (First)
            Enemigo target = selectFirstEnemy(targetsInRange);
            balas.add(new Bala("bala-" + (++idCounter), torre, target, 15.0));
            torre.resetCooldown();
        } 
        else if (torre instanceof TorreDeFuego) {
            Enemigo target = selectFirstEnemy(targetsInRange);
            ((TorreDeFuego) torre).setObjetivo(target);
            balas.add(new Bala("bala-" + (++idCounter), torre, target, 5.0));
            torre.resetCooldown();
        } 
        else if (torre instanceof TorreDeHielo) {
            Enemigo target = selectFirstEnemy(targetsInRange);
            ((TorreDeHielo) torre).setObjetivo(target);
            balas.add(new Bala("bala-" + (++idCounter), torre, target, 0.0));
            torre.resetCooldown();
        } 
        else if (torre instanceof TorreElectrica) {
            Enemigo target = selectFirstEnemy(targetsInRange);
            ((TorreElectrica) torre).setObjetivo(target);
            balas.add(new Bala("bala-" + (++idCounter), torre, target, 10.0));
            torre.resetCooldown();
        }
        else if (torre instanceof Cañon) {
            Enemigo target = selectFirstEnemy(targetsInRange);
            ((Cañon) torre).setObjetivo(target);
            balas.add(new Bala("bala-" + (++idCounter), torre, target, 25.0));
            torre.resetCooldown();
        }
    }

    private Enemigo selectFirstEnemy(List<Enemigo> list) {
        Enemigo first = list.get(0);
        for (Enemigo e : list) {
            // El más avanzado es el que tiene mayor waypointIndex.
            // Si tienen el mismo, el que esté más cerca del waypoint de destino.
            if (e.getWaypointIndex() > first.getWaypointIndex()) {
                first = e;
            } else if (e.getWaypointIndex() == first.getWaypointIndex()) {
                // Distancia a waypoint
                float[] wp = nivelActual.getWaypoints().get(e.getWaypointIndex());
                double distE = Math.hypot(wp[0] - e.getX(), wp[1] - e.getY());
                double distF = Math.hypot(wp[0] - first.getX(), wp[1] - first.getY());
                if (distE < distF) {
                    first = e;
                }
            }
        }
        return first;
    }

    // Colocar torre
    public boolean placeTower(int ix, int iy) {
        if (estado != EstadoJuego.PLAYING) {
            return false;
        }

        // Validar coordenadas dentro del grid de 20x15
        if (ix < 0 || ix >= 20 || iy < 0 || iy >= 15) {
            throw new IllegalArgumentException("Posición fuera de límites");
        }

        // Verificar si es camino
        if (nivelActual.intersectsPath(ix, iy)) {
            throw new IllegalStateException("No se pueden colocar torres en el camino");
        }

        // Verificar si ya hay una torre
        for (Torre t : torres) {
            if (Math.round(t.getX()) == ix && Math.round(t.getY()) == iy) {
                throw new IllegalStateException("Ya hay una torre en esta posición");
            }
        }

        // Determinar costo según tipo seleccionado
        int cost = getTowerCost(selectedTowerType);
        if (!jugador.spendMoneda(cost)) {
            throw new IllegalStateException("Oro insuficiente. Necesitas " + cost + " monedas");
        }

        // Crear la torre
        Torre nueva = createTower(selectedTowerType, ix, iy);
        torres.add(nueva);
        return true;
    }

    // Vender torre
    public void sellTowerAt(int ix, int iy) {
        Torre found = null;
        for (Torre t : torres) {
            if (Math.round(t.getX()) == ix && Math.round(t.getY()) == iy) {
                found = t;
                break;
            }
        }

        if (found == null) {
            throw new IllegalStateException("No hay ninguna torre en esta posición");
        }

        torres.remove(found);
        // Reembolsar 50% de su costo actual
        int refund = (int) (found.GetCostoTorre() * 0.5);
        jugador.addMoneda(refund);
    }

    // Mejorar torre
    public void upgradeTowerAt(int ix, int iy) {
        Torre found = null;
        for (Torre t : torres) {
            if (Math.round(t.getX()) == ix && Math.round(t.getY()) == iy) {
                found = t;
                break;
            }
        }

        if (found == null) {
            throw new IllegalStateException("No hay ninguna torre en esta posición");
        }

        int upgradeCost = (int) (found.GetCostoTorre() * 0.5);
        if (!jugador.spendMoneda(upgradeCost)) {
            throw new IllegalStateException("Oro insuficiente para mejorar. Necesitas " + upgradeCost + " monedas");
        }

        found.upgrade();
    }

    private int getTowerCost(int type) {
        return switch (type) {
            case 1 -> 100;
            case 2 -> 150;
            case 3 -> 200;
            case 4 -> 250;
            case 5 -> 180;
            case 6 -> 150;
            case 7 -> 220;
            default -> 100;
        };
    }

    private Torre createTower(int type, int ix, int iy) {
        String tId = "torre-" + (++idCounter);
        return switch (type) {
            case 1 -> new TorreComun(tId, ix, iy);
            case 2 -> new TorreDeArea(tId, ix, iy);
            case 3 -> new Cañon(tId, ix, iy);
            case 4 -> new TorreFuerte(tId, ix, iy);
            case 5 -> new TorreDeFuego(tId, ix, iy);
            case 6 -> new TorreDeHielo(tId, ix, iy);
            case 7 -> new TorreElectrica(tId, ix, iy);
            default -> new TorreComun(tId, ix, iy);
        };
    }

    // Getters y Setters de control
    public Nivel getNivelActual() {
        return nivelActual;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public EstadoJuego getEstado() {
        return estado;
    }

    public void setEstado(EstadoJuego estado) {
        this.estado = estado;
    }

    public List<Torre> getTorres() {
        return torres;
    }

    public List<Bala> getBalas() {
        return balas;
    }

    public List<VisualEffect> getEfectosVisuales() {
        return efectosVisuales;
    }

    public int getSelectedTowerType() {
        return selectedTowerType;
    }

    public void setSelectedTowerType(int type) {
        this.selectedTowerType = type;
    }
}
