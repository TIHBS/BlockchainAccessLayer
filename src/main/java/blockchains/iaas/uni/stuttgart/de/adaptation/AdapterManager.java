/********************************************************************************
 * Copyright (c) 2019-2024 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 * Co-author: Akshay Patel
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.adaptation;

import java.util.*;

import blockchains.iaas.uni.stuttgart.de.api.connectionprofiles.AbstractConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.BlockchainIdNotFoundException;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.BlockchainNodeUnreachableException;
import blockchains.iaas.uni.stuttgart.de.plugin.PluginManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class AdapterManager {
    private final Map<String, Pair<BlockchainAdapter, AbstractConnectionProfile>> map = Collections.synchronizedMap(new HashMap<>());
    private final BlockchainAdapterFactory factory;

    private AdapterManager(PluginManager pluginManager) {
        this.factory = new BlockchainAdapterFactory(pluginManager);
    }


    public BlockchainAdapter getAdapter(String blockchainId) throws BlockchainIdNotFoundException, BlockchainNodeUnreachableException {
        AbstractConnectionProfile connectionProfile = ConnectionProfilesManager.getInstance().getConnectionProfiles().get(blockchainId);
        // no connection profile!
        if (connectionProfile == null) {
            final String msg = String.format("blockchain-id <%s> does not exist!", blockchainId);
            log.error(msg);
            throw new BlockchainIdNotFoundException(msg);
        }

        // we already have an adapter for it
        if (map.containsKey(blockchainId)) {
            Pair<BlockchainAdapter, AbstractConnectionProfile> result = map.get(blockchainId);
            // is the connection profile still the same?
            if (result.getRight().equals(connectionProfile)) {
                return map.get(blockchainId).getLeft();
            }
            // no we need to create it!
        }

        try {
            final BlockchainAdapter adapter = factory.createBlockchainAdapter(connectionProfile);
            map.put(blockchainId, ImmutablePair.of(adapter, connectionProfile));
            return Objects.requireNonNull(adapter);
        } catch (Exception e) {
            throw new BlockchainNodeUnreachableException("Failed to create a blockchain adapter. Reason: " + e.getMessage());
        }
    }

}
