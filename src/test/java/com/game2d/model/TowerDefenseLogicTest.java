package com.game2d.model;

import com.miJuego.model.EstadoJuego;
import com.miJuego.model.Juego;
import com.miJuego.model.Jugador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TowerDefenseLogicTest {

    private Juego juego;

    @BeforeEach
    void setUp() {
        // Inicializamos el núcleo de tu juego limpio antes de cada test
        juego = new Juego();
        juego.restart();
        juego.setEstado(EstadoJuego.PLAYING);
    }

    // 1. TEST MONEDAS: Ver si no te deja comprar si no te alcanza el oro
    @Test
    void testNoSePuedeComprarTorreSinMonedasSuficientes() {
        // Ponemos el tipo de torre 1 (Común - Costo: 100)
        juego.setSelectedTowerType(1);

        // Forzamos al jugador a tener solo 10 monedas (insuficiente)
        juego.getJugador().setMoneda(10);

        // Tu modelo hace un 'try-catch' porque 'placeTower' tira excepción si falla el negocio.
        // Verificamos que efectivamente lance la excepción esperada al intentar plantar en (5,5)
        assertThrows(RuntimeException.class, () -> {
            juego.placeTower(5, 5);
        }, "Debería lanzar una excepción porque el jugador no tiene oro suficiente.");

        // Verificamos que la lista de torres efectivamente haya quedado vacía
        assertEquals(0, juego.getTorres().size(), "No debería haberse agregado ninguna torre.");
    }

    // 2. TEST SUPERPOSICIÓN: Ver si te deja plantar una torre encima de otra
    @Test
    void testNoSePuedePlantarTorreSobreOtra() {
        // Aseguramos que tenga oro de sobra (ej: 1000)
        juego.getJugador().setMoneda(1000);

        // Seleccionamos y plantamos la primera torre (Tipo 1: Común) en la posición (8, 6)
        juego.setSelectedTowerType(1);
        juego.placeTower(8, 6);
        assertEquals(1, juego.getTorres().size(), "La primera torre debió colocarse.");

        // Seleccionamos otra torre (Tipo 3: Cañón) e intentamos ponerla EXACTAMENTE en el mismo lugar (8, 6)
        juego.setSelectedTowerType(3);

        // Comprobamos que el juego tire excepción por intentar pisar la posición
        assertThrows(RuntimeException.class, () -> {
            juego.placeTower(8, 6);
        }, "Debería lanzar una excepción por intentar superponer torres en la misma celda.");

        // Aseguramos que la lista se mantenga en 1 sola torre y no se haya bugeado
        assertEquals(1, juego.getTorres().size(), "El mapa debería seguir teniendo una única torre.");
    }

    // 3. TEST EXTRA (VIDAS)
    @Test
    void testEstadoInicialJugadorYConsistencia() {
        // Este test verifica que al reiniciar la partida, el jugador arranque con los valores estables
        // y que los getters expuestos al modelo respondan correctamente.
        Jugador jugador = juego.getJugador();

        // Forzamos un daño directo a su vida para comprobar que los valores cambian consistentemente
        int vidaInicial = jugador.getHealth();

        // Simulamos que modificamos la vida. Lo dejamos mal a proposito
        jugador.setHealth(vidaInicial - 6);

        // El test valida que el setter y getter impacten directo en el negocio
        assertEquals(vidaInicial - 6, juego.getJugador().getHealth(), "La salud del jugador debería haber bajado a la configurada.");
    }
}