# Build
FROM gradle:8.5-jdk21 AS build

WORKDIR /app
COPY . .

RUN gradle bootJar --no-daemon

# Run
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]