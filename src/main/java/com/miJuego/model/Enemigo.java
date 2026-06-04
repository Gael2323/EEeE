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

    // Sistema de animación de caminar
    protected int animFrameCount = 5;        // Cantidad de frames de animación (0 a 4 por defecto)
    protected float animFrameDuration = 0.15f; // Duración de cada frame en segundos
    protected float animTimer = 0f;           // Timer acumulador para la animación
    protected int currentAnimFrame = 0;       // Frame actual de la animación

    // Dimensiones de dibujo para el sprite
    protected float width = 1.15f;
    protected float height = 1.15f;

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

    // Setters públicos para rapidez y dañoBase (usados desde sandbox)
    public void setRapidez(double rapidez) {
        this.rapidez = rapidez;
    }

    public double getRapidez() {
        return rapidez;
    }

    public void setDañoBase(double dañoBase) {
        this.dañoBase = dañoBase;
    }

    // Helpers de efectos de estado (usados desde sandbox sin acceso a campos protected)
    public boolean tieneFuego()        { return fuegoTimer > 0; }
    public boolean tieneRalentizar()   { return ralentizarTimer > 0; }
    public boolean tieneParalizacion() { return paralizacionTimer > 0; }

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

        // Actualizar animación de caminar (solo si se está moviendo)
        if (paralizacionTimer <= 0 && animFrameCount > 0) {
            animTimer += deltaSeconds;
            if (animTimer >= animFrameDuration) {
                animTimer -= animFrameDuration;
                currentAnimFrame = (currentAnimFrame + 1) % animFrameCount;
            }
        }
    }

    /**
     * Retorna el frame actual de la animación de caminar (0 a animFrameCount-1).
     */
    public int getAnimationFrame() {
        return currentAnimFrame;
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
        return width;
    }

    @Override
    public Float getHeight() {
        return height;
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
