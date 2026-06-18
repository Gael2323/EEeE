package com.miJuego.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CorruptedFolder extends Enemigo {

    private float invocacionTimer = 0f;
    private static final float COOLDOWN_INVOCACION = 3.5f;
    private int invocadosCount = 0;
    private final Random random = new Random();

    public CorruptedFolder(String id) {
        super(id, "CorruptedFolder", 250.0, 40, 25);
        this.rapidez = 1.2; // Lento, tipo tanque
        this.damageBase = 3.0; // Hace bastante damage si llega al final
        this.width = 2.0f;
        this.height = 2.0f;
    }

    @Override
    public List<Enemigo> morir() {
        // Al morir no se divide en sub-enemigos
        return new ArrayList<>();
    }

    public Enemigo updateInvocacion(float deltaSeconds) {
        // Si está aturdido o paralizado, no invoca
        if (paralizacionTimer > 0) {
            return null;
        }

        invocacionTimer += deltaSeconds;
        if (invocacionTimer >= COOLDOWN_INVOCACION) {
            invocacionTimer = 0f;
            invocadosCount++;

            // Elegir un tipo de archivo al azar
            String[] tipos = {"Word", "PNG", "ZIP", "pop", "txt", "xls"};
            String elegido = tipos[random.nextInt(tipos.length)];

            String nuevoId = this.id + "-invocado-" + invocadosCount;
            InvocadoCorrupted invocado = new InvocadoCorrupted(nuevoId, elegido);

            // Hereda posición exacta
            invocado.x = this.x;
            invocado.y = this.y;
            // Hereda el nodo destino actual
            invocado.setTargetNode(this.getTargetNode());
            // Hereda cantidad de nodos visitados
            invocado.setNodosVisitados(this.getNodosVisitados());

            return invocado;
        }
        return null;
    }

    @Override
    public Optional<String> getImagePath() {
        return Optional.of("assets/ingame/enemies/corrupted_folder/Corrupted_Folder" + currentOctant + ".png");
    }

    // Métodos getters/setters útiles para testear
    public float getInvocacionTimer() {
        return invocacionTimer;
    }

    public void setInvocacionTimer(float timer) {
        this.invocacionTimer = timer;
    }
}
