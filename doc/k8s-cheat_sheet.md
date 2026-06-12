## Start / stop
```bash
sudo systemctl start k3s
sudo systemctl start k3s-agent

sudo systemctl status k3s

sudo systemctl start k3s
sudo systemctl start k3s-agent
```

## k3s apply
```bash
# Apply single file
kubectl apply -f deployment.yaml

# Apply all files in dir
kubectl apply -f ./k8s
kubectl delete -f ./k8s

# get deployment
kubectl get deployments
```

## helm
helm install test ./helm
helm uninstall test


## Check the NodePort assigned:
```bash
kubectl get svc k8s-hello-service

NAME                TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
k8s-hello-service   NodePort   10.43.255.100   <none>        80:3xxxx/TCP     1m
```
The 3xxxx is the NodePort on your local machine (WSL). Open in your browser or `curl http://localhost:<NodePort>`

## Delete
```bash
kubectl delete -n default service FOO
kubectl delete -n default deployment FOO

kubectl delete all --all -n default
```


## encode k8s secret value.
By default, it's insecure. Can be easily decoded !!!
`echo -n 'username' | base64`
`echo "SGVsbG8gV29ybGQ=" | base64 --decode`