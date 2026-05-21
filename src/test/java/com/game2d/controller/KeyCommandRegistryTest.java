package com.game2d.controller;

import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyCommandRegistryTest {

    @Test
    void resolveByKeyCode() {
        KeyCommandRegistry registry = new KeyCommandRegistry()
                .bind(KeyEvent.VK_W, GameCommands.MOVE_UP);

        assertEquals(GameCommands.MOVE_UP,
                registry.resolve(KeyEvent.VK_W, "W").orElseThrow());
    }

    @Test
    void resolveByKeyLabelWhenCodeNotBound() {
        KeyCommandRegistry registry = new KeyCommandRegistry()
                .bind("Space", GameCommands.PAUSE);

        assertEquals(GameCommands.PAUSE,
                registry.resolve(KeyEvent.VK_SPACE, "Space").orElseThrow());
    }

    @Test
    void unbindRemovesAssociation() {
        KeyCommandRegistry registry = new KeyCommandRegistry()
                .bind(KeyEvent.VK_P, GameCommands.PAUSE)
                .unbind(KeyEvent.VK_P);

        assertTrue(registry.resolve(KeyEvent.VK_P, "P").isEmpty());
    }
}
