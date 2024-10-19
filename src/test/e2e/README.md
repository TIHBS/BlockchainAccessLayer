# E2E Integration Testing

## 1. Build Plugins

### Build Ethereum plugin:
- Clone from https://github.com/TIHBS/blockchain-access-layer-ethereum-plugin
- Build using `mvn package -DskipTests=true`
- Copy the resulting `jar` file to: `$USER_HOME/.bal/plugins`

### Build Fabric plugin:
- Clone from https://github.com/TIHBS/blockchain-access-layer-fabric-plugin
- Build using `mvn package -DskipTests=true`
- Copy the resulting `jar` file to: `$USER_HOME/.bal/plugins`

## 2. Start Blockchains

### Start Ethereum Ganache
- Use port 8545
- Use a high block gas limit
- Use mnemonic "smart contract composition"

### Start Fabric test-network


## 3. Deploy Smart Contract

### Deploy Ethereum Smart Contracts
This includes the resource manager and a sample 'Hotel Manager' smart contract that uses it.
- Clone from https://github.com/TIHBS/EthereumResourceManager
- Ensure to install truffle
- Run `truffle migrate` from the root directory of the cloned repo

### Deploy Fabric Smart Contracts

## 4. Configure SCIP Gateway
- Copy the file [connectionProfiles.json](./connectionProfiles.json) to `$USER_HOME/.bal`
- Copy the file [UTC--2019-05-30T11-21-08.970000000Z--90645dc507225d61cb81cf83e7470f5a6aa1215a](./UTC--2019-05-30T11-21-08.970000000Z--90645dc507225d61cb81cf83e7470f5a6aa1215a) to `$USER_HOME/.bal/ethereum`.
- Start the Gateway. It should automatically pickup the two plugins.

## 5. Useful Scripts

### Test Connectivity to Ethereum
`curl --location 'http://127.0.0.1:8080/configure/test?blockchain-id=eth-0'`

### Invoke Room Availability Function
```bash
curl --location 'http://localhost:8080?blockchain-id=eth-0&blockchain=ethereum&address=0xE39Cd8aE628c3AE5E463172060B031494056205a' \
--header 'Content-Type: application/json' \
--data '{
    "jsonrpc": "2.0",
    "method": "Invoke",
    "params": {
        "signature": {
            "name": "isRoomAvailable",
            "function": "true",
            "parameters": [
                {
                    "name": "txId",
                    "type": "{ \"type\": \"string\" }",
                    "value": ""
                }
            ]
        },
        "inputArguments": [
            {
                "name": "txId",
                "value": "tx1"
            }
        ],
        "outputParams": [],
        "callbackBinding": "json-rpc",
        "nonce": 10,
        "degreeOfConfidence": 99,
        "callbackUrl": "http://localhost:8080/submit-transaction/dummy",
        "timeout": 100000,
        "correlationId": "abc",
        "sideEffects": "true",
        "digitalSignature": ""
    },
    "id": 123
}'
```

### Query Room Availability Event

```bash
curl --location 'http://localhost:8080?blockchain-id=eth-0&blockchain=ethereum&address=0xE39Cd8aE628c3AE5E463172060B031494056205a' \
--header 'Content-Type: application/json' \
--data '{
    "jsonrpc": "2.0",
    "method": "Query",
    "params": {
        "signature": {
            "name": "Error",
            "function": "false",
            "parameters": [
                {
                    "name": "txId",
                    "type": "{ \"type\": \"string\" }",
                    "value": ""
                },
                {
                    "name": "msg",
                    "type": "{ \"type\": \"string\" }",
                    "value": ""
                }
            ]
        }
    },
    "id": 123
}'
```