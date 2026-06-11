package com.miJuego.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ScoreboardTest {
    private static final String FILE_NAME = "scoreboard.txt";
    private static final String BACKUP_FILE_NAME = "scoreboard.txt.bak";
    private boolean backupExists = false;

    @BeforeEach
    void setUp() throws IOException {
        // Respaldar archivo existente si lo hay
        Path originalPath = Paths.get(FILE_NAME);
        if (Files.exists(originalPath)) {
            Files.move(originalPath, Paths.get(BACKUP_FILE_NAME));
            backupExists = true;
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Eliminar el archivo de prueba
        Files.deleteIfExists(Paths.get(FILE_NAME));

        // Restaurar el respaldo original si existía
        if (backupExists) {
            Files.move(Paths.get(BACKUP_FILE_NAME), Paths.get(FILE_NAME));
            backupExists = false;
        }
    }

    @Test
    void testScoreboardSaveAndLoad() {
        // Guardar algunas entradas
        Scoreboard.save("Alice", 1500);
        Scoreboard.save("Bob", 2000);
        Scoreboard.save("Charlie", 1800);

        List<Scoreboard.Entry> entries = Scoreboard.load();
        
        // Debe tener 3 entradas ordenadas de mayor a menor (Bob, Charlie, Alice)
        assertEquals(3, entries.size());
        assertEquals("Bob", entries.get(0).getName());
        assertEquals(2000, entries.get(0).getScore());
        
        assertEquals("Charlie", entries.get(1).getName());
        assertEquals(1800, entries.get(1).getScore());
        
        assertEquals("Alice", entries.get(2).getName());
        assertEquals(1500, entries.get(2).getScore());
    }

    @Test
    void testScoreboardTop10Limit() {
        // Guardar 12 entradas con puntajes crecientes
        for (int i = 1; i <= 12; i++) {
            Scoreboard.save("Jugador" + i, i * 100);
        }

        List<Scoreboard.Entry> entries = Scoreboard.load();
        
        // Debe mantener solo las 10 mejores puntuaciones (las del 3 al 12)
        assertEquals(10, entries.size());
        
        // El mejor puntaje debe ser el de Jugador12 (1200)
        assertEquals("Jugador12", entries.get(0).getName());
        assertEquals(1200, entries.get(0).getScore());
        
        // El peor puntaje guardado en el TOP 10 debe ser el de Jugador3 (300)
        assertEquals("Jugador3", entries.get(9).getName());
        assertEquals(300, entries.get(9).getScore());
    }

    @Test
    void testScoreboardEmptyFile() {
        // Cargar desde scoreboard vacío/inexistente
        List<Scoreboard.Entry> entries = Scoreboard.load();
        assertTrue(entries.isEmpty());
    }

    @Test
    void testScoreboardSanitizeName() {
        // Guardar un nombre que tenga punto y coma para validar que se limpie y no rompa el formato CSV
        Scoreboard.save("Hacker;Pro", 500);
        
        List<Scoreboard.Entry> entries = Scoreboard.load();
        assertEquals(1, entries.size());
        assertEquals("HackerPro", entries.get(0).getName());
    }
}

