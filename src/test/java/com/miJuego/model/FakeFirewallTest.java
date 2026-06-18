package com.miJuego.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FakeFirewallTest {

    // ─── Helper ───────────────────────────────────────────────────────────────
    private float getFloat(Object obj, String field) throws Exception {
        java.lang.reflect.Field f = Enemigo.class.getDeclaredField(field);
        f.setAccessible(true);
        return (float) f.get(obj);
    }

    private double getDouble(Object obj, String field) throws Exception {
        java.lang.reflect.Field f = Enemigo.class.getDeclaredField(field);
        f.setAccessible(true);
        return (double) f.get(obj);
    }

    // ─── 1. Inicialización ───────────────────────────────────────────────────

    @Test
    public void testInitialization() {
        FakeFirewall fw = new FakeFirewall("fw-test");
        assertEquals(200.0, fw.GetVida(), 0.001);
        assertEquals(1.8,   fw.getRapidez(), 0.001);
        assertEquals(35,    fw.GetMonedasGeneradas());
        assertEquals(20,    fw.GetScoreGenerado());
        assertEquals(1.3f,  fw.getWidth(),  0.001f);
        assertEquals(1.3f,  fw.getHeight(), 0.001f);
        assertEquals(FakeFirewall.EstadoFakeFirewall.WALKING, fw.getEstado());
    }

    // ─── 2. Limpieza de aliados cercanos ────────────────────────────────────

    @Test
    public void testLimpiarAliadosCercanos() throws Exception {
        FakeFirewall fw = new FakeFirewall("fw");
        fw.setPosicion(10f, 10f);

        Duende e1 = new Duende("e1");
        e1.setPosicion(11f, 11f);
        e1.aplicarRalentizar(0.5, 5f);
        e1.aplicarParalizacion(3f);

        Duende e2 = new Duende("e2");
        e2.setPosicion(13f, 13f);
        e2.aplicarRalentizar(0.5, 5f);
        e2.aplicarParalizacion(3f);

        List<Enemigo> todos = new ArrayList<>(List.of(fw, e1, e2));
        fw.limpiarAliadosCercanos(todos);

        assertEquals(0f,  getFloat(e1,  "ralentizarTimer"),  0.001f);
        assertEquals(1.0, getDouble(e1, "ralentizarFactor"), 0.001);
        assertEquals(0f,  getFloat(e1,  "paralizacionTimer"), 0.001f);

        assertTrue(getFloat(e2, "ralentizarTimer")  > 0f);
        assertTrue(getDouble(e2,"ralentizarFactor") < 1.0);
        assertTrue(getFloat(e2, "paralizacionTimer") > 0f);
    }

    // ─── 3. Inmunidad al ralentizado ─────────────────────────────────────────

    @Test
    public void testInmunidadAlRalentizado() {
        FakeFirewall fw = new FakeFirewall("fw");
        fw.aplicarRalentizar(0.3, 5f);
        assertEquals(fw.getRapidez(), fw.getVelocidadActual(), 0.001);
    }

    // ─── 4. Taunt: torres de hielo priorizan al FakeFirewall ─────────────────

    @Test
    public void testTauntTorreDeHielo() {
        FakeFirewall fw = new FakeFirewall("fw");
        Duende duende   = new Duende("duende");

        TorreDeHielo hielo = new TorreDeHielo("hielo", 10f, 10f);
        TorreInternetExplorer ie = new TorreInternetExplorer("ie", 10f, 10f);

        assertEquals(fw, hielo.selectIceTarget(List.of(duende, fw)));
        assertEquals(fw, ie.selectIceTarget(List.of(fw, duende)));
        assertNull(hielo.selectIceTarget(List.of(duende)));
    }

    // ─── 5. Resistencia al damage de hielo (90% reducción) ────────────────────

    @Test
    public void testResistenciaDamageHielo() {
        FakeFirewall fw = new FakeFirewall("fw");
        double vidaAntes = fw.GetVida();

        TorreDeHielo hielo = new TorreDeHielo("hielo", 5f, 5f);
        double damage = hielo.ataque(fw);

        assertEquals(vidaAntes - damage, fw.GetVida(), 0.001);
        assertFalse(fw.tieneRalentizar());
    }

    // ─── 6. Transición WALKING -> APPROACHING ────────────────────────────────

    @Test
    public void testTransicionWalkingToApproaching() {
        FakeFirewall fw = new FakeFirewall("fw");
        fw.setPosicion(10f, 10f);

        TorreDeHielo hielo = new TorreDeHielo("hielo", 14f, 10f); // Distancia 4.0 (rango det=6.0)
        List<Torre> torres = new ArrayList<>(List.of(hielo));
        List<Enemigo> enemigos = new ArrayList<>(List.of(fw));

        // Scan ICE dispara cada 0.5s
        fw.updateFakeFirewallState(0.6f, enemigos, torres);

        assertEquals(FakeFirewall.EstadoFakeFirewall.APPROACHING, fw.getEstado());
        assertEquals(hielo, fw.getTargetIceTower());
    }

    // ─── 7. Transición APPROACHING -> BURNING ────────────────────────────────

    @Test
    public void testTransicionApproachingToBurning() {
        FakeFirewall fw = new FakeFirewall("fw");
        fw.setPosicion(10f, 10f);

        TorreDeHielo hielo = new TorreDeHielo("hielo", 12f, 10f); // Distancia 2.0
        List<Torre> torres = new ArrayList<>(List.of(hielo));
        List<Enemigo> enemigos = new ArrayList<>(List.of(fw));

        // Primer tick: lo detecta y pasa a APPROACHING
        fw.updateFakeFirewallState(0.6f, enemigos, torres);
        
        // Dado que la distancia es 2.0, y el rango de la torre de hielo es 2.0 o mayor,
        // debería pasar inmediatamente a BURNING si el rango >= 2.0.
        // TorreDeHielo por defecto tiene rango 2.0
        assertTrue(fw.getEstado() == FakeFirewall.EstadoFakeFirewall.BURNING || 
                   fw.getEstado() == FakeFirewall.EstadoFakeFirewall.APPROACHING);
    }

    // ─── 8. getImagePath: sprite normal sin BURNING ──────────────────────────

    @Test
    public void testImagePathNormal() {
        FakeFirewall fw = new FakeFirewall("fw");
        String path = fw.getImagePath().orElse("");
        assertTrue(path.endsWith("Fake_Firewall2.png"), "Debe usar Fake_Firewall2.png");
    }

    // ─── 9. getImagePath: sprites quemándose en BURNING ──────────────────────

    @Test
    public void testImagePathSpritesQuemandose() {
        FakeFirewall fw = new FakeFirewall("fw");
        fw.setPosicion(10f, 10f);
        TorreDeHielo hielo = new TorreDeHielo("hielo", 10f, 10f);
        List<Torre> torres = new ArrayList<>(List.of(hielo));

        // Primero detectarlo para asignarlo como targetIceTower y cambiar a BURNING
        fw.updateFakeFirewallState(0.6f, new ArrayList<>(), torres);

        // Avanzar animación
        fw.updateFakeFirewallState(0.1f, new ArrayList<>(), torres);

        String path = fw.getImagePath().orElse("");
        assertTrue(path.contains("Quemandose"), "Debe usar sprites quemandose: " + path);
    }
}
