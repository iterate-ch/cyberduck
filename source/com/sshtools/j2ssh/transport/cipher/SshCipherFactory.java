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
package com.sshtools.j2ssh.transport.cipher;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sshtools.j2ssh.configuration.SshAPIConfiguration;
import com.sshtools.j2ssh.configuration.ExtensionAlgorithm;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;


/**
 * Creates instances of all available ciphers. Additional ciphers can be
 * implemented by using the SshCipher interface and adding a CipherAlgorithm
 * element into the CipherConfiguraton element of the SSH API configuraiton
 * file sshtools.xml
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SshCipherFactory {
    private static Map ciphers;
    private static String defaultCipher;
    private static Logger log = Logger.getLogger(SshCipherFactory.class);

    static {
        ciphers = new HashMap();

        log.info("Loading supported cipher algorithms");

        ciphers.put("3des-cbc", TripleDesCbc.class);
        ciphers.put("blowfish-cbc", BlowfishCbc.class);

        // Load ciphers from configuration file
        SshAPIConfiguration config = ConfigurationLoader.getAPIConfiguration();

        if (config!=null) {

            List addons = config.getCipherExtensions();

            Iterator it = addons.iterator();

            // Add the ciphers to our supported list
            while (it.hasNext()) {
                ExtensionAlgorithm cipherAlgorithm = (ExtensionAlgorithm) it.next();

                String name = cipherAlgorithm.getAlgorithmName();

                if (ciphers.containsKey(cipherAlgorithm.getAlgorithmName())) {
                    log.debug("Standard cipher " + name
                              + " is being overidden by "
                              + cipherAlgorithm.getImplementationClass());
                } else {
                    log.debug(cipherAlgorithm.getAlgorithmName()
                              + " cipher is implemented by "
                              + cipherAlgorithm.getImplementationClass());
                }

                try {
                    ciphers.put(cipherAlgorithm.getAlgorithmName(),
                                ConfigurationLoader.getExtensionClass(cipherAlgorithm
                                                                      .getImplementationClass()));
                } catch (ClassNotFoundException cnfe) {
                    log.error("Could not locate "
                              + cipherAlgorithm.getImplementationClass());
                }
            }

            defaultCipher = config.getDefaultCipher();
        }

        // If no default cipher is set or the cipher is incorrect
        if ((defaultCipher==null) || !ciphers.containsKey(defaultCipher)) {
            log.debug("The default cipher is not set! using first in list");

            Iterator it = ciphers.keySet().iterator();
            defaultCipher = (String) it.next();
        }
    }

    /**
     * Constructor for the SshCipherFactory object
     */
    protected SshCipherFactory() {
    }

    /**
     * Returns the configurations default cipher.
     *
     * @return the default cipher algorithm name
     */
    public static String getDefaultCipher() {
        return defaultCipher;
    }

    /**
     * Returns the list of supported ciphers.
     *
     * @return The supported Ciphers
     */
    public static List getSupportedCiphers() {
        // Get the list of ciphers
        ArrayList list = new ArrayList(ciphers.keySet());

        // Remove the default from wherever it may be
        list.remove(defaultCipher);

        // Add it to the top of the list
        list.add(0, defaultCipher);

        // Return the list
        return list;
    }

    /**
     * Creates a new instance of the algorithm.
     *
     * @param algorithmName The name of the algorithm to create
     *
     * @return The new instance
     *
     * @exception AlgorithmNotSupportedException if the algorithm is not
     *            supported
     */
    public static SshCipher newInstance(String algorithmName)
                                 throws AlgorithmNotSupportedException {
        log.info("Creating new " + algorithmName + " cipher instance");

        try {
            return (SshCipher) ((Class) ciphers.get(algorithmName)).newInstance();
        } catch (Exception e) {
            throw new AlgorithmNotSupportedException(algorithmName
                                                     + " is not supported!");
        }
    }
}
