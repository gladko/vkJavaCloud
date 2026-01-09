FROM openjdk:17-jdk-alpine

COPY build/libs/consul-foo-0.0.1.jar consul-foo.jar
ENTRYPOINT ["java", "-jar", "consul-foo.jar"]