FROM eclipse-temurin:21-jammy AS build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn

# Fix mvnw permissions
RUN chmod +x mvnw

# Create maven cache directory
RUN mkdir -p /root/.m2

# Copy source code
COPY src src

# Build with tests (ensures code quality)
RUN ./mvnw -B clean package

FROM eclipse-temurin:21-jre-jammy
ARG JAR_FILE=/workspace/target/*.jar
COPY --from=build ${JAR_FILE} app.jar

# Create non-root user
RUN groupadd -r app && useradd -r -g app app
USER app

EXPOSE 8080
ENTRYPOINT ["java","-Xmx128m","-Xms64m","-XX:MaxMetaspaceSize=64m","-XX:+UseSerialGC","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
