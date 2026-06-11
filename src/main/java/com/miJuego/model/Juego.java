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
        restart(nivelActual != null ? nivelActual.getNumeroNivel() : 1);
    }

    public void restart(int levelNum) {
        int initialCoins = (levelNum >= 2) ? 400 : 150;
        if (levelNum == 99) initialCoins = 1000;
        this.jugador = new Jugador(0, 20, initialCoins); // 20 vidas, oro inicial según nivel
        this.nivelActual = new Nivel(levelNum);
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

            // Si es Boss Peedy, procesar habilidades y movimiento libre (no sigue waypoints)
            if (enemigo instanceof BossPeedy peedy) {
                peedy.updateHabilidades(deltaSeconds, torres, enemigos, efectosVisuales);
                
                if (peedy.getEstadoBoss() == BossPeedy.BossState.STUNNING ||
                    peedy.getEstadoBoss() == BossPeedy.BossState.SPINNING ||
                    peedy.getEstadoBoss() == BossPeedy.BossState.LANDING ||
                    peedy.getEstadoBoss() == BossPeedy.BossState.RECOMPOSING) {
                    // Se queda totalmente quieto durante estas animaciones cinemáticas
                    continue;
                }
                
                // Determinar coordenadas de destino (hacia una torre si está atacando, o destino libre)
                float tx = peedy.getDestX();
                float ty = peedy.getDestY();
                
                if (peedy.getEstadoBoss() == BossPeedy.BossState.FLYING && peedy.getTargetTower() != null) {
                    Torre t = peedy.getTargetTower();
                    if (torres.contains(t)) {
                        tx = t.getX() + 0.5f;
                        ty = t.getY() + 0.5f;
                    } else {
                        // La torre objetivo ya no existe
                        peedy.terminarVuelo();
                        peedy.elegirDestinoAleatorio();
                        tx = peedy.getDestX();
                        ty = peedy.getDestY();
                    }
                }
                
                float dx = tx - peedy.getX();
                float dy = ty - peedy.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                double speed = (peedy.getEstadoBoss() == BossPeedy.BossState.FLYING) ? 8.0 : peedy.getVelocidadActual();
                
                if (dist <= speed * deltaSeconds) {
                    peedy.setPosicion(tx, ty);
                    if (peedy.getEstadoBoss() == BossPeedy.BossState.FLYING) {
                        if (peedy.getTargetTower() != null) {
                            // Iniciamos la secuencia cinemática (360 -> landing -> recompose)
                            peedy.setEstadoBoss(BossPeedy.BossState.SPINNING);
                            peedy.resetAnimTimer();
                        } else {
                            // Era un vuelo a una coordenada libre, vuelve a caminar
                            peedy.terminarVuelo();
                            peedy.elegirDestinoAleatorio();
                        }
                    } else {
                        // Caminando libre, elige otro destino
                        peedy.elegirDestinoAleatorio();
                    }
                } else {
                    // Avanzar hacia el destino
                    float newX = peedy.getX() + (float) (dx / dist * speed * deltaSeconds);
                    float newY = peedy.getY() + (float) (dy / dist * speed * deltaSeconds);
                    peedy.setPosicion(newX, newY);
                }
                
                continue; // Omite el movimiento terrestre de waypoints
            }

            // Verificar si murió por daño de fuego/efectos en este tick
            if (enemigo.isDead()) {
                enemyIterator.remove();
                handleEnemyDeath(enemigo, nuevosEnemigos);
                continue;
            }

            // Movimiento
            WaypointNode targetNode = enemigo.getTargetNode();
            if (targetNode != null) {
                float dx = targetNode.x - enemigo.getX();
                float dy = targetNode.y - enemigo.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                double speed = enemigo.getVelocidadActual();

                if (dist <= speed * deltaSeconds) {
                    // Llegó al nodo
                    enemigo.setPosicion(targetNode.x, targetNode.y);
                    enemigo.avanzarNodo();
                    
                    if (targetNode.siguientes.isEmpty()) {
                        enemyIterator.remove();
                        jugador.decreaseHealth((int) enemigo.dañorBase());
                        if (jugador.getHealth() <= 0) {
                            estado = EstadoJuego.GAME_OVER;
                        }
                    } else {
                        // Siguiente rama (aleatoria si hay bifurcación)
                        int randomIndex = (int) (Math.random() * targetNode.siguientes.size());
                        enemigo.setTargetNode(targetNode.siguientes.get(randomIndex));
                    }
                } else {
                    // Avanzar hacia el nodo
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
        java.util.List<Bala> nuevasBalas = new java.util.ArrayList<>();
        
        while (bIt.hasNext()) {
            Bala bala = bIt.next();
            bala.update(deltaSeconds);
            if (bala.isHit()) {
                bIt.remove();
                
                // Efecto splash adicional si es Avast
                if (bala.getSourceTower() instanceof TorreAvast) {
                    TorreAvast avast = (TorreAvast) bala.getSourceTower();
                    efectosVisuales.add(new VisualEffect(
                        bala.getX(), bala.getY(), 0, 0, 
                        0.5f, "explosion", 2.0f
                    ));
                    
                    double rangeSq = 2.0f * 2.0f; // Splash radius = 2 celdas
                    for (Enemigo e : enemigos) {
                        if (e != bala.getTarget()) {
                            double dx = e.getX() - bala.getX();
                            double dy = e.getY() - bala.getY();
                            if (dx * dx + dy * dy <= rangeSq) {
                                e.setVida(e.GetVida() - (avast.isManualTargetingMode() ? 20.0 : 15.0) * avast.getNivelMejora()); // Daño splash. Mayor si es en modo zona
                            }
                        }
                    }
                } else if (bala.getSourceTower() instanceof TorreDeFuego fuego) {
                    double areaSq = fuego.getAreaAGolpear() * fuego.getAreaAGolpear();
                    double dps = fuego.getDañoPorQuemadura() * fuego.getNivelMejora();
                    for (Enemigo e : enemigos) {
                        if (e != bala.getTarget()) {
                            double dx = e.getX() - bala.getX();
                            double dy = e.getY() - bala.getY();
                            if (dx * dx + dy * dy <= areaSq) {
                                e.aplicarFuego(dps, 3.0f);
                                e.setVida(e.GetVida() - 5.0 * fuego.getNivelMejora());
                            }
                        }
                    }
                } else if (bala.getSourceTower() instanceof TorreMessenger messenger && bala.getTarget() != null) {
                    int bounces = bala.getBounces();
                    if (bounces < messenger.getRebotesMaximos()) {
                        Enemigo nextTarget = null;
                        double closestSq = messenger.getRangoDeRebote() * messenger.getRangoDeRebote();
                        for (Enemigo e : enemigos) {
                            if (e != bala.getTarget() && e.GetVida() > 0 && !bala.getHitEnemies().contains(e)) {
                                double dx = e.getX() - bala.getTarget().getX();
                                double dy = e.getY() - bala.getTarget().getY();
                                double distSq = dx * dx + dy * dy;
                                if (distSq <= closestSq) {
                                    closestSq = distSq;
                                    nextTarget = e;
                                }
                            }
                        }
                        if (nextTarget != null) {
                            Bala newBala = new Bala("bala-" + (++idCounter), messenger, nextTarget, bala.getDaño() * 0.8, bala.getTarget().getX() + 0.3f, bala.getTarget().getY() + 0.3f);
                            newBala.setBounces(bounces + 1);
                            newBala.getHitEnemies().addAll(bala.getHitEnemies());
                            newBala.getHitEnemies().add(bala.getTarget());
                            nuevasBalas.add(newBala);
                        }
                    }
                }
            }
        }
        
        // Agregar los rebotes generados
        balas.addAll(nuevasBalas);

        // 4. Torres buscando objetivos y disparando
        for (Torre torre : torres) {
            torre.updateCooldown(deltaSeconds);
            torre.findTarget(enemigos);
            if (torre.canShoot()) {
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
                List<Bala> generatedBullets = ((Atacante) torre).atacar(targetsInRange, () -> "bala-" + (++idCounter));
                if (generatedBullets != null && !generatedBullets.isEmpty()) {
                    balas.addAll(generatedBullets);
                }
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



    private Enemigo selectFirstEnemy(List<Enemigo> list) {
        Enemigo first = list.get(0);
        for (Enemigo e : list) {
            if (e.getNodosVisitados() > first.getNodosVisitados()) {
                first = e;
            } else if (e.getNodosVisitados() == first.getNodosVisitados()) {
                WaypointNode wpE = e.getTargetNode();
                WaypointNode wpF = first.getTargetNode();
                if (wpE != null && wpF != null) {
                    double distE = Math.hypot(wpE.x - e.getX(), wpE.y - e.getY());
                    double distF = Math.hypot(wpF.x - first.getX(), wpF.y - first.getY());
                    if (distE < distF) {
                        first = e;
                    }
                }
            }
        }
        return first;
    }

    public Enemigo selectFirstEnemyRecursivo(List<Enemigo> lista, int indice) {
        if (lista == null || lista.isEmpty()) return null;
        if (indice == lista.size() - 1) {
            return lista.get(indice);
        }
        
        Enemigo mejorDelResto = selectFirstEnemyRecursivo(lista, indice + 1);
        Enemigo actual = lista.get(indice);
        
        if (actual.getNodosVisitados() > mejorDelResto.getNodosVisitados()) {
            return actual;
        } else if (actual.getNodosVisitados() == mejorDelResto.getNodosVisitados()) {
            WaypointNode wpA = actual.getTargetNode();
            WaypointNode wpR = mejorDelResto.getTargetNode();
            if (wpA != null && wpR != null) {
                double distA = Math.hypot(wpA.x - actual.getX(), wpA.y - actual.getY());
                double distR = Math.hypot(wpR.x - mejorDelResto.getX(), wpR.y - mejorDelResto.getY());
                if (distA < distR) {
                    return actual;
                }
            }
        }
        return mejorDelResto;
    }


    // Colocar torre
    public boolean placeTower(int ix, int iy) {
        if (estado != EstadoJuego.PLAYING) {
            return false;
        }

        // Validar coordenadas dentro del grid dinámico
        if (ix < 0 || ix >= CameraContext.getWorldW() || iy < 0 || iy >= CameraContext.getWorldH()) {
            throw new IllegalArgumentException("Posición fuera de límites");
        }

        // Verificar si es camino
        if (nivelActual.intersectsPath(ix, iy)) {
            throw new IllegalStateException("No se pueden colocar torres en el camino");
        }

        // Verificar si está dentro de los límites de construcción del nivel
        if (!nivelActual.isValidPlacementArea(ix, iy)) {
            throw new IllegalStateException("Solo puedes colocar torres dentro del documento");
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
            case 2 -> 80;
            case 3 -> 150;
            case 4 -> 200;
            case 5 -> 250;
            case 6 -> 180;
            case 7 -> 175;
            case 8 -> 220;
            default -> 100;
        };
    }

    private Torre createTower(int type, int ix, int iy) {
        String tId = "torre-" + (++idCounter);
        return switch (type) {
            case 1 -> new TorreComun(tId, ix, iy);
            case 2 -> new TorreMcAfee(tId, ix, iy);
            case 3 -> new TorreDeArea(tId, ix, iy);
            case 4 -> new TorreAvast(tId, ix, iy);
            case 5 -> new TorreFuerte(tId, ix, iy);
            case 6 -> new TorreFirefox(tId, ix, iy);
            case 7 -> new TorreInternetExplorer(tId, ix, iy);
            case 8 -> new TorreMessenger(tId, ix, iy);
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
