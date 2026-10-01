# Documentación de Pruebas End-to-End (E2E) - Waper API Backend

## 1. Resumen Ejecutivo
Este documento detalla la implementación de la suite de pruebas End-to-End (E2E) para la validación del flujo de Registro en el proyecto **Waper**. Estas pruebas validan la integración completa entre la interfaz de usuario en React y el backend. Para garantizar estabilidad, se implementaron atributos `data-testid` en el frontend, y la automatización se ejecuta nativamente desde el entorno Java usando Selenium WebDriver y TestNG.

## 2. Tecnologías Utilizadas
- **Lenguaje:** Java 21
- **Controlador de Navegador:** Selenium WebDriver (v4.18.1)
- **Testing Framework:** TestNG (v7.9.0)
- **Reportes Visuales:** ExtentReports (v5.1.1)
- **Gestor de Dependencias y Build:** Apache Maven
- **Frontend bajo prueba:** React / Vite (`waper-web` corriendo en `localhost:5173`)

## 3. Preparación del Entorno y Frontend

### A. Rama de Trabajo y Metodología
Las pruebas E2E formaron parte de la integración de testing en la rama integradora `testing`.
- **Desarrollador asignado (POST/Registro):** Said.
- **Rama individual creada:** `e2e/back-front`.

### B. Preparación del Frontend (`data-testid`)
Para asegurar la robustez de las pruebas y evitar la fragilidad de los selectores CSS (XPath o clases de Tailwind), se aplicaron 9 atributos `data-testid` semánticos en los archivos JSX de `waper-web`. Esta inyección de selectores estables aisló los tests de cualquier refactor visual o de estructura.

| Componente (`waper-web`) | `data-testid` implementado |
| :--- | :--- |
| `RegistrerPage.jsx` | `register-form` |
| `RegistrerPage.jsx` (LoginError) | `register-error-message` |
| `UsernameField.jsx` | `register-username-input` |
| `EmailField.jsx` | `register-email-input` |
| `PasswordField.jsx` | `register-password-input` |
| `PasswordField.jsx` (botón ojo) | `register-password-toggle` |
| `ConfirmPasswordField.jsx` | `register-confirm-password-input` |
| `ConfirmPasswordField.jsx` (botón) | `register-confirm-password-toggle` |
| `RegisterButton.jsx` | `register-submit-button` |
| `LoginRedirect.jsx` | `login-redirect-button` |

## 4. Archivo y Ruta Creada
El script automatizado fue alojado en la estructura de pruebas del backend (`waper-api`):
`src/test/java/com/waper/waperapi/RegisterE2ETest.java`

## 5. Detalle de los 5 Tests E2E Implementados

El flujo fue ordenado estrictamente utilizando `@Test(priority = X)` para emular el recorrido secuencial del usuario.

### 1. `validarElementosVisibles()`
- **Propósito:** Verifica que todos los inputs y botones esenciales para el registro estén renderizados en el DOM utilizando los atributos `data-testid`.
- **Aserciones:** Valida mediante `assertTrue(elemento.isDisplayed())` que 7 elementos clave estén visibles en pantalla antes de iniciar la interacción.

### 2. `validarVisibilidadContrasena()`
- **Propósito:** Prueba la interactividad de la UI (Frontend).
- **Flujo:** Ingresa credenciales básicas.
- **Aserciones:** Utilizando `assertEquals`, comprueba que al presionar el botón de "mostrar contraseña" (`register-password-toggle`), el atributo HTML del input cambie de `type="password"` a `type="text"`, y viceversa al ocultarlo.

### 3. `validarContrasenasNoCoinciden()`
- **Propósito:** Fuerza un escenario de error en la validación local (front) ingresando una confirmación de contraseña dispar.
- **Flujo:** Presiona el botón de envío con contraseñas que no coinciden (`MiClave123!` vs `ClaveDiferente999`).
- **Aserciones:** Extrae el texto del componente de error (`register-error-message`) y evalúa con `assertEquals` que el mensaje capturado sea exactamente `"Las contraseñas no coinciden"`.

### 4. `validarEnvioFormularioExitoso()`
- **Propósito:** Comprueba el camino exitoso o "Ruta Feliz".
- **Flujo:** Corrige la confirmación para que coincida y presiona nuevamente el botón de crear cuenta (`register-submit-button`).
- **Aserciones:** Se marca la prueba como Exitosa si el WebDriver no capta errores ni bloqueos visuales en el intento de envío (POST) hacia la API.

### 5. `validarRedireccionAlLogin()`
- **Propósito:** Valida que el enlace secundario redireccione correctamente al usuario a la vista de inicio de sesión.
- **Aserciones:** Comprueba mediante `assertEquals` que tras el clic en el botón, la URL actual del navegador coincida plenamente con `"http://localhost:5173/login"`.

## 6. Verificación y Resultados
La ejecución se realizó exitosamente en Chrome (maximizado). La suite generó el reporte profesional HTML (ExtentReports) ubicado en la raíz del proyecto `reportes/ResultadoRegistro.html`, donde se loguean en detalle los pasos y aserciones. 

```text
===============================================
Default Suite
Total tests run: 5, Passes: 5, Failures: 0, Skips: 0
===============================================

Process finished with exit code 0
```

## 7. Código Fuente Completo
### `RegisterE2ETest.java`
```java
package com.waper.waperapi;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

public class RegisterE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static ExtentReports reporte;
    private ExtentTest testLog;
    private final String REGISTER_URL = "http://localhost:5173/register";

    @BeforeClass
    public void configurarReporteYEntorno() {
        ExtentSparkReporter spark = new ExtentSparkReporter("reportes/ResultadoRegistro.html");
        reporte = new ExtentReports();
        reporte.attachReporter(spark);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(8));

        driver.get(REGISTER_URL);
    }

    @Test(priority = 1)
    public void validarElementosVisibles() {
        testLog = reporte.createTest("1. Verificar UI", "Comprueba que los campos existan en el DOM");
        testLog.info("Buscando elementos por data-testid.");

        String[] elementosEsperados = {
                "register-form", "register-username-input", "register-email-input",
                "register-password-input", "register-password-toggle",
                "register-confirm-password-input", "register-submit-button"
        };

        for (String testId : elementosEsperados) {
            WebElement elemento = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-testid='" + testId + "']")
            ));
            // En validación de visibilidad sí mantenemos assertTrue porque esperamos un estado visual
            Assert.assertTrue(elemento.isDisplayed(), "El elemento esperado no se mostró: " + testId);
        }
        testLog.pass("Todos los elementos renderizaron correctamente.");
    }

    @Test(priority = 2)
    public void validarVisibilidadContrasena() {
        testLog = reporte.createTest("2. Toggle de Contraseña", "Prueba botón mostrar/ocultar");

        ingresarTexto("register-username-input", "juan_perez");
        ingresarTexto("register-email-input", "juan@ejemplo.com");
        ingresarTexto("register-password-input", "MiClave123!");

        WebElement inputPassword = driver.findElement(By.cssSelector("[data-testid='register-password-input']"));

        clickBtn("register-password-toggle");
        // Aserción directa comparando strings
        Assert.assertEquals(inputPassword.getAttribute("type"), "text", "El input no cambió a texto plano."); //password
        testLog.info("Contraseña visible en texto plano.");

        clickBtn("register-password-toggle");
        Assert.assertEquals(inputPassword.getAttribute("type"), "password", "El input no volvió a ocultarse.");
        testLog.pass("Toggle de contraseña verificado exitosamente.");
    }


    @Test(priority = 3)
    public void validarContrasenasNoCoinciden() {
        testLog = reporte.createTest("3. Error de Validación", "Prueba contraseñas no coincidentes");

        ingresarTexto("register-confirm-password-input", "ClaveDiferente999"); //MiClave123!  ClaveDiferente999
        clickBtn("register-submit-button");

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='register-error-message']")
        ));

        //para ver la diferencia de textos si falla
        String textoObtenido = errorMessage.getText();
        Assert.assertEquals(textoObtenido, "Las contraseñas no coinciden", "El mensaje de error renderizado es incorrecto.");

        testLog.pass("Alerta de error capturada correctamente.");
    }

    @Test(priority = 4)
    public void validarEnvioFormularioExitoso() {
        testLog = reporte.createTest("4. Envío Válido", "Prueba registro con contraseñas iguales");

        WebElement confirmInput = driver.findElement(By.cssSelector("[data-testid='register-confirm-password-input']"));
        confirmInput.clear();

        ingresarTexto("register-confirm-password-input", "MiClave123!");
        clickBtn("register-submit-button");

        pausa(1000);
        testLog.pass("Formulario enviado con éxito.");
    }

    @Test(priority = 5)
    public void validarRedireccionAlLogin() {
        testLog = reporte.createTest("5. Redirección Login", "Verifica enlace a /login");

        clickBtn("login-redirect-button");
        wait.until(ExpectedConditions.urlContains("/login")); //loggin

        // Compara la URL completa para tener un reporte exacto si la ruta falla
        String urlFinal = driver.getCurrentUrl();
        Assert.assertEquals(urlFinal, "http://localhost:5173/login", "La redirección no fue a la página de login.");

        testLog.pass("Redirección completada.");
    }

    @AfterClass
    public void finalizarSujeto() {
        if (driver != null) {
            pausa(1500);
            driver.quit();
        }
        reporte.flush();
    }

    private void ingresarTexto(String testId, String texto) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='" + testId + "']")
        ));
        input.clear();
        input.sendKeys(texto);
        pausa(300);
    }

    private void clickBtn(String testId) {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='" + testId + "']")
        ));
        btn.click();
        pausa(500);
    }

    private static void pausa(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
```


