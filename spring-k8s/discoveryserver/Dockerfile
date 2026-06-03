FROM openjdk:17-jdk-alpine

COPY build/libs/eureka-0.0.1.jar eureka.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/eureka.jar"]