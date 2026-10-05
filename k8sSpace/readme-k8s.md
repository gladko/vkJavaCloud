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

## importing docker image to k3s (containerd) through .tar file
docker save just-hello:latest -o just-hello.tar
sudo k3s ctr images import just-hello.tar
sudo k3s ctr images ls | grep just-hello

## importing docker image to k3s via gradle
./gradlew :k8sSpace:k8s-main:importImageToK3s
./gradlew :k8sSpace:k8s-translate:importImageToK3s

## get k3s images
sudo k3s ctr images ls -q

## Push image to Docker Hub (example)
docker tag just-hello vladika/just-hello:latest
docker push vladika/just-hello:latest

## push to local registry
docker tag just-hello localhost:5000/just-hello:latest
docker push localhost:5000/just-hello:latest

## restart
kubectl rollout restart deployment my-app
kubectl rollout status deployment my-app

## scale
kubectl scale deployment k8s-translate --replicas=10

## logs
kubectl logs -f deployment/k8s-main

## Client examples
See com.hazelcast.kubernetes.KubernetesClient
and com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategy


# Grps
```bash
# only generates code
./gradlew k8sSpace:api:generateProto

# build and check
./gradlew k8sSpace:api:jar
jar tf k8sSpace/api/build/libs/*.jar | grep GreetingServiceGrpc

curl "http://localhost:8080/helloGrps?name=Ivan"
```