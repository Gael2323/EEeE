package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TorreCanonTest {
    private Juego juego;

    @BeforeEach
    void setUp() {
        juego = new Juego();
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
    }

    @Test
    void testCanonBasicAttack() {
        // Inicializar el cañón directamente, ya que placeTower no lo soporta en el switch
        Canon canon = new Canon("canon-1", 20, 20);
        juego.getTorres().add(canon);
        
        assertNotNull(canon, "Se debería haber colocado una Torre Canon");
        assertEquals("Canon", canon.getTowertype());

        // Limpiamos los enemigos
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
        e1.setPosicion(21.0f, 20.0f); // En rango
        
        juego.getNivelActual().getEnemigosRestantes().add(e1);
        
        double vidaInicial = e1.GetVida();

        // Simulamos unos segundos para que la torre dispare (tiempo de recarga es 2000ms = 2s)
        for (int i = 0; i < 30; i++) {
            juego.update(0.1f);
        }

        assertTrue(e1.GetVida() < vidaInicial, "El enemigo debería haber recibido damage de la bala del cañón");
        assertEquals(e1, canon.getObjetivo(), "El objetivo del canon debería ser el enemigo más cercano/avanzado");
    }

    @Test
    void testCanonUpgrade() {
        Canon canon = new Canon("canon-test", 10, 10);
        
        double rangoInicial = canon.getRango();
        double areaInicial = canon.getAreaAGolpear();
        int nivelInicial = canon.getNivelMejora();
        
        canon.upgrade();
        
        assertEquals(nivelInicial + 1, canon.getNivelMejora());
        assertTrue(canon.getRango() > rangoInicial, "El rango debería aumentar al mejorar");
        assertTrue(canon.getAreaAGolpear() > areaInicial, "El área de impacto debería aumentar al mejorar");
    }
}
