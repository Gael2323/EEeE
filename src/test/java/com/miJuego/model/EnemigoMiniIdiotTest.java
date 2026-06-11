package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

public class EnemigoMiniIdiotTest {
    private Juego juego;

    @BeforeEach
    void setUp() {
        // Inicializamos el núcleo de tu juego limpio antes de cada test
        juego = new Juego();
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
        ProgresoJuego.mcafeeUnlocked = true;
    }

    @Test
    void testEnemigoMultipleSeDivideEnMiniIdiots() {
        // Colocamos una Torre McAfee (tipo 2) para atacar al enemigo
        juego.setSelectedTowerType(2);
        juego.getJugador().setMoneda(600);
        juego.placeTower(20, 20);

        // Limpiamos los enemigos de la oleada actual del nivel 1 para que no se spawneen
        // otros enemigos durante la simulación de este test.
        if (juego.getNivelActual().getOleadaActualObj() != null) {
            while (!juego.getNivelActual().getOleadaActualObj().isEmpty()) {
                juego.getNivelActual().getOleadaActualObj().pollEnemigo();
            }
        }

        // Creamos al EnemigoMultiple (vida 150.0). Al morir, este se divide en 2 EnemigoMiniIdiot.
        EnemigoMultiple parent = new EnemigoMultiple("boss-idiot");
        parent.setNodosVisitados(1);
        parent.setTargetNode(null); // Lo dejamos quieto
        parent.setPosicion(19, 19); // Dentro del rango de la torre

        // Lo agregamos a la lista activa de enemigos
        juego.getNivelActual().getEnemigosRestantes().add(parent);

        System.out.println("=== SIMULACIÓN EN DIRECTO DEL SPLIT DE ENEMIGO MÚLTIPLE ===");
        System.out.printf("Vida inicial de parent: %.2f%n", parent.GetVida());

        // Hacemos correr la actualización del juego.
        // Cada paso es de 0.2s. En total simulamos 30 pasos (6.0 segundos).
        for (int i = 1; i <= 30; i++) {
            juego.update(0.2f);

            List<Enemigo> listaEnemigos = juego.getNivelActual().getEnemigosRestantes();
            System.out.printf("[Paso %d - %.2fs] Enemigos activos en juego: %d%n", i, i * 0.2f, listaEnemigos.size());
            
            for (Enemigo e : listaEnemigos) {
                System.out.printf("   - Enemigo: %s (Clase: %s) | Vida: %.2f | Posición: (%.2f, %.2f)%n",
                        e.getId(), e.getClass().getSimpleName(), e.GetVida(), e.getX(), e.getY());
            }
        }
        System.out.println("=== FIN DE LA SIMULACIÓN ===");

        // Al finalizar la simulación, el parent original debió haber muerto (fue removido)
        // y en su lugar deben haberse creado 2 enemigos del tipo EnemigoMiniIdiot
        List<Enemigo> listaFinal = juego.getNivelActual().getEnemigosRestantes();
        
        boolean originalMuerto = true;
        int cantidadMiniIdiots = 0;
        for (Enemigo e : listaFinal) {
            if (e.getId().equals("boss-idiot")) {
                originalMuerto = false;
            }
            if (e instanceof EnemigoMiniIdiot) {
                cantidadMiniIdiots++;
            }
        }

        org.junit.jupiter.api.Assertions.assertTrue(originalMuerto, "El EnemigoMultiple original debió haber muerto");
        org.junit.jupiter.api.Assertions.assertEquals(2, cantidadMiniIdiots, "Deberían haberse generado exactamente 2 EnemigoMiniIdiot tras la muerte");
    }
}