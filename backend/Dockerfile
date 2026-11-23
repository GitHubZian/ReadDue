# syntax=docker/dockerfile:1

# ---------- 构建 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- 运行 ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd -r app && useradd -r -g app -u 1001 app \
    && mkdir -p /data/uploads && chown -R app:app /data /app
COPY --from=build /build/target/backend-*.jar app.jar
USER app
ENV SPRING_PROFILES_ACTIVE=prod \
    FILE_STORAGE_DIR=/data/uploads
EXPOSE 8125
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
