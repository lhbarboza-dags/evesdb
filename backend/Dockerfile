# Runtime-only image: copies the jar built on the host by scripts/build.sh.
# Needs no network during the build (Maven runs on the host). For a self-contained build on a machine that can reach
# Maven Central, use Dockerfile.multistage instead.
FROM eclipse-temurin:17-jre
RUN useradd --system --uid 10001 app
WORKDIR /app
COPY target/sibol-eves-api.jar app.jar
USER app
EXPOSE 8080
# No curl/wget in the image: plain bash /dev/tcp HTTP request to the actuator.
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080 && printf 'GET /actuator/health HTTP/1.0\\r\\n\\r\\n' >&3 && grep -q UP <&3"]
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
