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
import com.sshtools.j2ssh.session.SessionDataProvider;


/**
 * <p>
 * This interface should be implemented for each platform. It should implement
 * the creation of a process through the method start and provide a standard
 * set of input/output streams to redirect the processes stdin/stdout
 * </p>
 * To configure the process provider edit the NativeProcessProvider element in
 * the platform configuration file platform.xml.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public abstract class NativeProcessProvider
    implements SessionDataProvider {
    private static Logger log = Logger.getLogger(NativeProcessProvider.class);
    private static Class implementor;

    static {
        try {
            PlatformConfiguration platform =
                ConfigurationLoader.getPlatformConfiguration();

            if (platform!=null) {
                implementor =
                    ConfigurationLoader.getExtensionClass(platform
                                                          .getNativeProcessProvider());
            }
        } catch (Exception e) {
            log.error("Failed to load native process provider", e);
            implementor = null;
        }
    }

    /**
     * Creates a new instance of the native process provider
     *
     * @return a newly created process provider
     */
    public static NativeProcessProvider newInstance() {
        try {
            return (NativeProcessProvider) implementor.newInstance();
        } catch (Exception e) {
            log.error("Failed to create native process provider instance", e);

            return null;
        }
    }

    /**
     * Stops/Kills the process
     */
    public abstract void kill();

    /**
     * Creates a process
     *
     * @param command The command to execute
     * @param environment A Map of name-value pairs for attempted environment
     *        settings
     * @param nativeSettings A map of name/value pairs of native settings
     *
     * @return
     */
    public abstract boolean start(String command, Map environment,
                                  Map nativeSettings);
}
