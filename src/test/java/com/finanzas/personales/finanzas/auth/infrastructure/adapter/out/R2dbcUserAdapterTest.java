package com.finanzas.personales.finanzas.auth.infrastructure.adapter.out;

import com.finanzas.personales.finanzas.auth.domain.model.User;
import com.finanzas.personales.finanzas.auth.domain.model.UserRole;
import com.finanzas.personales.finanzas.config.SqlQueryLoader;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link R2dbcUserAdapter}.
 * Mockea {@link DatabaseClient} y {@link SqlQueryLoader} para aislar la lógica del adaptador.
 */
@ExtendWith(MockitoExtension.class)
class R2dbcUserAdapterTest {

    @Mock
    private DatabaseClient databaseClient;

    @Mock
    private SqlQueryLoader sqlQueryLoader;

    @Mock
    private GenericExecuteSpec executeSpec;

    @Mock
    private RowsFetchSpec<User> rowsFetchSpec;

    @Mock
    private RowsFetchSpec<Boolean> booleanFetchSpec;

    @Mock
    private FetchSpec<java.util.Map<String, Object>> fetchSpec;

    @Mock
    private Row row;

    @Mock
    private RowMetadata metadata;

    private R2dbcUserAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new R2dbcUserAdapter(databaseClient, sqlQueryLoader);
    }

    /**
     * Verifica que findByEmail retorna un usuario cuando existe en la BD.
     */
    @SuppressWarnings("unchecked")
    @Test
    void should_returnUser_when_emailExists() {
        // Arrange
        when(sqlQueryLoader.load("auth/buscar_usuario_por_email")).thenReturn("SELECT * FROM users WHERE email = :email");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("email", "user@financiera.com")).thenReturn(executeSpec);

        RowsFetchSpec<User> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.just(
                User.builder()
                        .id("uuid-1")
                        .email("user@financiera.com")
                        .name("Usuario")
                        .password("$2a$10$hash")
                        .role(UserRole.USER)
                        .build()
        ));

        // Act & Assert
        StepVerifier.create(adapter.findByEmail("user@financiera.com"))
                .expectNextMatches(user -> user.getEmail().equals("user@financiera.com")
                        && user.getRole() == UserRole.USER)
                .verifyComplete();

        verify(sqlQueryLoader).load("auth/buscar_usuario_por_email");
    }

    /**
     * Verifica que findByEmail retorna vacío cuando el email no existe.
     */
    @SuppressWarnings("unchecked")
    @Test
    void should_returnEmpty_when_emailNotFound() {
        // Arrange
        when(sqlQueryLoader.load("auth/buscar_usuario_por_email")).thenReturn("SELECT * FROM users WHERE email = :email");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("email", "noexiste@financiera.com")).thenReturn(executeSpec);

        RowsFetchSpec<User> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(adapter.findByEmail("noexiste@financiera.com"))
                .verifyComplete();
    }

    /**
     * Verifica que save ejecuta el INSERT y retorna el usuario.
     */
    @Test
    void should_returnUser_when_saveIsSuccessful() {
        // Arrange
        User user = User.builder()
                .id("uuid-new")
                .email("nuevo@financiera.com")
                .name("Nuevo Usuario")
                .password("$2a$10$encodedHash")
                .role(UserRole.USER)
                .build();

        when(sqlQueryLoader.load("auth/registrar_usuario")).thenReturn("INSERT INTO users ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        // Act & Assert
        StepVerifier.create(adapter.save(user))
                .expectNextMatches(saved -> saved.getEmail().equals("nuevo@financiera.com")
                        && saved.getId().equals("uuid-new"))
                .verifyComplete();

        verify(sqlQueryLoader).load("auth/registrar_usuario");
    }

    /**
     * Verifica que existsByEmail retorna true cuando el email existe.
     */
    @SuppressWarnings("unchecked")
    @Test
    void should_returnTrue_when_emailExists() {
        // Arrange
        when(sqlQueryLoader.load("auth/verificar_existencia_email")).thenReturn("SELECT COUNT(1) > 0 ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("email", "user@financiera.com")).thenReturn(executeSpec);

        RowsFetchSpec<Boolean> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(adapter.existsByEmail("user@financiera.com"))
                .expectNext(true)
                .verifyComplete();
    }

    /**
     * Verifica que existsByEmail retorna false cuando el email no existe.
     */
    @SuppressWarnings("unchecked")
    @Test
    void should_returnFalse_when_emailDoesNotExist() {
        // Arrange
        when(sqlQueryLoader.load("auth/verificar_existencia_email")).thenReturn("SELECT COUNT(1) > 0 ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("email", "noexiste@financiera.com")).thenReturn(executeSpec);

        RowsFetchSpec<Boolean> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(adapter.existsByEmail("noexiste@financiera.com"))
                .expectNext(false)
                .verifyComplete();
    }
}
