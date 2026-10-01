package com.waper.waperapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.waper.waperapi.config.SecurityConfig;
import com.waper.waperapi.controller.AuthController;
import com.waper.waperapi.controller.UserController;
import com.waper.waperapi.model.User;
import com.waper.waperapi.repository.UserRepository;
import com.waper.waperapi.security.JwtService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = UserIntegrationTest.IntegrationConfig.class,
    properties = {
        "spring.config.import=",
        "jwt.secret=waper-integration-test-secret-only-not-for-production-1234567890",
        "jwt.expiration-seconds=3600",
        "app.cors.allowed-origins=http://localhost:5173"
    }
)
@AutoConfigureMockMvc
class UserIntegrationTest {

    // Importamos los componentes reales del flujo sin escanear MongoConfig.
    // Solo se excluye la infraestructura de persistencia; los filtros siguen activos.
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        DataMongoAutoConfiguration.class,
        DataMongoRepositoriesAutoConfiguration.class
    })
    @Import({UserController.class, AuthController.class, SecurityConfig.class, JwtService.class})
    static class IntegrationConfig {
    }

    private static final String USER_ID = "507f1f77bcf86cd799439011";
    private static final String EMAIL = "catalina@example.test";
    private static final String PASSWORD = "Testing1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApplicationContext context;

    // Unico mock: la frontera entre la aplicacion y la base de datos.
    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void doesNotCreateAMongoClient() {
        assertThat(context.getBeansOfType(MongoClient.class)).isEmpty();
    }

    @Test
    @DisplayName("Registro: devuelve 201 y guarda la password cifrada sin exponerla")
    void registersUserWithEncodedPassword() throws Exception {
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername("catalina_test")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(USER_ID);
            return saved;
        });

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Catalina Test",
                      "username": "catalina_test",
                      "email": "catalina@example.test",
                      "password": "Testing1234"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/users/" + USER_ID))
            .andExpect(jsonPath("$.id").value(USER_ID))
            .andExpect(jsonPath("$.email").value(EMAIL))
            .andExpect(jsonPath("$.username").value("catalina_test"))
            .andExpect(jsonPath("$.role").value("STUDENT"))
            .andExpect(jsonPath("$.password").doesNotExist());

        ArgumentCaptor<User> storedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(storedUser.capture());
        assertThat(storedUser.getValue().getPassword()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, storedUser.getValue().getPassword())).isTrue();
    }

    @Test
    @DisplayName("Login: valida la password real y devuelve un JWT firmado para el usuario")
    void authenticatesUserAndIssuesValidJwt() throws Exception {
        User user = existingUser();
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));

        // Control negativo: el login debe comprobar la password, no solo emitir tokens.
        mockMvc.perform(post("/api/public/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"catalina@example.test","password":"WrongPassword123"}
                    """))
            .andExpect(status().isUnauthorized());

        String body = mockMvc.perform(post("/api/public/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"catalina@example.test","password":"Testing1234"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.expiresInSeconds").value(3600))
            .andExpect(jsonPath("$.user.id").value(USER_ID))
            .andExpect(jsonPath("$.user.email").value(EMAIL))
            .andExpect(jsonPath("$.user.password").doesNotExist())
            .andReturn().getResponse().getContentAsString();

        String token = new ObjectMapper().readTree(body).get("token").asText();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo(EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Perfil: requiere autenticacion y devuelve el perfil del propietario sin password")
    void readsOwnProfileWithRealJwtAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/" + USER_ID))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(userRepository);

        User user = existingUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/users/" + USER_ID)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(USER_ID))
            .andExpect(jsonPath("$.name").value("Catalina Test"))
            .andExpect(jsonPath("$.email").value(EMAIL))
            .andExpect(jsonPath("$.role").value("STUDENT"))
            .andExpect(jsonPath("$.interests[0]").value("Musica"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    private User existingUser() {
        return new User(USER_ID, "catalina_test", "Catalina Test", EMAIL,
            passwordEncoder.encode(PASSWORD), "Perfil de prueba", "",
            List.of("Musica"), 0, 0, "STUDENT");
    }
}
