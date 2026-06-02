
## Install docker image
[install](https://github.com/etcd-io/etcd/releases/tag/v3.6.7)


## Launch etcd on Windows:
```bash
ETCD_HOME=~/workspace/tools/etcd-v3.6.7-windows-amd64
exec $ETCD_HOME/etcd
# ~/workspace/tools/etcd-v3.6.7-windows-amd64/etcd
```
Then check...
From another terminal, use etcdctl to set a key:
```bash
./etcdctl put greeting "Hello, etcd"
OK
```
Then kill...
```bash
kill -9 $(ps | grep "/tools/etcd" | awk '{print $3}')
```

## Launch etcd with docker:
Start simple
```bash
docker run -d -p 2379:2379 -p 2380:2380 gcr.io/etcd-development/etcd:v3.6.7 /usr/local/bin/etcd \
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
Then check
```bash
docker exec etcd-gcr-${ETCD_VER} /usr/local/bin/etcd --version
docker exec etcd-gcr-${ETCD_VER} /usr/local/bin/etcdctl version
docker exec etcd-gcr-${ETCD_VER} /usr/local/bin/etcdutl version
docker exec etcd-gcr-${ETCD_VER} /usr/local/bin/etcdctl endpoint health
docker exec etcd-gcr-${ETCD_VER} /usr/local/bin/etcdctl put foo bar
docker exec etcd-gcr-${ETCD_VER} /usr/local/bin/etcdctl get foo
```



## RUN with toxiproxy
```bash
# 1. start etcd
docker run -d -p 2379:2379 -p 2380:2380 gcr.io/etcd-development/etcd:v3.6.7 /usr/local/bin/etcd \
  --listen-client-urls http://0.0.0.0:2379 \
  --advertise-client-urls http://0.0.0.0:2379
  
# 2. start toxiproxy
docker run -p 8474:8474 -d --name toxiproxy --net=host shopify/toxiproxy

# 3. configure toxiproxy
#   3.1 go into toxiproxy container  
docker exec -it toxiproxy sh
#   3.2 in toxiproxy-container create proxy rule
/go/bin/toxiproxy-cli  create -l localhost:12379 -u localhost:2379 etcd-proxy

# 4. in another terminal or in web browser validate that the rule works
curl http://localhost:12379/version

# 5. modify the proxy rule
/go/bin/toxiproxy-cli toxic add etcd-proxy -t latency -n myToxic -a latency=3000 
/go/bin/toxiproxy-cli toxic remove -n myToxic etcd-proxy

/go/bin/toxiproxy-cli delete etcd-proxy

# 6. stop and remove toxiproxy container
docker rm -f $(docker ps -q --filter ancestor=shopify/toxiproxy)
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
curl -L http://localhost:2379/v3/kv/put   -X POST -d '{"key": "Zm9v", "value": "YmFy"}'
```
