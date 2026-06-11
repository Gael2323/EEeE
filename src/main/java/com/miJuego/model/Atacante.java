package com.miJuego.model;

import java.util.List;
import java.util.function.Supplier;

/**
 * Interfaz que define la capacidad de atacar.
 * Puede ser implementada por torres, hechizos, aliados u otras entidades.
 */
public interface Atacante {
    /**
     * Calcula y genera los proyectiles (balas) producto del ataque.
     * 
     * @param enemigosEnRango Lista de enemigos detectados en el rango del atacante.
     * @param idGenerator     Generador de IDs únicos para los proyectiles creados.
     * @return Una lista de proyectiles que se deben agregar al juego, o una lista vacía si no atacó.
     */
    List<Bala> atacar(List<Enemigo> enemigosEnRango, Supplier<String> idGenerator);
}
