package com.miJuego.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un nodo en el camino por el que avanzan los enemigos.
 * Permite tener caminos bifurcados (múltiples siguientes nodos).
 */
public class WaypointNode {
    public float x;
    public float y;
    public List<WaypointNode> siguientes;

    public WaypointNode(float x, float y) {
        this.x = x;
        this.y = y;
        this.siguientes = new ArrayList<>();
    }

    public void addSiguiente(WaypointNode nodo) {
        if (!this.siguientes.contains(nodo)) {
            this.siguientes.add(nodo);
        }
    }
}
