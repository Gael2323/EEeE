package com.miJuego.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class EnemigoComun extends Enemigo {
    private boolean tieneEscudoElectrico;

    public EnemigoComun(String id) {
        super(id, "EnemigoComun", 80.0, 15, 10);
        this.tieneEscudoElectrico = true; // Por defecto arranca con escudo eléctrico
        this.rapidez = 1.8; // Velocidad estándar
        this.dañoBase = 1.0;
        this.width = 1.15f;
        this.height = 1.15f;
    }

    public EnemigoComun(String id, boolean tieneEscudoElectrico) {
        super(id, "EnemigoComun", 80.0, 15, 10);
        this.tieneEscudoElectrico = tieneEscudoElectrico;
        this.rapidez = 1.8;
        this.dañoBase = 1.0;
        this.width = 1.15f;
        this.height = 1.15f;
    }

    public boolean isTieneEscudoElectrico() {
        return tieneEscudoElectrico;
    }

    public void perderEscudoEl() {
        // En el UML: perderEscudoEl() -> tieneEscudoElectrico = false
        this.tieneEscudoElectrico = false;
    }

    @Override
    public List<Enemigo> morir() {
        // En el UML: morir() -> retorna listaVacia
        return new ArrayList<>();
    }

    @Override
    public Color getFallbackColor() {
        if (paralizacionTimer > 0 || ralentizarTimer > 0 || fuegoTimer > 0) {
            return super.getFallbackColor();
        }
        // Si tiene escudo, color magenta/violeta. Si no, color marrón/rojo oscuro.
        return tieneEscudoElectrico ? new Color(218, 112, 214) : new Color(165, 42, 42);
    }
    
    @Override
    public void setVida(double vida) {
        // Si tiene escudo eléctrico y el daño no es de TorreElectrica, se reduce a la mitad.
        // Pero como setVida se llama desde la torre directamente en ataque(),
        // aplicaremos la reducción de daño si tiene el escudo activo, excepto si la torre es eléctrica.
        // Como no sabemos la fuente de daño desde setVida, el cálculo se hace en la torre misma.
        // Pero para estar doblemente seguros, si se baja vida y tiene escudo eléctrico,
        // podemos controlar el daño o dejar que la lógica de TorreElectrica se encargue de perderEscudoEl().
        // Mantengamos la lógica de daño simple: las torres no eléctricas hacen menos daño a escudos
        // (esto lo manejaremos en la llamada a ataque() de las torres).
        super.setVida(vida);
    }
}
