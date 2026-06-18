package com.miJuego.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CorruptedFolderTest {

    @Test
    public void testCorruptedFolderStats() {
        CorruptedFolder folder = new CorruptedFolder("test-folder");
        assertEquals(250.0, folder.GetVida(), 0.001);
        assertEquals(1.2, folder.getRapidez(), 0.001);
        assertEquals(40, folder.GetMonedasGeneradas());
        assertEquals(25, folder.GetScoreGenerado());
        assertEquals(2.0f, folder.getWidth(), 0.001f);
        assertEquals(2.0f, folder.getHeight(), 0.001f);
    }

    @Test
    public void testCorruptedFolderSummonMechanics() {
        CorruptedFolder folder = new CorruptedFolder("test-folder");
        folder.setPosicion(10.5f, 20.5f);
        WaypointNode targetNode = new WaypointNode(15f, 25f);
        folder.setTargetNode(targetNode);
        folder.setNodosVisitados(4);

        // Al inicio, no hay invocación aún
        Enemigo e1 = folder.updateInvocacion(1.0f);
        assertNull(e1);
        assertEquals(1.0f, folder.getInvocacionTimer(), 0.001f);

        // Avanzamos 2.5s más (total 3.5s)
        Enemigo summoned = folder.updateInvocacion(2.5f);
        assertNotNull(summoned);
        assertInstanceOf(InvocadoCorrupted.class, summoned);

        InvocadoCorrupted subEnemy = (InvocadoCorrupted) summoned;
        assertEquals(50.0, subEnemy.GetVida(), 0.001);
        assertEquals(5, subEnemy.GetMonedasGeneradas());
        assertEquals(5, subEnemy.GetScoreGenerado());
        assertEquals(3.0, subEnemy.getRapidez(), 0.001);

        // Verificar herencia de posición y trayectoria
        assertEquals(10.5f, subEnemy.getX(), 0.001f);
        assertEquals(20.5f, subEnemy.getY(), 0.001f);
        assertEquals(targetNode, subEnemy.getTargetNode());
        assertEquals(4, subEnemy.getNodosVisitados());

        // Asegurarse de que el timer se reinició
        assertEquals(0f, folder.getInvocacionTimer(), 0.001f);
    }
}
