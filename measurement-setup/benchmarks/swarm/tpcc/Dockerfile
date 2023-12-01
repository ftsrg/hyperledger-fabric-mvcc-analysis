FROM node:10.16-alpine

# Install packages for dependency compilation
RUN apk add --no-cache python g++ make git

# execute as the "node" user, created in the base image
USER node:node
WORKDIR /hyperledger/caliper/workspace

# 1 & 2. change the NPM global install directory
# https://docs.npmjs.com/resolving-eacces-permissions-errors-when-installing-packages-globally#manually-change-npms-default-directory
# 3. install Caliper globally
RUN mkdir /home/node/.npm-global \
    && npm config set prefix '/home/node/.npm-global' \
    && npm install -g --only=prod @hyperledger/caliper-cli@0.3.0

ENV PATH /home/node/.npm-global/bin:$PATH
ENV CALIPER_WORKSPACE /hyperledger/caliper/workspace
ENV CALIPER_BIND_ARGS -g

RUN caliper bind --caliper-bind-sut fabric:1.4.4

ENTRYPOINT ["caliper"]
CMD ["--version"]