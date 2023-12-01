# Hyperledger Fabric Distributed Performance Measurement

Artifacts for measuring the performance of a Hyperledger Fabric network

## Usage

### Configuration

The configuration of the cloud servers (port, hostname, SSH password, IP address) must be set in a `node-config.yaml` file, for which there is a [sample file](measurement-setup/node-config.yaml) in this directory.

In this file you can define the nodes of the network (service labels are important, they can be used later to install services on the given node). It is also necessary to generate an ssh key, which must also be specified in the `node-config.yaml` for each node (or reuse the same key through the anchor configuration at the top).

### Initialization

The following commands install the cluster management tool and setup the cluster based on the provided configuration.

```
npm install
./setup-cluster.sh
```


### Create the Docker Swarm network

The following command creates a cluster with the manager node configured in the config, then joins the other nodes as worker nodes.

```
./create-swarm.sh
```

### Run the measurement

Once the cluster is ready, run the measurement via the following command:

```
./run-tpcc.sh
```

### Troubleshooting

1. The `./logs` folder contains the logs of the nodes. Usually, if the cluster admin command throws an error, a description of the error should be found here.
2. Portainer: the `./run-tpcc.sh` command deploys the Portainer service to the manager node. By opening the VM URL and port defined for it (9000 by default), logs are available through a browser in the Portainer dashboard for every service (after logging in with `admin` username and password defined in the [portainer configuration file](measurement-setup/benchmarks/swarm/tpcc/configs/portainer/default_password.txt)).
3. The `./setup-cluster.sh` command generates scripts for SSH login for every node (e.g., `ssh_node01.sh`), where further node-level investigation can be done (e.g., inspecting the Swarm cluster via the manager node).

