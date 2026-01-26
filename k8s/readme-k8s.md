cd `/mnt/c/Users/vkozak/workspace/projects/vkPets/vkJavaCloud/k8s/k8sHello`
docker build -t k8s-hello .
docker run -d -p 8080:8080 k8s-hello



docker build -t k8s-hello:local .
### Push image to Docker Hub (example)
docker tag k8s-hello:local vladika/k8s-hello:latest
docker push vladika/k8s-hello:latest


## Commands
```bash
sudo systemctl stop k3s
sudo systemctl stop k3s-agent
sudo systemctl status k3s
```

## check status
kubectl cluster-info
curl -k https://127.0.0.1:6443


## deploy
```bash
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

## Check the NodePort assigned:
```bash
kubectl get svc k8s-hello-service

NAME                TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
k8s-hello-service   NodePort   10.43.255.100   <none>        80:3xxxx/TCP     1m
```
The 3xxxx is the NodePort on your local machine.

Open in your browser or curl:
http://localhost:<NodePort>



See com.hazelcast.kubernetes.KubernetesClient 
and com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategy