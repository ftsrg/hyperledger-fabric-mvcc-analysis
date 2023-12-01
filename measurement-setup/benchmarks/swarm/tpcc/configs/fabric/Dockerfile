FROM hyperledger/fabric-tools:2.4.2 AS crypto-build-stage

WORKDIR /usr/src/fabric
COPY . /usr/src/fabric/
RUN ./generate.sh

FROM node:10-alpine AS wallet-build-stage

WORKDIR /usr/src/converter
RUN apk --no-cache add alpine-sdk python3

COPY --chown=node:node ./cryptogen-to-wallet/package.json ./cryptogen-to-wallet/package-lock.json /usr/src/converter/
RUN npm ci --only=production

COPY --chown=node:node ./cryptogen-to-wallet/ /usr/src/converter/
COPY --from=crypto-build-stage /usr/src/fabric/crypto-config /usr/src/converter/crypto-config

RUN node convert.js ./crypto-config centralbank.example.com CentralBankOrgMSP ./wallet
RUN node convert.js ./crypto-config fi.example.com FIOrgMSP ./wallet


FROM scratch AS export-stage
COPY --from=crypto-build-stage /usr/src/fabric/crypto-config /crypto-config
COPY --from=crypto-build-stage /usr/src/fabric/notls /notls
COPY --from=crypto-build-stage /usr/src/fabric/tls /tls
COPY --from=wallet-build-stage /usr/src/converter/wallet /wallet