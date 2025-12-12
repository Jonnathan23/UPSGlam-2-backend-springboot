# Configuración de Credenciales

Este directorio contiene los archivos de configuración que deben montarse como volúmenes en Docker.

## Estructura Requerida

```
config/
├── application.properties          # Archivo de configuración con credenciales
└── envs/
    └── serviceAccountKey.json      # Archivo de credenciales de Firebase
```

## Configuración

### 1. Crear application.properties

Copia el template y edita con tus credenciales:

```bash
cp ../src/main/resources/application.template.properties application.properties
```

Luego edita `application.properties` con tus valores:

```properties
spring.application.name=app
fastapi.url=http://localhost:8000
firebase.api.key=TU_API_KEY_AQUI
supabase.url=TU_SUPABASE_URL_AQUI
supabase.key=TU_SUPABASE_KEY_AQUI
supabase.bucket=TU_BUCKET_NAME_AQUI

server.address=0.0.0.0
server.port=8080
```

### 2. Copiar serviceAccountKey.json

```bash
mkdir -p envs
cp ../src/main/resources/envs/serviceAccountKey.json envs/serviceAccountKey.json
```

## Seguridad

⚠️ **IMPORTANTE:** 
- Este directorio contiene credenciales sensibles
- NO lo subas a Git (debe estar en `.gitignore`)
- Solo úsalo localmente o en servidores seguros

