

./gradlew  :etcdSpace:sandbox:buildDockerImage
./gradlew  :etcdSpace:sandbox:pushDockerImage
./gradlew  :etcdSpace:sandbox:importImageToK3s


## Launch vk-etcd and sandbox in docker with custom network:
```bash
#docker run -d --network vkcloud-manual-network -p 2379:2379 -p 2380:2380 vk-etcd
docker run -d --name vk-etcd --network vkcloud-manual-network \
 -p 2379:2379 -p 2380:2380 \
 localhost:5000/vk-etcd:latest

docker run --network vkcloud-manual-network \
  -p 7777:7777 \
  -e ETCD_ENDPOINT="http://vk-etcd:2379" \
  localhost:5000/etcd-sandbox:latest

# check connectivity
# docker exec -it serviceA ping serviceB
docker run --network vkcloud-manual-network -it alpine sh
# then 
ping vk-etcd
# dig ... 
docker exec <CONTAINER-NAME> ping vk-etcd

./gradlew  :etcdSpace:sandbox:startContainer
```

## Launch vk-etcd and sandbox in k8s
```bash
kubectl apply -f k8s
kubectl delete -f k8s

kubectl get pods -l app=vk-etcd
kubectl logs <pod-name>
```

## Launch etcd with docker:
Start simple
```bash
docker run -d -p 2379:2379 -p 2380:2380 \
  gcr.io/etcd-development/etcd:v3.6.7 /usr/local/bin/etcd \
  --listen-client-urls http://0.0.0.0:2379 \
  --advertise-client-urls http://0.0.0.0:2379
```

Start complex
```bash
ETCD_VER=v3.6.7

rm -rf /tmp/etcd-data.tmp && mkdir -p /tmp/etcd-data.tmp && \
  docker rmi gcr.io/etcd-development/etcd:${ETCD_VER} || true && \
  docker run \
  -p 2379:2379 \
  -p 2380:2380 \
  --mount type=bind,source=/tmp/etcd-data.tmp,destination=/etcd-data \
  --name etcd-gcr-${ETCD_VER} \
  gcr.io/etcd-development/etcd:${ETCD_VER} \
  /usr/local/bin/etcd \
  --name s1 \
  --data-dir /etcd-data \
  --listen-client-urls http://0.0.0.0:2379 \
  --advertise-client-urls http://0.0.0.0:2379 \
  --listen-peer-urls http://0.0.0.0:2380 \
  --initial-advertise-peer-urls http://0.0.0.0:2380 \
  --initial-cluster s1=http://0.0.0.0:2380 \
  --initial-cluster-token tkn \
  --initial-cluster-state new \
  --log-level info \
  --logger zap \
  --log-outputs stderr
```


## Java client
[jetcd](https://github.com/etcd-io/jetcd)
[jetcd-tests](https://github.com/etcd-io/jetcd/tree/main/jetcd-core/src/test/java/io/etcd/jetcd/impl)

## Examples
[Managing Cluster Membership with Etcd](https://dev.to/frosnerd/managing-cluster-membership-with-etcd-l0k)


## HTTP API
[API](https://etcd.io/docs/v3.4/dev-guide/api_grpc_gateway)
```bash
curl http://localhost:2379/version
curl -L http://localhost:2379/v3/kv/put   -X POST -d '{"key": "X", "value": "Y"}'
```
