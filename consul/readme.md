## Run Consul in docker

[how to](https://hub.docker.com/_/consul)

Start `docker run -d -p 8500:8500 consul:1.15.4`
See web UI in localhost:8500

## Run several consul nodes
Start first: `docker run -d --name=dev-consul -e CONSUL_BIND_INTERFACE=eth0 -p 8500:8500 consul:1.15.4`
Get IP address: `docker exec -ti  a1d94165fca2 sh -c "ip address ls"`
Assuming that returned IP-address is `172.18.0.2` start second and third: `docker run -d -e CONSUL_BIND_INTERFACE=eth0 consul:1.15.4 agent -dev -join=172.18.0.2`
Get/check members: `docker exec -t dev-consul consul members`


## Java clients
See [Consul Clients](https://mvnrepository.com/open-source/consul-clients)
TOP:
https://spring.io/projects/spring-cloud-consul
https://github.com/Ecwid/consul-api
https://github.com/rickfast/consul-client  ARCHIVED
https://github.com/vert-x3/vertx-consul-client