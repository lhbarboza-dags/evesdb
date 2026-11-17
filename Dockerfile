# Single-container image for free PaaS hosting (Northflank, Render, …): Spring Boot serves the API *and* the React app.
# Build context = repository root. The platform builds it on its own servers (needs internet for Maven/npm).
# Local/VM setups keep using docker-compose.yml / deploy/compose.prod.yml with the per-folder Dockerfiles.

# ---- 1. React build
FROM node:20.18-alpine AS web
WORKDIR /web
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# ---- 2. Spring Boot build, with the React build packaged as classpath:/static
FROM maven:3.9.9-eclipse-temurin-17 AS api
WORKDIR /api
COPY backend/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
COPY --from=web /web/dist/ ./src/main/resources/static/
RUN mvn -B -q -DskipTests package

# ---- 3. Runtime
FROM eclipse-temurin:17-jre
RUN useradd --system --uid 10001 app
WORKDIR /app
COPY --from=api /api/target/sibol-eves-api.jar app.jar
USER app
EXPOSE 8080
# Small-RAM friendly JVM settings (free tiers have 512 MB–1 GB).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 \
  CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/${PORT:-8080} && printf 'GET /actuator/health HTTP/1.0\\r\\n\\r\\n' >&3 && grep -q UP <&3"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
