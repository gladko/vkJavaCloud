
## Install docker image
[install](https://github.com/etcd-io/etcd/releases/tag/v3.6.7)

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

## Launch etcd on Windows:
```bash
$ ./etcd
```

From another terminal, use etcdctl to set a key:
```bash
$ ./etcdctl put greeting "Hello, etcd"
OK
```

From the same terminal, retrieve the key:
```bash
$ ./etcdctl get greeting
greeting
Hello, etcd
```

## Java client
[api](https://github.com/etcd-io/jetcd)
[api-tests](https://github.com/etcd-io/jetcd/tree/main/jetcd-core/src/test/java/io/etcd/jetcd/impl)
[Managing Cluster Membership with Etcd](https://dev.to/frosnerd/managing-cluster-membership-with-etcd-l0k)
