package blockchains.iaas.uni.stuttgart.demo; /*******************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

import java.util.HashMap;
import java.util.Map;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.BlockchainNodeUnreachableException;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;

// todo make thread-safe
public class GatewayManager {
    private static GatewayManager instance;
    private Map<String, Gateway> gateways;
    // todo make the key include blockchain-id as well
    private Map<String, Network> channels;
    // todo make the key include blockchaon-id and channel name as well
    private Map<String, Contract> contracts;

    private GatewayManager() {
        gateways = new HashMap<>();
        channels = new HashMap<>();
        contracts = new HashMap<>();

//        ConnectionProfilesManager.getInstance().setListener(() -> {
//            gateways.values().forEach(Gateway::close);
//            gateways.clear();
//            channels.clear();
//            contracts.clear();
//        });
    }

    public Gateway getGateway(String blockchainId) throws BlockchainNodeUnreachableException {
        if (gateways.containsKey(blockchainId)) {
            return gateways.get(blockchainId);
        }
        // TODO: Fix code below
//        AbstractConnectionProfile profile = ConnectionProfilesManager.getInstance().getConnectionProfiles().get(blockchainId);
//
//        if (!(profile instanceof blockchains.iaas.uni.stuttgart.demo.FabricConnectionProfile)) {
//            throw new InvalidScipParameterException();
//        }
//        final String walletPath = ((blockchains.iaas.uni.stuttgart.demo.FabricConnectionProfile) profile).getWalletPath();
//        final String networkConfigPath = ((blockchains.iaas.uni.stuttgart.demo.FabricConnectionProfile) profile).getConnectionProfilePath();
//        final String user = ((blockchains.iaas.uni.stuttgart.demo.FabricConnectionProfile) profile).getUserName();
//        // Load an existing wallet holding identities used to access the network.
//        Path walletDirectory = Paths.get(walletPath);
//
//        Wallet wallet = null;
//        try {
//            wallet = Wallet.createFileSystemWallet(walletDirectory);
//
//            // Path to a connection profile describing the network.
//            Path networkConfigFile = Paths.get(networkConfigPath);
//
//            // Configure the gateway connection used to access the network.
//            Gateway result = Gateway.createBuilder()
//                    .identity(wallet, user)
//                    .networkConfig(networkConfigFile)
//                    .connect();
//            gateways.put(blockchainId, result);
//
//            return result;
//        } catch (IOException e) {
//            throw new BlockchainNodeUnreachableException("Cannot create Fabric gateway. Reason: " + e.getMessage());
//        }
        throw new BlockchainNodeUnreachableException("Feature not yet implemented");
    }

    public Network getChannel(String blockchainId, String channelName) {
        if (channels.containsKey(channelName)) {
            return channels.get(channelName);
        }

        Network channel = this.getGateway(blockchainId).getNetwork(channelName);
        channels.put(channelName, channel);

        return channel;
    }

    public Contract getContract(String blockchainId, String channelName, String chaincodeName) {
        if (contracts.containsKey(chaincodeName)) {
            return contracts.get(chaincodeName);
        }

        Contract contract = this.getChannel(blockchainId, channelName).getContract(chaincodeName);
        contracts.put(chaincodeName, contract);

        return contract;
    }

    public static GatewayManager getInstance() {
        if (instance == null) {
            instance = new GatewayManager();
        }

        return instance;
    }
}
