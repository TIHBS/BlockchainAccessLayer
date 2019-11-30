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
package blockchains.iaas.uni.stuttgart.de.connectionprofiles.profiles;

import java.util.Properties;

import blockchains.iaas.uni.stuttgart.de.connectionprofiles.AbstractConnectionProfile;

public class BitcoinConnectionProfile extends AbstractConnectionProfile {
    private static final String PREFIX = "node.bitcoind.";
    // Configuration parameters for the 'bitcoind' JSON-RPC client ('BtcdClient')
    public static final String RPC_PROTOCOL = "rpc.protocol";
    public static final String RPC_HOST = PREFIX + "rpc.host";
    public static final String RPC_PORT = PREFIX + "rpc.port";
    public static final String RPC_USER = PREFIX + "rpc.user";
    public static final String RPC_PASSWORD = PREFIX + "rpc.password";
    public static final String HTTP_AUTHENTICATION_SCHEME = PREFIX + "http.auth_scheme";
    // Configuration parameters for the 'bitcoind' notification daemon ('BtcdDaemon')
    public static final String NOTIFICATION_ALERT_PORT = PREFIX + "notification.alert.port";
    public static final String NOTIFICATION_BLOCK_PORT = PREFIX + "notification.block.port";
    public static final String NOTIFICATION_WALLET_PORT = PREFIX + "notification.wallet.port";

    private String rpcProtocol;
    private String rpcHost;
    private String rpcPort;
    private String rpcUser;
    private String rpcPassword;
    private String httpAuthScheme;
    private String notificationAlertPort;
    private String notificationBlockPort;
    private String notificationWalletPort;

    public BitcoinConnectionProfile() {
    }

    public BitcoinConnectionProfile(String rpcProtocol, String rpcHost, String rpcPort, String rpcUser, String rpcPassword, String httpAuthScheme, String notificationAlertPort, String notificationBlockPort, String notificationWalletPort) {
        this.rpcProtocol = rpcProtocol;
        this.rpcHost = rpcHost;
        this.rpcPort = rpcPort;
        this.rpcUser = rpcUser;
        this.rpcPassword = rpcPassword;
        this.httpAuthScheme = httpAuthScheme;
        this.notificationAlertPort = notificationAlertPort;
        this.notificationBlockPort = notificationBlockPort;
        this.notificationWalletPort = notificationWalletPort;
    }

    public String getRpcProtocol() {
        return rpcProtocol;
    }

    public void setRpcProtocol(String rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }

    public String getRpcHost() {
        return rpcHost;
    }

    public void setRpcHost(String rpcHost) {
        this.rpcHost = rpcHost;
    }

    public String getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(String rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getRpcUser() {
        return rpcUser;
    }

    public void setRpcUser(String rpcUser) {
        this.rpcUser = rpcUser;
    }

    public String getRpcPassword() {
        return rpcPassword;
    }

    public void setRpcPassword(String rpcPassword) {
        this.rpcPassword = rpcPassword;
    }

    public String getHttpAuthScheme() {
        return httpAuthScheme;
    }

    public void setHttpAuthScheme(String httpAuthScheme) {
        this.httpAuthScheme = httpAuthScheme;
    }

    public String getNotificationAlertPort() {
        return notificationAlertPort;
    }

    public void setNotificationAlertPort(String notificationAlertPort) {
        this.notificationAlertPort = notificationAlertPort;
    }

    public String getNotificationBlockPort() {
        return notificationBlockPort;
    }

    public void setNotificationBlockPort(String notificationBlockPort) {
        this.notificationBlockPort = notificationBlockPort;
    }

    public String getNotificationWalletPort() {
        return notificationWalletPort;
    }

    public void setNotificationWalletPort(String notificationWalletPort) {
        this.notificationWalletPort = notificationWalletPort;
    }

    @Override
    public Properties getAsProperties() {
        final Properties result = super.getAsProperties();
        result.setProperty(HTTP_AUTHENTICATION_SCHEME, this.httpAuthScheme);
        result.setProperty(NOTIFICATION_ALERT_PORT, this.notificationAlertPort);
        result.setProperty(NOTIFICATION_BLOCK_PORT, this.notificationBlockPort);
        result.setProperty(NOTIFICATION_WALLET_PORT, this.notificationWalletPort);
        result.setProperty(RPC_HOST, this.rpcHost);
        result.setProperty(RPC_PASSWORD, this.rpcPassword);
        result.setProperty(RPC_PORT, this.rpcPort);
        result.setProperty(RPC_USER, this.rpcUser);
        result.setProperty(RPC_PROTOCOL, this.rpcProtocol);
        return result;
    }
}
