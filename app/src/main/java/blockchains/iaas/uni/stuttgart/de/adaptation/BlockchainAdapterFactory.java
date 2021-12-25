/********************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.adaptation;

import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtenstion;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.AbstractConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.profiles.BitcoinConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.profiles.EthereumConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.profiles.FabricConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.adaptation.adapters.bitcoin.BitcoinAdapter;
import blockchains.iaas.uni.stuttgart.de.adaptation.adapters.fabric.FabricAdapter;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.api.utils.PoWConfidenceCalculator;
import com.neemre.btcdcli4j.core.BitcoindException;
import com.neemre.btcdcli4j.core.CommunicationException;
import com.neemre.btcdcli4j.core.client.BtcdClient;
import com.neemre.btcdcli4j.core.client.BtcdClientImpl;
import com.neemre.btcdcli4j.daemon.BtcdDaemon;
import com.neemre.btcdcli4j.daemon.BtcdDaemonImpl;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.pf4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockchainAdapterFactory {
    String pluginPath = "/home/ash/software/apache-tomcat-8.5.73/plugins";

    PluginManager pluginManager = new DefaultPluginManager(Paths.get(pluginPath)) {
        //
        @Override
        protected PluginLoader createPluginLoader() {
            // load only jar plugins
            return new JarPluginLoader(this);
        }

        @Override
        protected PluginDescriptorFinder createPluginDescriptorFinder() {
            // read plugin descriptor from jar's manifest
            return new ManifestPluginDescriptorFinder();
        }

    };

    public BlockchainAdapterFactory() {
        // TODO: Create plugin management rest api
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
    }


    private static final Logger log = LoggerFactory.getLogger(BlockchainAdapterFactory.class);

    public BlockchainAdapter createBlockchainAdapter(AbstractConnectionProfile connectionProfile, String blockchainId) throws Exception {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("nodeUrl", "http://localhost:7545");
        parameters.put("type", "ethereum");
        parameters.put("averageBlockTimeSeconds", "2");

        try {
            if (connectionProfile instanceof EthereumConnectionProfile) {
                System.out.println("--------------------------------------------");
                return createAdapter(parameters.get("type"), parameters);
                //     return createEthereumAdapter((EthereumConnectionProfile) connectionProfile);
            } else if (connectionProfile instanceof BitcoinConnectionProfile) {
                return createBitcoinAdapter((BitcoinConnectionProfile) connectionProfile);
            } else if (connectionProfile instanceof FabricConnectionProfile) {
                return createFabricAdapter((FabricConnectionProfile) connectionProfile, blockchainId);
            } else {
                log.error("Invalid connectionProfile type!");
                return null;
            }
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

    private BitcoinAdapter createBitcoinAdapter(BitcoinConnectionProfile gateway) throws BitcoindException, CommunicationException {
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        final CloseableHttpClient httpProvider = HttpClients.custom().setConnectionManager(connManager).build();
        final BtcdClient client = new BtcdClientImpl(httpProvider, gateway.getAsProperties());
        final BtcdDaemon daemon = new BtcdDaemonImpl(client);
        final BitcoinAdapter result = new BitcoinAdapter(client, daemon);
        final PoWConfidenceCalculator cCalc = new PoWConfidenceCalculator();
        cCalc.setAdversaryRatio(gateway.getAdversaryVotingRatio());
        result.setConfidenceCalculator(cCalc);

        return result;
    }

    private FabricAdapter createFabricAdapter(FabricConnectionProfile gateway, String blockchainId) {
        return FabricAdapter.builder()
                .blockchainId(blockchainId)
                .build();
    }

    private BlockchainAdapter createAdapter(String blockchainType, Map<String, String> parameters) {
        List<IAdapterExtenstion> adapterExtensions = pluginManager.getExtensions(IAdapterExtenstion.class);
        for (IAdapterExtenstion adapterExtension : adapterExtensions) {
            if (adapterExtension.getBlockChainId().equals(blockchainType)) {
                BlockchainAdapter a = adapterExtension.getAdapter(parameters);
                System.out.println(">>> " + a.testConnection());
                return a;
            }
        }
        System.err.println("No extension for blockchain-id: " + blockchainType);
        return null;
    }

}
