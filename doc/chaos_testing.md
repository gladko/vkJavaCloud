See [toxiproxy.md](toxiproxy.md)

## Chaos testing scenarios
Assuming node registry TTL = 30 sec
### Scenario_1: start service registry after service node
Assuming node registry TTL = 30 sec
1. Start node
2. Start service registry after 10, 30, 60 sec
3. Check if node registry exists

### Scenario_2: restart service registry
Assuming node registry TTL = 30 sec
1. Start service registry
2. Start node
3. Check if node is registered
4. Stop service registry
5. Start service registry after 10, 30, 60 sec
6. Check if node registry exists
7. Check if node registry exists after 60 sec

### Scenario_3: network issue
1. Start service registry
2. Start service toxiproxy
3. Start node, register it through toxiproxy
4. Check if node is registered
5. Simulate disconnect with toxiproxy
6. Restore connectivity after 10, 30, 60 sec
7. Check if node registry exists
8. Check if node registry exists after 60 sec
