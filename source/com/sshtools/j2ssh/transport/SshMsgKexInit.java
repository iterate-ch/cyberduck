/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
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

package com.sshtools.j2ssh.transport;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.cipher.SshCipherFactory;
import com.sshtools.j2ssh.transport.compression.SshCompressionFactory;
import com.sshtools.j2ssh.transport.hmac.SshHmacFactory;
import com.sshtools.j2ssh.transport.kex.SshKeyExchangeFactory;
import com.sshtools.j2ssh.transport.publickey.SshKeyPairFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * @author $author$
 * @version $Revision$
 */
public class SshMsgKexInit
        extends SshMessage {
    /**  */
    protected final static int SSH_MSG_KEX_INIT = 20;
    private List supportedCompCS;
    private List supportedCompSC;
    private List supportedEncryptCS;
    private List supportedEncryptSC;
    private List supportedKex;
    private List supportedLangCS;
    private List supportedLangSC;
    private List supportedMacCS;
    private List supportedMacSC;
    private List supportedPK;

    // Message values
    private byte[] cookie;
    private boolean firstKexFollows;

    /**
     * Creates a new SshMsgKexInit object.
     */
    public SshMsgKexInit() {
        super(SSH_MSG_KEX_INIT);
    }

    /**
     * Creates a new SshMsgKexInit object.
     *
     * @param props
     */
    public SshMsgKexInit(SshConnectionProperties props) {
        super(SSH_MSG_KEX_INIT);

        // Create some random data
        cookie = new byte[16];

        // Seed the random number generator
        Random r = ConfigurationLoader.getRND();

        // Get the next random bytes into our cookie
        r.nextBytes(cookie);

        // Get the supported algorithms from the factory objects but adding the
        // preffered algorithm to the top of the list
        supportedKex = sortAlgorithmList(SshKeyExchangeFactory
                .getSupportedKeyExchanges(),
                props.getPrefKex());

        supportedPK = sortAlgorithmList(SshKeyPairFactory.getSupportedKeys(),
                props.getPrefPublicKey());

        supportedEncryptCS = sortAlgorithmList(SshCipherFactory
                .getSupportedCiphers(),
                props.getPrefCSEncryption());

        supportedEncryptSC = sortAlgorithmList(SshCipherFactory
                .getSupportedCiphers(),
                props.getPrefSCEncryption());

        supportedMacCS = sortAlgorithmList(SshHmacFactory.getSupportedMacs(),
                props.getPrefCSMac());

        supportedMacSC = sortAlgorithmList(SshHmacFactory.getSupportedMacs(),
                props.getPrefSCMac());

        supportedCompCS = sortAlgorithmList(SshCompressionFactory
                .getSupportedCompression(),
                props.getPrefCSComp());

        supportedCompSC = sortAlgorithmList(SshCompressionFactory
                .getSupportedCompression(),
                props.getPrefSCComp());

        // We currently don't support language preferences
        supportedLangCS = new ArrayList();
        supportedLangSC = new ArrayList();

        // We don't guess (I don't see the point of this in the protocol!)
        firstKexFollows = false;
    }

    /**
     * @return
     */
    public String getMessageName() {
        return "SSH_MSG_KEX_INIT";
    }

    public boolean firstPacketFollows() {
        return firstKexFollows;
    }

    /**
     * @return
     */
    public List getSupportedCSComp() {
        return supportedCompCS;
    }

    /**
     * @return
     */
    public List getSupportedCSEncryption() {
        return supportedEncryptCS;
    }

    /**
     * @return
     */
    public List getSupportedCSMac() {
        return supportedMacCS;
    }

    /**
     * @return
     */
    public List getSupportedKex() {
        return supportedKex;
    }

    /**
     * @param pks
     */
    public void setSupportedPK(List pks) {
        supportedPK.clear();
        supportedPK.addAll(pks);
        sortAlgorithmList(supportedPK, SshKeyPairFactory.getDefaultPublicKey());
    }

    /**
     * @return
     */
    public List getSupportedPublicKeys() {
        return supportedPK;
    }

    /**
     * @return
     */
    public List getSupportedSCComp() {
        return supportedCompSC;
    }

    /**
     * @return
     */
    public List getSupportedSCEncryption() {
        return supportedEncryptSC;
    }

    /**
     * @return
     */
    public List getSupportedSCMac() {
        return supportedMacSC;
    }

    /**
     * @param list
     * @return
     */
    public String createDelimString(List list) {
        // Set up the seperator (blank to start cause we dont want a comma
        // at the beginning of the list)
        String sep = "";
        String ret = "";

        // Iterate through the list
        Iterator it = list.iterator();

        while (it.hasNext()) {
            // Add the seperator and then the item
            ret += (sep + (String) it.next());

            sep = ",";
        }

        return ret;
    }

    /**
     * @return
     */
    public String toString() {
        String ret = "SshMsgKexInit:\n";
        ret += ("Supported Kex " + supportedKex.toString() + "\n");
        ret += ("Supported Public Keys " + supportedPK.toString() + "\n");
        ret += ("Supported Encryption Client->Server "
                + supportedEncryptCS.toString() + "\n");
        ret += ("Supported Encryption Server->Client "
                + supportedEncryptSC.toString() + "\n");
        ret += ("Supported Mac Client->Server " + supportedMacCS.toString()
                + "\n");
        ret += ("Supported Mac Server->Client " + supportedMacSC.toString()
                + "\n");
        ret += ("Supported Compression Client->Server "
                + supportedCompCS.toString() + "\n");
        ret += ("Supported Compression Server->Client "
                + supportedCompSC.toString() + "\n");
        ret += ("Supported Languages Client->Server "
                + supportedLangCS.toString() + "\n");
        ret += ("Supported Languages Server->Client "
                + supportedLangSC.toString() + "\n");
        ret += ("First Kex Packet Follows ["
                + (firstKexFollows ? "TRUE]" : "FALSE]"));

        return ret;
    }

    /**
     * @param baw
     * @throws InvalidMessageException
     */
    protected void constructByteArray(ByteArrayWriter baw) throws
            InvalidMessageException {
        try {
            baw.write(cookie);

            baw.writeString(createDelimString(supportedKex));
            baw.writeString(createDelimString(supportedPK));
            baw.writeString(createDelimString(supportedEncryptCS));
            baw.writeString(createDelimString(supportedEncryptSC));
            baw.writeString(createDelimString(supportedMacCS));
            baw.writeString(createDelimString(supportedMacSC));
            baw.writeString(createDelimString(supportedCompCS));
            baw.writeString(createDelimString(supportedCompSC));
            baw.writeString(createDelimString(supportedLangCS));
            baw.writeString(createDelimString(supportedLangSC));
            baw.write((firstKexFollows ? 1 : 0));
            baw.writeInt(0);
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("Error writing message data: "
                    + ioe.getMessage());
        }
    }

    /**
     * @param bar
     * @throws InvalidMessageException
     */
    protected void constructMessage(ByteArrayReader bar) throws
            InvalidMessageException {
        try {
            cookie = new byte[16];
            bar.read(cookie);

            supportedKex = loadListFromString(bar.readString());
            supportedPK = loadListFromString(bar.readString());
            supportedEncryptCS = loadListFromString(bar.readString());
            supportedEncryptSC = loadListFromString(bar.readString());
            supportedMacCS = loadListFromString(bar.readString());
            supportedMacSC = loadListFromString(bar.readString());
            supportedCompCS = loadListFromString(bar.readString());
            supportedCompSC = loadListFromString(bar.readString());
            supportedLangCS = loadListFromString(bar.readString());

            supportedLangSC = loadListFromString(bar.readString());

            firstKexFollows = (bar.read() == 0) ? false : true;
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("Error reading message data: "
                    + ioe.getMessage());
        }
    }

    private List loadListFromString(String str) {
        // Create a tokeizer object
        StringTokenizer tok = new StringTokenizer(str, ",");

        List ret = new ArrayList();

        // Iterate through the tokens adding the items to the list
        while (tok.hasMoreElements()) {
            ret.add(tok.nextElement());
        }

        return ret;
    }

    private List sortAlgorithmList(List list, String pref) {
        if (list.contains(pref)) {
            // Remove the prefered from the list wherever it may be
            list.remove(pref);

            // Add it to the beginning of the list
            list.add(0, pref);
        }

        return list;
    }
}
