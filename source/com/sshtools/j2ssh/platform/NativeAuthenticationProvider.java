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
package com.sshtools.j2ssh.platform;

import org.apache.log4j.Logger;

import java.util.Map;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.PlatformConfiguration;


/**
 * <p>
 * This abstract class defines the native platform authentication provider. By
 * extending and implementing the abstract methods of this class specific
 * authentication operations can be performed on any platform type. This may
 * involve calling native methods through the JNI.
 * </p>
 *
 * <p>
 * Calling <code>getInstance</code> returns the currently configured
 * authentication provider. To configure set the NativeAuthenticationProvider
 * element in the platform configuration file platform.xml.
 * </p>
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public abstract class NativeAuthenticationProvider {
    private static Logger log =
        Logger.getLogger(NativeAuthenticationProvider.class);
    private static NativeAuthenticationProvider instance;

    static {
        try {
            PlatformConfiguration platform =
                ConfigurationLoader.getPlatformConfiguration();

            /**
             * TODO: We need to allow for dynamic loading of classes from
             * extension jars specified in the platform configuration
             */
            if (platform!=null) {
                Class cls =
                    ConfigurationLoader.getExtensionClass(platform
                                                          .getNativeAuthenticationProvider());

                instance = (NativeAuthenticationProvider) cls.newInstance();
            }
        } catch (Exception e) {
            log.error("Failed to load native authentication provider", e);
            instance = null;
        }
    }

    /**
     * Gets the users home directory
     *
     * @param username the username
     * @param tokens     a map of name/value pairs for storing platform specific
     *        information that might be required by subsequent native calls
     *
     * @return the users home directory
     */
    public abstract String getHomeDirectory(String username, Map tokens);

    /**
     * Logs a user into the system using password authentication
     *
     * @param username the users name
     * @param password the users password
     * @param tokens a map of name/value pairs for storing platform specific
     *        information that might be required by subsequent native calls
     *
     * @return <tt>true</tt> if the logon succeeded otherwise <tt>false</tt>
     */
    public abstract boolean logonUser(String username, String password,
                                      Map tokens);

    /**
     * Logs a user into the system
     *
     * @param username the users name
     * @param tokens a map of name/value pairs for storing platform specific
     *        information that might be required by subsequent native calls
     *
     * @return <tt>true</tt> if the logon succeeded otherwise <tt>false</tt>
     */
    public abstract boolean logonUser(String username, Map tokens);

    /**
     * Gets the current instance of the configured native authentication
     * provider.
     *
     * @return the currently configured authenicaton provider
     */
    public static NativeAuthenticationProvider getInstance() {
        return instance;
    }
}
