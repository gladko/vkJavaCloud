
## remove docker images
```bash
docker rmi -f eureka
docker rmi -f eu-main
docker rmi -f eu-translate
```


## docker compose how-to
```bash
docker compose up
docker compose down
```

## docker stack
```bash
# start
docker swarm init --advertise-addr 172.17.244.18
docker stack deploy -c docker-compose.yml vk-stack

# usage
http://172.17.244.18:8761/
curl 172.17.244.18:8080/ttt
curl 172.17.244.18:8080/services

# It does NOT work with EUREKA. Probably because eu-translate service is registered with wrong IP.
curl 172.17.244.18:8080/hi

# stop
docker stack rm vk-stack
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

## create custom docker network.
Allows docker individual containers access each other by name.
`docker network create vkcloud-manual-network`

docker-compose creates it automatically.

Show containers attached to a specific Docker network
```bash
 docker network inspect vkcloud-manual-network \
  --format '{{ range $id, $c := .Containers }}{{ $c.Name }} {{ end }}'
```