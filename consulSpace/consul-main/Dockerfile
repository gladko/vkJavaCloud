FROM openjdk:17-jdk-alpine

COPY build/libs/consul-main-0.0.1.jar consul-main.jar
ENTRYPOINT ["java", "-jar", "consul-main.jar"]