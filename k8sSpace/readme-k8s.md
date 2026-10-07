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


# resolving k3s API endpoint
```bash
# 1. URL - exec and look for clusters.cluster.server field. Expected https://127.0.0.1:6443
kubectl config view --minify

# 2. AUTH  
# Extract your personal admin token directly from your .kube/config file using
kubectl config view --minify --raw -o jsonpath='{.users[0].user.token}'
# If that command returns blank because K3s is using client certificates instead of tokens, you can generate a temporary development token by
kubectl create clusterrolebinding default-admin-binding --clusterrole=cluster-admin --serviceaccount=default:default 2>/dev/null 
kubectl create token default
#or
kubectl -n kubernetes-dashboard create token admin-user --duration=24h

#3. test
curl -k -H "Authorization: Bearer $K3S_TOKEN" -H "Accept: application/json" -X GET \
     "$K3S_URL/apis/stable.example.com/v1"     
     
curl -k -H "Authorization: Bearer $K3S_TOKEN" -H "Accept: application/json" -X GET \
     "$K3S_URL/apis/stable.example.com/v1/namespaces/default/crontabs/nightly-backup-cron"
```

$K3S_URL / apis / stable.example.com / v1 / namespaces / default / crontabs / <RESOURCE_INSTANCE_NAME>
└──┬──┘          └───────┬────────┘   └┬┘             └────┬───┘   └───┬────┘
Cluster API            API Group    Version            Namespace    Resource Plural
(from K3s config)     (from your CRD)  (v1)             (default)    (from your CRD)

../gradlew :k8sSpace:k8s-main:importImageToK3s
kubectl apply -f k8s-main
kubectl get svc k8s-main
curl http://172.17.244.18:31591/testDiscoveryWatcher
kubectl logs -f deployment/k8s-main
