package com.miJuego.model;

import java.awt.Color;
import java.util.Optional;

public class TorreMessenger extends TorreElectrica {
    private int rebotesMaximos = 999; // Infinita cantidad de rebotes posibles
    private double rangoDeRebote = 3.0;

    public TorreMessenger(String id, float x, float y) {
        super(id, x, y);
        this.towertype = "TorreMessenger";
        this.costoTorre = 350.0;
        
        // Atributos base reducidos (poco daño) pero con cadena infinita
        this.rango = 3.5;
        // Ajustamos el daño base a un valor bajo
        // La TorreElectrica hereda dañoComun, necesitamos sobreescribir o usar set
    }

    @Override
    public double ataque(Enemigo enemigo) {
        double dañoEfectivo = 2.0 * nivelMejora; // Poco daño
        enemigo.setVida(enemigo.GetVida() - dañoEfectivo);
        // Pequeño parálisis
        enemigo.aplicarParalizacion(0.2f);
        return dañoEfectivo;
    }

    @Override
    public String getTowerSprite() {
        if (currentTarget == null) {
            return "assets/ingame/Sprite_TorreMessenger4.png"; // Usamos el 4 (abajo) como reposo
        }
        double dx = currentTarget.getX() - this.x;
        double dy = currentTarget.getY() - this.y;
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
        return "assets/ingame/Sprite_TorreMessenger" + spriteIndex + ".png";
    }

    public int getRebotesMaximos() {
        return rebotesMaximos + (nivelMejora - 1); // 1 rebote adicional por cada nivel de mejora
    }

    public double getRangoDeRebote() {
        return rangoDeRebote;
    }

    @Override
    public void upgrade() {
        super.upgrade();
        // El upgrade de TorreElectrica ya sube el nivelMejora
    }

    @Override
    public Color getFallbackColor() {
        return new Color(0, 191, 255); // Celeste eléctrico
    }

    @Override
    public Optional<String> getImagePath() {
        return Optional.of(getTowerSprite());
    }

    @Override
    public java.util.List<Bala> atacar(java.util.List<Enemigo> enemigosEnRango, java.util.function.Supplier<String> idGenerator) {
        if (enemigosEnRango.isEmpty()) return java.util.Collections.emptyList();
        Enemigo target = selectFirstEnemy(enemigosEnRango);
        this.resetCooldown();
        return java.util.List.of(new Bala(idGenerator.get(), this, target, 15.0));
    }
}
