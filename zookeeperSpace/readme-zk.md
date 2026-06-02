## ports
zk-client       2181
hello-service   18000
foo-service     18100


## Start zookeeper
```bash
~/workspace/tools/apache-zookeeper-3.8.4-bin/bin/zkServer.sh start
```

## Start zookeeper docker image
```bash
docker run -p 2181:2181 -p 2180:8080 -d zookeeper 
```
This image includes EXPOSE 2181 2888 3888 8080 (the zookeeper client port, follower port, election port, 
AdminServer port respectively), so standard container linking will make it automatically available to the linked 
containers.


CLI `./workspace/tools/apache-zookeeper-3.8.4-bin/bin/zkCli.sh -server 172.17.244.18:2181`

Spring registry: org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry


## x-discovery problems:
no way to subscribe for node updates. Only explicit `queryForInstances` is possible.