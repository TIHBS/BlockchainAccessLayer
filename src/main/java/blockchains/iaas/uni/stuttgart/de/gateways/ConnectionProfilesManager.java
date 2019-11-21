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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionProfilesManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectionProfilesManager.class);
    private static final String DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME = "gatewayConfiguration.json";
    private static ConnectionProfilesManager instance;
    private Map<String, AbstractConnectionProfile> connectionProfilesMap;
    private ObjectReader reader;

    private ConnectionProfilesManager() {
        this.reader = new ObjectMapper().readerFor(new TypeReference<Map<String, AbstractConnectionProfile>>() {
        });
    }

    public Map<String, AbstractConnectionProfile> getGateways() {
        return this.connectionProfilesMap;
    }

    /**
     * Adds all connection profiles contained in the newMap. Replaces profiles with an existing name.
     *
     * @param newMap a map of name->connection profiles
     */
    protected void loadGateways(Map<String, AbstractConnectionProfile> newMap) {
        if (this.connectionProfilesMap == null) {
            this.connectionProfilesMap = new HashMap<>();
        }

        this.connectionProfilesMap.putAll(newMap);
    }

    public void resetConnectionProfiles() {
        this.connectionProfilesMap.clear();
    }

    public void loadGatewaysFromFile(File file) {
        try {
            Map<String, AbstractConnectionProfile> newMap = this.reader.readValue(file);
            this.loadGateways(newMap);
        } catch (IOException e) {
            log.error("Failed to load connection profiles from the file!", e);
        }
    }

    public void loadGatewaysFromString(String jsonString) {
        try {
            Map<String, AbstractConnectionProfile> newMap = reader.readValue(jsonString);
            this.loadGateways(newMap);
        } catch (IOException e) {
            log.error("Failed to load connection profiles from the string!", e);
        }
    }

    public void loadDefaultConnectionProfiles() {
        try {
            this.resetConnectionProfiles();
            final File file = new File(Objects.requireNonNull(
                    ConnectionProfilesManager.class.getClassLoader().getResource(DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME)).toURI());
            if (file.exists()) {
                this.loadGatewaysFromFile(file);
            } else {
                log.error("Cannot load default connection profiles. The file: " + file.toString() + " does not exist!");
            }
        } catch (URISyntaxException e) {
            log.error("An unexpected error occurred! ", e);
            throw new RuntimeException(e);
        }
    }

    public static ConnectionProfilesManager getInstance() {
        if (instance == null) {
            instance = new ConnectionProfilesManager();
            instance.loadDefaultConnectionProfiles();
        }

        return instance;
    }

    // TODO separate keystore management from gateway management
    // TODO expose gateways as a REST resource
}

