# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime (Debian Jammy, required for Playwright/Chromium dependencies)
FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget fonts-liberation libnss3 libatk-bridge2.0-0 libdrm2 \
    libxcomposite1 libxdamage1 libxrandr2 libgbm1 libpango-1.0-0 \
    libcairo2 libasound2 libatspi2.0-0 libxshmfence1 \
    libcups2 libxfixes3 libxkbcommon0 \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

# Install Playwright browsers at build time so they are cached in the image
RUN java -jar app.jar --install-playwright 2>/dev/null || true && \
    java -cp app.jar -Dloader.main=com.microsoft.playwright.CLI \
    org.springframework.boot.loader.launch.PropertiesLauncher install chromium 2>/dev/null || true

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
