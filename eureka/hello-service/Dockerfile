FROM openjdk:17-jdk-alpine

COPY build/libs/hello-service-0.0.1.jar hello-service.jar
ENTRYPOINT ["java", "-jar", "hello-service.jar"]
#EXPOSE 8080

# Copy the application's JAR file into the container
# COPY build/libs/hello-service-*.jar /app/hello-service.jar
#COPY build/libs/hello-service-0.0.1.jar /app/hello-service.jar

# Specify the command to run the application
#CMD ["java", "-jar", "/app/hello-service.jar"]