package blockchains.iaas.uni.stuttgart.de.adaptation;

import blockchains.iaas.uni.stuttgart.de.adaptation.adapters.EthereumAdapter;
import blockchains.iaas.uni.stuttgart.de.config.Configuration;
import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.CipherException;

import java.io.IOException;

/********************************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 ********************************************************************************/
public class BlockchainAdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(BlockchainAdapterFactory.class);

    public BlockchainAdapter createBlockchainAdapter(NodeType nodeType) throws IOException, CipherException {
        switch (nodeType) {
            case ETHEREUM:
                return createEthereumAdapter();
            case BITCOIN:
                log.error("Bitcoin nodes are not yet supported!");
            default:
                log.error("Invalid node type!");
                return null;
        }

    }

    private EthereumAdapter createEthereumAdapter() throws IOException, CipherException {
        final EthereumAdapter result = new EthereumAdapter(Configuration.getInstance().properties.getProperty("ethereum-node-url"));
        result.setCredentials(Configuration.getInstance().properties.getProperty("ethereum-keystore-password"),
                Configuration.getInstance().properties.getProperty("ethereum-keystore-path"));

        return result;

    }
}
