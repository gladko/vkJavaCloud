## APPs
- translate-service:  trivial spring boot app. Can register itself with ServiceRegistry.
- main-service: a client app that uses different APIs for the translate-service discovery


## ports
eureka-server  8761


# Start second instance of eu-translate
java -Dserver.port=18001 -jar build/libs/eu-translate-0.0.1.jar
java -Dserver.port=18002 -Deureka.client.serviceUrl.defaultZone="http://172.18.0.2:8761/eureka" -jar build/libs/eu-translate-0.0.1.jar


## docker how-to manually
1. in WSL go to `/mnt/c/Users/vkozak/workspace/projects/vkPets/vkJavaCloud`
2. build
```bash
../../gradlew build
docker build -t eu-translate .
docker build -t eu-main .
```
3. run eu-translate
```bash
docker run -d -p 8001:8080 -e SPRING_PROFILES_ACTIVE=docker -e EUREKA_URL='http://172.18.0.2:8761/eureka' eu-translate
docker exec -it <container-name> sh
docker container stop <HASH>
```

docker build -t eureka-server .
docker run -d -p 8761:8761 --name eureka eureka

## docker how-to via gradle
```bash
./gradlew eurekaSpace:eureka:startContainer
./gradlew eurekaSpace:eu-translate:startContainer
./gradlew eurekaSpace:eu-main:startContainer
```
## check
curl localhost:8080/hi

./gradlew eurekaSpace:eureka:removeContainer

## k8s
 - Push image to local docker repo
`./gradlew :eurekaSpace:pushImages`
or
```bash
docker tag eureka:latest localhost:5000/eureka:latest
docker push localhost:5000/eureka:latest
```

deploy
```bash
kubectl apply -f eureka/deployment.yaml
kubectl apply -f eu-translate/deployment.yaml
kubectl apply -f eu-main/deployment.yaml
```

use
```bash
# resolve port of eu-main service
kubectl get scv

curl localhost:32423/discovery?service=eu-translate
curl localhost:32423/hi

# show eureka dashboard
curl 10.43.232.93:8761
```

## Access k8s services. Port forward
Create port forward: `kubectl port-forward svc/eureka 8761:8761 -n default`
Show current port forwards:   `sudo lsof -iTCP -sTCP:LISTEN | grep kubectl`
Stop forward: kill the `kubectl` process shown above
