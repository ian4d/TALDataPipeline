# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build.gradle .
COPY settings.gradle .
COPY gradle gradle
COPY gradlew gradlew
COPY src ./src
COPY .env ./.env
RUN mkdir -p _data/downloads/raw
RUN mkdir -p _data/episodes
RUN mkdir -p _data/contributors
RUN mkdir -p _posts
CMD ./gradlew run