package com.sigoc.sigoc_web.infrastructure.adapters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Pbkdf2PasswordHasherAdapterTest {

    private final Pbkdf2PasswordHasherAdapter hasher = new Pbkdf2PasswordHasherAdapter();

    @Test
    void hashYVerificacionCompatiblesConDesktop() {
        String h = hasher.hash("ClaveSegura123");
        assertTrue(h.startsWith("PBKDF2$120000$"));
        assertTrue(hasher.verificar("ClaveSegura123", h));
        assertFalse(hasher.verificar("Otra", h));
        assertFalse(hasher.esClaveInicial(h));
    }

    @Test
    void reconoceHashSembrado() {
        String sembrado = "PBKDF2$120000$U0lHT0MtREVNT1NUUkFDSU9OLVNBTFQ=$rTzG6iZdG+6OFOOfY11QKZauNQ6Az1Yie4YbgDMcUT8=";
        assertTrue(hasher.esClaveInicial(sembrado));
        assertFalse(hasher.verificar("x", "texto-plano"));
    }
}
