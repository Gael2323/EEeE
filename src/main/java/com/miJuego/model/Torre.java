package com.miJuego.model;

import com.game2d.model.Drawable;
import com.game2d.model.FallbackShape;
import com.game2d.model.Renderable;

import java.awt.Color;
import java.net.URL;
import java.util.Optional;

public abstract class Torre implements Drawable {
    protected String id;
    protected float x;
    protected float y;
    protected double costoTorre;
    protected int tiempoRecarga; // Cooldown en milisegundos (ej. 1000 ms)
    protected String towertype;
    
    // Atributos de juego adicionales
    protected float cooldownTimer = 0f; // Tiempo restante para volver a disparar (en segundos)
    protected double rango = 3.5; // Rango por defecto
    protected int nivelMejora = 1; // Nivel de la torre (empieza en 1)

    public Torre(String id, float x, float y, double costoTorre, int tiempoRecarga, String towertype) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.costoTorre = costoTorre;
        this.tiempoRecarga = tiempoRecarga;
        this.towertype = towertype;
    }

    // Métodos UML
    public void setCostoTorre(double costoTorre) {
        this.costoTorre = costoTorre;
    }

    public double GetCostoTorre() {
        return costoTorre;
    }

    public abstract void upgrade();

    // Getters y Setters de juego
    public String getTowertype() {
        return towertype;
    }

    public int getTiempoRecarga() {
        return tiempoRecarga;
    }

    public void setTiempoRecarga(int tiempoRecarga) {
        this.tiempoRecarga = tiempoRecarga;
    }

    public double getRango() {
        return rango;
    }

    public void setRango(double rango) {
        this.rango = rango;
    }

    public int getNivelMejora() {
        return nivelMejora;
    }

    public float getCooldownTimer() {
        return cooldownTimer;
    }

    public void resetCooldown() {
        this.cooldownTimer = (float) (tiempoRecarga / 1000.0);
    }

    public void updateCooldown(float deltaSeconds) {
        if (cooldownTimer > 0) {
            cooldownTimer -= deltaSeconds;
        }
    }

    public boolean canShoot() {
        return cooldownTimer <= 0;
    }

    // Implementación de Drawable
    @Override
    public String getId() {
        return id;
    }

    @Override
    public Float getX() {
        return x;
    }

    @Override
    public Float getY() {
        return y;
    }

    @Override
    public Float getWidth() {
        return 1.0f; // Tamaño fijo de 1 celda en el grid
    }

    @Override
    public Float getHeight() {
        return 1.0f;
    }

    @Override
    public Optional<String> getImagePath() {
        return Optional.empty();
    }

    @Override
    public Optional<URL> getImageUrl() {
        return Optional.empty();
    }

    @Override
    public Color getFallbackColor() {
        return Color.GRAY;
    }

    @Override
    public FallbackShape getFallbackShape() {
        return FallbackShape.RECTANGLE;
    }

    @Override
    public int getLayer() {
        return 10; // Se dibuja por encima del suelo y del camino
    }
}
