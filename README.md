# 💱 Convertidor de Monedas — Alura Latam

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.java.com)
[![Maven](https://img.shields.io/badge/Maven-Gson%20%7C%20JUnit-blue)](https://maven.apache.org)
[![Tests](https://github.com/SkrillyG/Convertidor-de-Moneda-Alura/actions/workflows/ci.yml/badge.svg)](https://github.com/SkrillyG/Convertidor-de-Moneda-Alura/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

Aplicación de consola para convertir monedas en tiempo real usando la API de ExchangeRate-API. Challenge de **Orientación a Objetos y consumo de APIs** de Alura Latam.

## ✨ Características

| Característica | Detalle |
|---|---|
| 🔐 Credencial segura | La API key se lee de una variable de entorno, nunca del código |
| 🛡️ Autenticación Bearer | La key viaja en una cabecera HTTP, no en la URL |
| ⚡ Doble caché | Memoria + archivo en disco; los datos se actualizan una vez al día |
| 🔁 Reintentos | Hasta 2 reintentos ante errores `429` y `5xx` |
| 🌍 Catálogo completo | Puedes elegir cualquier par de monedas soportado por la API |
| 📊 Historial | Guarda las conversiones realizadas durante la sesión |
| 🧪 Pruebas | 10 tests JUnit + cobertura JaCoCo + CI en GitHub Actions |

## 📑 Índice

- [Requisitos](#requisitos)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Uso](#uso)
- [Arquitectura](#arquitectura)
- [Pruebas y calidad](#pruebas-y-calidad)
- [Seguridad](#seguridad)
- [Autores](#autores)

## ✅ Requisitos

- **JDK 17** o superior
- **IntelliJ IDEA** (recomendado) o Maven CLI
- Una **API key gratuita** de [ExchangeRate-API](https://app.exchangerate-api.com/sign-up)

> El proyecto usa Maven. IntelliJ descarga las dependencias automáticamente al abrir `pom.xml`.

## ⚙️ Configuración

La API key **no se guarda en el repositorio**. Se lee de `EXCHANGE_RATE_API_KEY`.

**macOS / zsh:**

```bash
echo 'export EXCHANGE_RATE_API_KEY="TU_API_KEY"' >> ~/.zshrc
source ~/.zshrc
```

**IntelliJ IDEA:**

`Run → Edit Configurations… → Environment variables`

```
EXCHANGE_RATE_API_KEY=TU_API_KEY
```

La key debe tener entre **24 y 32 caracteres hexadecimales**. El programa la valida al iniciar.

## ▶️ Ejecución

### Desde IntelliJ

Abre `Main.java` y ejecútalo. Acepta **Load Maven projects** si aparece el aviso.

### Desde terminal

```bash
cd "Conversor de Moneda v3"
mvn compile exec:java
```

### JAR ejecutable

```bash
cd "Conversor de Moneda v3"
mvn package
./run.sh
```

El JAR queda en `target/convertidor-monedas-1.0.0.jar`.

## 🎮 Uso

| Opción | Acción |
|---|---|
| `1` — `6` | Conversiones rápidas: USD↔CRC, BRL↔CRC, ARS↔USD |
| `7` | Catálogo completo: elige origen y destino entre todas las monedas soportadas |
| `8` | Ver el historial de conversiones de la sesión |
| `9` | Salir |

Ejemplo de salida:

```text
  100 USD equivale a 51.200 CRC
  Tasa: 1 USD = 512 CRC
  Datos actualizados: Tue, 24 Nov 2026 00:00:00 +0000
```

## 🏗️ Arquitectura

| Clase | Responsabilidad |
|---|---|
| `Main` | Menú, validación de la key, historial y selección de pares |
| `ConverterRates` | Interacción con el usuario y formato de salida |
| `ExchangeRateClient` | HTTP, Bearer, reintentos, caché en memoria y disco |
| `TargetCurrency` | Modelo JSON de tasas |
| `SupportedCodesResponse` | Modelo JSON del catálogo de monedas |
| `CurrencyPair` | Enum de los pares rápidos |
| `CurrencyCalculator` | Lógica pura de conversión y validación |

```text
Usuario → Main → ConverterRates → ExchangeRateClient → ExchangeRate-API
                       ↓                  ↓
                  Historial        Caché (memoria + disco)
```

## 🧪 Pruebas y calidad

```bash
cd "Conversor de Moneda v3"
mvn test
```

- **10 tests JUnit 5**, incluyendo un servidor HTTP local para simular respuestas reales
- **JaCoCo** genera el reporte de cobertura en `target/site/jacoco/`
- **GitHub Actions** ejecuta `mvn test` en cada push
- **Dependabot** revisa las dependencias semanalmente

## 🔒 Seguridad

- La key original estaba expuesta en el código y en el historial de Git; fue **revocada y reemplazada**
- Autenticación mediante **Bearer token**, sin credenciales en la URL
- `.gitignore` excluye `.env`, `.idea/`, `out/`, `target/` y archivos temporales
- Ninguna credencial forma parte del código fuente

## 👤 Autores

**SkrillyG** (Aarón Aguero E.) — Challenge Alura Latam.
