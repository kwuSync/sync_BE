FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies --no-daemon || true

COPY . .
RUN gradle clean build -x test --no-daemon

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
# RUN ./gradlew build -x test

EXPOSE 8080

ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseG1GC -XX:MaxMetaspaceSize=128m"

ENTRYPOINT ["java", "-jar", "./build/libs/sync_BE-0.0.1-SNAPSHOT.jar"]