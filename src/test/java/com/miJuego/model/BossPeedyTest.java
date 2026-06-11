package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

public class BossPeedyTest {
    private Juego juego;

    @BeforeEach
    void setUp() {
        // Inicializamos el núcleo de tu juego limpio antes de cada test
        juego = new Juego();
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
    }

    @Test
    void testBossPeedyAparicionYMovimiento() {
        System.out.println("=== SIMULACIÓN POR EVENTOS: APARICIÓN Y MOVIMIENTO ===");
        
        BossPeedy peedy = new BossPeedy("peedy-test");
        peedy.setPosicion(10.0f, 10.0f);
        juego.getNivelActual().getEnemigosRestantes().add(peedy);

        System.out.printf(">>> EVENTO: Boss Peedy apareció en posición inicial (%.2f, %.2f) | Estado: %s%n",
                peedy.getX(), peedy.getY(), peedy.getEstadoBoss());

        org.junit.jupiter.api.Assertions.assertEquals(BossPeedy.BossState.WALKING, peedy.getEstadoBoss());

        float lastX = peedy.getX();
        float lastY = peedy.getY();
        
        // Hacemos correr actualizaciones lógicas paso a paso
        for (int i = 1; i <= 30; i++) {
            juego.update(0.1f);
            
            // Logueamos solo si la distancia recorrida es significativa (cambio de ubicación)
            double dist = Math.hypot(peedy.getX() - lastX, peedy.getY() - lastY);
            if (dist >= 1.5) {
                System.out.printf(">>> EVENTO: Boss Peedy cambió de ubicación a (%.2f, %.2f) | Distancia desde último reporte: %.2f%n",
                        peedy.getX(), peedy.getY(), dist);
                lastX = peedy.getX();
                lastY = peedy.getY();
            }
        }
        System.out.println("=== FIN DE LA SIMULACIÓN ===\n");

        org.junit.jupiter.api.Assertions.assertTrue(peedy.getX() != 10.0f || peedy.getY() != 10.0f,
                "BossPeedy debió haberse movido libremente");
    }

    @Test
    void testBossPeedyAturdeTorres() {
        System.out.println("=== SIMULACIÓN POR EVENTOS: ATURDIMIENTO DE TORRES ===");

        BossPeedy peedy = new BossPeedy("peedy-test");
        peedy.setPosicion(20.0f, 20.0f);
        juego.getNivelActual().getEnemigosRestantes().add(peedy);

        TorreComun torre = new TorreComun("torre-cercana", 21, 20);
        juego.getTorres().add(torre);

        TorreComun torreLejos = new TorreComun("torre-lejana", 26, 20);
        juego.getTorres().add(torreLejos);

        System.out.printf(">>> EVENTO: Torre '%s' colocada en (21, 20) | Torre '%s' colocada en (26, 20)%n",
                torre.getId(), torreLejos.getId());

        // Configuramos al boss para iniciar rugido
        peedy.setEstadoBoss(BossPeedy.BossState.STUNNING);
        peedy.resetAnimTimer();
        System.out.println(">>> EVENTO: Boss Peedy inició preparación para grito de aturdimiento (STUNNING)");

        boolean reportadoStun = false;

        // Simulamos actualizaciones cortas de 0.05 segundos
        for (int i = 1; i <= 15; i++) {
            juego.update(0.05f);

            if (!torre.canShoot() && !reportadoStun) {
                reportadoStun = true;
                System.out.printf(">>> EVENTO: ¡RUGIDO ATURDIDOR! La torre '%s' fue aturdida en el paso %d (tiempo: %.2fs)%n",
                        torre.getId(), i, i * 0.05f);
            }
        }
        System.out.println("=== FIN DE LA SIMULACIÓN ===\n");

        org.junit.jupiter.api.Assertions.assertFalse(torre.canShoot(), "La torre cercana debió haber sido aturdida");
        org.junit.jupiter.api.Assertions.assertTrue(torreLejos.canShoot(), "La torre lejana no debió ser afectada");
    }

    @Test
    void testBossPeedyRompeTorres() {
        System.out.println("=== SIMULACIÓN POR EVENTOS: DESTRUCCIÓN DE TORRE ===");

        BossPeedy peedy = new BossPeedy("peedy-test");
        peedy.setPosicion(22.0f, 22.0f);
        juego.getNivelActual().getEnemigosRestantes().add(peedy);

        TorreComun torreObjetivo = new TorreComun("torre-target", 20, 20);
        juego.getTorres().add(torreObjetivo);

        System.out.printf(">>> EVENTO: Torre '%s' colocada en (20, 20) como objetivo de destrucción%n", torreObjetivo.getId());

        // Seteamos por reflexión el target
        try {
            java.lang.reflect.Field targetField = BossPeedy.class.getDeclaredField("targetTower");
            targetField.setAccessible(true);
            targetField.set(peedy, torreObjetivo);
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail(e.getMessage());
        }

        peedy.setEstadoBoss(BossPeedy.BossState.SPINNING);
        peedy.resetAnimTimer();
        System.out.println(">>> EVENTO: Boss Peedy inició vuelo de aproximación 360 (SPINNING)");

        BossPeedy.BossState ultimoEstado = peedy.getEstadoBoss();
        boolean reportadoImpacto = false;

        for (int i = 1; i <= 30; i++) {
            juego.update(0.05f);

            if (peedy.getEstadoBoss() != ultimoEstado) {
                System.out.printf(">>> EVENTO: Boss Peedy transicionó de estado a %s (tiempo: %.2fs)%n",
                        peedy.getEstadoBoss(), i * 0.05f);
                ultimoEstado = peedy.getEstadoBoss();
            }

            if (!juego.getTorres().contains(torreObjetivo) && !reportadoImpacto) {
                reportadoImpacto = true;
                System.out.printf(">>> EVENTO: ¡IMPACTO SUPERHERO LANDING! La torre '%s' fue totalmente destruida (tiempo: %.2fs)%n",
                        torreObjetivo.getId(), i * 0.05f);
            }
        }
        System.out.println("=== FIN DE LA SIMULACIÓN ===\n");

        org.junit.jupiter.api.Assertions.assertFalse(juego.getTorres().contains(torreObjetivo));
    }

    @Test
    void testBossPeedyRemolinoDeBugs() {
        System.out.println("=== SIMULACIÓN POR EVENTOS: REMOLINO DE BUGS ===");

        BossPeedy peedy = new BossPeedy("peedy-test");
        peedy.setPosicion(20.0f, 20.0f);
        juego.getNivelActual().getEnemigosRestantes().add(peedy);

        WaypointNode spawn = juego.getNivelActual().getSpawnNodes().get(0);
        Enemigo e1 = new PopUp("popup-temp");
        e1.setPosicion(spawn.x, spawn.y);
        e1.setTargetNode(spawn);
        e1.setNodosVisitados(1);
        juego.getNivelActual().getEnemigosRestantes().add(e1);

        System.out.printf(">>> EVENTO: Enemigo '%s' apareció en el spawn inicial en (%.2f, %.2f)%n",
                e1.getId(), e1.getX(), e1.getY());

        peedy.setEstadoBoss(BossPeedy.BossState.STUNNING);
        peedy.resetAnimTimer();
        
        try {
            java.lang.reflect.Field habTypeField = BossPeedy.class.getDeclaredField("activeHabilidadType");
            habTypeField.setAccessible(true);
            habTypeField.set(peedy, 1); // 1 = Teleport
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail(e.getMessage());
        }

        System.out.println(">>> EVENTO: Boss Peedy inició preparación para Remolino de Bugs (STUNNING)");

        float startX = e1.getX();
        float startY = e1.getY();
        boolean reportadoTeleport = false;

        for (int i = 1; i <= 20; i++) {
            juego.update(0.05f);

            if (e1.getX() != startX && !reportadoTeleport) {
                reportadoTeleport = true;
                System.out.printf(">>> EVENTO: ¡REMOLINO DE BUGS! El enemigo '%s' fue teletransportado de (%.2f, %.2f) a (%.2f, %.2f) | Nodos visitados: %d%n",
                        e1.getId(), startX, startY, e1.getX(), e1.getY(), e1.getNodosVisitados());
            }
        }
        System.out.println("=== FIN DE LA SIMULACIÓN ===\n");

        org.junit.jupiter.api.Assertions.assertTrue(e1.getNodosVisitados() >= 4);
    }
}
