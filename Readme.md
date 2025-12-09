# 📸 UPSGlam 2.0 - Backend Reactivo con Spring WebFlux

## 📝 Descripción del Proyecto

Este repositorio contiene el **Backend Reactivo** de la plataforma social UPSGlam 2.0. Actúa como un **API Gateway** inteligente que orquesta la autenticación, el almacenamiento y el procesamiento de imágenes.

### 🚀 Arquitectura del Sistema

El sistema sigue una arquitectura de microservicios moderna:

1.  **Spring Boot (Este Repo):**
    *   Gestiona la autenticación con **Firebase**.
    *   Actúa como Gateway para el servicio de procesamiento.
    *   Sube las imágenes procesadas a **Supabase Storage**.
    *   Devuelve respuestas JSON estructuradas al cliente.
2.  **FastAPI (VisionProcessingGPU-Kit):**
    *   Microservicio externo en Python.
    *   Procesa imágenes usando **GPU (CUDA)** y OpenCV.
    *   Aplica filtros: Canny, Gaussian, Negative, Emboss, Watermark, Ripple, Collage.
    *   [Repositorio GitHub](https://github.com/Juanja1306/VisionProcessingGPU-Kit)
3.  **Firebase:**
    *   **Auth:** Gestión de usuarios y tokens JWT.
    *   **Firestore:** Persistencia de datos de usuario.
4.  **Supabase:**
    *   **Storage:** Almacenamiento de objetos para guardar las imágenes procesadas.

---

## 🛠️ Tecnologías Clave

*   **Framework:** Spring Boot 3.9
*   **Modelo de Concurrencia:** Spring WebFlux 
*   **Cliente HTTP:** WebClient 
*   **Seguridad:** Spring Security + Firebase Admin SDK
*   **Almacenamiento:** Supabase Storage API
*   **Lenguaje:** Java 21

---

## ⚙️ Configuración del Entorno

### 1. Requisitos Previos
*   Java 21 JDK
*   Maven (o usar `./mvnw`)
*   Servicio FastAPI corriendo en `http://localhost:8000`

### 2. Variables de Entorno (`application.properties`)

Crea o edita el archivo `src/main/resources/application.properties` con las siguientes claves:

```properties
spring.application.name=app
fastapi.url=http://localhost:8000
firebase.api.key=XXX
supabase.url=XXX
supabase.key=XXX
supabase.bucket=XXX
```

### 3. Credenciales de Firebase
Coloca tu archivo `serviceAccountKey.json` en:
`src/main/resources/envs/serviceAccountKey.json`

---

## 🔌 Endpoints de la API

Todos los endpoints de procesamiento requieren un **Token Bearer de Firebase** válido en el header `Authorization`.

### 🔐 Autenticación

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Registro de usuario (Email/Password) |
| `POST` | `/api/auth/login` | Login de usuario (Devuelve Token) |

### 🎨 Procesamiento de Imágenes

Todos estos endpoints aceptan `multipart/form-data` con un archivo `file`. Devuelven un JSON con la URL de la imagen procesada.

**Respuesta Exitosa (200 OK):**
```json
{
    "userName": "Nombre del Usuario",
    "imageUrl": "https://tu-proyecto.supabase.co/storage/v1/object/public/UPSGlam/uuid_imagen.png"
}
```

| Filtro | Endpoint | Parámetros Opcionales (Form-Data) |
| :--- | :--- | :--- |
| **Canny** | `/api/process/canny` | `kernel_size`, `sigma`, `low_threshold`, `high_threshold`, `use_auto` |
| **Gaussian** | `/api/process/gaussian` | `kernel_size`, `sigma`, `use_auto` |
| **Negative** | `/api/process/negative` | *Ninguno* |
| **Emboss** | `/api/process/emboss` | `kernel_size`, `bias_value`, `use_auto` |
| **Watermark** | `/api/process/watermark` | `scale`, `transparency`, `spacing` |
| **Ripple** | `/api/process/ripple` | `edge_threshold`, `color_levels`, `saturation` |
| **Collage** | `/api/process/collage` | *Ninguno* |

---

## 🏃 Ejecución

### Opción 1: Ejecución Local

```bash
.\mvnw spring-boot:run
```

### Opción 2: Docker

#### Opción 2.1: Docker Compose (Recomendado - Incluye FastAPI)

**Requisitos:**
- Docker con soporte NVIDIA GPU (para FastAPI)
- La imagen de FastAPI se descarga automáticamente desde Docker Hub: `juanja/gpu-vision-kit:latest` de Microservicio FastAPI -> [VisionProcessingGPU-Kit](https://github.com/Juanja1306/VisionProcessingGPU-Kit) 

**Pasos:**

1. Construir la imagen de Spring Boot:
```bash
docker build -t upsglam-backend:latest .
```

2. Ejecutar ambos servicios con Docker Compose:
```bash
docker-compose up
```

**Nota:** La primera vez que ejecutes `docker-compose up`, Docker descargará automáticamente la imagen `juanja/gpu-vision-kit:latest` desde Docker Hub.

3. Ejecutar en segundo plano:
```bash
docker-compose up -d
```

4. Ver logs:
```bash
docker-compose logs -f
```

5. Detener servicios:
```bash
docker-compose down
```

#### Opción 2.2: Docker CLI (Solo Spring Boot)

#### 1. Construir la imagen
```bash
docker build -t upsglam-backend:latest .
```

#### 2. Ejecutar el contenedor
```powershell
# Simple - Todo está en la imagen (application.properties y serviceAccountKey.json)
docker run --rm `
  --name upsglam-backend `
  -p 8080:8080 `
  upsglam-backend:latest
```

**Nota:** El `application.properties` y `serviceAccountKey.json` ya están incluidos en la imagen, así que no necesitas pasar variables de entorno ni montar volúmenes.

#### 3. Ver logs
```bash
docker logs -f upsglam-backend
```

---

## 🐛 Solución de Problemas Comunes

1.  **Error 400/403 en Supabase (Invalid Compact JWS):**
    *   Asegúrate de usar la **Legacy API Key** (JWT que empieza por `ey...`), no el nuevo `sb_secret`.
2.  **Error 404 (Bucket not found) al ver la imagen:**
    *   Tu bucket en Supabase debe ser **PÚBLICO**. Ve a Storage -> Buckets -> Edit Bucket -> Public: ON.
3.  **Error 500 en Procesamiento:**
    *   Verifica que el servicio FastAPI esté corriendo en el puerto 8000.