package com.game2d.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionMessagesTest {

    @Test
    void formatUsesMessageWhenPresent() {
        assertEquals("sin oro", ExceptionMessages.format(new IllegalStateException("sin oro")));
    }

    @Test
    void formatUsesClassNameWhenMessageMissing() {
        assertEquals("IllegalStateException",
                ExceptionMessages.format(new IllegalStateException()));
    }
}
