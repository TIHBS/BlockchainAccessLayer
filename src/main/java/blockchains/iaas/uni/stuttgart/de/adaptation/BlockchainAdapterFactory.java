/********************************************************************************
 * Copyright (c) 2019-2024 Institute for the Architecture of Application System -
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
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Log4j2
public class BlockchainAdapterFactory {
    public BlockchainAdapterFactory() {

    }

    public BlockchainAdapter createBlockchainAdapter(AbstractConnectionProfile connectionProfile) throws Exception {

        try {
            return createAdapter(connectionProfile);
        } catch (Exception e) {
            final String msg = String.format("Error while creating a blockchain adapter for. Details: %s", e.getMessage());
            log.error(msg, e);
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
        log.error("No extension for blockchain-id: {}", connectionProfile);

        return null;
    }

}
