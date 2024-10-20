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

### SCIP methods
```bash
curl --location 'http://localhost:8080?blockchain-id=eth-0&blockchain=ethereum&address=0xD6de246d347982F64b809E01Fb9511e39506eF64' \
--header 'Content-Type: application/json' \
--data '{
    "jsonrpc": "2.0",
    "method": "Invoke",
    "params": {
        "signature": {
            "name": "sendCoin",
            "function": "true",
            "parameters": []
        },
        "inputArguments": [],
        "outputParams": [],
        "callbackBinding": "json-rpc",
        "nonce": 11,
        "degreeOfConfidence": 99,
        "callbackUrl": "http://localhost:8080/submit-transaction/dummy",
        "timeout": 100000,
        "correlationId": "abcde",
        "sideEffects": "true",
        "digitalSignature": ""
    },
    "id": 123
}'
```

```bash
curl --location 'http://localhost:8080?blockchain-id=eth-0&blockchain=ethereum&address=0xD6de246d347982F64b809E01Fb9511e39506eF64' \
--header 'Content-Type: application/json' \
--data '{
    "jsonrpc": "2.0",
    "method": "Query",
    "params": {
        "signature": {
            "name": "Transfer",
            "function": "false",
            "parameters": [
                {
                    "name": "msg",
                    "type": "{ \"type\": \"boolean\" }",
                    "value": ""
                }
            ]
        }
    },
    "id": 123
}'
```

```bash
curl --location 'http://localhost:8080?blockchain-id=eth-0&blockchain=ethereum&address=0xD6de246d347982F64b809E01Fb9511e39506eF64' \
--header 'Content-Type: application/json' \
--data '{
    "jsonrpc": "2.0",
    "method": "Subscribe",
    "params": {
        "signature": {
            "name": "Transfer",
            "function": "false",
            "parameters": [
                {
                    "name": "msg",
                    "type": "{ \"type\": \"boolean\" }",
                    "value": ""
                }
            ]
        },
        "callbackBinding": "json-rpc",
        "callbackUrl": "http://localhost:8080/submit-transaction/dummy",
        "correlationId": "abcdefg"
    },
    "id": 123
}'
```

```bash
curl --location 'http://localhost:8080?blockchain-id=eth-0&blockchain=ethereum&address=0x182761AC584C0016Cdb3f5c59e0242EF9834fef0' \
--header 'Content-Type: application/json' \
--data '{
    "jsonrpc": "2.0",
    "method": "SendTx",
    "params": {
        "callbackBinding": "json-rpc",
        "nonce": 12,
        "degreeOfConfidence": 99,
        "callbackUrl": "http://localhost:8080/submit-transaction/dummy",
        "value": 5000,
        "timeout": 100000,
        "correlationId": "abcdefg",
        "digitalSignature": ""
    },
    "id": 123
}'
```