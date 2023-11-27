#!/bin/sh
export MICROFAB_CONFIG='{
    "endorsing_organizations":[
        {
            "name": "org1"
        }
    ],
    "channels":[
        {
            "name": "demo-channel", 
            "endorsing_organizations":[
                "org1"
            ]
        }
    ]
}'
//export FABRIC_LOGGING_SPEC=DEBUG
export CORE_CHAINCODE_LOGGING_LEVEL=DEBUG

docker run -p 8080:8080 --rm -e MICROFAB_CONFIG -e FABRIC_LOGGING_SPEC -e CORE_CHAINCODE_LOGGING_LEVEL ibmcom/ibp-microfab
