package com.finanzas.personales.finanzas.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link SqlQueryLoader}.
 * Verifica la carga de archivos SQL desde el classpath y el comportamiento del caché.
 */
class SqlQueryLoaderTest {

    private final SqlQueryLoader sqlQueryLoader = new SqlQueryLoader();

    /**
     * Verifica que se carga correctamente un archivo SQL existente.
     */
    @Test
    void should_loadSqlFile_when_fileExists() {
        // Act
        String sql = sqlQueryLoader.load("auth/buscar_usuario_por_email");

        // Assert
        assertNotNull(sql);
        assertFalse(sql.isEmpty());
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("users"));
    }

    /**
     * Verifica que el caché retorna la misma instancia en lecturas repetidas.
     */
    @Test
    void should_returnCachedQuery_when_loadedTwice() {
        // Act
        String first = sqlQueryLoader.load("auth/buscar_usuario_por_email");
        String second = sqlQueryLoader.load("auth/buscar_usuario_por_email");

        // Assert — misma referencia en memoria (viene del caché)
        assertSame(first, second);
    }

    /**
     * Verifica que se lanza excepción cuando el archivo SQL no existe.
     */
    @Test
    void should_throwException_when_fileNotFound() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> sqlQueryLoader.load("modulo_inexistente/query_falso"));
    }

    /**
     * Verifica que se pueden cargar queries de diferentes módulos.
     */
    @Test
    void should_loadQueriesFromDifferentModules() {
        // Act
        String authQuery = sqlQueryLoader.load("auth/registrar_usuario");
        String txQuery = sqlQueryLoader.load("transactions/registrar_transaccion");

        // Assert
        assertNotNull(authQuery);
        assertNotNull(txQuery);
        assertTrue(authQuery.contains("INSERT INTO users"));
        assertTrue(txQuery.contains("INSERT INTO transactions"));
    }
}
