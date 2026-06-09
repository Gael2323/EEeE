package com.miJuego.model;

import com.game2d.model.Drawable;
import com.game2d.model.FallbackShape;

import java.awt.Color;
import java.net.URL;
import java.util.Optional;

public class Bala implements Drawable {
    private final String id;
    private float x;
    private float y;
    private final Enemigo target;
    private final float destX;
    private final float destY;
    private final Torre sourceTower;
    private final double daño;
    private final float speed = 12.0f; // Velocidad del proyectil (celdas por segundo)
    private boolean hit = false;
    
    // Para rebotes (cadena de rayos)
    private int bounces = 0;
    private java.util.Set<Enemigo> hitEnemies = new java.util.HashSet<>();

    public Bala(String id, Torre sourceTower, Enemigo target, double daño) {
        this(id, sourceTower, target, daño, sourceTower.getX() + 0.5f, sourceTower.getY() + 0.5f);
    }

    public Bala(String id, Torre sourceTower, Enemigo target, double daño, float startX, float startY) {
        this.id = id;
        this.sourceTower = sourceTower;
        this.x = startX;
        this.y = startY;
        this.target = target;
        if (target != null) {
            this.destX = target.getX() + 0.3f;
            this.destY = target.getY() + 0.3f;
        } else if (sourceTower instanceof TorreAvast avast) {
            this.destX = avast.getTargetX() + 0.5f;
            this.destY = avast.getTargetY() + 0.5f;
        } else {
            this.destX = this.x;
            this.destY = this.y;
        }
        this.daño = daño;
    }

    public void update(float deltaSeconds) {
        if (hit) return;
        if (target != null && target.isDead()) {
            hit = true;
            return;
        }

        // Vector hacia el destino
        float targetX = target != null ? target.getX() + 0.3f : this.destX;
        float targetY = target != null ? target.getY() + 0.3f : this.destY;
        
        float dx = targetX - this.x;
        float dy = targetY - this.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist <= speed * deltaSeconds) {
            // Impactar al destino
            this.x = targetX;
            this.y = targetY;
            this.hit = true;
            
            // Aplicar daño si es a un enemigo directo
            if (target != null) {
                if (sourceTower instanceof DañoDeTorre) {
                    ((DañoDeTorre) sourceTower).ataque(target);
                } else {
                    target.setVida(target.GetVida() - daño);
                }
            }
            
            // El daño de área de Avast se hace en Juego.java

        } else {
            // Avanzar hacia el objetivo
            this.x += (float) (dx / dist * speed * deltaSeconds);
            this.y += (float) (dy / dist * speed * deltaSeconds);
        }
    }

    public boolean isHit() {
        return hit;
    }

    public Torre getSourceTower() {
        return sourceTower;
    }

    public Enemigo getTarget() {
        return target;
    }

    public double getDaño() {
        return daño;
    }

    public int getBounces() {
        return bounces;
    }

    public void setBounces(int bounces) {
        this.bounces = bounces;
    }

    public java.util.Set<Enemigo> getHitEnemies() {
        return hitEnemies;
    }

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
        return 0.35f; // Proyectiles ligeramente más grandes para que se note la imagen
    }

    @Override
    public Float getHeight() {
        return 0.35f;
    }

    @Override
    public Optional<String> getImagePath() {
        return Optional.of("assets/ingame/projectil.png");
    }

    @Override
    public Optional<URL> getImageUrl() {
        return Optional.empty();
    }

    @Override
    public Color getFallbackColor() {
        // Balas de diferentes colores según la torre
        if (sourceTower instanceof TorreFirefox) {
            return Color.RED;
        } else if (sourceTower instanceof TorreDeHielo) {
            return Color.CYAN;
        } else if (sourceTower instanceof TorreElectrica) {
            return Color.YELLOW;
        } else if (sourceTower instanceof TorreAvast) {
            return Color.DARK_GRAY;
        }
        return Color.BLACK;
    }

    @Override
    public FallbackShape getFallbackShape() {
        return FallbackShape.ELLIPSE;
    }

    @Override
    public int getLayer() {
        return 8; // Por encima de los enemigos
    }
}
