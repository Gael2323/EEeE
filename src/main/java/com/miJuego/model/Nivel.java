package com.miJuego.model;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Nivel {
    private int numeroNivel;
    private List<Enemigo> enemigosRestantes; // Enemigos activos en el mapa
    
    // Configuración del camino del nivel
    private List<float[]> waypoints;
    private List<WaypointNode> spawnNodes;
    
    // Cola de oleadas (generics)
    private List<Oleada<? extends Enemigo>> oleadas;
    private Oleada<? extends Enemigo> oleadaActualObj;
    
    private float spawnTimer = 0f;
    private float spawnInterval = 1.8f; // Segundos entre spawn de enemigos
    private int totalEnemigosOleada = 0;
    private int enemigosSpawnados = 0;
    private int selectedInitialSpawnIndex = -1;
    
    private int oleadaActual = 1;
    private int maximaOleadas = 5;

    private Random random = new Random();

    // Límite de área de construcción para Nivel 1 (el documento de Word)
    private Path2D.Float buildPolygon;

    public Nivel(int numeroNivel) {
        this.numeroNivel = numeroNivel;
        this.enemigosRestantes = new ArrayList<>();
        this.oleadas = new ArrayList<>();
        this.waypoints = new ArrayList<>();
        this.spawnNodes = new ArrayList<>();
        configurarNivel();
    }

    private WaypointNode crearNodo(float px, float py, float scaleX, float scaleY) {
        WaypointNode node = new WaypointNode(px * scaleX, py * scaleY);
        waypoints.add(new float[]{node.x, node.y});
        return node;
    }

    private void configurarNivel() {
        waypoints.clear();
        spawnNodes.clear();
        oleadas.clear();
        oleadaActualObj = null;
        enemigosRestantes.clear();
        spawnTimer = 0f;
        enemigosSpawnados = 0;

        if (numeroNivel == 1) {
            buildPolygon = new Path2D.Float();
            buildPolygon.moveTo(8.1279f, 23.4375f);
            buildPolygon.lineTo(2.0232f, 18.6718f);
            buildPolygon.lineTo(16.3488f, 7.3281f);
            buildPolygon.lineTo(32.0f, 17.125f);
            buildPolygon.lineTo(24.5348f, 23.5f);
            buildPolygon.closePath();

            // Ruta Lineal
            float[][] pts = {
                {32f, 8.360f}, {19.627f, 8.360f}, {16.523f, 10.843f},
                {16.395f, 11.422f}, {16.709f, 11.875f}, {16.907f, 12.626f},
                {16.093f, 13.485f}, {16.790f, 15.406f}, {16.163f, 16.234f},
                {14.466f, 17.125f}, {12.477f, 18.640f}, {13.221f, 21.250f},
                {10.024f, 23.843f}, {10.024f, 24f}
            };
            WaypointNode last = null;
            WaypointNode first = null;
            for(float[] p : pts) {
                WaypointNode n = crearNodo(p[0], p[1], 1f, 1f);
                if (first == null) first = n;
                if (last != null) last.addSiguiente(n);
                last = n;
            }
            spawnNodes.add(first);

            // Wave: 15 Pop-Ups con variantes
            spawnInterval = 2.0f;
            Oleada<PopUp> oleada1 = new Oleada<>(1);
            for (int i = 0; i < 15; i++) {
                PopUp.Variante var;
                if (i < 3) var = PopUp.Variante.ERROR;
                else if (i < 8) var = PopUp.Variante.PREMIO;
                else var = PopUp.Variante.DESCARGA;
                
                PopUp p = new PopUp("popup-" + i, var);
                oleada1.addEnemigo(p);
            }
            oleadas.add(oleada1);
            oleadaActualObj = oleada1;
        } else if (numeroNivel == 99) {
            // Nivel 99 Simple (Test)
            buildPolygon = null;
            
            // Ruta lineal horizontal simple en el medio
            float[][] pts = {
                {0f, 24f}, {16f, 24f}, {32f, 24f}, {48f, 24f}, {64f, 24f}
            };
            WaypointNode last = null;
            WaypointNode first = null;
            for(float[] p : pts) {
                WaypointNode n = crearNodo(p[0], p[1], 1f, 1f);
                if (first == null) first = n;
                if (last != null) last.addSiguiente(n);
                last = n;
            }
            spawnNodes.add(first);
            
            maximaOleadas = 2; // Test nivel con 2 oleadas
            // Oleada 1: Cinemática (vacía pero con flag)
            Oleada<Enemigo> oleadaCinematica = new Oleada<>(1);
            oleadaCinematica.setCinematicWave(true);
            oleadas.add(oleadaCinematica);
            
            // Oleada 2: Boss Peedy + Duendes
            Oleada<Enemigo> oleadaBoss = new Oleada<>(2);
            oleadaBoss.setBossWave(true);
            BossPeedy peedy = new BossPeedy("lvl99-bosspeedy");
            oleadaBoss.addEnemigo(peedy);
            // Agregamos algunos enemigos normales para molestar
            for (int i=0; i<10; i++) {
                oleadaBoss.addEnemigo(new Duende("lvl99-duende-" + i));
            }
            oleadas.add(oleadaBoss);
            
            oleadaActualObj = oleadas.get(0);
        } else {
            // Niveles > 1 (Mundo aleatorio)
            buildPolygon = null;
            
            // La imagen de la papelera es 1672x941. El mundo lógico es 64x48.
            float scaleX = 64f / 1672f;
            float scaleY = 48f / 941f;
            
            // SPAWNS
            WaypointNode spawn1 = crearNodo(331, 221, scaleX, scaleY);
            WaypointNode spawn2 = crearNodo(513, 181, scaleX, scaleY);
            WaypointNode spawn3 = crearNodo(683, 173, scaleX, scaleY);
            spawnNodes.add(spawn1);
            spawnNodes.add(spawn2);
            spawnNodes.add(spawn3);

            // Nodos del camino 1
            float[][] c1Coords = {
                {346,230}, {442,308}, {430,356}, {464,398}, {546,440},
                {462,524}, {439,576}, {467,620}, {550,668}, {727,735},
                {784,768}, {1092,598}, {1272,691}, {1290,692}, {1376,708},
                {1455,676}, {1507,623}, {1480,554}, {1438,520} // END
            };
            List<WaypointNode> nodosC1 = new ArrayList<>();
            for(float[] p : c1Coords) {
                nodosC1.add(crearNodo(p[0], p[1], scaleX, scaleY));
            }
            spawn1.addSiguiente(nodosC1.get(0));
            for(int i=0; i<nodosC1.size()-1; i++) {
                nodosC1.get(i).addSiguiente(nodosC1.get(i+1));
            }

            // Nodos del camino 2
            float[][] c2Coords = {
                {530,210}, {625,264}, {663,337}, {753,383}, {767,422}, {712,561}
            };
            List<WaypointNode> nodosC2 = new ArrayList<>();
            for(float[] p : c2Coords) {
                nodosC2.add(crearNodo(p[0], p[1], scaleX, scaleY));
            }
            spawn2.addSiguiente(nodosC2.get(0));
            for(int i=0; i<nodosC2.size()-1; i++) {
                nodosC2.get(i).addSiguiente(nodosC2.get(i+1));
            }
            
            // Bifurcacion de Spawn 2
            WaypointNode n2A = crearNodo(523, 643, scaleX, scaleY);
            n2A.addSiguiente(nodosC1.get(8)); // Se conecta con {550,668} (índice 8 de C1)
            
            WaypointNode n2B = crearNodo(980, 407, scaleX, scaleY); // Este es un nodo del camino 3 también
            
            nodosC2.get(5).addSiguiente(n2A);
            nodosC2.get(5).addSiguiente(n2B); // El enemigo decide aleatorio en 712,561

            // Nodos del camino 3
            float[][] c3Coords = {
                {691,180}, {833,245}, {851,343}
            };
            List<WaypointNode> nodosC3 = new ArrayList<>();
            for(float[] p : c3Coords) {
                nodosC3.add(crearNodo(p[0], p[1], scaleX, scaleY));
            }
            spawn3.addSiguiente(nodosC3.get(0));
            for(int i=0; i<nodosC3.size()-1; i++) {
                nodosC3.get(i).addSiguiente(nodosC3.get(i+1));
            }
            nodosC3.get(2).addSiguiente(n2B); // Conecta {851,343} con {980,407} (Bifurcacion B de c2)
            
            // Nodos finales desde n2B ({980,407})
            float[][] c3BifurCoords = {
                {1067,413}, {1125,442}, {1139,484}, {1088,541}, {1076,574}, {1107,598}
            };
            List<WaypointNode> nodosC3Fin = new ArrayList<>();
            for(float[] p : c3BifurCoords) {
                nodosC3Fin.add(crearNodo(p[0], p[1], scaleX, scaleY));
            }
            n2B.addSiguiente(nodosC3Fin.get(0));
            // Aca se conecta con el otro waypoint y el enemigo puede elegir ese camino
            // Podemos permitir que n2B vuelva a conectarse al camino 1 o siga su propio camino
            n2B.addSiguiente(nodosC1.get(11)); // Se une a {1092,598} de c1 (opción aleatoria 2)

            for(int i=0; i<nodosC3Fin.size()-1; i++) {
                nodosC3Fin.get(i).addSiguiente(nodosC3Fin.get(i+1));
            }
            // El final de la rama se une al camino principal (c1) que va a la salida
            nodosC3Fin.get(5).addSiguiente(nodosC1.get(11)); // {1107,598} se une a {1092,598}

            generarEnemigosOleadaAleatoria();
        }

        totalEnemigosOleada = oleadaActualObj != null ? oleadaActualObj.size() : 0;
    }

    private void generarEnemigosOleadaAleatoria() {
        Oleada<Enemigo> nuevaOleada = new Oleada<>(oleadaActual);
        // OLEADA ALEATORIA (Dificultad progresiva por nivel y por oleada)
        float dificultadGlobal = numeroNivel + (oleadaActual * 0.4f);
        int cantEnemigos = (int) (dificultadGlobal * 8) + random.nextInt(10);
        spawnInterval = Math.max(0.3f, 2.0f - (dificultadGlobal * 0.12f));

        for(int i=0; i<cantEnemigos; i++) {
            Enemigo e;
            int tipo = random.nextInt(100);
            // Mayor dificultad, más chance de enemigos difíciles
            if (tipo < 40 - (dificultadGlobal * 2)) {
                e = new Duende("lvl" + numeroNivel + "-w" + oleadaActual + "-duende-" + i);
            } else if (tipo < 70 - (dificultadGlobal * 1.5)) {
                e = new EnemigoComun("lvl" + numeroNivel + "-w" + oleadaActual + "-comun-" + i, random.nextBoolean());
            } else if (tipo < 90) {
                e = new EnemigoMultiple("lvl" + numeroNivel + "-w" + oleadaActual + "-multiple-" + i);
            } else {
                e = new PopUp("lvl" + numeroNivel + "-w" + oleadaActual + "-popup-" + i, PopUp.Variante.ERROR);
            }

            // Escalado de stats
            e.setVida(e.GetVida() * (1 + (dificultadGlobal * 0.15)));
            
            nuevaOleada.addEnemigo(e);
        }
        oleadas.add(nuevaOleada);
        if (oleadaActualObj == null) {
            oleadaActualObj = nuevaOleada;
        }
    }

    public int getOleadaActual() {
        return oleadaActual;
    }

    public int getMaximaOleadas() {
        return maximaOleadas;
    }

    public void prepararSiguienteOleada() {
        if (oleadaActual < maximaOleadas) {
            oleadaActual++;
            if (numeroNivel != 99 && numeroNivel != 1) { // 99 y 1 ya tienen sus oleadas precargadas
                generarEnemigosOleadaAleatoria();
                oleadaActualObj = oleadas.get(oleadas.size() - 1);
            } else {
                oleadaActualObj = oleadas.get(oleadaActual - 1);
            }
            totalEnemigosOleada = oleadaActualObj.size();
            enemigosSpawnados = 0;
            this.spawnPaused = true;
        }
    }

    public void iniciarOleada() {
        this.spawnPaused = false;
    }

    private boolean spawnPaused = false;

    public void setSpawnPaused(boolean spawnPaused) {
        this.spawnPaused = spawnPaused;
    }

    public boolean isSpawnPaused() {
        return spawnPaused;
    }

    public boolean verificarFinDeOleada() {
        return (oleadaActualObj == null || oleadaActualObj.isEmpty()) && enemigosRestantes.isEmpty();
    }

    public boolean verificarFinDeNivel() {
        if (numeroNivel == 1) {
            return verificarFinDeOleada(); // Nivel 1 tiene 1 sola oleada
        }
        return verificarFinDeOleada() && oleadaActual >= maximaOleadas;
    }

    public void updateSpawn(float deltaSeconds) {
        if (oleadaActualObj == null || oleadaActualObj.isEmpty() || spawnPaused) {
            return;
        }
        spawnTimer += deltaSeconds;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            Enemigo spawnado = oleadaActualObj.pollEnemigo();
            if (spawnado != null) {
                // Elegir un spawn point
                WaypointNode spawn;
                if (numeroNivel == 2 && oleadaActual == 1 && enemigosSpawnados < totalEnemigosOleada / 2) {
                    if (selectedInitialSpawnIndex == -1) {
                        selectedInitialSpawnIndex = random.nextInt(spawnNodes.size());
                    }
                    // La primera mitad de la oleada 1 sale de un mismo spawn elegido aleatoriamente al inicio
                    spawn = spawnNodes.get(selectedInitialSpawnIndex);
                } else {
                    spawn = spawnNodes.get(random.nextInt(spawnNodes.size()));
                }
                
                spawnado.setPosicion(spawn.x, spawn.y);
                if (!spawn.siguientes.isEmpty()) {
                    WaypointNode next = spawn.siguientes.get(random.nextInt(spawn.siguientes.size()));
                    spawnado.setTargetNode(next);
                }
                enemigosRestantes.add(spawnado);
                enemigosSpawnados++;
            }
        }
    }

    // Devuelve true si un casillero (ix, iy) está sobre el camino
    public boolean intersectsPath(int ix, int iy) {
        float cx = ix + 0.5f;
        float cy = iy + 0.5f;

        // Simplificado para la lista cruda de waypoints. Para grafos es inexacto pero suficiente para prevenir colisiones base.
        for (float[] wp : waypoints) {
            if (Math.hypot(wp[0] - cx, wp[1] - cy) < 1.0f) {
                return true;
            }
        }
        return false;
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
        return (oleadaActualObj != null ? oleadaActualObj.size() : 0) + enemigosRestantes.size();
    }

    public boolean isValidPlacementArea(int ix, int iy) {
        if (buildPolygon != null) {
            return buildPolygon.contains(ix + 0.5f, iy + 0.5f);
        }
        return ix >= 0 && ix < com.miJuego.model.CameraContext.getWorldW() && iy >= 0 && iy < com.miJuego.model.CameraContext.getWorldH();
    }
    
    public Oleada<? extends Enemigo> getOleadaActualObj() {
        return oleadaActualObj;
    }

    public List<WaypointNode> getSpawnNodes() {
        return spawnNodes;
    }
}
