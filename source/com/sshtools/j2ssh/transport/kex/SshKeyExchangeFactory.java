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
package com.sshtools.j2ssh.transport.kex;

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


/**
 * Creates new instances of SshKeyExchange objects. Will load additional key
 * exchange methods from the SSH API configuration file ssh.xml.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SshKeyExchangeFactory {
    private static Map kexs;
    private static String defaultAlgorithm;
    private static Logger log = Logger.getLogger(SshKeyExchangeFactory.class);

    static {
        kexs = new HashMap();

        log.info("Loading key exchange methods");

        kexs.put("diffie-hellman-group1-sha1", DhGroup1Sha1.class);

        // Load external compression from configuration file
        SshAPIConfiguration config = ConfigurationLoader.getAPIConfiguration();

        if (config!=null) {

                List list = config.getKeyExchangeExtensions();

                if (list!=null) {
                    Iterator it = list.iterator();

                    while (it.hasNext()) {
                        ExtensionAlgorithm algorithm =
                                (ExtensionAlgorithm) it.next();

                        String name = algorithm.getAlgorithmName();

                        if (kexs.containsKey(name)) {
                            log.debug("Standard key exchange " + name
                                      + " is being overidden by "
                                      + algorithm.getImplementationClass());
                        } else {
                            log.debug(algorithm.getAlgorithmName()
                                      + " key exchange is implemented by "
                                      + algorithm.getImplementationClass());
                        }

                        try {
                            kexs.put(algorithm.getAlgorithmName(),
                                     ConfigurationLoader.getExtensionClass(algorithm
                                                                           .getImplementationClass()));
                        } catch (ClassNotFoundException cnfe) {
                            log.error("Could not locate "
                                      + algorithm.getImplementationClass());
                        }
                    }
                }


            defaultAlgorithm = config.getDefaultKeyExchange();
        }

        if ((defaultAlgorithm==null) || !kexs.containsKey(defaultAlgorithm)) {
            log.debug("The default key exchange is not set! using first in list");

            Iterator it = kexs.keySet().iterator();
            defaultAlgorithm = (String) it.next();
        }
    }

    /**
     * Constructor for the SshKeyExchangeFactory object
     */
    protected SshKeyExchangeFactory() {
    }

    /**
     * Gets the default key exchange method name
     *
     * @return the default key exchange algorithm name
     */
    public static String getDefaultKeyExchange() {
        return defaultAlgorithm;
    }

    /**
     * Gets the supported key exchange methods
     *
     * @return The supported key exchanges
     */
    public static List getSupportedKeyExchanges() {
        return new ArrayList(kexs.keySet());
    }

    /**
     * Creates a new instance of the key exchange method specified
     *
     * @param methodName The key exchange method
     *
     * @return The new instance
     *
     * @exception AlgorithmNotSupportedException if the method is not supported
     */
    public static SshKeyExchange newInstance(String methodName)
                                      throws AlgorithmNotSupportedException {
        try {
            return (SshKeyExchange) ((Class) kexs.get(methodName)).newInstance();
        } catch (Exception e) {
            throw new AlgorithmNotSupportedException(methodName
                                                     + " is not supported!");
        }
    }
}
