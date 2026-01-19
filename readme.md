in WSL go to `/mnt/c/Users/vkozak/workspace/projects/vkPets/vkJavaCloud`


## remove docker images
```bash
docker rmi -f eureka-server
docker rmi -f hello-service
docker rmi -f foo-service
```


## docker compose how-to
```bash
docker compose up
docker compose down
```

## docker stack
```bash
docker swarm init --advertise-addr 172.17.244.18

docker stack deploy -c eureka/docker-compose.yml eureka-stack
docker stack rm eureka-stack

docker swarm leave --force
```

## docker clean up
kill all running containers:            `docker kill $(docker ps -q)`
delete all containers:                  `docker rm $(docker ps -a -q)`
delete hello-world containers:         `docker rm $(docker ps -a -q  --filter ancestor=hello-world)`

## k3s how-to
```
kubectl apply -f deployment.yaml
kubectl get deployments
```

## Kill all vkPets

```bash
kill -9 $(ps -ef | grep vk.vkPets | awk '{print $2}')
```

```powershel
jcmd | Select-String "vk.vkPets" | ForEach-Object {
    kill ($_ -split '\s+')[0]
}
```


https://github.com/spring-cloud/spring-cloud-kubernetes


## Chaos testing 
Assuming node registry TTL = 30 sec
### Scenario_1: start service registry after service node
Assuming node registry TTL = 30 sec
1. Start node
2. Start service registry after 10, 30, 60 sec
3. Check if node registry exists

### Scenario_2: restart service registry
Assuming node registry TTL = 30 sec
1. Start service registry
2. Start node
3. Check if node is registered
4. Stop service registry
5. Start service registry after 10, 30, 60 sec
6. Check if node registry exists
7. Check if node registry exists after 60 sec

### Scenario_3: network issue
1. Start service registry
2. Start service toxiproxy
3. Start node, register it through toxiproxy 
4. Check if node is registered
5. Simulate disconnect with toxiproxy
6. Restore connectivity after 10, 30, 60 sec
7. Check if node registry exists
8. Check if node registry exists after 60 sec
