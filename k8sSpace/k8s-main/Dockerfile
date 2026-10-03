FROM openjdk:17-jdk-alpine

COPY build/libs/k8s-main-0.0.1.jar main.jar
ENTRYPOINT ["java", "-jar", "main.jar"]