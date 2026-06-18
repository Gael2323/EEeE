package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TorreMessengerTest  {
    private Juego juego;

    @BeforeEach
    void setUp() {
        // Inicializamos el núcleo de tu juego limpio antes de cada test
        juego = new Juego();
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
        ProgresoJuego.messengerTowerUnlocked = true; // Reset static progress
    }
    @Test
    void testTorreMessengerGolpeeTodosLosEnemigos(){
        juego.setSelectedTowerType(8); // 8: TorreMessenger
        juego.getJugador().setMoneda(600);
        juego.placeTower(20, 20);

        Enemigo e1 = new PopUp("e1");
        Enemigo e2 = new PopUp("e2");
        Enemigo e3 = new PopUp("e3");
        Enemigo e4 = new PopUp("e4");

        // Los dejamos quietos (sin targetNode) en un camino cercano en cadena:
        // Distancias: e1(19,19) -> e2(18,19) es 1.0; e2(18,19) -> e3(16,19) es 2.0; e3(16,19) -> e4(15,19) es 1.0
        // Todas las distancias son menores al rango de rebote (3.0) de la TorreMessenger.
        e1.setNodosVisitados(1);
        e1.setTargetNode(null);
        e1.setPosicion(19, 19);

        e2.setNodosVisitados(1);
        e2.setTargetNode(null);
        e2.setPosicion(18, 19);

        e3.setNodosVisitados(1);
        e3.setTargetNode(null);
        e3.setPosicion(16, 19);

        e4.setNodosVisitados(1);
        e4.setTargetNode(null);
        e4.setPosicion(15, 19);

        // Agregamos los enemigos a la lista activa del nivel actual
        juego.getNivelActual().getEnemigosRestantes().add(e1);
        juego.getNivelActual().getEnemigosRestantes().add(e2);
        juego.getNivelActual().getEnemigosRestantes().add(e3);
        juego.getNivelActual().getEnemigosRestantes().add(e4);

        System.out.println("=== SIMULACIÓN EN DIRECTO DE TORRE MESSENGER ===");
        System.out.printf("Vida inicial -> e1: %.2f | e2: %.2f | e3: %.2f | e4: %.2f%n",
                e1.GetVida(), e2.GetVida(), e3.GetVida(), e4.GetVida());

        // Hacemos correr la actualización del juego paso a paso (50ms por update)
        for (int i = 1; i <= 60; i++) {
            juego.update(0.05f); // 50ms por paso (total 3 segundos)
            
            // Si hay alguna bala activa, mostramos su progreso hacia el enemigo
            if (!juego.getBalas().isEmpty()) {
                System.out.printf("[Paso %d - %.2fs] Balas en vuelo: %d%n", i, i * 0.05f, juego.getBalas().size());
                for (Bala b : juego.getBalas()) {
                    System.out.printf("   - Proyectil %s (rebotes: %d) en (%.2f, %.2f) dirigiéndose a %s%n",
                            b.getId(), b.getBounces(), b.getX(), b.getY(), b.getTarget().getId());
                }
            }
            
            // Imprimimos la vida de cada enemigo paso a paso para ver el damage en directo
            System.out.printf("[Paso %d - %.2fs] Vida actual -> e1: %.2f | e2: %.2f | e3: %.2f | e4: %.2f%n",
                    i, i * 0.05f, e1.GetVida(), e2.GetVida(), e3.GetVida(), e4.GetVida());
        }
        System.out.println("=== FIN DE LA SIMULACIÓN ===");

        // Verificaciones
        org.junit.jupiter.api.Assertions.assertTrue(e1.GetVida() < 40.0, "e1 debió haber recibido damage");
        org.junit.jupiter.api.Assertions.assertTrue(e2.GetVida() < 40.0, "e2 debió haber recibido damage por rebote");
        org.junit.jupiter.api.Assertions.assertTrue(e3.GetVida() < 40.0, "e3 debió haber recibido damage por rebote");
        org.junit.jupiter.api.Assertions.assertTrue(e4.GetVida() < 40.0, "e4 debió haber recibido damage por rebote");
    }

}
