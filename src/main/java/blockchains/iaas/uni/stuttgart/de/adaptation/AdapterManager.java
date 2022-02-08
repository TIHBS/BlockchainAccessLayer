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

import java.util.*;

import blockchains.iaas.uni.stuttgart.de.api.connectionprofiles.AbstractConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.BlockchainIdNotFoundException;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.BlockchainNodeUnreachableException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdapterManager {
    private static final Logger log = LoggerFactory.getLogger(AdapterManager.class);
    private BlockchainAdapterFactory factory = new BlockchainAdapterFactory();
    private static AdapterManager instance = null;
    private final Map<String, Pair<BlockchainAdapter, AbstractConnectionProfile>> map = Collections.synchronizedMap(new HashMap<>());

    private AdapterManager() {
    }

    public static AdapterManager getInstance() {
        if (instance == null) {
            instance = new AdapterManager();
        }

        return instance;
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
            final BlockchainAdapter adapter = factory.createBlockchainAdapter(connectionProfile, blockchainId);
            map.put(blockchainId, ImmutablePair.of(adapter, connectionProfile));
            return Objects.requireNonNull(adapter);
        } catch (Exception e) {
            throw new BlockchainNodeUnreachableException("Failed to create a blockchain adapter. Reason: " + e.getMessage());
        }
    }

    public AbstractConnectionProfile[] getActiveAdapters() {
        AbstractConnectionProfile[] a = new AbstractConnectionProfile[map.size()];
        List<Pair<BlockchainAdapter, AbstractConnectionProfile>> targetList = new ArrayList<>(map.values());
        for (int i = 0; i < targetList.size(); i++) {
            Pair<BlockchainAdapter, AbstractConnectionProfile> s = targetList.get(i);
            a[i] = s.getValue();
        }
        return a;
    }

//    public AbstractConnectionProfile[] deactivateAdapter(String adapterId) {
//        List<Pair<BlockchainAdapter, AbstractConnectionProfile>> targetList = new ArrayList<>(map.values());
//        for (int i = 0; i < targetList.size(); i++) {
//            Pair<BlockchainAdapter, AbstractConnectionProfile> s = targetList.get(i);
//            if(adapterId.equals(s.getValue().))
//        }
//        return a;
//    }
}
;