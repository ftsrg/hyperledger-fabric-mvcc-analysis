#!/bin/bash
npx cluster-admin general exec --nodes peer0fi peer0cb peer1fi peer1cb peer2fi peer2cb peer3fi peer3cb --command "sudo docker volume rm test_inmemory"
