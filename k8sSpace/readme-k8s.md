# build and deploy just-hello app
```bash
cp justHello

# build
docker build -t just-hello .
# verify
docker images
# try to run
docker run -d -p 8080:8080 just-hello
curl localhost:8080

# push to local registry
docker tag just-hello localhost:5000/just-hello:latest
docker push localhost:5000/just-hello:latest

# deploy into k8s
kubectl apply -f .
```

### Push image to Docker Hub (example)
docker tag just-hello vladika/just-hello:latest
docker push vladika/just-hello:latest


## Client examples
See com.hazelcast.kubernetes.KubernetesClient
and com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategy