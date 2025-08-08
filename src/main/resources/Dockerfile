# ========== DOCKER CONFIGURATION ==========

# Dockerfile
FROM openjdk:17-jdk-slim

LABEL maintainer="Banking System Team"
LABEL description="Secure Banking System Application"

# Create application directory
WORKDIR /app

# Create non-root user for security
RUN groupadd -r bankingapp && useradd -r -g bankingapp bankingapp

# Copy application jar
COPY target/banking-system-*.jar app.jar

# Set ownership and permissions
RUN chown -R bankingapp:bankingapp /app
RUN chmod 755 /app/app.jar

# Switch to non-root user
USER bankingapp

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM optimization for containers
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

