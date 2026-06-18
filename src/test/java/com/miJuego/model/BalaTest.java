package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BalaTest {

    private Torre sourceTower;
    private Enemigo targetEnemy;

    @BeforeEach
    void setUp() {
        // Setup a basic scenario
        sourceTower = new TorreComun("torre-1", 5.0f, 5.0f);
        targetEnemy = new PopUp("dummy", PopUp.Variante.PREMIO);
        targetEnemy.setPosicion(10.0f, 5.0f); // 5 blocks away horizontally
        targetEnemy.setVida(100.0);
    }

    @Test
    void testInitialState() {
        Bala bala = new Bala("bala-1", sourceTower, targetEnemy, 20.0);
        
        assertFalse(bala.isHit(), "La bala no debe empezar impactada");
        assertEquals(20.0, bala.getDamage(), "El daño debe coincidir con el asignado");
        assertEquals(sourceTower, bala.getSourceTower());
        assertEquals(targetEnemy, bala.getTarget());
    }

    @Test
    void testUpdateMovesTowardsTarget() {
        Bala bala = new Bala("bala-1", sourceTower, targetEnemy, 20.0);
        
        // La bala empieza en (5.5, 5.5) y va hacia (10.3, 5.3) 
        // Actualizamos un lapso de tiempo pequeño para ver que se mueve sin llegar
        bala.update(0.1f); 
        
        assertFalse(bala.isHit(), "La bala no debió impactar aún con un delta muy bajo");
    }

    @Test
    void testHitTargetAppliesDamage() {
        Bala bala = new Bala("bala-1", sourceTower, targetEnemy, 20.0);
        
        // Simular un tiempo largo para asegurar que la bala lo alcanza instantáneamente
        bala.update(10.0f); // 10 segundos a 12 speed son 120 bloques, lo alcanza seguro
        
        assertTrue(bala.isHit(), "La bala debió impactar al objetivo");
        assertEquals(85.0, targetEnemy.GetVida(), "El objetivo debió recibir 15 de daño de la torre común");
    }

    @Test
    void testTargetAlreadyDead() {
        Bala bala = new Bala("bala-1", sourceTower, targetEnemy, 20.0);
        targetEnemy.setVida(0); // Objetivo ya muerto
        
        bala.update(0.1f);
        
        assertTrue(bala.isHit(), "La bala debe marcarse como 'hit' inmediatamente si el target está muerto y desaparecer");
    }

    @Test
    void testBounces() {
        Bala bala = new Bala("bala-1", sourceTower, targetEnemy, 20.0);
        assertEquals(0, bala.getBounces(), "La bala inicial no debe tener rebotes");
        
        bala.setBounces(2);
        assertEquals(2, bala.getBounces(), "Los rebotes deben setearse correctamente");
        
        bala.getHitEnemies().add(targetEnemy);
        assertTrue(bala.getHitEnemies().contains(targetEnemy), "La lista de enemigos golpeados debe actualizarse");
    }
}
