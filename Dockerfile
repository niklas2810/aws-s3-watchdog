FROM maven:3.6.3-openjdk-14-slim AS maven-builder
COPY src /build/src
COPY pom.xml /build/pom.xml
RUN mvn -f /build/pom.xml clean package

FROM openjdk:14-jdk-slim
WORKDIR /app
COPY --from=maven-builder /build/target/*-shaded.jar /app/application.jar


ENTRYPOINT ["java", "-jar", "/app/application.jar"]