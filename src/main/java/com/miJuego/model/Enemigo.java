package com.miJuego.model;

import com.game2d.model.Drawable;
import com.game2d.model.FallbackShape;

import java.awt.Color;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public abstract class Enemigo implements Drawable {
    protected String id;
    protected String enemyType;
    protected double vida;
    protected int monedasGeneradas;
    protected int scoreGenerado;
    
    // Posición y orientación para el juego
    protected float x;
    protected float y;
    protected int waypointIndex = 0; // Siguiente waypoint al que se dirige
    protected double rapidez = 2.0;  // Velocidad base
    protected double dañoBase = 1.0; // Daño que inflige si llega al final

    // Timers para efectos de estado
    protected float fuegoTimer = 0f;
    protected double fuegoDps = 0.0;
    
    protected float ralentizarTimer = 0f;
    protected double ralentizarFactor = 1.0; // 1.0 = normal, 0.5 = mitad de velocidad

    protected float paralizacionTimer = 0f; // Si es mayor a 0, no se mueve

    public Enemigo(String id, String enemyType, double vida, int monedasGeneradas, int scoreGenerado) {
        this.id = id;
        this.enemyType = enemyType;
        this.vida = vida;
        this.monedasGeneradas = monedasGeneradas;
        this.scoreGenerado = scoreGenerado;
    }

    // Métodos UML
    public double dañorBase() {
        return dañoBase;
    }

    public abstract List<Enemigo> morir();

    public int acelaracionMovimiento() {
        // En el UML retorna 'int rapidez'. Devolvemos la rapidez efectiva escalada.
        return (int) getVelocidadActual();
    }

    // Getters y Setters de la especificación UML (con los tipos que define el diagrama)
    public void setVida(double vida) {
        this.vida = vida;
    }

    public double GetVida() {
        return vida;
    }

    public void setMonedasGeneradas(double monedas) {
        this.monedasGeneradas = (int) monedas;
    }

    public int GetMonedasGeneradas() {
        return monedasGeneradas;
    }

    public void setScoreGenerado(double scoreGenerado) {
        this.scoreGenerado = (int) scoreGenerado;
    }

    public int GetScoreGenerado() {
        return scoreGenerado;
    }

    // Métodos de juego para actualización
    public double getVelocidadActual() {
        if (paralizacionTimer > 0) {
            return 0.0;
        }
        if (ralentizarTimer > 0) {
            return rapidez * ralentizarFactor;
        }
        return rapidez;
    }

    public void aplicarFuego(double dps, float duracionSeconds) {
        this.fuegoTimer = duracionSeconds;
        this.fuegoDps = dps;
    }

    public void aplicarRalentizar(double factor, float duracionSeconds) {
        this.ralentizarTimer = duracionSeconds;
        // Mantenemos el factor más ralentizante (el valor más bajo) si ya estaba ralentizado
        this.ralentizarFactor = Math.min(this.ralentizarFactor, factor);
    }

    public void aplicarParalizacion(float duracionSeconds) {
        this.paralizacionTimer = Math.max(this.paralizacionTimer, duracionSeconds);
    }

    public void actualizarEfectosYDaño(float deltaSeconds) {
        // Actualizar fuego
        if (fuegoTimer > 0) {
            fuegoTimer -= deltaSeconds;
            // Daño constante por segundo
            vida -= fuegoDps * deltaSeconds;
        } else {
            fuegoDps = 0.0;
        }

        // Actualizar ralentización
        if (ralentizarTimer > 0) {
            ralentizarTimer -= deltaSeconds;
            if (ralentizarTimer <= 0) {
                ralentizarFactor = 1.0;
            }
        }

        // Actualizar paralización
        if (paralizacionTimer > 0) {
            paralizacionTimer -= deltaSeconds;
        }
    }

    public void setPosicion(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public int getWaypointIndex() {
        return waypointIndex;
    }

    public void setWaypointIndex(int index) {
        this.waypointIndex = index;
    }

    public void avanzarWaypoint() {
        this.waypointIndex++;
    }

    public boolean isDead() {
        return vida <= 0;
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
        return 0.6f; // Enemigos son un poco más chicos que una celda completa
    }

    @Override
    public Float getHeight() {
        return 0.6f;
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
        if (paralizacionTimer > 0) {
            return new Color(255, 255, 100); // Amarillo eléctrico si está paralizado
        }
        if (ralentizarTimer > 0) {
            return new Color(173, 216, 230); // Celeste/azul si está ralentizado
        }
        if (fuegoTimer > 0) {
            return new Color(255, 120, 50); // Naranja si está prendido fuego
        }
        return Color.RED;
    }

    @Override
    public FallbackShape getFallbackShape() {
        return FallbackShape.ELLIPSE;
    }

    @Override
    public int getLayer() {
        return 5; // Se dibujan por encima del camino pero por debajo de las torres
    }
}
