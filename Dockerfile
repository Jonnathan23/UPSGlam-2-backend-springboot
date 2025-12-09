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
LABEL description="UPSGlam 2.0 Backend - Spring Boot Application"

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR desde el stage de build
COPY --from=build /app/target/app-0.0.1-SNAPSHOT.jar app.jar

# Copiar application.properties desde el build stage
# Spring Boot lo buscará automáticamente en el directorio de trabajo
COPY --from=build /app/src/main/resources/application.properties /app/application.properties

# Nota: serviceAccountKey.json ya está dentro del JAR (en src/main/resources/envs/)
# ClassPathResource lo encontrará automáticamente desde el classpath del JAR

# Cambiar ownership al usuario spring
RUN chown spring:spring app.jar /app/application.properties

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
# Spring Boot buscará application.properties automáticamente en el directorio actual
# El serviceAccountKey.json está dentro del JAR y será encontrado por ClassPathResource
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.config.location=file:/app/application.properties"]

