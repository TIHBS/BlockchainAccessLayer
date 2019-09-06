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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.exceptions.BlockchainIdNotFoundException;
import blockchains.iaas.uni.stuttgart.de.gateways.GatewayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdapterManager {
    private static final Logger log = LoggerFactory.getLogger(AdapterManager.class);
    //private static final String DEFAULT_ETHEREUM_ID = "eth-0";
    //private static final String DEFUALT_BICOIN_ID = "btc-0";
    private BlockchainAdapterFactory factory = new BlockchainAdapterFactory();
    private static AdapterManager instance = null;
    private final Map<String, BlockchainAdapter> map = Collections.synchronizedMap(new HashMap<>());

    private AdapterManager() {

    }

    public static AdapterManager getInstance() {
        if (instance == null) {
            instance = new AdapterManager();
            instance.initialize();
        }

        return instance;
    }

    public BlockchainAdapter getAdapter(String blockchainId) throws BlockchainIdNotFoundException {
        if (map.containsKey(blockchainId)) {
            return map.get(blockchainId);
        } else {
            final String msg = String.format("blockchain-id <%s> does not exist!", blockchainId);
            log.error(msg);
            throw new BlockchainIdNotFoundException(msg);
        }
    }

    private void initialize() {
        try {
            createAdapters();
        } catch (Exception e) {
            //TODO better handling of errors
        }
    }

    private void createAdapters() throws Exception {
        for(String id : GatewayManager.getInstance().getGateways().keySet()) {
            map.put(id, factory.createBlockchainAdapter(id));
        }
    }
}
