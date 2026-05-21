package com.game2d.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleGameInputTest {

    @Test
    void pointerDownCarriesCoordinates() {
        GameInput input = SimpleGameInput.pointerDown(3f, 4f);
        assertEquals(InputKind.POINTER_DOWN, input.getKind());
        assertEquals(3f, input.getX().orElseThrow());
        assertEquals(4f, input.getY().orElseThrow());
        assertFalse(input.getKeyCode().isPresent());
        assertFalse(input.getActionId().isPresent());
    }

    @Test
    void actionCarriesActionId() {
        GameInput input = SimpleGameInput.action("start");
        assertEquals(InputKind.ACTION, input.getKind());
        assertEquals("start", input.getActionId().orElseThrow());
        assertFalse(input.getX().isPresent());
    }

    @Test
    void keyPressedCarriesKeyCode() {
        GameInput input = SimpleGameInput.keyPressed("Up");
        assertEquals(InputKind.KEY_PRESSED, input.getKind());
        assertEquals("Up", input.getKeyCode().orElseThrow());
    }

    @Test
    void gameInputDefaultsAreEmpty() {
        GameInput input = new GameInput() {
            @Override
            public InputKind getKind() {
                return InputKind.ACTION;
            }
        };
        assertFalse(input.getX().isPresent());
        assertFalse(input.getY().isPresent());
        assertFalse(input.getKeyCode().isPresent());
        assertFalse(input.getActionId().isPresent());
    }
}
