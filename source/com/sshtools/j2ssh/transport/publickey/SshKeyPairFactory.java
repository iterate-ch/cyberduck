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
package com.sshtools.j2ssh.transport.publickey;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;
import com.sshtools.j2ssh.transport.publickey.dsa.SshDssKeyPair;
import com.sshtools.j2ssh.transport.publickey.rsa.SshRsaKeyPair;

import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshKeyPairFactory {
    private static Map pks;
    private static String defaultAlgorithm;
    private static Log log = LogFactory.getLog(SshKeyPairFactory.class);

    static {
        pks = new HashMap();
        log.info("Loading public key algorithms");
        pks.put("ssh-dss", SshDssKeyPair.class);
        pks.put("ssh-rsa", SshRsaKeyPair.class);

        if ((defaultAlgorithm == null) || !pks.containsKey(defaultAlgorithm)) {
            log.debug("The default public key is not set! using first in list");

            Iterator it = pks.keySet().iterator();
            defaultAlgorithm = (String) it.next();
        }
    }

    /**
     * Creates a new SshKeyPairFactory object.
     */
    protected SshKeyPairFactory() {
    }

    /**
     *
     */
    public static void initialize() {
    }

    /**
     * @return
     */
    public static String getDefaultPublicKey() {
        return defaultAlgorithm;
    }

    /**
     * @return
     */
    public static List getSupportedKeys() {
        // Get the list of pks
        return new ArrayList(pks.keySet());
    }

    /**
     * @param methodName
     * @return
     * @throws AlgorithmNotSupportedException
     */
    public static SshKeyPair newInstance(String methodName)
            throws AlgorithmNotSupportedException {
        try {
            return (SshKeyPair) ((Class) pks.get(methodName)).newInstance();
        }
        catch (Exception e) {
            throw new AlgorithmNotSupportedException(methodName +
                    " is not supported!");
        }
    }

    /**
     * @param algorithm
     * @return
     */
    public static boolean supportsKey(String algorithm) {
        return pks.containsKey(algorithm);
    }

    /**
     * @param encoded
     * @return
     * @throws InvalidSshKeyException
     * @throws AlgorithmNotSupportedException
     */
    public static SshPrivateKey decodePrivateKey(byte[] encoded)
            throws InvalidSshKeyException, AlgorithmNotSupportedException {
        try {
            ByteArrayReader bar = new ByteArrayReader(encoded);
            String algorithm = bar.readString();

            if (supportsKey(algorithm)) {
                SshKeyPair pair = newInstance(algorithm);

                return pair.decodePrivateKey(encoded);
            }
            else {
                throw new AlgorithmNotSupportedException(algorithm +
                        " is not supported");
            }
        }
        catch (IOException ioe) {
            throw new InvalidSshKeyException(ioe.getMessage());
        }
    }

    /**
     * @param encoded
     * @return
     * @throws InvalidSshKeyException
     */
    public static SshPublicKey decodePublicKey(byte[] encoded)
            throws InvalidSshKeyException {
        try {
            ByteArrayReader bar = new ByteArrayReader(encoded);
            String algorithm = bar.readString();

            if (supportsKey(algorithm)) {
                SshKeyPair pair = newInstance(algorithm);

                return pair.decodePublicKey(encoded);
            }
            else {
                throw new AlgorithmNotSupportedException(algorithm +
                        " is not supported");
            }
        }
        catch (IOException ioe) {
            throw new InvalidSshKeyException(ioe.getMessage());
        }
    }
}
