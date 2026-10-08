package ch.cyberduck.core.sftp.auth;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Captures the debug output of the Kerberos and GSS-API implementation of Java, which is printed to the standard
 * streams, and writes it to the log when closed. The system properties are read once when the implementation is first
 * used, so output is complete only for the first authentication attempt after the application was started.
 */
final class KerberosDebug implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(KerberosDebug.class);

    private static final String[] PROPERTIES = {"sun.security.krb5.debug", "sun.security.jgss.debug"};

    private final PrintStream out = System.out;
    private final PrintStream err = System.err;
    private final String[] saved = new String[PROPERTIES.length];
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    KerberosDebug() {
        for(int i = 0; i < PROPERTIES.length; i++) {
            saved[i] = System.getProperty(PROPERTIES[i]);
            System.setProperty(PROPERTIES[i], "true");
        }
        try {
            final PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8.name());
            System.setOut(capture);
            System.setErr(capture);
        }
        catch(UnsupportedEncodingException e) {
            // Cannot happen for UTF-8
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        System.setOut(out);
        System.setErr(err);
        for(int i = 0; i < PROPERTIES.length; i++) {
            if(null == saved[i]) {
                System.clearProperty(PROPERTIES[i]);
            }
            else {
                System.setProperty(PROPERTIES[i], saved[i]);
            }
        }
        try {
            log.debug("Kerberos debug output:\n{}", buffer.toString(StandardCharsets.UTF_8.name()));
        }
        catch(UnsupportedEncodingException e) {
            log.warn("Failure reading Kerberos debug output: {}", e.getMessage());
        }
    }
}
