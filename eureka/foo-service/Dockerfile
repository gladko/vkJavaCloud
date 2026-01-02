FROM openjdk:17-jdk-alpine

COPY build/libs/foo-service-0.0.1.jar foo-service.jar
ENTRYPOINT ["java", "-jar", "foo-service.jar"]