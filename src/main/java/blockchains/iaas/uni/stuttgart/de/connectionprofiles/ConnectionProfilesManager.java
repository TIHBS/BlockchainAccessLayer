/*******************************************************************************
 * Copyright (c) 2019-2024 Institute for the Architecture of Application System
 * - University of Stuttgart
 * Author: Ghareeb Falazi
 * Co-author: Akshay Patel
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
import com.fasterxml.jackson.databind.jsontype.NamedType;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Manages the connection profiles recognized by the BAL. When it first initialized, it loads the profiles stored in
 * .bal/connectionProfiles.json inside the user home directory. If the file does not exist, no profiles will be loaded.
 */
@Log4j2
public class ConnectionProfilesManager {
    public static final Path initialConfigurationFilePath = Paths.get(System.getProperty("user.home"), ".bal", "connectionProfiles.json");
    private static ConnectionProfilesManager instance;
    @Setter
    private ConnectionProfileListener listener;


    private final Map<String, AbstractConnectionProfile> connectionProfilesMap;
    private static final ObjectMapper mapper = new ObjectMapper();

    private ConnectionProfilesManager() {
        this.connectionProfilesMap = new HashMap<>();
    }

    public Map<String, AbstractConnectionProfile> getConnectionProfiles() {
        return this.connectionProfilesMap;
    }


    public String getConnectionProfilesAsJson() throws JsonProcessingException {
        ObjectWriter writer = mapper.writerFor(new TypeReference<Map<String, AbstractConnectionProfile>>() {
        });
        return writer.writeValueAsString(this.connectionProfilesMap);
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
            ObjectReader reader = mapper.readerFor(new TypeReference<Map<String, AbstractConnectionProfile>>() {
            });
            Map<String, AbstractConnectionProfile> newMap = reader.readValue(file);
            this.loadConnectionProfiles(newMap);
        } catch (IOException e) {
            log.error("Failed to load connection profiles from the file!", e);
        }
    }

    public void loadConnectionProfilesFromJson(String jsonString) {
        try {
            ObjectReader reader = mapper.readerFor(new TypeReference<Map<String, AbstractConnectionProfile>>() {
            });
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

    public static void registerConnectionProfileSubtypeClass(Class<? extends AbstractConnectionProfile> clazz, String typeName) {
        mapper.registerSubtypes(new NamedType(clazz, typeName));
    }

    /**
     * Should only be used for testing purposes!
     */
    protected static void resetInstance() {
        instance = null;
    }

    private void loadInitialConnectionProfilesIfExist() {
        log.info("Loading connection profiles from file: {}", initialConfigurationFilePath);
        File initialFile = initialConfigurationFilePath.toFile();

        if (initialFile.exists() && initialFile.isFile()) {
            this.loadConnectionProfilesFromFile(initialFile);
        }
    }

    // TODO separate keystore management from gateway management
    // TODO make persistent
}

