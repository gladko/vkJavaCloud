## Run Consul in docker

[how to](https://hub.docker.com/_/consul)

Start `docker run -d -p 8500:8500 consul:1.15.4`
See web UI in localhost:8500

## Run several consul nodes
Start first: `docker run -d --name=dev-consul -e CONSUL_BIND_INTERFACE=eth0 -p 8500:8500 consul:1.15.4`
Get IP address: `docker exec -ti  a1d94165fca2 sh -c "ip address ls"`
Assuming that returned IP-address is `172.18.0.2` start second and third: `docker run -d -e CONSUL_BIND_INTERFACE=eth0 consul:1.15.4 agent -dev -join=172.18.0.2`
Get/check members: `docker exec -t dev-consul consul members`


## Java clients
See [Consul Clients](https://mvnrepository.com/open-source/consul-clients)
TOP:
https://spring.io/projects/spring-cloud-consul
https://github.com/Ecwid/consul-api
https://github.com/rickfast/consul-client  ARCHIVED
https://github.com/vert-x3/vertx-consul-client

## plain docker
```bash
docker run -d --name consul --network vkcloud-manual-network -p 8500:8500 consul:1.15.4

docker run -d --network vkcloud-manual-network -e SERVER_PORT=18001 -e SPRING_CLOUD_CONSUL_HOST=consul -p 18001:18001 localhost:5000/consul-translate
docker run -d --network vkcloud-manual-network -e SERVER_PORT=18002 -e SPRING_CLOUD_CONSUL_HOST=consul -p 18002:18002 localhost:5000/consul-translate
docker run -d --network vkcloud-manual-network -e SERVER_PORT=18003 -e SPRING_CLOUD_CONSUL_HOST=consul -p 18003:18003 localhost:5000/consul-translate

docker run -d --network vkcloud-manual-network -e SERVER_PORT=8080 -e SPRING_CLOUD_CONSUL_HOST=consul -p 8080:8080 localhost:5000/consul-main

curl localhost:8080/ttt
curl localhost:8080/hi
```


## docker swarm
Result: only one instance of translate service is registered
```bash
../gradlew buildImages 

# start
docker swarm init --advertise-addr 172.17.244.18
docker stack deploy -c docker-compose.yml vk-stack

# usage
lynx http://172.17.244.18:8500/ui/dc1/services/consul-translate/instances
curl 172.17.244.18:8080/ttt
curl 172.17.244.18:8080/services
curl 172.17.244.18:8080/hi

# stop
docker stack rm vk-stack
docker swarm leave --force
```

## k8s
Result: so so
```bash
# build and push images to local registry
../gradlew pushImages 

kubectl apply -f k8s/
kubectl delete -f k8s/

# access to consul UI
kubectl port-forward svc/consul 8500:8500

# use consul-main
kubectl get svc consul-main
curl localhost:30XXX

```

## to access Consul web UI
`kubectl port-forward svc/consul 8500:8500`
or expose the consul via NodePort