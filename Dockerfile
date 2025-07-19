FROM ubuntu:latest
LABEL authors="Admin"

ENTRYPOINT ["top", "-b"]

# Usamos una imagen oficial de OpenJDK 17 para construir la app
FROM openjdk:17-jdk-slim AS build

# Definimos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos los archivos de configuración y código fuente a la imagen
COPY ./build.gradle.kts ./settings.gradle.kts ./gradle.properties ./
COPY ./gradle ./gradle
COPY ./src ./src

# Construimos el proyecto y empaquetamos la app como JAR
RUN ./gradlew clean bootJar --no-daemon

# Segunda etapa: creamos la imagen final para producción
FROM openjdk:17-jdk-slim

# Directorio donde se ejecutará la app
WORKDIR /app

# Copiamos el JAR construido en la etapa anterior
COPY --from=build /app/build/libs/*.jar app.jar

# Puerto en el que corre la app (ajusta si usas otro)
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java","-jar","app.jar"]
