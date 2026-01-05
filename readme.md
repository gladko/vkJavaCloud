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