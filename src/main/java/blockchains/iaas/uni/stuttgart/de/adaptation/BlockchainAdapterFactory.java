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

import java.io.IOException;
import java.util.Map;

import blockchains.iaas.uni.stuttgart.de.adaptation.adapters.BitcoinAdapter;
import blockchains.iaas.uni.stuttgart.de.adaptation.adapters.EthereumAdapter;
import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.adaptation.utils.PoWConfidenceCalculator;
import blockchains.iaas.uni.stuttgart.de.gateways.AbstractGateway;
import blockchains.iaas.uni.stuttgart.de.gateways.BitcoinGateway;
import blockchains.iaas.uni.stuttgart.de.gateways.EthereumGateway;
import blockchains.iaas.uni.stuttgart.de.gateways.GatewayManager;
import com.neemre.btcdcli4j.core.BitcoindException;
import com.neemre.btcdcli4j.core.CommunicationException;
import com.neemre.btcdcli4j.core.client.BtcdClient;
import com.neemre.btcdcli4j.core.client.BtcdClientImpl;
import com.neemre.btcdcli4j.daemon.BtcdDaemon;
import com.neemre.btcdcli4j.daemon.BtcdDaemonImpl;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.CipherException;

public class BlockchainAdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(BlockchainAdapterFactory.class);

    public BlockchainAdapter createBlockchainAdapter(String gatewayKey) throws Exception {
        Map<String, AbstractGateway> allGateways = GatewayManager.getInstance().getGateways();
        AbstractGateway gateway = allGateways.get(gatewayKey);

        if (gateway == null) {
            log.error("gateway not found: {}", gatewayKey);
            return null;
        }

        try {
            if (gateway instanceof EthereumGateway) {
                return createEthereumAdapter((EthereumGateway) gateway);
            } else if (gateway instanceof BitcoinGateway) {
                return createBitcoinAdapter((BitcoinGateway) gateway);
            } else {
                log.error("Invalid gateway type!");
                return null;
            }
        } catch (Exception e) {
            final String msg = String.format("Error while creating a blockchain adapter for %s. Details: %s", gatewayKey, e.getMessage());
            log.error(msg);
            throw new Exception(msg, e);
        }
    }

    private EthereumAdapter createEthereumAdapter(EthereumGateway gateway) throws IOException, CipherException {
        final EthereumAdapter result = new EthereumAdapter(gateway.getNodeUrl());
        result.setCredentials(gateway.getKeystorePassword(), gateway.getKeystorePath());
        final PoWConfidenceCalculator cCalc = new PoWConfidenceCalculator();
        cCalc.setAdversaryRatio(gateway.getAdversaryVotingRatio());
        result.setConfidenceCalculator(cCalc);

        return result;
    }

    private BitcoinAdapter createBitcoinAdapter(BitcoinGateway gateway) throws BitcoindException, CommunicationException {
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
}
