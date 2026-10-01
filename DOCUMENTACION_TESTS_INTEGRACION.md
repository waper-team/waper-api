# Documentación de Pruebas de Integración - Waper API Backend

**Responsable:** Catalina Perez  
**Rama de trabajo:** `feature/integration-tests-catalina`  
**Rama destino del PR:** `testing`

## 1. Resumen

Se implementaron tres tests de integración para registro, autenticación y consulta del perfil. Las peticiones pasan por Spring MVC y la cadena de seguridad real mediante MockMvc. El único componente mockeado es `UserRepository`, que representa el acceso a MongoDB.

El alcance es la integración de los componentes del backend en un contexto Spring de pruebas. No se levantan un servidor HTTP real, el frontend ni el BFF, ni se comprueba la persistencia real en MongoDB.

## 2. Tecnologías utilizadas

- Java 21 y Spring Boot 4.0.6.
- JUnit Jupiter para ejecutar los tests.
- `@SpringBootTest` y `@AutoConfigureMockMvc` para el contexto y las peticiones.
- Mockito con `@MockitoBean`, `when`, `verify` y `ArgumentCaptor`.
- AssertJ y las aserciones de MockMvc para validar resultados.
- Maven Wrapper para compilar y ejecutar.

## 3. Archivo y configuración

Código: [UserIntegrationTests.java](src/test/java/com/waper/waperapi/UserIntegrationTests.java).

La configuración interna `IntegrationConfig` importa los componentes reales `UserController`, `AuthController`, `SecurityConfig` y `JwtService`. Se mantienen activos los filtros de seguridad, la validación de entradas, BCrypt y la generación y validación de JWT.

Solo se sustituye el repositorio:

```java
@MockitoBean
private UserRepository userRepository;
```

No se escanea `MongoConfig` y se excluyen `MongoAutoConfiguration`, `DataMongoAutoConfiguration` y `DataMongoRepositoriesAutoConfiguration`. Antes de cada test se comprueba que no exista ningún bean `MongoClient`.

Las propiedades del test desactivan la importación del `.env` y proporcionan una clave JWT exclusiva de pruebas, una expiración de 3600 segundos y el origen CORS local. No se requieren credenciales de MongoDB ni Redis.

## 4. Detalle de los tres tests

### A. Registro: `registersUserWithEncodedPassword()`

**Petición:** `POST /api/users` con nombre, username, email y contraseña válidos.

**Acceso a BD simulado:** las consultas de existencia de email y username devuelven `false`; `save` devuelve el usuario recibido con un ID asignado.

**Verificaciones:**

- Respuesta `201 Created` y cabecera `Location` con la ruta del usuario.
- ID, email y username esperados; rol `STUDENT`.
- Ausencia del campo `password` en el JSON.
- Una llamada a `save`, capturando el usuario mediante `ArgumentCaptor`.
- La contraseña entregada al repositorio es distinta al texto original y coincide al verificarla con el `PasswordEncoder` real.

### B. Login: `authenticatesUserAndIssuesValidJwt()`

**Petición:** `POST /api/public/auth/token`.

**Acceso a BD simulado:** la búsqueda por email devuelve un usuario de prueba cuya contraseña fue cifrada con BCrypt real.

**Verificaciones:**

- Una contraseña incorrecta obtiene `401 Unauthorized`.
- La contraseña correcta obtiene `200 OK`, token no vacío y expiración informada de 3600 segundos.
- La respuesta contiene el ID y email esperados, sin contraseña.
- `JwtService` valida la firma y vigencia del token; su sujeto corresponde al email, según la autenticación de la base utilizada.
- No se llama a `save` durante este flujo con contraseña ya cifrada y rol establecido.

### C. Perfil: `readsOwnProfileWithRealJwtAuthentication()`

**Petición:** `GET /api/users/{id}`.

**Acceso a BD simulado:** la búsqueda por email permite cargar al usuario en la seguridad; la búsqueda por ID devuelve su perfil.

**Verificaciones:**

- Sin token responde `401 Unauthorized`, sin consultar el repositorio.
- Con un JWT generado por el servicio real y enviado como `Authorization: Bearer ...`, responde `200 OK`.
- El JSON contiene el ID, nombre, email, rol e interés esperados, sin contraseña.

Los controles negativos de login y perfil están dentro de sus respectivos métodos: la clase tiene exactamente tres métodos `@Test`.

## 5. Ejecución y resultados

Desde la raíz de `waper-api`, en PowerShell:

```powershell
.\mvnw.cmd -o "-Dtest=UserIntegrationTests" test
```

La opción `-o` ejecuta Maven sin descargar dependencias; requiere tener el Wrapper y las dependencias disponibles localmente. En una primera instalación, ejecutar sin `-o` para permitir su descarga:

```powershell
.\mvnw.cmd "-Dtest=UserIntegrationTests" test
```

No es necesario iniciar los servicios de WAPER. Se selecciona esta clase para evitar ejecutar otras pruebas del proyecto que puedan depender de una BD real.

Resultado verificado el **1 de octubre de 2026**, en la rama de trabajo creada desde `testing`:

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

El reporte local se genera en `target/surefire-reports/com.waper.waperapi.UserIntegrationTests.txt`. Este resultado corresponde a los tres tests de esta entrega, no a toda la suite del repositorio.

## 6. Referencia y entrega

La estructura de esta documentación toma como referencia `DOCUMENTACION_TESTS.md` de la rama `unitarias-me`. Se utiliza un archivo separado para conservar la documentación de las pruebas unitarias del equipo.

La entrega incluye la clase de tests y este documento en un PR de `feature/integration-tests-catalina` hacia `testing`.
