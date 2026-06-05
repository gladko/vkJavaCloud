docker build -t k8s-hello .
docker run -d -p 8080:8080 k8s-hello


docker build -t k8s-hello:local .
### Push image to Docker Hub (example)
docker tag k8s-hello:local vladika/k8s-hello:latest
docker push vladika/k8s-hello:latest

### Push image to local docker repo
docker tag k8s-hello:local localhost:5000/k8s-hello:latest
docker push localhost:5000/k8s-hello:latest



## Client examples
See com.hazelcast.kubernetes.KubernetesClient
and com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategy