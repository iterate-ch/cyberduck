/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.configuration;

import java.util.ArrayList;
import java.util.List;

import java.math.BigInteger;

import com.sshtools.j2ssh.transport.cipher.SshCipherFactory;
import com.sshtools.j2ssh.transport.compression.SshCompressionFactory;
import com.sshtools.j2ssh.transport.hmac.SshHmacFactory;
import com.sshtools.j2ssh.transport.kex.SshKeyExchangeFactory;
import com.sshtools.j2ssh.transport.publickey.SshKeyPairFactory;

import org.apache.log4j.Logger;


/**
 * Represents an SSH connection. An instance of this class is passed to the
 * connect methods of the transport protocol. It specifies all the preferred
 * algorithms and methods for the connection to use
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SshConnectionProperties {


    private static Logger log = Logger.getLogger(SshConnectionProperties.class);

    protected String host;
    protected String prefDecryption = SshCipherFactory.getDefaultCipher();
    protected String prefEncryption = SshCipherFactory.getDefaultCipher();
    protected String prefKex = SshKeyExchangeFactory.getDefaultKeyExchange();
    protected String prefPK = SshKeyPairFactory.getDefaultPublicKey();
    protected String prefRecvComp = SshCompressionFactory.getDefaultCompression();
    protected String prefRecvMac = SshHmacFactory.getDefaultHmac();
    protected String prefSendComp = SshCompressionFactory.getDefaultCompression();
    protected String prefSendMac = SshHmacFactory.getDefaultHmac();
    protected String username;
    protected int port = 22;

    /**
     * Constructor for the SshConnectionProperties object
     */
    public SshConnectionProperties()  {
    }

    /**
     * Sets the host name for this connection
     *
     * @param host The new host value
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the host name for the connection
     *
     * @return The host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the port for the connection
     *
     * @param port The new port value
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the port for this connection
     *
     * @return The port value
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the preferred client->server compression
     *
     * @param pref The new prefCSComp value
     */
    public void setPrefCSComp(String pref) {
        prefSendComp = pref;
    }

    /**
     * Gets the preferred client->server compression
     *
     * @return The method name
     */
    public String getPrefCSComp() {
        return prefSendComp;
    }

    /**
     * Sets the prefEncryption attribute of the SshConnectionProperties object
     *
     * @param pref The method name
     */
    public void setPrefCSEncryption(String pref) {
        prefEncryption = pref;
    }

    /**
     * Gets the preferred client->server encryption method for the connection
     *
     * @return The prefEncryption value
     */
    public String getPrefCSEncryption() {
        return prefEncryption;
    }

    /**
     * Sets the preferred client->server message authentication
     *
     * @param pref The new prefCSMac value
     */
    public void setPrefCSMac(String pref) {
        prefSendMac = pref;
    }

    /**
     * Gets the preferred client->server message authentication
     *
     * @return The method name
     */
    public String getPrefCSMac() {
        return prefSendMac;
    }

    /**
     * Sets the preferred key exchange for the connection
     *
     * @param pref
     */
    public void setPrefKex(String pref) {
        prefKex = pref;
    }

    /**
     * Gets the preferred key exchange method
     *
     * @return The method value
     */
    public String getPrefKex() {
        return prefKex;
    }

    /**
     * Sets the preferred public key mechanism
     *
     * @param pref The method name
     */
    public void setPrefPublicKey(String pref) {
        prefPK = pref;
    }

    /**
     * Gets the preferred public key mechanism
     *
     * @return The method name
     */
    public String getPrefPublicKey() {
        return prefPK;
    }

    /**
     * Sets the preferred server->client compression
     *
     * @param pref The new prefSCComp value
     */
    public void setPrefSCComp(String pref) {
        prefRecvComp = pref;
    }

    /**
     * Gets the preferred server->client compression
     *
     * @return The method name
     */
    public String getPrefSCComp() {
        return prefRecvComp;
    }

    /**
     * Sets the preferred server->client encryption for the connection
     *
     * @param pref The method name
     */
    public void setPrefSCEncryption(String pref) {
        prefDecryption = pref;
    }

    /**
     * Gets the preferred server->client encryption for the conneciton
     *
     * @return The method name
     */
    public String getPrefSCEncryption() {
        return prefDecryption;
    }

    /**
     * Sets the preferred server->client message authentication
     *
     * @param pref The new prefSCMac value
     */
    public void setPrefSCMac(String pref) {
        prefRecvMac = pref;
    }

    /**
     * Gets the preferred server->client message authentication
     *
     * @return The method name
     */
    public String getPrefSCMac() {
        return prefRecvMac;
    }

    /**
     * Sets the username for the connection
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the username for the connection
     *
     * @return The username value
     */
    public String getUsername() {
        return username;
    }
}
