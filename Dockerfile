# ---------- FASE DE BUILD ----------
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiamos archivos necesarios para Maven Wrapper
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Damos permisos de ejecución al wrapper
RUN chmod +x mvnw

# Descargamos dependencias y compilamos
RUN ./mvnw -q -DskipTests package

# Copiamos todo el código fuente y recompilamos (por si acaso)
COPY src src
RUN ./mvnw -q -DskipTests package

# ---------- FASE DE RUNTIME ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el jar generado desde la fase de build
COPY --from=build /app/target/*.jar app.jar

# Render expone el puerto en la variable PORT
ENV PORT=8081

EXPOSE 8081

# Ejecutamos Spring Boot usando la variable PORT
CMD ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]
