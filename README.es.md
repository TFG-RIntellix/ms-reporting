# ms-reporting

**Microservicio de generación de informes de crédito de la plataforma RIntellix.**

`Java 17` · `Spring Boot 4` · `Apache Kafka` · `Thymeleaf` · `Playwright` · `Google Gemini` · `Arquitectura Hexagonal`

---

## 1. Descripción general

`ms-reporting` transforma un resultado de scoring de riesgo finalizado en un **informe de
crédito en PDF** legible por personas. Es un servicio totalmente dirigido por eventos: no expone
endpoints REST para el propio flujo de generación de informes, sino que reacciona a un evento de
Kafka publicado cuando una simulación ha sido calificada, renderiza un informe HTML a partir de
una plantilla Thymeleaf, lo convierte a PDF y entrega el fichero a `ms-core-data` para su
almacenamiento.

Responsabilidades principales:

- Consumir el evento de scoring finalizado desde Kafka (publicado por `ms-risk-engine`).
- Utilizar **Google Gemini** para generar las secciones narrativas del informe (explicación en
  lenguaje natural) a partir de los factores de riesgo obtenidos mediante SHAP.
- Renderizar el informe como HTML (plantilla Thymeleaf `credit_report.html`) y convertirlo a PDF
  mediante una instancia *headless* de **Playwright/Chromium**.
- Entregar el PDF resultante a `ms-core-data` para que se persista y pueda descargarse
  posteriormente.

## 2. Aspectos clave del sistema

- **Arquitectura hexagonal (puertos y adaptadores).** `domain` (entidades, enumerados,
  excepciones, puertos de salida), `application` (casos de uso, puertos de entrada),
  `infrastructure` (consumidor Kafka, adaptadores de salida de PDF/IA, configuración).
- **Totalmente dirigido por eventos, sin API pública.** `ScoringKafkaConsumer` es el único punto
  de entrada; este servicio no tiene, por diseño, ningún controlador REST.
- **Renderizado de HTML a PDF con Playwright.** En lugar de una librería PDF tradicional, el
  informe se renderiza primero como HTML (Thymeleaf) y después se convierte con Chromium
  *headless* mediante Playwright, permitiendo reutilizar las mismas herramientas de
  estilo/maquetación que en una página web.
- **Narrativa asistida por IA generativa.** La dependencia `google-genai` se utiliza para
  convertir la salida estructurada de explicabilidad SHAP en una sección narrativa legible del
  informe.
- **Resiliencia en las llamadas salientes.** `spring-retry` y `aspectjweaver` sustentan la
  lógica de reintentos alrededor de las llamadas de generación con IA, de forma que los fallos
  transitorios no invaliden todo el informe.

### Estructura del repositorio

El siguiente esquema ilustra la distribución del código fuente y cómo las piezas clave de la arquitectura descrita encajan en las carpetas principales del proyecto:

![Estructura de directorios](./estructura_directorios_ms_reporting.svg)

## 3. Tecnologías

- **Lenguaje / runtime:** Java 17
- **Framework:** Spring Boot 4 (`spring-boot-starter-web`, `spring-boot-starter-webflux`,
  `spring-boot-starter-validation`, `spring-boot-starter-actuator`, `spring-boot-starter-thymeleaf`)
- **Mensajería:** `spring-kafka`
- **Renderizado de informes:** Thymeleaf (HTML) + Microsoft Playwright (HTML → PDF)
- **IA generativa:** Google Gemini (`google-genai`)
- **Resiliencia:** `spring-retry`, AspectJ
- **Utilidades:** Lombok, Jackson

## 4. Requisitos previos

- JDK 17 o superior
- Maven 3.9+

## 5. Puesta en marcha

> `**IMPORTANTE**`

> **Despliegue global de la plataforma**:
> Este repositorio contiene únicamente el código del servicio de reporting. Para levantar la plataforma RIntellix completa (incluyendo este servicio, Kafka, Keycloak y el resto de microservicios), clona el repositorio principal de infraestructura **[TFG-RIntellix/rintellix-deployment]** y sigue sus instrucciones.

Los siguientes comandos se proporcionan para el desarrollo local, revisión de código y ejecución de pruebas:

```bash
# 1. Clonar el repositorio
git clone https://github.com/TFG-RIntellix/ms-reporting.git
cd ms-reporting

# 2. Compilar y ejecutar pruebas locales
mvn clean test
```

> **Nota para desarrollo local:** ejecutar los tests de Playwright localmente requiere tener instalado el navegador Chromium y sus dependencias nativas (fuentes, `libnss3`, `libgbm1`, etc.).

## 6. Configuración

Las siguientes propiedades se consumen a través de `application.yaml` o variables de entorno correspondientes:

| Propiedad | Descripción | Valor por defecto |
|---|---|---|
| `spring.kafka.bootstrap-servers` | Servidores bootstrap del broker de Kafka | `localhost:9092` |
| `app.kafka.topics.scoring-result` | Topic de resultados de scoring a consumir | `risk.scoring.result` |
| `gemini.api-key` | Credenciales para la API de Google Gemini | *(requerido)* |
| `app.clients.core-data.url` | URL base de `ms-core-data` para almacenar PDFs | `http://localhost:8081` |
| `server.port` | Puerto en el que escucha el servicio (interno) | `8083` |

## 7. Servicios relacionados

- **ms-risk-engine** — publica el evento de scoring que consume este servicio.
- **ms-core-data** — almacena el PDF generado y lo sirve para su descarga.

## 8. Autora

Lucía Fernández Mancebo — TFG *RIntellix*, Universidad de Cantabria.



