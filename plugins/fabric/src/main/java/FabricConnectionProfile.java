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

import blockchains.iaas.uni.stuttgart.de.api.connectionprofiles.AbstractConnectionProfile;

import java.util.Properties;


public class FabricConnectionProfile extends AbstractConnectionProfile {
    private static final String PREFIX = "hyperledger.fabric.";
    private static final String WALLET_PATH = PREFIX + "walletPath";
    private static final String USER_NAME = PREFIX + "userName";
    private static final String CONNECTION_PROFILE_PATH = PREFIX + "connectionProfilePath";
    private String walletPath;
    private String userName;
    private String connectionProfilePath;

    public FabricConnectionProfile() {
    }

    public FabricConnectionProfile(String walletPath, String userName, String connectionProfilePath) {
        this.walletPath = walletPath;
        this.userName = userName;
        this.connectionProfilePath = connectionProfilePath;
    }

    public String getWalletPath() {
        return walletPath;
    }

    public void setWalletPath(String walletPath) {
        this.walletPath = walletPath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getConnectionProfilePath() {
        return connectionProfilePath;
    }

    public void setConnectionProfilePath(String connectionProfilePath) {
        this.connectionProfilePath = connectionProfilePath;
    }

    @Override
    public Properties getAsProperties() {
        final Properties result = super.getAsProperties();
        result.setProperty(WALLET_PATH, this.walletPath);
        result.setProperty(USER_NAME, this.userName);
        result.setProperty(CONNECTION_PROFILE_PATH, this.connectionProfilePath);

        return result;
    }
}
