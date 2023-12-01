#!/bin/bash

npx cluster-admin general ssh-copy-id --all
npx cluster-admin general exec --all --command 'echo SSH works'

npx cluster-admin general gen-login -a

npx cluster-admin docker install --all
npx cluster-admin docker-compose install --all

npx cluster-admin general exec --all --command 'sudo ufw allow 1883,3000,5000,5601,7946,8000,8080,8086,8088,9000,9001,9090,9100,9200/tcp'
npx cluster-admin general exec --all --command 'sudo ufw allow 4789/udp'
