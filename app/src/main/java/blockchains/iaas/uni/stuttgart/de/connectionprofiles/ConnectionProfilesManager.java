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
package blockchains.iaas.uni.stuttgart.de.connectionprofiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import blockchains.iaas.uni.stuttgart.de.api.connectionprofiles.AbstractConnectionProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the connection profiles recognized by the BAL. When it first initialized, it loads the profiles stored in
 * .bal/connectionProfiles.json inside the user home directory. If the file does not exist, no profiles will be loaded.
 */
public class ConnectionProfilesManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectionProfilesManager.class);
    public static final Path initialConfigurationFilePath = Paths.get(System.getProperty("user.home"), ".bal", "connectionProfiles.json");
    private static ConnectionProfilesManager instance;
    private ConnectionProfileListener listener;

    private Map<String, AbstractConnectionProfile> connectionProfilesMap;
    private ObjectReader reader;
    private ObjectWriter writer;

    private ConnectionProfilesManager() {
        this.connectionProfilesMap = new HashMap<>();
        this.reader = new ObjectMapper().readerFor(new TypeReference<Map<String, AbstractConnectionProfile>>() {
        });
        this.writer = new ObjectMapper().writerFor(new TypeReference<Map<String, AbstractConnectionProfile>>() {
        });
    }

    public Map<String, AbstractConnectionProfile> getConnectionProfiles() {
        return this.connectionProfilesMap;
    }


    public String getConnectionProfilesAsJson() throws JsonProcessingException {
        return this.writer.writeValueAsString(this.connectionProfilesMap);
    }

    /**
     * Adds all connection profiles contained in the newMap. Replaces profiles with an existing name.
     *
     * @param newMap a map of name->connection profiles
     */
    public void loadConnectionProfiles(Map<String, AbstractConnectionProfile> newMap) {
        this.connectionProfilesMap.putAll(newMap);

        if (listener != null) {
            listener.connectionProfileChanged();
        }
    }

    public void resetConnectionProfiles() {
        this.connectionProfilesMap.clear();

        if (listener != null) {
            listener.connectionProfileChanged();
        }
    }

    public void loadConnectionProfilesFromFile(File file) {
        try {
            Map<String, AbstractConnectionProfile> newMap = this.reader.readValue(file);
            this.loadConnectionProfiles(newMap);
        } catch (IOException e) {
            log.error("Failed to load connection profiles from the file!", e);
        }
    }

    public void loadConnectionProfilesFromJson(String jsonString) {
        try {
            Map<String, AbstractConnectionProfile> newMap = reader.readValue(jsonString);
            this.loadConnectionProfiles(newMap);
        } catch (IOException e) {
            log.error("Failed to load connection profiles from the string!", e);
        }
    }

    public static ConnectionProfilesManager getInstance() {
        if (instance == null) {
            instance = new ConnectionProfilesManager();
            instance.loadInitialConnectionProfilesIfExist();
        }

        return instance;
    }

    public void setListener(ConnectionProfileListener listener) {
        this.listener = listener;
    }

    /**
     * Should only be used for testing purposes!
     */
    protected static void resetInstance() {
        instance = null;
    }

    private void loadInitialConnectionProfilesIfExist() {
        File initialFile = initialConfigurationFilePath.toFile();

        if (initialFile.exists() && initialFile.isFile()) {
            this.loadConnectionProfilesFromFile(initialFile);
        }
    }

    // TODO separate keystore management from gateway management
    // TODO make persistent
}

