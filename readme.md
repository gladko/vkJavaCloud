# Personal playground for studying cloud related technologies. First of all, related with service registry/discovery.

## 3rd-party projects that use service registry
- [Zookeeper in Kafka](https://github.com/AutoMQ/automq/wiki/What-is-the-Zookeeper-in-Kafka-All-You-Need-to-Know)
- [Hazelcast Cloud Discovery](https://docs.hazelcast.com/hazelcast/5.4/plugins/cloud-discovery)
- https://github.com/hazelcast/hazelcast-eureka
- https://github.com/hazelcast/hazelcast-zookeeper
- [Zookeeper in ignite](https://github.com/apache/ignite/blob/master/modules/zookeeper/src/main/java/org/apache/ignite/spi/discovery/zk/ZookeeperDiscoverySpi.java)


https://github.com/spring-cloud/spring-cloud-kubernetes

## Current state
|              | consul                                                     | etcd | eureka | zookeeper | k8s  |
|--------------|------------------------------------------------------------| ---- | ----- | --------- |------|
| build / test | works                                                      |  |  |  |  |
| runscenario  | works                                                        |  |  |  |  |
| docker       | works                                                        |  |  |  |  |
| compose      | half: only one instance of translate service is registered |  |  |  |  |
| k8s          | so so                                                      |  |  |  |  |