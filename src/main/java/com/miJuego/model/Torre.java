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
    
    // Rastreo de objetivos para la orientación visual
    protected Enemigo currentTarget;

    public void findTarget(java.util.List<Enemigo> enemigos) {
        if (currentTarget != null && currentTarget.GetVida() > 0) {
            double dx = currentTarget.getX() - this.x;
            double dy = currentTarget.getY() - this.y;
            if (dx * dx + dy * dy <= rango * rango) {
                return;
            }
        }
        
        currentTarget = null;
        double minDistSq = Double.MAX_VALUE;
        for (Enemigo e : enemigos) {
            if (e.GetVida() <= 0) continue;
            double dx = e.getX() - this.x;
            double dy = e.getY() - this.y;
            double distSq = dx * dx + dy * dy;
            if (distSq <= rango * rango && distSq < minDistSq) {
                minDistSq = distSq;
                currentTarget = e;
            }
        }
    }

    /**
     * Prefijo del sprite de esta torre. Las subclases lo sobreescriben
     * para usar su propio set de sprites (ej: "torremc" para McAfee).
     * Los archivos deben seguir el formato: {prefijo}0.png .. {prefijo}7.png
     * donde 0=arriba, 1=arriba-derecha, 2=derecha, 3=abajo-derecha,
     *       4=abajo, 5=abajo-izquierda, 6=izquierda, 7=arriba-izquierda
     */
    public String getSpritePrefix() {
        return "torrecomun"; // Por defecto usa los sprites de torre común
    }

    public String getTowerSprite() {
        if (currentTarget == null) {
            return "assets/ingame/Torre_Reposo.png"; // Reposo/default
        }
        double dx = currentTarget.getX() - this.x;
        double dy = currentTarget.getY() - this.y;
        double angle = Math.atan2(dy, dx); // de -PI a PI
        
        // Convertir de [-PI, PI] a [0, 2*PI]
        if (angle < 0) {
            angle += 2 * Math.PI;
        }
        
        // Dividimos en 8 sectores de 45 grados (PI/4 rad), centrados con un offset de 22.5 grados (PI/8)
        double shifted = angle + Math.PI / 8.0;
        if (shifted >= 2 * Math.PI) {
            shifted -= 2 * Math.PI;
        }
        
        int octant = (int) (shifted / (Math.PI / 4.0));
        // Mapeo: atan2 octant -> dirección del sprite
        // octant 0 = derecha   -> sprite 2 (derecha)
        // octant 1 = abajo-der -> sprite 3 (abajo-derecha)
        // octant 2 = abajo     -> sprite 4 (abajo)
        // octant 3 = abajo-izq -> sprite 5 (abajo-izquierda)
        // octant 4 = izquierda -> sprite 6 (izquierda)
        // octant 5 = arriba-iz -> sprite 7 (arriba-izquierda)
        // octant 6 = arriba    -> sprite 0 (arriba)
        // octant 7 = arriba-de -> sprite 1 (arriba-derecha)
        int spriteIndex = switch (octant) {
            case 0 -> 2; // Derecha
            case 1 -> 3; // Abajo-Derecha
            case 2 -> 4; // Abajo
            case 3 -> 5; // Abajo-Izquierda
            case 4 -> 6; // Izquierda
            case 5 -> 7; // Arriba-Izquierda
            case 6 -> 0; // Arriba
            case 7 -> 1; // Arriba-Derecha
            default -> 4; // Abajo por defecto
        };
        return "assets/ingame/" + getSpritePrefix() + spriteIndex + ".png";
    }

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
        return 2.0f;
    }

    @Override
    public Float getHeight() {
        return 2.0f;
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
