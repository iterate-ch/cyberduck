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
package com.sshtools.j2ssh.transport.kex;

import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshKeyExchangeFactory {
    private static Map kexs;
    private static String defaultAlgorithm;
    private static Log log = LogFactory.getLog(SshKeyExchangeFactory.class);

    static {
        kexs = new HashMap();
        log.info("Loading key exchange methods");
        kexs.put("diffie-hellman-group1-sha1", DhGroup1Sha1.class);

        if ((defaultAlgorithm == null) || !kexs.containsKey(defaultAlgorithm)) {
            log.debug("The default key exchange is not set! using first in list");

            Iterator it = kexs.keySet().iterator();
            defaultAlgorithm = (String) it.next();
        }
    }

    /**
     * Creates a new SshKeyExchangeFactory object.
     */
    protected SshKeyExchangeFactory() {
    }

    /**
     * @return
     */
    public static String getDefaultKeyExchange() {
        return defaultAlgorithm;
    }

    /**
     * @return
     */
    public static List getSupportedKeyExchanges() {
        return new ArrayList(kexs.keySet());
    }

    /**
     * @param methodName
     * @return
     * @throws AlgorithmNotSupportedException
     */
    public static SshKeyExchange newInstance(String methodName)
            throws AlgorithmNotSupportedException {
        try {
            return (SshKeyExchange) ((Class) kexs.get(methodName)).newInstance();
        }
        catch (Exception e) {
            throw new AlgorithmNotSupportedException(methodName +
                    " is not supported!");
        }
    }
}
