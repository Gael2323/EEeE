package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TorreAvastTest {
    private Juego juego;

    @BeforeEach
    void setUp() {
        juego = new Juego();
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
    }

    @Test
    void testAvastManualTargeting() {
        // Colocamos una Torre Avast (tipo 4) en (20, 20)
        juego.setSelectedTowerType(4); 
        juego.getJugador().setMoneda(1000); 
        juego.placeTower(20, 20);

        TorreAvast avast = null;
        for (Torre t : juego.getTorres()) {
            if (t instanceof TorreAvast) {
                avast = (TorreAvast) t;
                break;
            }
        }
        
        assertNotNull(avast, "Se debería haber colocado una Torre Avast");
        assertEquals("TorreAvast", avast.getTowertype());
        
        // Inicialmente manualTargetingMode es false
        assertFalse(avast.isManualTargetingMode());
        
        // Cambiamos a modo manual
        avast.setManualTargetingMode(true);
        avast.setTargetCoordinates(25.0f, 25.0f);
        
        assertTrue(avast.isManualTargetingMode());
        assertEquals(25.0f, avast.getTargetX());
        assertEquals(25.0f, avast.getTargetY());

        // Limpiamos los enemigos automáticos
        if (juego.getNivelActual().getOleadaActualObj() != null) {
            while (!juego.getNivelActual().getOleadaActualObj().isEmpty()) {
                juego.getNivelActual().getOleadaActualObj().pollEnemigo();
            }
        }
        juego.getNivelActual().getEnemigosRestantes().clear();

        // Agregamos un enemigo en rango
        Enemigo e1 = new PopUp("e1");
        e1.setNodosVisitados(1);
        e1.setTargetNode(null);
        e1.setPosicion(21.0f, 21.0f); 
        juego.getNivelActual().getEnemigosRestantes().add(e1);
        
        // En modo manual, la torre no debería tener un objetivo
        avast.findTarget(juego.getNivelActual().getEnemigosRestantes());
        
        // Al atacar, la bala no debería apuntar a un enemigo, sino tener target null
        java.util.List<Bala> balas = avast.atacar(juego.getNivelActual().getEnemigosRestantes(), () -> "test-bala");
        assertEquals(1, balas.size());
        assertNull(balas.get(0).getTarget(), "La bala debería apuntar a un área, no a un enemigo (target null)");
    }

    @Test
    void testAvastUpgrade() {
        TorreAvast avast = new TorreAvast("avast-test", 10, 10);
        
        double rangoInicial = avast.getRango();
        int nivelInicial = avast.getNivelMejora();
        int recargaInicial = avast.getTiempoRecarga();
        
        avast.upgrade();
        
        assertEquals(nivelInicial + 1, avast.getNivelMejora());
        assertTrue(avast.getRango() > rangoInicial, "El rango debería aumentar al mejorar");
        assertTrue(avast.getTiempoRecarga() < recargaInicial, "El tiempo de recarga debería disminuir al mejorar");
    }
}
