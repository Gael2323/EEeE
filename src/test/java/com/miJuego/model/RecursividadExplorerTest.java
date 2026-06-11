package com.miJuego.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RecursividadExplorerTest {

    @Test
    public void testExplorerRecursivoSeleccionaElMasAvanzado() {
        // Simulamos un camino con dos nodos
        WaypointNode nodoMeta1 = new WaypointNode(10, 10);
        WaypointNode nodoMeta2 = new WaypointNode(20, 20); // Más lejos

        // Creamos una torre Internet Explorer (aunque aquí solo probamos la lógica de selección de Juego)
        Juego juego = new Juego();

        // Creamos 4 enemigos
        Enemigo e1 = new Duende("e1");
        Enemigo e2 = new Duende("e2");
        Enemigo e3 = new Duende("e3");
        Enemigo e4 = new Duende("e4");

        // Configuramos el progreso de cada enemigo
        // Enemigo 1: recién empieza
        e1.setNodosVisitados(1);
        e1.setTargetNode(nodoMeta1);
        e1.setPosicion(0, 0); // Está a distancia ~14 de (10,10)

        // Enemigo 2: va por el mismo nodo, pero más cerca de la meta
        e2.setNodosVisitados(1);
        e2.setTargetNode(nodoMeta1);
        e2.setPosicion(5, 5); // Está a distancia ~7 de (10,10)

        // Enemigo 3: ya pasó al siguiente nodo (va ganando la carrera general)
        e3.setNodosVisitados(2);
        e3.setTargetNode(nodoMeta2);
        e3.setPosicion(12, 12);

        // Enemigo 4: está muy atrás
        e4.setNodosVisitados(0);
        e4.setTargetNode(nodoMeta1);
        e4.setPosicion(0, 0);

        List<Enemigo> listaEnemigos = new ArrayList<>();
        listaEnemigos.add(e1);
        listaEnemigos.add(e2);
        listaEnemigos.add(e3);
        listaEnemigos.add(e4);

        // Llamamos a la función recursiva desde el índice 0
        Enemigo seleccionado = juego.selectFirstEnemyRecursivo(listaEnemigos, 0);

        // Debería seleccionar al e3 porque tiene mayor cantidad de nodos visitados (2)
        assertEquals(e3, seleccionado, "Debería seleccionar al enemigo con más nodos visitados");

        // Si ahora e3 y e2 están en el mismo nodo (2), desempatarán por distancia
        e2.setNodosVisitados(2);
        e2.setTargetNode(nodoMeta2);
        e2.setPosicion(18, 18); // Muy cerca de (20,20)
        
        Enemigo seleccionadoDesempate = juego.selectFirstEnemyRecursivo(listaEnemigos, 0);

        // e2 está más cerca de la meta (18,18 a 20,20) que e3 (12,12 a 20,20)
        assertEquals(e2, seleccionadoDesempate, "Debería seleccionar al enemigo más cercano al nodo en caso de empate");
    }
}
