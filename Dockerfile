# 1️⃣ Imagen base con Java 21
FROM eclipse-temurin:21-jdk

# 2️⃣ Directorio de trabajo dentro del contenedor
WORKDIR /app

# 3️⃣ Copiamos todo el proyecto
COPY src/main/kotlin/com/diana/ComTec/config .

# 4️⃣ Damos permisos al wrapper de Gradle
RUN chmod +x ./gradlew

# 5️⃣ Compilamos la app (crea el .jar)
RUN ./gradlew bootJar

# 6️⃣ Puerto que Render usará
EXPOSE 8080

# 7️⃣ Comando para arrancar Spring Boot
CMD ["sh", "-c", "java -jar build/libs/*.jar"]
