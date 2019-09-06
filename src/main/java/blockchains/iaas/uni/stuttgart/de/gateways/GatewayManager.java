/*******************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System
 * - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.de.gateways;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayManager {
    private static final Logger log = LoggerFactory.getLogger(GatewayManager.class);
    private static final String DEFAULT_GATEWAY_CONFIGURATION_FILE_NAME = "gatewayConfiguration.json";
    private static GatewayManager instance;
    private File gatewaysConfigurationFile;
    private Map<String, AbstractGateway> gatewayMap;

    private GatewayManager(File gatewaysConfigurationFile) {
        this.gatewaysConfigurationFile = gatewaysConfigurationFile;
    }

    public Map<String, AbstractGateway> getGateways() {
        return this.gatewayMap;
    }

    private void loadGatewaysFromFile() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            this.gatewayMap = mapper.readValue(this.gatewaysConfigurationFile, new TypeReference<Map<String, AbstractGateway>>() {
            });
        } catch (IOException e) {
            log.error("Failed to load gateways from the file!", e);
        }
    }

    public static GatewayManager getInstance() {
        try {
            if (instance == null) {
                final File file = new File(Objects.requireNonNull(
                        GatewayManager.class.getClassLoader().getResource(DEFAULT_GATEWAY_CONFIGURATION_FILE_NAME)).toURI());
                instance = new GatewayManager(file);
                instance.loadGatewaysFromFile();
            }

            return instance;
        } catch (URISyntaxException e) {
            log.error("An unexpected error occurred! ", e);
            throw new RuntimeException(e);
        }
    }

    // TODO provide ability to add/remove/configure gateways externally
    // TODO separate keystore management from gateway management
    // TODO expose gateways as a REST resource
}

