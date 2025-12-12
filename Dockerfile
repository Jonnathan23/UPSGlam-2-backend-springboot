# Stage 1: Build stage - Compilar la aplicación
FROM maven:3.9.11-eclipse-temurin-21 AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven (para cache de dependencias)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Copiar código fuente
COPY src ./src

# Construir la aplicación (skip tests para producción, o quita -DskipTests si quieres ejecutar tests)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage - Imagen final optimizada
FROM eclipse-temurin:21-jre-alpine

# Agregar metadatos
LABEL maintainer="UPSGlam Team"
LABEL description="UPSGlam 1.0 Backend - Spring Boot Application"

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR desde el stage de build
COPY --from=build /app/target/app-0.0.1-SNAPSHOT.jar app.jar

# Crear directorios para credenciales (se montarán como volúmenes)
# NO copiamos application.properties ni serviceAccountKey.json aquí
# Estos deben ser montados como volúmenes al ejecutar el contenedor
RUN mkdir -p /app/envs && \
    chown -R spring:spring /app

# Cambiar al usuario no-root
USER spring:spring

# Exponer el puerto
EXPOSE 8080

# Instalar wget para health check
USER root
RUN apk add --no-cache wget
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Variables de entorno por defecto (pueden ser sobrescritas)
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando para ejecutar la aplicación
# application.properties debe estar montado en /app/application.properties
# serviceAccountKey.json debe estar montado en /app/envs/serviceAccountKey.json
# Si no están montados, la aplicación fallará con un error claro
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.config.location=file:/app/application.properties"]

