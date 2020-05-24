#!/bin/bash

# TODO parametrized via ENV variable
java -jar -Xmx1G -Dspring.profiles.active=nodemaster,worker /apps/node-agent.jar &

bash /worker.sh
