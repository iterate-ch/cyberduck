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

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.SshAPIConfiguration;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshPublicKeyFormatFactory {
    private static String defaultFormat;
    private static HashMap formatTypes = new HashMap();
    private static Log log = LogFactory.getLog(SshPublicKeyFormatFactory.class);
    private static Vector types = new Vector();

    static {
        List formats = new ArrayList();
        formats.add(SECSHPublicKeyFormat.class.getName());
        formats.add(OpenSSHPublicKeyFormat.class.getName());
        defaultFormat = "SECSH-PublicKey-Base64Encoded";

        try {
            if (ConfigurationLoader.isConfigurationAvailable(SshAPIConfiguration.class)) {
                SshAPIConfiguration config = (SshAPIConfiguration) ConfigurationLoader.getConfiguration(SshAPIConfiguration.class);
                defaultFormat = config.getDefaultPublicKeyFormat();
                formats.addAll(config.getPublicKeyFormats());
            }
        }
        catch (ConfigurationException ex) {
        }

        log.debug("Default public key format will be " + defaultFormat);

        SshPublicKeyFormat f;
        Iterator it = formats.iterator();
        String classname;

        while (it.hasNext()) {
            classname = (String) it.next();

            try {
                Class cls = ConfigurationLoader.getExtensionClass(classname);
                f = (SshPublicKeyFormat) cls.newInstance();
                log.debug("Installing " + f.getFormatType() +
                        " public key format");
                formatTypes.put(f.getFormatType(), cls);
                types.add(f.getFormatType());
            }
            catch (Exception iae) {
                log.warn("Public key format implemented by " + classname +
                        " will not be available", iae);
            }
        }
    }

    /**
     * @return
     */
    public static List getSupportedFormats() {
        return types;
    }

    /**
     * @param type
     * @return
     * @throws InvalidSshKeyException
     */
    public static SshPublicKeyFormat newInstance(String type)
            throws InvalidSshKeyException {
        try {
            if (formatTypes.containsKey(type)) {
                return (SshPublicKeyFormat) ((Class) formatTypes.get(type)).newInstance();
            }
            else {
                throw new InvalidSshKeyException("The format type " + type +
                        " is not supported");
            }
        }
        catch (IllegalAccessException iae) {
            throw new InvalidSshKeyException("Illegal access to class implementation of " + type);
        }
        catch (InstantiationException ie) {
            throw new InvalidSshKeyException("Failed to create instance of format type " + type);
        }
    }

    /**
     * @return
     */
    public static String getDefaultFormatType() {
        return defaultFormat;
    }
}
