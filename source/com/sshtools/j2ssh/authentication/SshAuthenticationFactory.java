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
package com.sshtools.j2ssh.authentication;

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
 * Factory object to create instances of SSH Authentication objects. Additional
 * authentication methods can be defined in the API configuration file
 * sshtools.xml by adding an lt;AuthenticationMethod&gt; element under the
 * &lt;AuthenticationConfiguration&gt; element.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SshAuthenticationFactory {
    private static Map auths;
    private static Logger log =
        Logger.getLogger(SshAuthenticationFactory.class);

    /** The Password authentication method */
    public final static String AUTH_PASSWORD = "password";

    /** The Public Key authentication method */
    public final static String AUTH_PK = "publickey";

    static {
        auths = new HashMap();

        log.info("Loading supported authentication methods");

        auths.put(AUTH_PASSWORD, PasswordAuthentication.class);
        auths.put(AUTH_PK, PublicKeyAuthentication.class);

        // Load external methods from configuration file
        SshAPIConfiguration config = ConfigurationLoader.getAPIConfiguration();

        if (config!=null) {

        List addons = config.getAuthenticationExtensions();

            Iterator it = addons.iterator();

            // Add the methods to our supported list
            while (it.hasNext()) {
                ExtensionAlgorithm method = (ExtensionAlgorithm) it.next();

                String name = method.getAlgorithmName();

                if (auths.containsKey(name)) {
                    log.debug("Standard authentication implementation for "
                              + name + " is being overidden by "
                              + method.getImplementationClass());
                } else {
                    log.debug(name + " authentication is implemented by "
                              + method.getImplementationClass());
                }

                try {
                    auths.put(name,
                              Class.forName(method.getImplementationClass()));
                } catch (ClassNotFoundException cnfe) {
                    log.error("Could not locate "
                              + method.getImplementationClass());
                }
            }
        }
    }

    /**
     * Constructor for SshAuthenticationFactory
     */
    protected SshAuthenticationFactory() {
    }

    /**
     * Gets the supported authentication methods
     *
     * @return A List of Strings containing the authentication methods
     */
    public static List getSupportedMethods() {
        // Get the list of ciphers
        ArrayList list = new ArrayList(auths.keySet());

        // Return the list
        return list;
    }

    /**
     * Creates a new instance of the SshAuthentication object which implements
     * methodName.
     *
     * @param methodName The method name to instansiate
     *
     * @return The instance
     *
     * @exception AlgorithmNotSupportedException if the method is not supported
     */
    public static SshAuthentication newInstance(String methodName)
                                         throws AlgorithmNotSupportedException {
        try {
            return (SshAuthentication) ((Class) auths.get(methodName))
                   .newInstance();
        } catch (Exception e) {
            throw new AlgorithmNotSupportedException(methodName
                                                     + " is not supported!");
        }
    }
}
