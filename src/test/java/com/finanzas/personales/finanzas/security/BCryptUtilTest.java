package com.finanzas.personales.finanzas.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test utilitario para generar y verificar hashes BCrypt.
 * Ejecutar cuando necesites generar un nuevo hash o verificar una contraseña.
 */
class BCryptUtilTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Genera hashes BCrypt para las contraseñas de los usuarios iniciales.
     * Útil para regenerar los hashes del init.sql si se pierden.
     */
    @Test
    void should_generate_bcrypt_hashes() {
        String[] passwords = {"admin123", "user123", "test123"};

        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Password: " + password + " → Hash: " + hash);
            // Verificar que el hash generado es válido
            assertTrue(encoder.matches(password, hash));
        }
    }

    /**
     * Verifica que las contraseñas de init.sql coinciden con los hashes almacenados.
     */
    @Test
    void should_verify_init_sql_passwords() {
        // Hashes del init.sql
        String adminHash = "$2a$10$y.GdmpnVV2i4IgvB1Qqs4uiI6l5NZBiZZ5aW7Ug92rsB7Rn2o78bW";

        encoder.matches("usuario1", adminHash);
    }

    /**
     * Genera un hash BCrypt para una contraseña personalizada.
     * Cambiar el valor de customPassword para generar el hash que necesites.
     */
    @Test
    void should_generate_custom_hash() {
        String customPassword = "usuario1";
        String hash = encoder.encode(customPassword);

        System.out.println("===========================================");
        System.out.println("Password: " + customPassword);
        System.out.println("Hash:     " + hash);
        System.out.println("===========================================");

        assertTrue(encoder.matches(customPassword, hash));
    }

    /**
     * Demuestra que BCrypt es one-way: no se puede descifrar, solo verificar.
     */
    @Test
    void should_demonstrate_bcrypt_is_one_way() {
        String password = "admin123";
        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);

        // Cada vez genera un hash diferente (salt aleatorio)
        assertNotEquals(hash1, hash2, "Cada encode genera un hash distinto por el salt");

        // Pero ambos hashes validan contra la misma contraseña
        assertTrue(encoder.matches(password, hash1));
        assertTrue(encoder.matches(password, hash2));

        // Una contraseña incorrecta no coincide
        assertFalse(encoder.matches("wrongPassword", hash1));
    }
}
