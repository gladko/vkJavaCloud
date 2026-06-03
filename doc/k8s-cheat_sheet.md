## k3s how-to
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
