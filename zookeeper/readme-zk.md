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

ZK Admin: http://localhost:2180/commands


## Expiration
default values (2000ms tickTime, iniLimit = 10 and syncLimit = 5)

Zookeeper server exposes minSessionTimeout and maxSessionTimeout configurations (in milliseconds) in zoo.cfg. 
During session establishment, a timeout is negotiated between client and server. The default minimum value of this 
timeout is 2 * tickTime, and max. is 20 * tickTime. So, with all default server settings, you can get a session timeout 
anywhere between 4 and 40 seconds.

# CuratorFramework client init
CuratorFramework.start() neither stuck nor fail if zk is down.

ZK stores data on disk and apply it when restarts.