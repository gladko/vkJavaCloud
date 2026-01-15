https://github.com/Shopify/toxiproxy/pkgs/container/toxiproxy

## download image
`docker pull shopify/toxiproxy`

## run
`docker run -d --name toxiproxy -p 8474:8474 shopify/toxiproxy`
If using Toxiproxy from the host rather than other containers, enable host networking with `--net=host`.
`docker run -d --name toxiproxy --net=host shopify/toxiproxy`

ATTEMPT:  `docker run --rm --net host --entrypoint="/go/bin/toxiproxy-cli" -it shopify/toxiproxy \
create -l localhost:13306 -u localhost:3306 db-proxy`

## interact with Toxiproxy API
You can now interact with Toxiproxy via its HTTP API on localhost:8474.
You can also use the Toxiproxy CLI: `docker exec -it toxiproxy /go/bin/toxiproxy-cli ...`
or interactively: `docker exec -it toxiproxy sh` and then `/go/bin/toxiproxy-cli ...`

Create proxy: `./toxiproxy-cli create my-httpproxy --listen "0.0.0.0:8079" --upstream "localhost:8071"`
or `toxiproxy-cli create -l localhost:26379 -u localhost:6379 shopify_test_redis_master`

Now you can tamper with it through the Toxiproxy API or CLI
`toxiproxy-cli toxic add -t latency -a latency=1000 shopify_test_redis_master`

## get proxies via API
`curl http://localhost:8474/proxies`

### Endpoints
All endpoints are JSON.

    GET /proxies - List existing proxies and their toxics
    POST /proxies - Create a new proxy
    POST /populate - Create or replace a list of proxies
    GET /proxies/{proxy} - Show the proxy with all its active toxics
    POST /proxies/{proxy} - Update a proxy's fields
    DELETE /proxies/{proxy} - Delete an existing proxy
    GET /proxies/{proxy}/toxics - List active toxics
    POST /proxies/{proxy}/toxics - Create a new toxic
    GET /proxies/{proxy}/toxics/{toxic} - Get an active toxic's fields
    POST /proxies/{proxy}/toxics/{toxic} - Update an active toxic
    DELETE /proxies/{proxy}/toxics/{toxic} - Remove an active toxic
    POST /reset - Enable all proxies and remove all active toxics
    GET /version - Returns the server version number
    GET /metrics - Returns Prometheus-compatible metrics

## info
Toxics manipulate the way traffic passes through toxiproxy.
Toxics manipulate the pipe between the client and upstream. They can be added and removed from proxies using the HTTP API.
Each toxic has its own parameters to change how it affects the proxy links.
And include: latency, down, bandwidth, slow_close, timeout, slicer, limit_data

Adding a toxic uses the following syntax:
```bash
toxiproxy-cli add <proxyName> --type <toxicType> --toxicName <toxicName> \
--attribute <key=value> --upstream --downstream
```



So adding a latency toxic with random jitter looks like this
```bash
toxiproxy-cli toxic add tox_wordpress_mysql_db --type latency --toxicName myToxic --attribute latency=300 --attribute jitter=150
# or shorter:
# toxiproxy-cli toxic add tox_wordpress_mysql_db -t latency -n myToxic -a latency=300 -a jitter=150
Added downstream latency toxic 'myToxic' on proxy 'tox_wordpress_mysql_db'

$ toxiproxy-cli list
Name                    Listen          Upstream                Enabled         Toxics
======================================================================================
tox_wordpress_mysql_db  [::]:3306       mysql:3306              enabled         1

$ toxiproxy-cli inspect tox_wordpress_mysql_db
Name: tox_wordpress_mysql_db    Listen: [::]:3306       Upstream: mysql:3306
======================================================================
Upstream toxics:
Proxy has no Upstream toxics enabled.

Downstream toxics:
myToxic:        type=latency    stream=downstream       toxicity=1.00   attributes=[    jitter=150      latency=300     ]
```



## FULL scenario
```bash
docker run -d -p 8090:8080 k8s-hello
docker run -d --name toxiproxy --net=host shopify/toxiproxy
docker exec -it toxiproxy sh
# in toxiproxy-container run
/go/bin/toxiproxy-cli  create -l localhost:8066 -u localhost:8090 xxx
# in another terminal or in web browser
curl localhost:8066

/go/bin/toxiproxy-cli toxic add -t latency -a latency=3000 xxx
```



