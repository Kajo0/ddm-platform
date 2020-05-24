#!/bin/bash

# TODO parametrized via ENV variable
java -jar -Xmx1G -Dspring.profiles.active=nodemaster,master /apps/node-agent.jar &

bash /master.sh
