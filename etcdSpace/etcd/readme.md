# Build image
Copy Dockerfile to etcd binaries directory
and
Build the Docker image:  `docker build -t vk-etcd .`
Run the container: `docker run -p 2379:2379 -p 2380:2380 vk-etcd`

# To run it in K8s
```bash
#Tag your image for the local registry
docker tag vk-etcd localhost:5000/vk-etcd:latest

#Push the image to the local registry
docker push localhost:5000/vk-etcd:latest

# Apply the deployment
kubectl apply -f etcd-deployment.yaml

# Verify it is running
kubectl get pods -l app=vk-etcd
kubectl logs <pod-name>
```
