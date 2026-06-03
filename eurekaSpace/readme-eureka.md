## APPs
- translate-service:  trivial spring boot app. Can register itself with ServiceRegistry.
- main-service: a client app that uses different APIs for the translate-service discovery


## ports
eureka-server  8761

## traditional non‑containerized environment
```bash
./gradlew eurekaSpace:build

./runscenario eurekaSpace/scenario

# Start additional instance of eu-translate
java -Dserver.port=18001 -jar build/libs/eu-translate-0.0.1.jar
java -Dserver.port=18002 -Deureka.client.serviceUrl.defaultZone="http://172.18.0.2:8761/eureka" -jar build/libs/eu-translate-0.0.1.jar

# check
curl localhost:8761
# or 
lynx 10.43.49.73:8761

curl localhost:8080/hi

./killall
```


## docker how-to manually
2. build
```bash
../../gradlew build
docker build -t eureka .
docker build -t eu-translate .
docker build -t eu-main .
```
3. run
```bash
docker run -d -p 8001:8080 -e SPRING_PROFILES_ACTIVE=docker -e EUREKA_URL='http://172.18.0.2:8761/eureka' eu-translate
# get into container. Then see logs
docker exec -it <container-name> sh
docker container stop <HASH>

docker run -d -p 8761:8761 --name eureka eureka
```


## docker how-to via gradle
```bash
./gradlew eurekaSpace:eureka:startContainer
./gradlew eurekaSpace:eu-translate:startContainer
./gradlew eurekaSpace:eu-main:startContainer

# check
curl localhost:8080/hi

# stop
./gradlew eurekaSpace:eureka:removeContainer
```

## docker compose
TODO

## k8s
1. Push image to local docker repo `./gradlew :eurekaSpace:pushImages`
or
```bash
docker tag eureka:latest localhost:5000/eureka:latest
docker push localhost:5000/eureka:latest
```

2. deploy
```bash
kubectl apply -f eureka/k8s/deployment.yaml
kubectl apply -f eureka/k8s/service.yaml

# then the same for  eu-translate and eu-main components
```
or with kustomization.yaml
`kubectl apply -k eurekaSpace`
or with helm
`TODO !!!`

3. use
```bash
# resolve port of eu-main service
kubectl get svc eu-main

# show eureka dashboard
curl 10.43.232.93:8761

curl localhost:32423/discovery?service=eu-translate
curl localhost:32423/hi
```

4. cleanup
`kubectl delete -k eurekaSpace`

## Access k8s services. Port forward
Create port forward: `kubectl port-forward svc/eureka 8761:8761 -n default`
Show current port forwards:   `sudo lsof -iTCP -sTCP:LISTEN | grep kubectl`
Stop forward: kill the `kubectl` process shown above
