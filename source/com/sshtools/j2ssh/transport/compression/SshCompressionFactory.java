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
package com.sshtools.j2ssh.transport.compression;

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
 * Creates new instances of SshCompression objects. Will load additional
 * compression methods from the SSH API configuration file sshtools.xml
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SshCompressionFactory {
    /** Defines the none method */
    public final static String COMP_NONE = "none";
    private static String defaultAlgorithm;
    private static Map comps;
    private static Logger log = Logger.getLogger(SshCompressionFactory.class);

    static {
        comps = new HashMap();

        log.info("Loading compression methods");

        comps.put(COMP_NONE, "");

        // Now load external compression from configuration file
        SshAPIConfiguration config = ConfigurationLoader.getAPIConfiguration();

        if (config!=null) {

                List list = config.getCompressionExtensions();

                if (list!=null) {
                    Iterator it = list.iterator();

                    while (it.hasNext()) {
                        ExtensionAlgorithm algorithm =
                            (ExtensionAlgorithm) it.next();

                        String name = algorithm.getAlgorithmName();

                        if (comps.containsKey(name)) {
                            log.debug("Standard compression method " + name
                                      + " is being overidden by "
                                      + algorithm.getImplementationClass());
                        } else {
                            log.debug(algorithm.getAlgorithmName()
                                      + " compression is implemented by "
                                      + algorithm.getImplementationClass());
                        }

                        try {
                            comps.put(algorithm.getAlgorithmName(),
                                      ConfigurationLoader.getExtensionClass(algorithm
                                                                            .getImplementationClass()));
                        } catch (ClassNotFoundException cnfe) {
                            log.error("Could not locate "
                                      + algorithm.getImplementationClass());
                        }
                    }

                    // end while
                }

                // end if
            defaultAlgorithm = config.getDefaultCompression();
        }

        if ((defaultAlgorithm==null) || !comps.containsKey(defaultAlgorithm)) {
            defaultAlgorithm = COMP_NONE;
        }
    }

    /**
     * Constructor for the SshCompressionFactory object
     */
    protected SshCompressionFactory() {
    }

    /**
     * Returns the default compression method.
     *
     * @return the default compression algorithm name
     */
    public static String getDefaultCompression() {
        return defaultAlgorithm;
    }

    /**
     * Returns the supported compression methods available.
     *
     * @return The supported compressions.
     */
    public static List getSupportedCompression() {
        return new ArrayList(comps.keySet());
    }

    /**
     * Create a new instance of the compression method.
     *
     * @param algorithmName The compression algorithm to create
     *
     * @return The new instance
     *
     * @exception AlgorithmNotSupportedException if the algorithm is not
     *            supported
     */
    public static SshCompression newInstance(String algorithmName)
                                      throws AlgorithmNotSupportedException {
        try {
            return (SshCompression) ((Class) comps.get(algorithmName))
                   .newInstance();
        } catch (Exception e) {
            throw new AlgorithmNotSupportedException(algorithmName
                                                     + " is not supported!");
        }
    }
}
