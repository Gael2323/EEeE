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
    private final Torre sourceTower;
    private final double daño;
    private final float speed = 12.0f; // Velocidad del proyectil (celdas por segundo)
    private boolean hit = false;

    public Bala(String id, Torre sourceTower, Enemigo target, double daño) {
        this.id = id;
        this.sourceTower = sourceTower;
        this.x = sourceTower.getX() + 0.5f; // Lanzar desde el centro de la torre
        this.y = sourceTower.getY() + 0.5f;
        this.target = target;
        this.daño = daño;
    }

    public void update(float deltaSeconds) {
        if (hit || target == null || target.isDead()) {
            hit = true;
            return;
        }

        // Vector hacia el enemigo
        float targetX = target.getX() + 0.3f; // Centro del enemigo
        float targetY = target.getY() + 0.3f;
        
        float dx = targetX - this.x;
        float dy = targetY - this.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist <= speed * deltaSeconds) {
            // Impactar al enemigo
            this.x = targetX;
            this.y = targetY;
            this.hit = true;
            
            // Aplicar daño del ataque de la torre al enemigo
            if (sourceTower instanceof DañoDeTorre) {
                ((DañoDeTorre) sourceTower).ataque(target);
            } else {
                target.setVida(target.GetVida() - daño);
            }
            
            // Si el cañón impacta, realiza daño en área (splash) adicional
            if (sourceTower instanceof Cañon) {
                Cañon c = (Cañon) sourceTower;
                double splashRadius = c.getAreaAGolpear();
                // Buscar otros enemigos en rango de la explosión
                // Esto se resolverá en la lista de enemigos activos del nivel,
                // la cual gestionaremos en Juego.java mediante un trigger de colisión
            }
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
        return 0.15f; // Proyectiles muy pequeños
    }

    @Override
    public Float getHeight() {
        return 0.15f;
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
        // Balas de diferentes colores según la torre
        if (sourceTower instanceof TorreDeFuego) {
            return Color.RED;
        } else if (sourceTower instanceof TorreDeHielo) {
            return Color.CYAN;
        } else if (sourceTower instanceof TorreElectrica) {
            return Color.YELLOW;
        } else if (sourceTower instanceof Cañon) {
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
