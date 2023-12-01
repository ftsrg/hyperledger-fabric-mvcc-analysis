# Hyperledger Fabric: MVCC Analysis

This repository is the collection of artifacts (smart contract and workload implementations, measurement setup, data analysis) relating to the MVCC analysis of the Hyperledger Fabric platform.

## Users' Guide

Please refer to the documentation of the available smart contracts and workloads on how to use them.

## Developers' Guide

The preferred way of contribution is: 
1. Fork the repository;
2. Apply your changes;
3. Submit your changes for review and merging in the form of a pull request.

## Smart Contracts

The repository contains the following micro-benchmark smart contract implementations:

* [Unpartitioned data schema-based](chaincodes/partitioned/README.md) for Hyperledger Fabric v2.x - Java
* [Partitioned data schema-based](chaincodes/unpartitioned/README.md) for Hyperledger Fabric v2.x - Java

## Workloads

The repository contains the following workload implementations for the analysis:
* [Hyperledger Caliper](measurement-setup/benchmarks/swarm/tpcc/configs/caliper)

## Measurements

Every service artifact of the measurement can be found in the [measurement-setup](measurement-setup/README.md) directory.

## Data analysis

The artifacts in the [data-analysis](data-analysis/README.md) directory can be used to reproduce the article figures based on the measurement results.

## Reference Format

TBD.

## Acknowledgement

The work of the authors was partially supported by the Cooperation Agreement between the Hungarian National Bank (MNB) and the Budapest University of Technology and Economics (BME) in the Digitisation, artificial intelligence and data age workgroup.

## License

The project uses the Apache License Version 2.0. For more information see [NOTICES.md](NOTICES.md), [CONTRIBUTORS.md](CONTRIBUTORS.md), and [LICENSE](LICENSE).
