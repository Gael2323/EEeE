package com.miJuego.model;

import java.awt.Color;
import java.util.Optional;

public class TorreComun extends Torre implements DañoDeTorre {
    private double dañoComun = 15.0;

    public TorreComun(String id, float x, float y) {
        super(id, x, y, 100.0, 500, "TorreComun");
        this.rango = 3.5;
    }

    @Override
    public double ataque(Enemigo enemigo) {
        // Hace daño básico al enemigo y lo devuelve
        double dañoEfectivo = dañoComun * nivelMejora;
        enemigo.setVida(enemigo.GetVida() - dañoEfectivo);
        return dañoEfectivo;
    }

    @Override
    public void upgrade() {
        nivelMejora++;
        costoTorre += 50.0;
        rango += 0.5;
        // Reducimos el tiempo de recarga ligeramente
        tiempoRecarga = Math.max(500, tiempoRecarga - 80);
    }

    @Override
    public Color getFallbackColor() {
        // Color azulado para torre común
        return new Color(70, 130, 180);
    }
    @Override
    public Optional<String> getImagePath() {
        return Optional.of("src/main/resources/torre_comun.png"); // Ruta relativa al proyecto
    }
}
