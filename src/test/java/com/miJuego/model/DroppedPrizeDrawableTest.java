package com.miJuego.model;

import com.game2d.model.FallbackShape;
import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DroppedPrizeDrawableTest {

    @Test
    void testBasicProperties() {
        DroppedPrizeDrawable prize = new DroppedPrizeDrawable(5f, 10f);
        
        assertEquals("dropped-prize", prize.getId());
        assertEquals(5f, prize.getX());
        assertEquals(10f, prize.getY());
        assertEquals(1.5f, prize.getWidth());
        assertEquals(1.5f, prize.getHeight());
        assertEquals(98, prize.getLayer());
    }

    @Test
    void testOptionals() {
        DroppedPrizeDrawable prize = new DroppedPrizeDrawable(5f, 10f);
        
    }
}
