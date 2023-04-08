# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build.gradle .
COPY settings.gradle .
COPY gradle gradle
COPY gradlew gradlew
COPY src ./src
RUN mkdir -p data/downloads/raw
RUN mkdir -p data/blog/episodes
RUN mkdir -p data/blog/contributors
CMD ./gradlew run