FROM openjdk:17-jdk-alpine

COPY build/libs/k8s-translate-0.0.1.jar translate.jar
ENTRYPOINT ["java", "-jar", "translate.jar"]