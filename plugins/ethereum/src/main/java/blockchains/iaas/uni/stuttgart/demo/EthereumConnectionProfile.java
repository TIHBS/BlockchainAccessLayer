/*******************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.demo;

import blockchains.iaas.uni.stuttgart.de.api.connectionprofiles.AbstractConnectionProfile;

import java.util.Properties;


public class EthereumConnectionProfile extends AbstractConnectionProfile {
    private static final String PREFIX = "ethereum.";
    public static final String NODE_URL = PREFIX + "nodeUrl";
    public static final String KEYSTORE_PATH = PREFIX + "keystorePath";
    public static final String KEYSTORE_PASSWORD = PREFIX + "keystorePassword";
    public static final String BLOCK_TIME = PREFIX + "blockTimeSeconds";
    private String nodeUrl;
    private String keystorePath;
    private String keystorePassword;
    private int pollingTimeSeconds;

    public EthereumConnectionProfile() {
    }

    public EthereumConnectionProfile(String nodeUrl, String keystorePath, String keystorePassword, int pollingTimeSeconds) {
        this.nodeUrl = nodeUrl;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.pollingTimeSeconds = pollingTimeSeconds;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public int getPollingTimeSeconds() {
        return pollingTimeSeconds;
    }

    public void setPollingTimeSeconds(int pollingTimeSeconds) {
        this.pollingTimeSeconds = pollingTimeSeconds;
    }

    @Override
    public Properties getAsProperties() {
        final Properties result = super.getAsProperties();
        result.setProperty(NODE_URL, this.nodeUrl);
        result.setProperty(KEYSTORE_PASSWORD, this.keystorePassword);
        result.setProperty(KEYSTORE_PATH, this.keystorePath);
        result.setProperty(BLOCK_TIME, String.valueOf(this.pollingTimeSeconds));

        return result;
    }
}
