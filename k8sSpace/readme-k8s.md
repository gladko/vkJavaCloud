# build and deploy just-hello app
```bash
cd k8sSpace

# build
docker build -t just-hello:latest justHello
# verify
docker images
# try to run
docker run -d -p 8080:8080 just-hello:latest
curl localhost:8080

# deploy into k8s
kubectl apply -f justHello

# clean up
kubectl delete -f justHello
```

## importing docker image to k3s (containerd)
docker save just-hello:latest -o just-hello.tar
sudo k3s ctr images import just-hello.tar
sudo k3s ctr images ls | grep just-hello

## Push image to Docker Hub (example)
docker tag just-hello vladika/just-hello:latest
docker push vladika/just-hello:latest

## push to local registry
docker tag just-hello localhost:5000/just-hello:latest
docker push localhost:5000/just-hello:latest

## Client examples
See com.hazelcast.kubernetes.KubernetesClient
and com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategy