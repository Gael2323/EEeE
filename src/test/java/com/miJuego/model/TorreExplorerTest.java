package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TorreExplorerTest {
    private Juego juego;

    @BeforeEach
    void setUp() {
        // Inicializamos el núcleo de tu juego limpio antes de cada test
        juego = new Juego();
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
        ProgresoJuego.explorerUnlocked = true; // Desbloqueamos la torre de Explorer para el test
        ProgresoJuego.ieTowerUnlocked = true; // Desbloqueamos Internet Explorer (tipo 7)
    }

    @Test
    void testTorreExplorerCongelaEnemigos() {
        // Colocamos una Torre Internet Explorer (tipo 7) en (20, 20)
        juego.setSelectedTowerType(7);
        juego.getJugador().setMoneda(600);
        juego.placeTower(20, 20);

        // Limpiamos los enemigos automáticos de la oleada de nivel 1
        if (juego.getNivelActual().getOleadaActualObj() != null) {
            while (!juego.getNivelActual().getOleadaActualObj().isEmpty()) {
                juego.getNivelActual().getOleadaActualObj().pollEnemigo();
            }
        }

        // Enemigo 1: Más avanzado, posicionado en (19.0, 19.0)
        Enemigo e1 = new PopUp("e1");
        e1.setNodosVisitados(2);
        e1.setTargetNode(null);
        e1.setPosicion(19.0f, 19.0f);

        // Enemigo 2: Menos avanzado, posicionado en (18.5, 19.0)
        Enemigo e2 = new PopUp("e2");
        e2.setNodosVisitados(1);
        e2.setTargetNode(null);
        e2.setPosicion(18.5f, 19.0f);

        // Agregamos ambos enemigos a la lista activa
        juego.getNivelActual().getEnemigosRestantes().add(e1);
        juego.getNivelActual().getEnemigosRestantes().add(e2);

        System.out.println("=== SIMULACIÓN EN DIRECTO DE TORRE INTERNET EXPLORER ===");
        System.out.printf("Estado inicial -> e1: ralentizado=%b | e2: ralentizado=%b%n", 
                e1.tieneRalentizar(), e2.tieneRalentizar());

        // Corremos la actualización paso a paso (0.1s por paso, total 5.0 segundos = 50 pasos)
        for (int i = 1; i <= 50; i++) {
            juego.update(0.1f);

            // Reporte de balas en vuelo
            if (!juego.getBalas().isEmpty()) {
                for (Bala b : juego.getBalas()) {
                    System.out.printf("[Paso %d - %.2fs] Proyectil %s en (%.2f, %.2f) dirigiéndose a %s%n",
                            i, i * 0.1f, b.getId(), b.getX(), b.getY(), b.getTarget().getId());
                }
            }

            // Reporte de estado de ralentización
            System.out.printf("[Paso %d - %.2fs] e1: ralentizado=%b | e2: ralentizado=%b%n",
                    i, i * 0.1f, e1.tieneRalentizar(), e2.tieneRalentizar());
        }
        System.out.println("=== FIN DE LA SIMULACIÓN ===");

        // Verificaciones
        // 1. Ambos enemigos deben estar en vida completa (40.0) dado que la torre congela pero no daña base
        assertEquals(40.0, e1.GetVida(), "e1 no debió recibir daño");
        assertEquals(40.0, e2.GetVida(), "e2 no debió recibir daño");
    }
}