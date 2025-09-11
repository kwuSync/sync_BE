FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY . .

RUN ./gradlew build -x test

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "./build/libs/sum_news_BE-0.0.1-SNAPSHOT.jar"]