# Documentación de Pruebas Unitarias - Waper API Backend

## 1. Resumen Ejecutivo
Este documento detalla la implementación de cinco (5) pruebas unitarias en el backend de Spring Boot del proyecto **Waper API**. Las pruebas fueron diseñadas para validar de forma aislada los métodos principales de los controladores (`UserController`, `AuthController`, `DataIngestController`), utilizando aislamiento mediante la simulación (mockeo) de dependencias y flujos externos (bases de datos a través de repositorios, gestores de autenticación y servicios JWT).

## 2. Tecnologías Utilizadas
- **Lenguaje:** Java 21
- **Framework:** Spring Boot 4.0.6
- **Testing Framework:** JUnit 5 (Jupiter)
- **Framework de Mockeo:** Mockito (`@Mock`, `@InjectMocks`, `MockitoExtension`, `when`, `verify`)
- **Gestor de Dependencias y Build:** Apache Maven (`./mvnw`)

## 3. Archivos y Rutas Creadas
Todas las clases de prueba unitaria se encuentran ubicadas en el directorio de pruebas bajo el paquete correspondiente:

1. `src/test/java/com/waper/waperapi/controller/UserControllerTest.java`
2. `src/test/java/com/waper/waperapi/controller/AuthControllerTest.java`
3. `src/test/java/com/waper/waperapi/controller/DataIngestControllerTest.java`

## 4. Detalle de los 5 Tests Unitarios Implementados

### A. `UserControllerTest` (2 tests)
1. **`testGetUserByIdFound()`**
   - **Propósito:** Evalúa la obtención exitosa de un usuario mediante su ID (`UserController.getUserById`).
   - **Flujo Externo Mockeado:** `UserRepository.findById("123")` devolviendo un `Optional.of(user)`.
   - **Aserciones:** Verifica que el código de estado HTTP sea `200 OK`, que el cuerpo no sea nulo, que el nombre de usuario coincida con el esperado y que el repositorio haya sido invocado exactamente 1 vez.

2. **`testCreateUserSuccess()`**
   - **Propósito:** Evalúa la creación correcta de un nuevo usuario (`UserController.createUser`).
   - **Flujo Externo Mockeado:** `UserRepository.existsById("123")` devolviendo `false` y `UserRepository.save(any(User.class))` devolviendo el usuario guardado.
   - **Aserciones:** Verifica el código de estado `200 OK`, el contenido del cuerpo retornado y las llamadas a los métodos del repositorio.

### B. `AuthControllerTest` (2 tests)
3. **`testTokenSuccess()`**
   - **Propósito:** Evalúa la emisión exitosa de un token JWT al autenticar credenciales válidas (`AuthController.token`).
   - **Flujos Externos Mockeados:** 
     - `AuthenticationManager.authenticate(...)` (simulando autenticación exitosa sin excepciones).
     - `JwtService.generateToken("admin")` retornando un token simulado.
     - `JwtService.getExpirationSeconds()` retornando la expiración configurada.
   - **Aserciones:** Valida respuesta `200 OK`, que el cuerpo sea una instancia de `AuthResponse` con el token y tiempo de expiración correctos, y que se haya llamado al gestor de autenticación y servicio JWT.

4. **`testTokenUnauthorized()`**
   - **Propósito:** Evalúa el rechazo de credenciales inválidas (`AuthController.token`).
   - **Flujo Externo Mockeado:** `AuthenticationManager.authenticate(...)` lanzando `BadCredentialsException`.
   - **Aserciones:** Valida que la respuesta devuelva estado `401 Unauthorized` con el mensaje `"Credenciales invalidas"` y que no se invoque al servicio JWT.

### C. `DataIngestControllerTest` (1 test)
5. **`testIngestSuccess()`**
   - **Propósito:** Evalúa la ingesta y guardado de payloads enviados desde el frontend (`DataIngestController.ingest`).
   - **Flujos Externos Mockeados:** 
     - `Authentication.getName()` retornando `"testuser"`.
     - `FrontendPayloadRepository.save(any(FrontendPayload.class))` simulando el almacenamiento en MongoDB.
   - **Aserciones:** Verifica que el objeto retornado contenga el ID asignado, el nombre de usuario extraído de la autenticación, el payload correspondiente y la marca de tiempo de recepción.

## 5. Verificación y Resultado de Ejecución
Las pruebas fueron ejecutadas exitosamente utilizando Maven wrapper (`./mvnw test`). El resultado confirma la compilación correcta y la ejecución satisfactoria de todas las pruebas:

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.waper.waperapi.controller.UserControllerTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.738 s
[INFO] Running com.waper.waperapi.controller.AuthControllerTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.106 s
[INFO] Running com.waper.waperapi.controller.DataIngestControllerTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.094 s
[INFO] Running com.waper.waperapi.WaperApiApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.873 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## 6. Código Fuente Completo de las Clases de Test

### `UserControllerTest.java`
```java
package com.waper.waperapi.controller;

import com.waper.waperapi.model.User;
import com.waper.waperapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    @Test
    void testGetUserByIdFound() {
        User user = new User();
        user.setId("123");
        user.setUsername("testuser");

        when(userRepository.findById("123")).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUserById("123");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        verify(userRepository, times(1)).findById("123");
    }

    @Test
    void testCreateUserSuccess() {
        User user = new User();
        user.setId("123");
        user.setUsername("newuser");

        when(userRepository.existsById("123")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<User> response = userController.createUser(user);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("newuser", response.getBody().getUsername());
        verify(userRepository, times(1)).existsById("123");
        verify(userRepository, times(1)).save(user);
    }
}
```

### `AuthControllerTest.java`
```java
package com.waper.waperapi.controller;

import com.waper.waperapi.dto.AuthRequest;
import com.waper.waperapi.dto.AuthResponse;
import com.waper.waperapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testTokenSuccess() {
        AuthRequest request = new AuthRequest("admin", "password123");
        when(jwtService.generateToken("admin")).thenReturn("mock-jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        ResponseEntity<?> response = authController.token(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthResponse);
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("mock-jwt-token", authResponse.token());
        assertEquals(3600L, authResponse.expiresInSeconds());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken("admin");
        verify(jwtService, times(1)).getExpirationSeconds();
    }

    @Test
    void testTokenUnauthorized() {
        AuthRequest request = new AuthRequest("admin", "wrongpassword");
        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authenticationManager)
            .authenticate(any(UsernamePasswordAuthenticationToken.class));

        ResponseEntity<?> response = authController.token(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Credenciales invalidas", response.getBody());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService);
    }
}
```

### `DataIngestControllerTest.java`
```java
package com.waper.waperapi.controller;

import com.waper.waperapi.model.FrontendPayload;
import com.waper.waperapi.repository.FrontendPayloadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataIngestControllerTest {

    @Mock
    private FrontendPayloadRepository frontendPayloadRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DataIngestController dataIngestController;

    @Test
    void testIngestSuccess() {
        Map<String, Object> payloadData = Map.of("key", "value");
        when(authentication.getName()).thenReturn("testuser");
        when(frontendPayloadRepository.save(any(FrontendPayload.class))).thenAnswer(invocation -> {
            FrontendPayload fp = invocation.getArgument(0);
            fp.setId("payload-id-1");
            return fp;
        });

        FrontendPayload result = dataIngestController.ingest(payloadData, authentication);

        assertNotNull(result);
        assertEquals("payload-id-1", result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals(payloadData, result.getPayload());
        assertNotNull(result.getReceivedAt());

        verify(authentication, times(1)).getName();
        verify(frontendPayloadRepository, times(1)).save(any(FrontendPayload.class));
    }
}
```
