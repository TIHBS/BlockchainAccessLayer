package blockchains.iaas.uni.stuttgart.de.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/********************************************************************************
 * Copyright (c) 2018 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
public class Configuration {

    private static Configuration instance;
    private static final String PROPERTIES_FILE = "config.properties";
    public Properties properties = null;

    private Configuration(){

    }

    private void readProperties() {
        properties = new Properties();
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Configuration getInstance(){
        if(instance == null) {
            instance = new Configuration();
            instance.readProperties();
        }

        return instance;
    }
}
