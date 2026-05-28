package com.miJuego.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Scoreboard {
    private static final String FILE_NAME = "scoreboard.txt";

    public static class Entry {
        private final String name;
        private final int score;
        private final String date;

        public Entry(String name, int score, String date) {
            this.name = name;
            this.score = score;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }

        public String getDate() {
            return date;
        }
        
        @Override
        public String toString() {
            return String.format("%s - %d pts (%s)", name, score, date);
        }
    }

    // Carga las puntuaciones desde el archivo
    public static List<Entry> load() {
        List<Entry> entries = new ArrayList<>();
        if (!Files.exists(Paths.get(FILE_NAME))) {
            return entries;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    try {
                        String name = parts[0];
                        int score = Integer.parseInt(parts[1]);
                        String date = parts[2];
                        entries.add(new Entry(name, score, date));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar scoreboard: " + e.getMessage());
        }

        // Ordenar de mayor a menor puntaje
        entries.sort(Comparator.comparingInt(Entry::getScore).reversed());
        return entries;
    }

    // Guarda una nueva puntuación y mantiene solo el TOP 10
    public static void save(String name, int score) {
        if (name == null || name.isBlank()) {
            name = "Jugador Anónimo";
        }
        name = name.replace(";", ""); // Evitar roturas de formato CSV

        List<Entry> entries = load();
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dateStr = dtf.format(LocalDateTime.now());
        
        entries.add(new Entry(name, score, dateStr));
        
        // Ordenar y recortar a 10
        entries.sort(Comparator.comparingInt(Entry::getScore).reversed());
        if (entries.size() > 10) {
            entries = entries.subList(0, 10);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Entry entry : entries) {
                writer.write(entry.getName() + ";" + entry.getScore() + ";" + entry.getDate());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar scoreboard: " + e.getMessage());
        }
    }
}
