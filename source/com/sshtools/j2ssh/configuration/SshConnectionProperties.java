/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.configuration;

import com.sshtools.j2ssh.transport.cipher.SshCipherFactory;
import com.sshtools.j2ssh.transport.compression.SshCompressionFactory;
import com.sshtools.j2ssh.transport.hmac.SshHmacFactory;
import com.sshtools.j2ssh.transport.kex.SshKeyExchangeFactory;
import com.sshtools.j2ssh.transport.publickey.SshKeyPairFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshConnectionProperties {
    private static Log log = LogFactory.getLog(SshConnectionProperties.class);

    /**  */
    public static final int USE_STANDARD_SOCKET = 1;

    /**  */
    public static final int USE_HTTP_PROXY = 2;

    /**  */
    public static final int USE_SOCKS4_PROXY = 3;

    /**  */
    public static final int USE_SOCKS5_PROXY = 4;

    /**  */
    protected int transportProvider = USE_STANDARD_SOCKET;

    /**  */
    protected String proxyHostname;

    /**  */
    protected int proxyPort;

    /**  */
    protected String proxyUsername;

    /**  */
    protected String proxyPassword;

    /**  */
    protected String host;

    /**  */
    protected String prefDecryption = SshCipherFactory.getDefaultCipher();

    /**  */
    protected String prefEncryption = SshCipherFactory.getDefaultCipher();

    /**  */
    protected String prefKex = SshKeyExchangeFactory.getDefaultKeyExchange();

    /**  */
    protected String prefPK = SshKeyPairFactory.getDefaultPublicKey();

    /**  */
    protected String prefRecvComp = SshCompressionFactory.getDefaultCompression();

    /**  */
    protected String prefRecvMac = SshHmacFactory.getDefaultHmac();

    /**  */
    protected String prefSendComp = SshCompressionFactory.getDefaultCompression();

    /**  */
    protected String prefSendMac = SshHmacFactory.getDefaultHmac();

    /**  */
    protected String username;

    /**  */
    protected int port = 22;

    /**
     * Creates a new SshConnectionProperties object.
     */
    public SshConnectionProperties() {
    }

    /**
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * @return
     */
    public int getTransportProvider() {
        return this.transportProvider;
    }

    /**
     * @param name
     */
    public void setTransportProviderString(String name) {
        if (name != null) {
            if (name.equalsIgnoreCase("http")) {
                this.transportProvider = USE_HTTP_PROXY;
            }
            else if (name.equalsIgnoreCase("socks4")) {
                this.transportProvider = USE_SOCKS4_PROXY;
            }
            else if (name.equalsIgnoreCase("socks5")) {
                this.transportProvider = USE_SOCKS5_PROXY;
            }
            else {
                this.transportProvider = USE_STANDARD_SOCKET;
            }
        }
        else {
            this.transportProvider = USE_STANDARD_SOCKET;
        }
    }

    /**
     * @return
     */
    public String getTransportProviderString() {
        if (this.transportProvider == USE_HTTP_PROXY) {
            return "http";
        }
        else if (this.transportProvider == USE_SOCKS4_PROXY) {
            return "socks4";
        }
        else if (this.transportProvider == USE_SOCKS5_PROXY) {
            return "socks5";
        }
        else {
            return "socket";
        }
    }

    /**
     * @return
     */
    public String getProxyHost() {
        return this.proxyHostname;
    }

    /**
     * @return
     */
    public int getProxyPort() {
        return this.proxyPort;
    }

    /**
     * @return
     */
    public String getProxyUsername() {
        return this.proxyUsername;
    }

    /**
     * @return
     */
    public String getProxyPassword() {
        return this.proxyPassword;
    }

    /**
     * @param transportProvider
     */
    public void setTransportProvider(int transportProvider) {
        this.transportProvider = transportProvider;
    }

    /**
     * @param proxyHostname
     */
    public void setProxyHost(String proxyHostname) {
        this.proxyHostname = proxyHostname;
    }

    /**
     * @param proxyPort
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * @param proxyUsername
     */
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    /**
     * @param proxyPassword
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /**
     * @param pref
     */
    public void setPrefCSComp(String pref) {
        this.prefSendComp = pref;
    }

    /**
     * @return
     */
    public String getPrefCSComp() {
        return this.prefSendComp;
    }

    /**
     * @param pref
     */
    public void setPrefCSEncryption(String pref) {
        this.prefEncryption = pref;
    }

    /**
     * @return
     */
    public String getPrefCSEncryption() {
        return this.prefEncryption;
    }

    /**
     * @param pref
     */
    public void setPrefCSMac(String pref) {
        this.prefSendMac = pref;
    }

    /**
     * @return
     */
    public String getPrefCSMac() {
        return this.prefSendMac;
    }

    /**
     * @param pref
     */
    public void setPrefKex(String pref) {
        this.prefKex = pref;
    }

    /**
     * @return
     */
    public String getPrefKex() {
        return this.prefKex;
    }

    /**
     * @param pref
     */
    public void setPrefPublicKey(String pref) {
        this.prefPK = pref;
    }

    /**
     * @return
     */
    public String getPrefPublicKey() {
        return this.prefPK;
    }

    /**
     * @param pref
     */
    public void setPrefSCComp(String pref) {
        this.prefRecvComp = pref;
    }

    /**
     * @return
     */
    public String getPrefSCComp() {
        return this.prefRecvComp;
    }

    /**
     * @param pref
     */
    public void setPrefSCEncryption(String pref) {
        this.prefDecryption = pref;
    }

    /**
     * @return
     */
    public String getPrefSCEncryption() {
        return this.prefDecryption;
    }

    /**
     * @param pref
     */
    public void setPrefSCMac(String pref) {
        this.prefRecvMac = pref;
    }

    /**
     * @return
     */
    public String getPrefSCMac() {
        return this.prefRecvMac;
    }

    /**
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return
     */
    public String getUsername() {
        return this.username;
    }
}