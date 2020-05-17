### Instance

#### Manual setup

Sample docker-compose file:
```bash
docker-compose -f master-2-nodes-sample-docker-compose.yml up
```

Sample setup request:
```json
{
  "nodes": [
    {
      "address": "localhost",
      "agentPort": "10050",
      "cpu": 4,
      "memoryInGb": 2,
      "name": "new-master",
      "port": "10000",
      "type": "master",
      "uiPort": "10030"
    },
    {
      "address": "localhost",
      "agentPort": "10051",
      "cpu": 4,
      "memoryInGb": 2,
      "name": "new-worker-1",
      "port": "10001",
      "type": "worker",
      "uiPort": "10031"
    },
    {
      "address": "localhost",
      "agentPort": "10052",
      "cpu": 4,
      "memoryInGb": 2,
      "name": "new-worker-2",
      "port": "10002",
      "type": "worker",
      "uiPort": "10032"
    }
  ]
}
```
