# Etapa 1: Compilar con Java 21
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copiar los archivos del proyecto
COPY . .

# ✅ Dar permisos de ejecución a gradlew
RUN chmod +x gradlew

# Compilar sin usar el demonio
RUN ./gradlew clean bootJar --no-daemon

# Etapa 2: Imagen liviana para ejecución
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el .jar generado desde la etapa de compilación
COPY --from=build /app/build/libs/*.jar app.jar

# Puerto (ajústalo si usas otro)
EXPOSE 8080

# Ejecutar el .jar
ENTRYPOINT ["java", "-jar", "app.jar"]
