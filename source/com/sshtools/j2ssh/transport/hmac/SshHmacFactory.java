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
package com.sshtools.j2ssh.transport.hmac;

import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshHmacFactory {
    private static String defaultAlgorithm;
    private static Map macs;
    private static Log log = LogFactory.getLog(SshHmacFactory.class);

    static {
        macs = new HashMap();

        log.info("Loading message authentication methods");

        macs.put("hmac-sha1", HmacSha.class);
        macs.put("hmac-sha1-96", HmacSha96.class);
        macs.put("hmac-md5", HmacMd5.class);
        macs.put("hmac-md5-96", HmacMd596.class);

        defaultAlgorithm = "hmac-sha1";
    }

    /**
     * Creates a new SshHmacFactory object.
     */
    protected SshHmacFactory() {
    }

    /**
     * @return
     */
    public final static String getDefaultHmac() {
        return defaultAlgorithm;
    }

    /**
     * @return
     */
    public static List getSupportedMacs() {
        return new ArrayList(macs.keySet());
    }

    /**
     * @param methodName
     * @return
     * @throws AlgorithmNotSupportedException
     */
    public static SshHmac newInstance(String methodName)
            throws AlgorithmNotSupportedException {
        try {
            return (SshHmac) ((Class) macs.get(methodName)).newInstance();
        }
        catch (Exception e) {
            throw new AlgorithmNotSupportedException(methodName +
                    " is not supported!");
        }
    }
}
