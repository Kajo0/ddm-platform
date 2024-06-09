# Local setup

Coordinator app by default works with profile `instance-local-docker` using local docker creation for workers

To run app use build and run command `./gradlew bootJar; java -jar ./build/libs/platform-coordinator-0.0.1-SNAPSHOT.jar`
or use gradle wrapper to run `./gradlew bootRun`. Second option is preferred due to current classloader issues.

Due to issue with `@PreDestroy` method when killing app run using `bootRun` command, it is necessary to clear existing
instances using before killing app or kill using `kill {pid}` from other shell.

First option using `.jar` has its issues with provided dependencies when evaluating uploaded `.jar`'s so the best option
for testing is to run the app using IDE as it handles dependencies and `@PreDestroy` method correctly.

Currently, every uploaded or generated after running Coordinator app is located in system temporary path e.g. for unix
like systems: `/tmp/coordinator` but it can be changed
in [`application.yml`](./src/main/resources/application.yml) `paths.root` property

## Build sample artifacts

To build all sample artifacts it is required to set up commands to switch java between `8` and `12`
in [`../common-func.sh`](../common-func.sh) file.

Then execute shell script to build all artifacts `cd scripts; ./buildAndCopySamples.sh`

## Manual instance setup

Run Coordinator app with `instance-manual-setup` profile instead of `instance-local-docker` defined as default
in [`application.yml`](./src/main/resources/application.yml)

Run workers, e.g. using sample docker-compose file:

```bash
docker-compose -f ./docker/master-2-nodes-sample-docker-compose.yml up -d
```

Send instance setup to the Coordinator app:

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

e.g. using cURL:

```
curl -X POST --location "http://localhost:7000/coordinator/command/instance/setup/manual" \
    -H "Content-Type: application/json" \
    -d '{
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
        }'
```

# TODO

- All TODO and FIXME left in the code
- Change returned `ok_process-id` string into real future API
- Handle evaluating uploaded dependencies running Coordinator app as fat-jar
