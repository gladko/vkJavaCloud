FROM openjdk:17-jdk-alpine

COPY build/libs/consul-hello-0.0.1.jar consul-hello.jar
ENTRYPOINT ["java", "-jar", "consul-hello.jar"]