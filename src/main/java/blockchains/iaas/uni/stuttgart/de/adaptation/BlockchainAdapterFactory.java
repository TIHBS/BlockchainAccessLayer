/********************************************************************************
 * Copyright (c) 2019-2022 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 * Co-author: Akshay Patel
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.adaptation;

import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtension;
import blockchains.iaas.uni.stuttgart.de.config.ObjectMapperProvider;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Context;
import java.util.List;
import java.util.Map;

public class BlockchainAdapterFactory {

    @Context
    ObjectMapperProvider objectMapperProvider;

    public BlockchainAdapterFactory() {

    }

    private static final Logger log = LoggerFactory.getLogger(BlockchainAdapterFactory.class);

    public BlockchainAdapter createBlockchainAdapter(Map<String, Object> connectionProfile, String blockchainId) throws Exception {

        try {
            return createAdapter((String) connectionProfile.get("@type"), connectionProfile);
        } catch (Exception e) {
            final String msg = String.format("Error while creating a blockchain adapter for. Details: %s", e.getMessage());
            log.error(msg);
            throw new Exception(msg, e);
        }
    }

//    private EthereumAdapter createEthereumAdapter(EthereumConnectionProfile gateway) throws IOException, CipherException {
//        final EthereumAdapter result = new EthereumAdapter(gateway.getNodeUrl(), gateway.getPollingTimeSeconds());
//        result.setCredentials(gateway.getKeystorePassword(), gateway.getKeystorePath());
//        final PoWConfidenceCalculator cCalc = new PoWConfidenceCalculator();
//        cCalc.setAdversaryRatio(gateway.getAdversaryVotingRatio());
//        result.setConfidenceCalculator(cCalc);
//
//        return result;
//    }

//    private BitcoinAdapter createBitcoinAdapter(BitcoinConnectionProfile gateway) throws BitcoindException, CommunicationException {
//        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
//        final CloseableHttpClient httpProvider = HttpClients.custom().setConnectionManager(connManager).build();
//        final BtcdClient client = new BtcdClientImpl(httpProvider, gateway.getAsProperties());
//        final BtcdDaemon daemon = new BtcdDaemonImpl(client);
//        final BitcoinAdapter result = new BitcoinAdapter(client, daemon);
//        final PoWConfidenceCalculator cCalc = new PoWConfidenceCalculator();
//        cCalc.setAdversaryRatio(gateway.getAdversaryVotingRatio());
//        result.setConfidenceCalculator(cCalc);
//
//        return result;
//    }
//
//    private FabricAdapter createFabricAdapter(FabricConnectionProfile gateway, String blockchainId) {
//        return FabricAdapter.builder()
//                .blockchainId(blockchainId)
//                .build();
//    }

    private BlockchainAdapter createAdapter(String blockchainType, Map<String, Object> parameters) {
        List<IAdapterExtension> adapterExtensions = BlockchainPluginManager.getInstance().getExtensions();
        for (IAdapterExtension adapterExtension : adapterExtensions) {
            if (adapterExtension.getBlockChainId().equals(blockchainType)) {
                // adapterExtension.registerConnectionProfile(objectMapperProvider.getContext());
                return adapterExtension.getAdapter(parameters);
            }
        }
        System.err.println("No extension for blockchain-id: " + blockchainType);
        return null;
    }

}
