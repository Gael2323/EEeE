package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TorreMcaffeTest {
    private Juego juego;

    @BeforeEach
    void setUp() {
        // Inicializamos el núcleo de tu juego limpio antes de cada test
        juego = new Juego();
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
        ProgresoJuego.mcafeeUnlocked = true; // Reset static progress
    }

    @Test
    void testTorreMcAfeeGolpeeEnemigo() {
        juego.setSelectedTowerType(2); // 2: TorreMcAfee
        juego.getJugador().setMoneda(600);
        juego.placeTower(20, 20);

        Enemigo e1 = new PopUp("e1");
        e1.setNodosVisitados(1);
        e1.setTargetNode(null);
        e1.setPosicion(19, 19);

        Enemigo e2 = new PopUp("e2");
        e2.setNodosVisitados(1);
        e2.setTargetNode(null);
        e2.setPosicion(18, 19);

        // Agregamos ambos enemigos a la lista activa del nivel actual
        juego.getNivelActual().getEnemigosRestantes().add(e1);
        juego.getNivelActual().getEnemigosRestantes().add(e2);

        System.out.println("=== SIMULACIÓN EN DIRECTO DE TORRE MCAFEE ===");
        System.out.printf("Vida inicial -> e1: %.2f | e2: %.2f%n", e1.GetVida(), e2.GetVida());

        // Hacemos correr la actualización del juego paso a paso (0.2s por update, total 5.0 segundos)
        for (int i = 1; i <= 25; i++) {
            juego.update(0.2f);

            // Si hay alguna bala activa, mostramos su progreso hacia el enemigo
            if (!juego.getBalas().isEmpty()) {
                System.out.printf("[Paso %d - %.2fs] Balas en vuelo: %d%n", i, i * 0.2f, juego.getBalas().size());
                for (Bala b : juego.getBalas()) {
                    System.out.printf("   - Proyectil %s en (%.2f, %.2f) dirigiéndose a %s%n",
                            b.getId(), b.getX(), b.getY(), b.getTarget().getId());
                }
            }

            // Imprimimos la vida de los enemigos paso a paso para ver el daño en directo
            System.out.printf("[Paso %d - %.2fs] Vida actual -> e1: %.2f | e2: %.2f%n", 
                    i, i * 0.2f, e1.GetVida(), e2.GetVida());
        }
        System.out.println("=== FIN DE LA SIMULACIÓN ===");

        // Verificaciones: ambos deben morir (vida = 0.0)
        org.junit.jupiter.api.Assertions.assertEquals(0.0, e1.GetVida(), "e1 debió haber muerto (vida = 0.0)");
        org.junit.jupiter.api.Assertions.assertEquals(0.0, e2.GetVida(), "e2 debió haber muerto (vida = 0.0)");
    }
}
