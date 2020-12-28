FROM openjdk:14-jdk-slim
COPY ./target/*-shaded.jar /app/application.jar
WORKDIR /app

ENTRYPOINT ["java", "-jar", "/app/application.jar"]