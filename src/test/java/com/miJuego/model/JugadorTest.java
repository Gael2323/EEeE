package com.miJuego.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JugadorTest {

    private Jugador jugador;

    @BeforeEach
    void setUp() {
        jugador = new Jugador(0.0, 100, 50);
    }

    @Test
    void testInitialValues() {
        assertEquals(0.0, jugador.getScore(), "El puntaje inicial debe ser 0.0");
        assertEquals(100, jugador.getHealth(), "La vida inicial debe ser 100");
        assertEquals(50, jugador.getMoneda(), "Las monedas iniciales deben ser 50");
    }

    @Test
    void testAddScore() {
        jugador.addScore(15.5);
        assertEquals(15.5, jugador.getScore(), "El puntaje debe aumentar correctamente");
        
        jugador.setScore(100.0);
        assertEquals(100.0, jugador.getScore(), "SetScore debe sobreescribir el valor");
    }

    @Test
    void testDecreaseHealth() {
        jugador.decreaseHealth(30);
        assertEquals(70, jugador.getHealth(), "La vida debe reducirse en 30");

        // Validar que la vida no baje de 0
        jugador.decreaseHealth(100);
        assertEquals(0, jugador.getHealth(), "La vida no debe bajar de cero");
        
        jugador.setHealth(200);
        assertEquals(200, jugador.getHealth(), "SetHealth debe actualizar la salud");
    }

    @Test
    void testMonedas() {
        jugador.addMoneda(25);
        assertEquals(75, jugador.getMoneda(), "Añadir monedas debe incrementar el total");
        
        jugador.setMoneda(100);
        assertEquals(100, jugador.getMoneda(), "SetMoneda debe sobreescribir el total");
    }

    @Test
    void testSpendMonedas() {
        jugador.setMoneda(100);
        
        // Gasto exitoso
        boolean success = jugador.spendMoneda(40);
        assertTrue(success, "Gastar menos monedas o igual al total debe ser exitoso");
        assertEquals(60, jugador.getMoneda(), "El balance de monedas debe reducirse correctamente");
        
        // Gasto fallido
        boolean fail = jugador.spendMoneda(100);
        assertFalse(fail, "Intentar gastar más de lo que se tiene debe fallar");
        assertEquals(60, jugador.getMoneda(), "El balance no se debe modificar si falla la compra");
    }
}
