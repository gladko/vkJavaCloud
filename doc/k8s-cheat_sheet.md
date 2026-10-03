## Start / stop k3s
```bash
sudo systemctl start k3s
sudo systemctl start k3s-agent

sudo systemctl status k3s
```

## k3s apply / delete
```bash
# Apply single file
kubectl apply -f deployment.yaml

# Apply all files in dir
kubectl apply -f ./k8s
kubectl delete -f ./k8s

# get deployment
kubectl get deployments

# delete
kubectl delete -n default service FOO
kubectl delete -n default deployment FOO
kubectl delete all --all -n default
```


## Access deployed services
### for NodePort service
```bash
# 1. get actual port
kubectl get svc k8s-hello-service

NAME                TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
k8s-hello-service   NodePort   10.43.255.100   <none>        80:3xxxx/TCP     1m
# where 3xxxx/TCP is the NodePort on your local machine (WSL). 

# 2. Access from WSL: 
curl http://localhost:<NodePort>

# 3. Access from Win host: 
#   3.1 get WSL IP (= to windows IP): hostname -I | awk '{print $1}'
curl http://172.17.244.18:<NodePort>
```

### for ClusterIp service: port-forward
```bash
# 1. show services
kubectl get svc 

# 2. create port forward: 
kubectl port-forward svc/eureka 8761:8761 -n default
kubectl port-forward svc/k8s-translate 8123:8080
# forward to pod
kubectl port-forward pod/my-pod 8080:8080

# 3. from both WSL and WINDOWS
curl http://localhost:8123

# Show current port forwards:  
sudo lsof -iTCP -sTCP:LISTEN | grep kubectl
ss -lntp | grep 8080

# To stop forward: kill the `kubectl` process shown above
```

## logs
```bash
# single log from pod
kubetcl logs <POD_NAME>
# follow logs
kubetcl logs -f <POD_NAME>

# deployment logs
kubectl logs deployment/my-app
```

## pods discovery & connectivity
```bash
kubectl get endpoints <SERVICE_NAME>

# enter into pod
kubectl exec -it <pod-name> -- sh
kubectl exec -it deployment/k8s-main -- sh

# run tmp pod with curl
kubectl run curl \
 --rm -it \
 --image=curlimages/curl \
 -- sh
 
# or
kubectl run netshoot \
  --rm -it \
  --image=nicolaka/netshoot \
  -- bash

for i in {1..20}; do
  curl http://k8s-translate:8080/ping
  echo
done
```

## encode k8s secret value.
By default, it's insecure. Can be easily decoded !!!
`echo -n 'username' | base64`
`echo "SGVsbG8gV29ybGQ=" | base64 --decode`