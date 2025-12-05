FROM eclipse-temurin:21-alpine AS build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN mkdir -p /root/.m2

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=/workspace/target/*.jar
COPY --from=build ${JAR_FILE} app.jar

RUN addgroup -S app && adduser -S app -G app
USER app

EXPOSE 8080
ENTRYPOINT ["java","-Xmx128m","-Xms64m","-XX:MaxMetaspaceSize=64m","-XX:+UseSerialGC","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
