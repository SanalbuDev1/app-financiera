package com.finanzas.personales.finanzas.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio utilitario para cargar queries SQL desde archivos {@code .sql} en el classpath.
 * Los archivos se organizan por módulo en {@code resources/sql/{modulo}/{query}.sql}.
 * Los queries se cachean en memoria tras la primera lectura para evitar I/O repetitivo.
 *
 * <p>Ejemplo de uso:</p>
 * <pre>
 *   String sql = sqlLoader.load("transactions/find_by_id");
 *   databaseClient.sql(sql).bind("id", id)...
 * </pre>
 */
@Slf4j
@Component
public class SqlQueryLoader {

    /** Ruta base donde se encuentran los archivos SQL en el classpath. */
    private static final String SQL_BASE_PATH = "sql/";

    /** Caché de queries ya leídos: clave = path relativo, valor = SQL como String. */
    private final Map<String, String> queryCache = new ConcurrentHashMap<>();

    /**
     * Carga un query SQL desde el classpath y lo cachea para usos posteriores.
     * El path es relativo a {@code resources/sql/}. No incluir la extensión {@code .sql}.
     *
     * @param queryPath ruta relativa al query (ej. {@code "transactions/find_by_id"})
     * @return contenido SQL del archivo como String
     * @throws IllegalArgumentException si el archivo no existe o no se puede leer
     */
    public String load(String queryPath) {
        return queryCache.computeIfAbsent(queryPath, this::readSqlFile);
    }

    /**
     * Lee el contenido de un archivo SQL desde el classpath.
     *
     * @param queryPath ruta relativa al query (sin extensión)
     * @return contenido SQL como String
     * @throws IllegalArgumentException si el archivo no existe
     */
    private String readSqlFile(String queryPath) {
        String resourcePath = SQL_BASE_PATH + queryPath + ".sql";
        ClassPathResource resource = new ClassPathResource(resourcePath);

        try (InputStream is = resource.getInputStream()) {
            String sql = StreamUtils.copyToString(is, StandardCharsets.UTF_8).trim();
            log.info("[SqlQueryLoader] Query cargado: {} ({} caracteres)", resourcePath, sql.length());
            return sql;
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "No se pudo cargar el archivo SQL: " + resourcePath, e);
        }
    }
}
