# Blockchain Access Layer (BAL) - SCIP Gateway

BAL is an extensible abstraction layer that allows client applications to access permissioned and permissionless
blockchains using a uniform interface. BAL is designed to support business process management systems to access
blockchains using
the [Blockchain Modeling and Execution (BlockME) method](https://link.springer.com/article/10.1007/s00450-019-00399-5).
It also implements the [Smart Contract Invocation Protocol (SCIP)](https://github.com/lampajr/scip) as a gateway.

BAL is a Java 8 web application that uses Jersey to expose a [RESTful API](#restful-api) and
a [JSON-RPC API](#json-rpc-api).

## Configuration

BAL allows simultaneous access to multiple blockchain systems. Currently, [Ethereum](https://ethereum.org/)
, [Bitcoin](https://bitcoin.org/), and [Hyperledger Fabric](https://www.hyperledger.org/projects/fabric) are supported.

- To access the Ethereum blockchain, BAL needs to be able to communicate with
  a [geth node](https://github.com/ethereum/go-ethereum)
  which has RPC connections enabled. Furthermore, BAL directly accesses the keystore file holding the private key of an
  Ethereum account used for sending and receiving transactions.
- To access the Bitcoin blockchain, BAL needs to be able to communicate with
  a [bitcoind node](https://bitcoin.org/en/bitcoin-core/)
  which has RPC connections enabled.
- To access a given Hyperledger Fabric network, it needs to have access to a wallet file with authorized users, and an
  appropriate [connection profile](https://hyperledger-fabric.readthedocs.io/en/latest/developapps/connectionprofile.html)
  .

### Configuring Access to Multiple Blockchains

BAL expectes a configuration file with the name `connectionProfiles.json` inside the path `[User Folder]/.bal/`. An
example for this file that accesses a geth node, a bitcoind node and a Hyperledger Fabric network is:

```[json]
{
  "eth-0": {
    "@type": "ethereum",
    "nodeUrl":"http://localhost:7545",
    "keystorePath":"C:\\Ethereum\\keystore\\UTC--2019-05-30T11-21-08.970000000Z--90645dc507225d61cb81cf83e7470f5a6aa1215a.json",
    "keystorePassword":"123456789",
    "adversaryVotingRatio": 0.2,
    "pollingTimeSeconds": 2
  },
  "btc-0" : {
    "@type": "bitcoin",
    "rpcProtocol": "http",
    "rpcHost": "129.69.214.211",
    "rpcPort": "8332",
    "rpcUser": "falazigb",
    "rpcPassword": "123456789",
    "httpAuthScheme": "Basic",
    "notificationAlertPort": "5158",
    "notificationBlockPort": "5159",
    "notificationWalletPort": "5160",
    "adversaryVotingRatio": "0.1"
  },
  "fabric-0" : {
    "@type": "fabric",
    "walletPath": "C:\\Users\\falazigb\\Documents\\GitHub\\fabric\\fabric-samples\\emc\\javascript\\wallet",
    "userName": "user1",
    "connectionProfilePath": "C:\\Users\\falazigb\\Documents\\GitHub\\fabric\\fabric-samples\\first-network\\connection-org1.json"
  }
}
```

## Building and Deployment

After cloning, you can build the project and package it into a WAR file using the following command:

```
mvn install
```

Then, the WAR file (which can be found in the folder 'target' generated after a successful build) can be deployed on an
Apache Tomcat server.

Required VM options while running

- `pf4j.pluginsDir` path where the plugins will be stored

## Plugin management

The project uses pf4j framework for managing the plugins. The application exposes RESTful APIs to:

- Upload the plugins as jar
- Remove plugins
- Get list of plugins with status
- Start plugin
- Disable plugin

### Supported plugins

- [Ethereum](https://github.com/TIHBS/blockchain-access-layer-ethereum-plugin)
- [Bitcoin](https://github.com/TIHBS/blockchain-access-layer-bitcoin-plugin)
- [Fabric](https://github.com/TIHBS/blockchain-access-layer-fabric-plugin)

One can create their own plugin. A plugin should:

- Use the core [api](https://github.com/TIHBS/blockchain-access-layer-api).
- Implement [IAdapterExtension](https://github.com/TIHBS/blockchain-access-layer-api/blob/main/src/main/java/blockchains/iaas/uni/stuttgart/de/api/IAdapterExtenstion.java) interface.
- Define a [Plugin](https://pf4j.org/doc/plugins.html).
- Provide a class that   extends [AbstractConnectionProfile](https://github.com/TIHBS/blockchain-access-layer-api/blob/main/src/main/java/blockchains/iaas/uni/stuttgart/de/api/connectionprofiles/AbstractConnectionProfile.java)

## Accessing the APIs

### RESTful API

The application exposes an asynchronous RESTful API to subscribe and unsubscribe from the provided operations.

**To summarize:**
The RESTful api provides the following resources/methods:

* A POST method is provided for each of the following paths to create the corresponding subscription:

```
{application-URL}/webapi/submit-transaction
{application-URL}/webapi/receive-transaction
{application-URL}/webapi/receive-transactions
{application-URL}/webapi/detect-orphaned-transaction
{application-URL}/webapi/ensure-transaction-state
{application-URL}/webapi/invoke-smart-contract-function
```

* A GET method is also provided for the aforementioned URLs that lists the currently active subscriptions.

* A DELETE method is provided in each of the following paths to manually delete the corresponding subscription:

```
{application-URL}/webapi/submit-transaction/{subscription-id}
{application-URL}/webapi/receive-transaction/{subscription-id}
{application-URL}/webapi/receive-transactions/{subscription-id}
{application-URL}/webapi/detect-orphaned-transaction/{subscription-id}
{application-URL}/webapi/ensure-transaction-state/{subscription-id}
{application-URL}/webapi/invoke-smart-contract-function
```

#### Plugin management RESTful apis:

In the current implementation, the plugin management apis do not require authentication.

##### **POST** `/webapi/plugins/`

- Example

```bash
curl --location --request POST '{application-URL}/webapi/plugins/' \
--form 'file=@"<path>/blockchain-access-layer-ethereum-plugin-1.0.0.jar"'
```

##### **GET** `/webapi/plugins/`

- Example

```bash
curl --location --request GET '{application-URL}/webapi/plugins/'
```

##### **DELETE** `/webapi/plugins/{plugin-id}`

- Example

```bash
curl --location --request DELETE '{application-URL}/webapi/plugins/ethereum-plugin'
```

##### **POST** `/webapi/plugins/{plugin-id}/start`

- Example

```bash
curl --location --request POST '{application-URL}/webapi/plugins/ethereum-plugin/start'
```

##### **POST** `/webapi/plugin-manager/{plugin-id}/disable`

- Example

```bash
curl --location --request POST '{application-URL}/webapi/plugins/ethereum-plugin/disable'
```

##### **POST** `/webapi/plugin-manager/{plugin-id}/unload`

- Example

```bash
curl --location --request POST '{application-URL}/webapi/plugins/ethereum-plugin/unload'
```

##### **POST** `/webapi/plugin-manager/{plugin-id}/enable`

- Example

```bash
curl --location --request POST '{application-URL}/webapi/plugins/ethereum-plugin/enable'
```

### JSON-RPC API

BAL implements the [JSON-RPC binding](https://github.com/lampajr/scip#json-rpc-binding) described in the SCIP
specifications. It can be accessed with any
standard [JSON-RPC client](https://www.jsonrpc.org/archive_json-rpc.org/implementations.html).

## Setting Up Various Blockchains for Testing

BAL needs to have access to a node for each blockchain instance it needs to communicate with. These nodes can be already
running nodes that you have access to. Otherwise, you need to setup and manage your own nodes. Below, are basic
instructions how to setup Ethereum, Bitcoin, and Hyperledger Fabric nodes.

## Case Studies

### BlockME Case Study

[Blockchain Modeling Extension (BlockME)](https://link.springer.com/article/10.1007/s00450-019-00399-5) is an extension
to BPMN 2.0 that allows business processes to communicate with heterogeneous blockchains. The case study invloves a
cryptocurrency exchange service utilitzing the blockchain access layer. The exchange uses the following simplified
BlockME-model:

![](app/src/main/resources/images/original-model.png)

Please follow these instructions:

1. Configure and run a local geth node (see above).
2. Configure and run a local bitcoind node (see above).
3. Configure the blockchain access layer to communicate with these nodes (see the
   file [gatewayConfiguration.json](src/main/resources/gatewayConfiguration.json)).
4. Build and deploy the blockchain access layer (see above).
5. Configure, build, deploy and initiate the process
   model ([see this Github repository for instructions](https://github.com/ghareeb-falazi/BlockME-UseCase))
6. Send ethers to the address maintained by the blockchain access layer (the first address of the keyfile mentioned in
   step 3).
7. Monitor the Tomcat server logs for both applications to see the progress. You can also use the Camunda Cockpit
   application (installed as part of step 4) to monitor the current state of instances of deployed process models.

The following series of screenshots show a sample execution of the case study:

1. Initiating the process instance:
   ![](app/src/main/resources/images/start.png)

2. Setting the source, and target addresses (exchange request parameters):
   ![](app/src/main/resources/images/input-params.png)

3. Sending a transaction to the address of the crypto-exchange using the Ethereum Wallet application:
   ![](app/src/main/resources/images/send-transaction-form.png)

4. While waiting for the resulting Bitcoin transaction sent to the client to receive 1 confirmation, the business
   process instance looks as follows:
   ![](app/src/main/resources/images/waiting-for-bitcoin-tx.png)

5. The log records produced by the process instance. The final message in the log shows the id of the transaction the
   exchange sent to the client.
   ![](app/src/main/resources/images/log.png)

6. [BlockCypher](https://live.blockcypher.com/btc-testnet/) can be used to explore Bitcoin testnet3 (and other)
   blockchains. The following screenshot represents the result of querying the transaction id reported in the previous
   step:
   ![](app/src/main/resources/images/blockcypher.png)
   You can find the details about the resulting testnet3 Bitcoin
   transaction [here](https://live.blockcypher.com/btc-testnet/tx/347d8f2bc8dbc7cf62d8313f66d2ae930c9e92632fb5a2cfb2507caaaffa7f71/)
   .

When we performed this sample execution, the setup was as follows:

* a _geth_ node is running on a virtual machine in a VSphere accessible from the local network.
* a _bitcoind_ (Bitcoin Core) node is running on a virtual machine in a VSphere accessible from the local network.
* The blockchain access layer is running in a local Tomcat server listening to port 8081
* The camunda engine is running in a local Tomcat server listening to port 8080

### SCIP Case Studies

Two case studies that demonstrate the usage of the BAL as a SCIP gateway can be
found [here](https://github.com/ghareeb-falazi/SCIP-CaseStudy)
and [here](https://github.com/ghareeb-falazi/SCIP-CaseStudy-2).
