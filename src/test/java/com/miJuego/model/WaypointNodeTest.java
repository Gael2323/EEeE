package com.miJuego.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WaypointNodeTest {

    @Test
    void testNodeCreation() {
        WaypointNode node = new WaypointNode(10.5f, 20.3f);
        assertEquals(10.5f, node.x);
        assertEquals(20.3f, node.y);
        assertTrue(node.siguientes.isEmpty(), "La lista de siguientes nodos debe inicializarse vacía");
    }

    @Test
    void testAddSiguiente() {
        WaypointNode root = new WaypointNode(0f, 0f);
        WaypointNode next1 = new WaypointNode(10f, 0f);
        WaypointNode next2 = new WaypointNode(10f, 10f);

        root.addSiguiente(next1);
        root.addSiguiente(next2);

        assertEquals(2, root.siguientes.size(), "Debería tener dos nodos siguientes");
        assertTrue(root.siguientes.contains(next1));
        assertTrue(root.siguientes.contains(next2));
    }

    @Test
    void testAddSiguienteDoesNotDuplicate() {
        WaypointNode root = new WaypointNode(0f, 0f);
        WaypointNode next1 = new WaypointNode(10f, 0f);

        root.addSiguiente(next1);
        root.addSiguiente(next1); // Intentar añadir el mismo nodo

        assertEquals(1, root.siguientes.size(), "No debería haber nodos duplicados en la lista");
    }
}
