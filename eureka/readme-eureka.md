## APPs
- hello-service:  trivial spring boot app. Can register itself with ServiceRegistry.
- foo-service: a client app that uses different APIs for hello-service discovery


## ports
eureka-server  8761
hello-service  18000
foo-service    18100


# Start second instance of hello-service
java -Dserver.port=18002 -jar build/libs/eu-hello-0.0.1.jar
java -Dserver.port=18002 -Deureka.client.serviceUrl.defaultZone="http://172.18.0.2:8761/eureka" -jar build/libs/eu-hello-0.0.1.jar



## docker how-to
1. in WSL go to `/mnt/c/Users/vkozak/workspace/projects/vkPets/vkJavaCloud`
2. 
```bash
./gradlew buildEurekaImages
```

2. build hello-service
```bash
../../gradlew build
docker build -t eu-hello .
```
3. run hello-service
```bash
docker run -d -p 8001:18000 eu-hello
docker run -d -p 8002:18000 eu-hello

docker run -d -p 8001:18000 -e SPRING_PROFILES_ACTIVE=docker -e EUREKA_URL='http://172.18.0.2:8761/eureka' eu-hello
docker exec -it <container-name> sh
docker container stop <HASH>
```

docker build -t eureka-server .
docker run -d -p 8761:8761 --name eureka-server eureka-server
