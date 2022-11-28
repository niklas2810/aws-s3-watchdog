FROM maven:3.8-eclipse-temurin-17-alpine AS maven-builder
COPY src /build/src
COPY pom.xml /build/pom.xml
RUN mvn -f /build/pom.xml clean package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=maven-builder /build/target/*-shaded.jar /app/application.jar


ENTRYPOINT ["java", "-jar", "/app/application.jar"]