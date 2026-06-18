package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

public class TorreFireFoxTest {
    private Juego juego;

    @BeforeEach
    void setUp() {
        // Inicializamos el núcleo de tu juego limpio antes de cada test
        juego = new Juego();
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
        ProgresoJuego.firefoxUnlocked = true; // Desbloqueamos la torre de Firefox para el test
    }

    @Test
    void testTorreFirefoxQuemaEnArea() {
        // Colocamos una Torre Firefox (tipo 6) en (20, 20)
        juego.setSelectedTowerType(6);
        juego.getJugador().setMoneda(600);
        juego.placeTower(20, 20);

        // Limpiamos los enemigos automáticos de la oleada de nivel 1
        if (juego.getNivelActual().getOleadaActualObj() != null) {
            while (!juego.getNivelActual().getOleadaActualObj().isEmpty()) {
                juego.getNivelActual().getOleadaActualObj().pollEnemigo();
            }
        }

        // Enemigo 1: Blanco directo de la bala
        Enemigo e1 = new PopUp("e1");
        e1.setNodosVisitados(1);
        e1.setTargetNode(null);
        e1.setPosicion(19.0f, 19.0f);

        // Enemigo 2: Fuera de objetivo principal, pero muy cerca (a distancia 0.7 del impacto, dentro de areaAGolpear=1.0)
        Enemigo e2 = new PopUp("e2");
        e2.setNodosVisitados(1);
        e2.setTargetNode(null);
        e2.setPosicion(18.5f, 19.5f);

        // Enemigo 3: Muy alejado (fuera del área de impacto de la explosión de fuego)
        Enemigo e3 = new PopUp("e3");
        e3.setNodosVisitados(1);
        e3.setTargetNode(null);
        e3.setPosicion(10.0f, 10.0f);

        // Agregamos los enemigos a la lista activa
        juego.getNivelActual().getEnemigosRestantes().add(e1);
        juego.getNivelActual().getEnemigosRestantes().add(e2);
        juego.getNivelActual().getEnemigosRestantes().add(e3);

        System.out.println("=== SIMULACIÓN EN DIRECTO DE TORRE FIREFOX (ÁREA DE FUEGO) ===");
        System.out.printf("Vida inicial -> e1: %.2f | e2: %.2f | e3: %.2f%n", e1.GetVida(), e2.GetVida(), e3.GetVida());

        // Hacemos correr el juego paso a paso (100ms por paso)
        for (int i = 1; i <= 20; i++) {
            juego.update(0.1f);

            // Reporte de estado
            System.out.printf("[Paso %d - %.1fs] Vida: e1=%.2f (Fuego: %b) | e2=%.2f (Fuego: %b) | e3=%.2f (Fuego: %b)%n",
                    i, i * 0.1f, 
                    e1.GetVida(), e1.tieneFuego(),
                    e2.GetVida(), e2.tieneFuego(),
                    e3.GetVida(), e3.tieneFuego());
        }
        System.out.println("=== FIN DE LA SIMULACIÓN ===");

        // Verificaciones
        // 1. e1 debió recibir el impacto directo y estar bajo el efecto de fuego
        org.junit.jupiter.api.Assertions.assertTrue(e1.GetVida() < 40.0, "e1 debió recibir damage");
        org.junit.jupiter.api.Assertions.assertTrue(e1.tieneFuego(), "e1 debió haber sido prendido fuego");

        // 2. e2 debió recibir damage splash y estar prendido fuego por la explosión en área
        org.junit.jupiter.api.Assertions.assertTrue(e2.GetVida() < 40.0, "e2 debió recibir damage de área");
        org.junit.jupiter.api.Assertions.assertTrue(e2.tieneFuego(), "e2 debió haber sido quemado por el splash");

        // 3. e3 está muy lejos, no debió recibir damage ni prenderse fuego
        org.junit.jupiter.api.Assertions.assertEquals(40.0, e3.GetVida(), "e3 no debió recibir damage");
        org.junit.jupiter.api.Assertions.assertFalse(e3.tieneFuego(), "e3 no debió haberse quemado");
    }
}
