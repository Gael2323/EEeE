package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TorreDeAreaTest {

    private TorreDeArea torre;
    private Enemigo e1;
    private Enemigo e2;
    private Enemigo e3;
    private Enemigo e4;

    @BeforeEach
    void setUp() {
        torre = new TorreDeArea("area-1", 5f, 5f);
        e1 = new PopUp("dummy1", PopUp.Variante.PREMIO);
        e2 = new PopUp("dummy2", PopUp.Variante.PREMIO);
        e3 = new PopUp("dummy3", PopUp.Variante.PREMIO);
        e4 = new PopUp("dummy4", PopUp.Variante.PREMIO);
        
        e1.setVida(100.0);
        e2.setVida(100.0);
        e3.setVida(100.0);
        e4.setVida(100.0);
    }

    @Test
    void testInitialProperties() {
        assertEquals("TorreDeArea", torre.getTowertype());
        assertEquals(3, torre.getCantidadEnemigosDañadoMax());
        assertEquals(150.0, torre.GetCostoTorre());
        assertEquals(3.0, torre.getRango());
    }

    @Test
    void testUpgrade() {
        torre.upgrade();
        assertEquals(2, torre.getNivelMejora());
        assertEquals(4, torre.getCantidadEnemigosDañadoMax()); // 3 + 1
        assertEquals(225.0, torre.GetCostoTorre()); // 150 + 75
        assertEquals(3.3, torre.getRango(), 0.01);
    }

    @Test
    void testAtaqueDirecto() {
        double damage = torre.ataque(e1);
        assertEquals(10.0, damage);
        assertEquals(90.0, e1.GetVida());
        
        torre.upgrade();
        double damageUpgraded = torre.ataque(e2);
        assertEquals(20.0, damageUpgraded); // 10 * 2
        assertEquals(80.0, e2.GetVida());
    }

    @Test
    void testAtacarMultiplesEnemigos() {
        AtomicInteger idCounter = new AtomicInteger(1);
        List<Enemigo> enemigosEnRango = Arrays.asList(e1, e2, e3, e4);
        
        // TorreDeArea solo ataca a max 3 enemigos inicialmente
        List<Bala> balas = torre.atacar(enemigosEnRango, () -> "bala-" + idCounter.getAndIncrement());
        
        assertEquals(3, balas.size(), "Debe generar 3 balas como máximo");
        assertEquals(3, torre.getCantidadEnemigosDañado(), "Debe registrar 3 enemigos dañados");
        assertEquals("bala-1", balas.get(0).getId());
        assertEquals("bala-2", balas.get(1).getId());
        assertEquals("bala-3", balas.get(2).getId());
    }
}
