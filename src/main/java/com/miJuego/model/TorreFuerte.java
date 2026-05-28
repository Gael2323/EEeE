package com.miJuego.model;

import java.awt.Color;

public class TorreFuerte extends Torre implements DañoDeTorre {
    private Enemigo objetivo;
    private double dañoBase = 80.0;

    public TorreFuerte(String id, float x, float y) {
        super(id, x, y, 250.0, 3500, "TorreFuerte");
        this.rango = 5.0;
    }

    @Override
    public double ataque(Enemigo enemigo) {
        double dañoEfectivo = dañoBase * nivelMejora;
        enemigo.setVida(enemigo.GetVida() - dañoEfectivo);
        return dañoEfectivo;
    }

    @Override
    public void upgrade() {
        nivelMejora++;
        costoTorre += 125.0;
        dañoBase += 40.0;
        rango += 0.5;
        tiempoRecarga = Math.max(1500, tiempoRecarga - 300);
    }

    public Enemigo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Enemigo objetivo) {
        this.objetivo = objetivo;
    }

    @Override
    public Color getFallbackColor() {
        // Color rojo oscuro para torre fuerte
        return new Color(139, 0, 0);
    }
}
