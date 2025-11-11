FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
RUN chmod +x ./gradlew

COPY build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon || true

COPY . .

RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseG1GC -XX:MaxMetaspaceSize=128m"

ENTRYPOINT ["/bin/sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]