# 📸 UPSGlam 2.0 - Backend Reactivo con Spring WebFlux

## 📝 Descripción del Proyecto

Este repositorio contiene el **Backend Reactivo** de la plataforma social UPSGlam 2.0, una aplicación tipo Instagram. El backend está desarrollado en **Java** utilizando **Spring WebFlux** para garantizar una arquitectura de microservicios sin bloqueo y escalable.

[cite_start]El objetivo principal es gestionar las publicaciones, la autenticación de usuarios y servir como API Gateway para el procesamiento de imágenes por GPU (servicio externo). [cite: 3, 19]

### 🏗️ Tecnologías Clave

* **Framework:** Spring Boot 3.x
* [cite_start]**Modelo de Concurrencia:** Spring WebFlux (Reactivo y No-Bloqueante) [cite: 3, 19]
* **Servidor:** Netty
* **Lenguaje:** Java 17+
* [cite_start]**Base de Datos & Auth:** Firebase Firestore y Firebase Authentication [cite: 20]
* **Seguridad:** Spring Security (Validación de Tokens Firebase)

---

## 🛠️ Requisitos Previos

Asegúrate de tener instalado y configurado lo siguiente en tu entorno local:

* **Java Development Kit (JDK):** Versión 17 o superior (LTS).
* **Maven** (Opcional, el proyecto usa el wrapper `mvnw`).
* **Postman** o cualquier cliente REST para probar los endpoints.
* **IDE:** IntelliJ IDEA o VS Code con soporte para Spring/Java.

---

## 🔥 Configuración de Firebase

Para que la aplicación se conecte a Firebase, necesitas el archivo de credenciales del proyecto:

1.  **Obtener credenciales:** Descarga el archivo `serviceAccountKey.json` desde la Consola de Firebase.
2.  **Ubicación:** Crea la carpeta ``envs`` y coloca este archivo dentro del directorio de recursos del proyecto:
    ```
    src/main/resources/envs/serviceAccountKey.json
    ```

> ⚠️ **¡Seguridad Crítica!** Asegúrate de que este archivo **NO** se suba al repositorio de Git. El archivo `.gitignore` ya debe excluirlo, pero verifica que contenga la línea `serviceAccountKey.json`.

---

---

## ⚙️ Descarga e Instalación de Dependencias

Antes de arrancar la aplicación, debes instalar las dependencias definidas en el `pom.xml`.

### 1. Instalar Dependencias

Desde la raíz del proyecto, ejecuta el siguiente comando:

```bash
.\mvnw install

```

---

## 🏃 Arrancar la Aplicación

Existen dos formas principales de ejecutar la aplicación Spring Boot:

### 1. Desarrollo (Hot Swap)

Ejecuta el siguiente comando desde la raíz del proyecto para compilar y arrancar el servidor **Netty** en modo desarrollo:

```bash
.\mvnw spring-boot:run
```