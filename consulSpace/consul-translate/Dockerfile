FROM openjdk:17-jdk-alpine

COPY build/libs/consul-translate-0.0.1.jar consul-translate.jar
ENTRYPOINT ["java", "-jar", "consul-translate.jar"]