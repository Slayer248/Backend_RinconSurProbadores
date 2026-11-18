package cl.duocuc.usuarios.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    @Test
    void generar_CumpleLongitudMinimaYComplejidad() {
        String passCorto = PasswordGenerator.generar(5);
        String passLargo = PasswordGenerator.generar(14);

        assertEquals(10, passCorto.length());
        assertEquals(14, passLargo.length());
        assertTrue(passLargo.matches(".*[A-Z].*"));
        assertTrue(passLargo.matches(".*[a-z].*"));
        assertTrue(passLargo.matches(".*[0-9].*"));
        assertTrue(passLargo.matches(".*[@#$%&*!].*"));
    }
}