package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
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
        // 1. APARICIÓN Y MOVIMIENTO LIBRE
        BossPeedy peedy = new BossPeedy("peedy-test");
        peedy.setPosicion(10.0f, 10.0f);
        juego.getNivelActual().getEnemigosRestantes().add(peedy);

        // Verificamos estado inicial
        org.junit.jupiter.api.Assertions.assertEquals(BossPeedy.BossState.WALKING, peedy.getEstadoBoss());
        org.junit.jupiter.api.Assertions.assertEquals(10.0f, peedy.getX());
        org.junit.jupiter.api.Assertions.assertEquals(10.0f, peedy.getY());

        // Hacemos que camine
        float startX = peedy.getX();
        float startY = peedy.getY();
        
        // Ejecutamos varios updates para comprobar que se desplaza libremente
        for (int i = 0; i < 20; i++) {
            juego.update(0.1f);
        }

        // Debería haberse movido de (10, 10) hacia su destino aleatorio
        org.junit.jupiter.api.Assertions.assertTrue(peedy.getX() != startX || peedy.getY() != startY,
                "BossPeedy debió haberse movido libremente");
    }

    @Test
    void testBossPeedyAturdeTorres() {
        // 2. ATURDIMIENTO DE TORRES (RUGIDO/GRITO)
        BossPeedy peedy = new BossPeedy("peedy-test");
        peedy.setPosicion(20.0f, 20.0f);
        juego.getNivelActual().getEnemigosRestantes().add(peedy);

        // Colocamos una torre cerca (distancia 1.0, rango de stun es 4.0)
        TorreComun torre = new TorreComun("torre-cercana", 21, 20);
        juego.getTorres().add(torre);

        // Colocamos otra torre lejos (distancia 6.0, fuera de rango)
        TorreComun torreLejos = new TorreComun("torre-lejana", 26, 20);
        juego.getTorres().add(torreLejos);

        // Forzamos al boss a iniciar la habilidad de aturdimiento (grito)
        // private void iniciarAturdir() se llama internamente, pero podemos cambiar el estado
        // y configurar los timers para simularlo.
        peedy.setEstadoBoss(BossPeedy.BossState.STUNNING);
        peedy.resetAnimTimer();
        
        // El grito se ejecuta cuando getStunningFrame() >= 3 (es decir, animTimer >= 0.45s)
        // Simulamos el paso de tiempo
        juego.update(0.5f);

        // Verificaciones
        org.junit.jupiter.api.Assertions.assertFalse(torre.canShoot(), "La torre cercana debió haber sido aturdida");
        org.junit.jupiter.api.Assertions.assertTrue(torreLejos.canShoot(), "La torre lejana no debió ser afectada");
    }

    @Test
    void testBossPeedyRompeTorres() {
        // 3. SECUENCIA SUPERHERO LANDING Y DESTRUCCIÓN DE TORRE
        BossPeedy peedy = new BossPeedy("peedy-test");
        peedy.setPosicion(22.0f, 22.0f);
        juego.getNivelActual().getEnemigosRestantes().add(peedy);

        // Colocamos la torre que será el objetivo
        TorreComun torreObjetivo = new TorreComun("torre-target", 20, 20);
        juego.getTorres().add(torreObjetivo);

        // Iniciamos picada hacia la torre
        peedy.setEstadoBoss(BossPeedy.BossState.SPINNING);
        peedy.resetAnimTimer();
        // Nos aseguramos que el target sea torreObjetivo
        // targetTower es un campo privado pero lo seteamos indirectamente forzando a iniciar el vuelo
        // o llamando al actualizador de habilidades. 
        // Para simplificar el unit test, podemos simular la picada e impacto:
        java.lang.reflect.Field targetField;
        try {
            targetField = BossPeedy.class.getDeclaredField("targetTower");
            targetField.setAccessible(true);
            targetField.set(peedy, torreObjetivo);
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("No se pudo setear targetTower por reflexión: " + e.getMessage());
        }

        // Paso A: SPINNING (dura 0.8s)
        juego.update(0.81f);
        org.junit.jupiter.api.Assertions.assertEquals(BossPeedy.BossState.LANDING, peedy.getEstadoBoss(),
                "Debería pasar a LANDING tras culminar SPINNING");

        // Paso B: LANDING (el impacto destruye la torre a los 0.50s)
        juego.update(0.51f);

        // Verificaciones
        org.junit.jupiter.api.Assertions.assertFalse(juego.getTorres().contains(torreObjetivo),
                "La torre objetivo debió haber sido destruida e impacto ejecutado");
    }

    @Test
    void testBossPeedyRemolinoDeBugs() {
        // 4. REMOLINO DE BUGS (TELEPORTACIÓN DE ENEMIGOS)
        BossPeedy peedy = new BossPeedy("peedy-test");
        peedy.setPosicion(20.0f, 20.0f);
        juego.getNivelActual().getEnemigosRestantes().add(peedy);

        // Creamos un enemigo secundario en el waypoint inicial
        WaypointNode spawn = juego.getNivelActual().getSpawnNodes().get(0);
        Enemigo e1 = new PopUp("popup-temp");
        e1.setPosicion(spawn.x, spawn.y);
        e1.setTargetNode(spawn);
        e1.setNodosVisitados(1);
        juego.getNivelActual().getEnemigosRestantes().add(e1);

        // Forzamos habilidad de teleportación (Remolino de Bugs es activeHabilidadType = 1)
        peedy.setEstadoBoss(BossPeedy.BossState.STUNNING);
        peedy.resetAnimTimer();
        
        try {
            java.lang.reflect.Field habTypeField = BossPeedy.class.getDeclaredField("activeHabilidadType");
            habTypeField.setAccessible(true);
            habTypeField.set(peedy, 1); // 1 = Teleport
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("No se pudo setear activeHabilidadType: " + e.getMessage());
        }

        // Avanzamos el tiempo para que se ejecute la habilidad (getStunningFrame() >= 3)
        juego.update(0.5f);

        // El enemigo e1 debió haber sido teletransportado 3 nodos adelante (y opcionalmente avanzar 1 más en su actualización del tick)
        org.junit.jupiter.api.Assertions.assertTrue(e1.getNodosVisitados() >= 4,
                "El enemigo debió avanzar al menos 3 nodos por la habilidad (actual: " + e1.getNodosVisitados() + ")");
    }
}
