package com.miJuego.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AresTest {

    @Test
    public void testAresInitialization() {
        Ares ares = new Ares("test-ares");
        assertEquals(350.0, ares.GetVida(), 0.001);
        assertEquals(0.8, ares.getRapidez(), 0.001);
        assertEquals(50, ares.GetMonedasGeneradas());
        assertEquals(30, ares.GetScoreGenerado());
        assertEquals(1.6f, ares.getWidth(), 0.001f);
        assertEquals(1.6f, ares.getHeight(), 0.001f);
        assertEquals(Ares.EstadoAres.WALKING, ares.getEstadoAres());
    }

    @Test
    public void testAresDeteccionYEscudoEnRango() {
        Ares ares = new Ares("test-ares");
        ares.setPosicion(10f, 10f);

        TorreMessenger messenger = new TorreMessenger("test-messenger", 12f, 12f); // Rango de la torre = 3.5

        // 1. Escanear y entrar en APPROACHING
        ares.updateAresState(0.5f, List.of(messenger));
        assertEquals(Ares.EstadoAres.APPROACHING, ares.getEstadoAres());
        assertEquals(messenger, ares.getTargetMessenger());

        // 2. Acercar a Ares a la torre pero dentro de su rango (distancia ~ 3.25, menor a 3.5)
        ares.setPosicion(12.3f, 12.3f);
        ares.updateAresState(0.1f, List.of(messenger));
        assertEquals(Ares.EstadoAres.SHIELDING, ares.getEstadoAres());
        assertEquals(10f, ares.getShieldTimer(), 0.001f);
        assertEquals(0.0, ares.getVelocidadActual(), 0.001);

        // 3. Verificar progreso y congelamiento de la animación del escudo
        // Frame 0: shieldTimeElapsed = 0.0
        String path0 = ares.getImagePath().orElse("");
        assertTrue(path0.contains("CargandoEscudo") && path0.contains("_0.png"));

        // Frame 3: shieldTimeElapsed = 0.5s (0.5 / 0.15 = 3)
        ares.updateAresState(0.5f, List.of(messenger));
        String path3 = ares.getImagePath().orElse("");
        assertTrue(path3.contains("CargandoEscudo") && path3.contains("_3.png"));

        // Frame 5: shieldTimeElapsed = 1.0s (1.0 / 0.15 = 6, capado a 5)
        ares.updateAresState(0.5f, List.of(messenger));
        String path5 = ares.getImagePath().orElse("");
        assertTrue(path5.contains("CargandoEscudo") && path5.contains("_5.png"));

        // Frame 5 congelado tras 3.0s más
        ares.updateAresState(3.0f, List.of(messenger));
        String pathFrozen = ares.getImagePath().orElse("");
        assertTrue(pathFrozen.contains("CargandoEscudo") && pathFrozen.contains("_5.png"));

        // 4. Dejar que expire el escudo
        ares.updateAresState(6.0f, List.of(messenger)); // Total transcurrido > 10.0s
        assertEquals(Ares.EstadoAres.WALKING, ares.getEstadoAres());
        assertNull(ares.getTargetMessenger());
        assertEquals(0.8, ares.getVelocidadActual(), 0.001);
        assertEquals(5.0f, ares.getCooldownTimer(), 0.001f);
    }

    @Test
    public void testTorreElectricaDamageReductionYPriorizacion() {
        Ares ares = new Ares("test-ares");
        TorreMessenger messenger = new TorreMessenger("test-messenger", 10f, 10f);
        TorreElectrica electrica = new TorreElectrica("test-electrica", 10f, 10f);

        // Comprobar que el primer ataque activa el escudo e inmediatamente aplica reducción de damage
        double vidaOriginal = ares.GetVida();
        electrica.ataque(ares);
        
        // Debe haberse activado el escudo
        assertTrue(ares.isShieldActive());
        assertEquals(0.0, ares.getVelocidadActual(), 0.001); // inmovilizado por escudo

        // Damage recibido debe ser 10 * 0.05 = 0.5
        double damageRecibido = vidaOriginal - ares.GetVida();
        assertEquals(0.5, damageRecibido, 0.001);

        // Debe ser inmune al efecto de paralización acumulado (debe ser 0)
        java.lang.reflect.Field fieldParalisis;
        try {
            fieldParalisis = Enemigo.class.getDeclaredField("paralizacionTimer");
            fieldParalisis.setAccessible(true);
            float paralisisTimerVal = (float) fieldParalisis.get(ares);
            assertEquals(0f, paralisisTimerVal, 0.001f);
        } catch (Exception e) {
            fail(e);
        }

        // Comprobar priorización en TorreMessenger y TorreElectrica
        Duende duende = new Duende("test-duende");
        List<Enemigo> lista = List.of(duende, ares);

        // Cuando Ares tiene el escudo activo, es priorizado
        Enemigo target1 = electrica.selectElectricTarget(lista);
        assertEquals(ares, target1);

        // Si el escudo de Ares no está activo, se prioriza el primero de la lista (duende)
        ares.setEstadoAres(Ares.EstadoAres.WALKING);
        Enemigo target2 = electrica.selectElectricTarget(lista);
        assertEquals(duende, target2);
    }
}
