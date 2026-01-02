FROM openjdk:17-jdk-alpine

COPY build/libs/eureka-server-0.0.1.jar eureka-server.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/eureka-server.jar"]