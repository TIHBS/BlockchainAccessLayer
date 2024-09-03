/*******************************************************************************
 * Copyright (c) 2019-2024 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 * Co-author: Akdhay Patel
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import java.util.Map;

import blockchains.iaas.uni.stuttgart.de.api.connectionprofiles.AbstractConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;


@RestController()
@RequestMapping("configure")
@Log4j2
public class ConnectionProfilesController {
    final BlockchainManager manager;

    public ConnectionProfilesController(BlockchainManager manager) {
        this.manager = manager;
    }

    @PostMapping()
    public void acceptConfiguration(Map<String, AbstractConnectionProfile> profiles) {
        log.info("Receiving a new set of connection profiles: {}", profiles);
        ConnectionProfilesManager.getInstance().loadConnectionProfiles(profiles);
    }

    @DeleteMapping
    public void resetConfigurations() {
        log.info("Resetting all connection profiles...");
        ConnectionProfilesManager.getInstance().resetConnectionProfiles();
    }

    @GetMapping()
    public String getConfigurations() throws JsonProcessingException {
        return ConnectionProfilesManager.getInstance().getConnectionProfilesAsJson();
    }

    @GetMapping(path = "/test")
    public String testConnection(@RequestParam(name = "blockchain-id") String blockchainId) {
        return manager.testConnection(blockchainId);
    }
}
