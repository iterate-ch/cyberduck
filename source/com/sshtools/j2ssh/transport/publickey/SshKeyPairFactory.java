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
package com.sshtools.j2ssh.transport.publickey;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.SshAPIConfiguration;
import com.sshtools.j2ssh.configuration.ExtensionAlgorithm;
import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;
import com.sshtools.j2ssh.transport.publickey.dsa.SshDssKeyPair;
import com.sshtools.j2ssh.transport.publickey.rsa.SshRsaKeyPair;


/**
 * This factory object creates instances of SshKeyPair objects. Further public
 * key mechanisms can be configured by placing an entry in the SSH API
 * configuration file sshtools.xml.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SshKeyPairFactory {
    private static Map pks;
    private static String defaultAlgorithm;
    private static Logger log = Logger.getLogger(SshKeyPairFactory.class);

    static {
        pks = new HashMap();

        log.info("Loading public key algorithms");

        pks.put("ssh-dss", SshDssKeyPair.class);
        pks.put("ssh-rsa", SshRsaKeyPair.class);

        // Load external pks from configuration file
        SshAPIConfiguration config = ConfigurationLoader.getAPIConfiguration();

        if (config!=null) {

                List list = config.getPublicKeyExtensions();

                if (list!=null) {
                    Iterator it = list.iterator();

                    while (it.hasNext()) {
                        ExtensionAlgorithm algorithm =
                            (ExtensionAlgorithm) it.next();

                        String name = algorithm.getAlgorithmName();

                        if (pks.containsKey(name)) {
                            log.debug("Standard public key " + name
                                      + " is being overidden by "
                                      + algorithm.getImplementationClass());
                        } else {
                            log.debug(algorithm.getAlgorithmName()
                                      + " public key is implemented by "
                                      + algorithm.getImplementationClass());
                        }

                        try {
                            pks.put(algorithm.getAlgorithmName(),
                                    ConfigurationLoader.getExtensionClass(algorithm
                                                                          .getImplementationClass()));
                        } catch (ClassNotFoundException cnfe) {
                            log.error("Could not locate "
                                      + algorithm.getImplementationClass());
                        }
                    }
                }


            defaultAlgorithm = config.getDefaultPublicKey();
        }

        if ((defaultAlgorithm==null) || !pks.containsKey(defaultAlgorithm)) {
            log.debug("The default public key is not set! using first in list");

            Iterator it = pks.keySet().iterator();
            defaultAlgorithm = (String) it.next();
        }
    }

    /**
     * Constructor for the SshPublicKeyFactory object
     */
    protected SshKeyPairFactory() {
    }

    /**
     * Gets the default public key algorithm
     *
     * @return the default public key algorithm name
     */
    public static String getDefaultPublicKey() {
        return defaultAlgorithm;
    }

    /**
     * Gets the supported public key algorithms
     *
     * @return The supported public key algorithms
     */
    public static List getSupportedKeys() {
        // Get the list of pks
        return new ArrayList(pks.keySet());
    }

    /**
     * Creates a new instance of the key pair
     *
     * @param methodName the public key algorithm
     *
     * @return The new instance
     *
     * @exception AlgorithmNotSupportedException if the algorithm is not
     *            supported
     */
    public static SshKeyPair newInstance(String methodName)
                                  throws AlgorithmNotSupportedException {
        try {
            return (SshKeyPair) ((Class) pks.get(methodName)).newInstance();
        } catch (Exception e) {
            throw new AlgorithmNotSupportedException(methodName
                                                     + " is not supported!");
        }
    }

    /**
     * Determines if the algorithm specified is supported
     *
     * @param algorithm the public key algorithm to verify
     *
     * @return <tt>true</tt> if the algorithm is supported otherwise
     *         <tt>false</tt>
     */
    public static boolean supportsKey(String algorithm) {
        return pks.containsKey(algorithm);
    }
}
