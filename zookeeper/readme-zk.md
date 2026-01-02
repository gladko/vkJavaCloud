default values (2000ms tickTime, iniLimit = 10 and syncLimit = 5)

Zookeeper server exposes minSessionTimeout and maxSessionTimeout configurations (in milliseconds) in zoo.cfg. 
During session establishment, a timeout is negotiated between client and server. The default minimum value of this 
timeout is 2 * tickTime, and max. is 20 * tickTime. So, with all default server settings, you can get a session timeout 
anywhere between 4 and 40 seconds.

# CuratorFramework client init
CuratorFramework.start() neither stuck nor fail if zk is down.

ZK stores data on disk and apply it when restarts.