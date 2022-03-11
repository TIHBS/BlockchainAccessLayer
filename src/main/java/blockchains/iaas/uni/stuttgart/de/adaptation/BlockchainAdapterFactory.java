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
import blockchains.iaas.uni.stuttgart.de.api.connectionprofiles.AbstractConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BlockchainAdapterFactory {


    public BlockchainAdapterFactory() {

    }

    private static final Logger log = LoggerFactory.getLogger(BlockchainAdapterFactory.class);

    public BlockchainAdapter createBlockchainAdapter(AbstractConnectionProfile connectionProfile) throws Exception {

        try {
            return createAdapter(connectionProfile);
        } catch (Exception e) {
            final String msg = String.format("Error while creating a blockchain adapter for. Details: %s", e.getMessage());
            log.error(msg);
            throw new Exception(msg, e);
        }
    }

    private BlockchainAdapter createAdapter(AbstractConnectionProfile connectionProfile) {
        List<IAdapterExtension> adapterExtensions = BlockchainPluginManager.getInstance().getExtensions();
        for (IAdapterExtension adapterExtension : adapterExtensions) {
            if (connectionProfile.getClass() == adapterExtension.getConnectionProfileClass()) {
                return adapterExtension.getAdapter(connectionProfile);
            }
        }
        System.err.println("No extension for blockchain-id: " + connectionProfile);
        return null;
    }

}
