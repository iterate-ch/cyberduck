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
package com.sshtools.j2ssh.transport.hmac;

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
 * This factory object creates instances of SshHmac objects. The standard
 * algorithms are supported as well as additional classes described in the
 * ssh.xml configuration file. These algorithms must follow the extensibility
 * naming convention as described in the SSH protocol specification.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SshHmacFactory {
    private static String defaultAlgorithm;
    private static Map macs;
    private static Logger log = Logger.getLogger(SshHmacFactory.class);

    static {
        macs = new HashMap();

        log.info("Loading message authentication methods");

        macs.put("hmac-sha1", HmacSha.class);
        macs.put("hmac-sha1-96", HmacSha96.class);
        macs.put("hmac-md5", HmacMd5.class);
        macs.put("hamc-md5-96", HmacMd596.class);

        SshAPIConfiguration config = ConfigurationLoader.getAPIConfiguration();

        if (config!=null) {

                List supported = config.getMacExtensions();

                if (macs!=null) {
                    Iterator it = supported.iterator();

                    while (it.hasNext()) {
                        ExtensionAlgorithm mac = (ExtensionAlgorithm) it.next();

                        String name = mac.getAlgorithmName();

                        if (macs.containsKey(name)) {
                            log.debug("Standard MAC " + name
                                      + " is being overidden by "
                                      + mac.getImplementationClass());
                        } else {
                            log.debug(mac.getAlgorithmName()
                                      + " message authentication is implemented by "
                                      + mac.getImplementationClass());
                        }

                        try {
                            macs.put(mac.getAlgorithmName(),
                                     ConfigurationLoader.getExtensionClass(mac
                                                                           .getImplementationClass()));
                        } catch (ClassNotFoundException cnfe) {
                            log.error("Could not locate "
                                      + mac.getImplementationClass());
                        }
                    }
                }


            defaultAlgorithm = config.getDefaultMac();
        }

        if ((defaultAlgorithm==null) || !macs.containsKey(defaultAlgorithm)) {
            log.debug("The default message authentication is not set! using first in list");

            Iterator it = macs.keySet().iterator();
            defaultAlgorithm = (String) it.next();
        }
    }

    /**
     * Constructor for the SshHmacFactory object
     */
    protected SshHmacFactory() {
    }

    /**
     * Gets the default message authentication method
     *
     * @return the default hmac algorithm name
     */
    public final static String getDefaultHmac() {
        return defaultAlgorithm;
    }

    /**
     * Gets the supported message authentication methods
     *
     * @return The supported message authentication methods
     */
    public static List getSupportedMacs() {
        return new ArrayList(macs.keySet());
    }

    /**
     * Creates a new instance of the message autentication method
     *
     * @param methodName The method name
     *
     * @return The new instance
     *
     * @exception AlgorithmNotSupportedException if the algorithm name is not
     *            supported
     */
    public static SshHmac newInstance(String methodName)
                               throws AlgorithmNotSupportedException {
        try {
            return (SshHmac) ((Class) macs.get(methodName)).newInstance();
        } catch (Exception e) {
            throw new AlgorithmNotSupportedException(methodName
                                                     + " is not supported!");
        }
    }
}
