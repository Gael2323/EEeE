package com.miJuego.model;

import java.awt.Color;

public class TorreAvast extends Torre {
    
    private boolean manualTargetingMode;
    private float targetX;
    private float targetY;

    public TorreAvast(String id, float x, float y) {
        super(id, x, y, 200.0, 3000, "TorreAvast"); // 3000ms cooldown (lento)
        this.rango = 5.0; // Largo alcance
        this.manualTargetingMode = false;
        // Por defecto, apunta al mismo lugar donde está la torre para evitar errores si no hay target
        this.targetX = x;
        this.targetY = y;
        this.resetCooldown();
    }

    public boolean isManualTargetingMode() {
        return manualTargetingMode;
    }

    public void setManualTargetingMode(boolean manualTargetingMode) {
        this.manualTargetingMode = manualTargetingMode;
    }

    public void setTargetCoordinates(float x, float y) {
        this.targetX = x;
        this.targetY = y;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    @Override
    public void findTarget(java.util.List<Enemigo> enemigos) {
        if (manualTargetingMode) {
            // En modo manual, no cambiamos el objetivo a un enemigo, disparamos a la zona.
            currentTarget = null;
        } else {
            // Comportamiento normal: buscar enemigos
            super.findTarget(enemigos);
        }
    }

    @Override
    public Color getFallbackColor() {
        return new Color(255, 165, 0); // Naranja/Amarillo de Avast
    }

    @Override
    public String getSpritePrefix() {
        return "torreavast";
    }

    @Override
    public String getTowerSprite() {
        if (!manualTargetingMode) {
            return super.getTowerSprite();
        }

        // Modo manual: Apuntar a la coordenada fijada
        if (targetX == getX() && targetY == getY()) {
            return "assets/ingame/" + getSpritePrefix() + "_reposo.png";
        }

        double dx = targetX - this.getX();
        double dy = targetY - this.getY();
        double angle = Math.atan2(dy, dx); 
        
        if (angle < 0) angle += 2 * Math.PI;
        
        double shifted = angle + Math.PI / 8.0;
        if (shifted >= 2 * Math.PI) shifted -= 2 * Math.PI;
        
        int octant = (int) (shifted / (Math.PI / 4.0));
        int spriteIndex = switch (octant) {
            case 0 -> 2; 
            case 1 -> 3; 
            case 2 -> 4; 
            case 3 -> 5; 
            case 4 -> 6; 
            case 5 -> 7; 
            case 6 -> 0; 
            case 7 -> 1; 
            default -> 4;
        };
        return "assets/ingame/" + getSpritePrefix() + "_" + spriteIndex + ".png";
    }

    @Override
    public void upgrade() {
        this.nivelMejora++;
        this.costoTorre += 100.0;
        this.rango += 0.5;
        this.tiempoRecarga = Math.max(1500, this.tiempoRecarga - 200);
    }

    @Override
    public java.util.List<Bala> atacar(java.util.List<Enemigo> enemigosEnRango, java.util.function.Supplier<String> idGenerator) {
        if (manualTargetingMode) {
            this.resetCooldown();
            return java.util.List.of(new Bala(idGenerator.get(), this, null, 25.0 * getNivelMejora()));
        }
        if (enemigosEnRango.isEmpty()) return java.util.Collections.emptyList();
        Enemigo target = selectFirstEnemy(enemigosEnRango);
        this.resetCooldown();
        return java.util.List.of(new Bala(idGenerator.get(), this, target, 25.0));
    }
}
