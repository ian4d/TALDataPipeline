# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build.gradle .
COPY settings.gradle .
COPY gradle gradle
COPY gradlew gradlew
COPY src ./src
RUN mkdir -p _data/downloads/raw
RUN mkdir -p _data/blog/episodes
RUN mkdir -p _data/blog/contributors
RUN mkdir -p _posts
CMD ./gradlew run