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
package com.sshtools.j2ssh.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Provides a simple console request mechanism for host key verification
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class ConsoleHostKeyVerification
    extends HostKeyVerification {
    /**
     * Creates a new ConsoleHostKeyVerification object.
     *
     * @throws InvalidHostFileException if the host file does not exist or is
     *         invalid
     */
    public ConsoleHostKeyVerification()
                               throws InvalidHostFileException {
        super();
    }

    /**
     * Creates a new ConsoleHostKeyVerification object.
     *
     * @param hostFile the path to the host verification file
     *
     * @throws InvalidHostFileException if the host file does not exist or is
     *         invalid
     */
    public ConsoleHostKeyVerification(String hostFile)
                               throws InvalidHostFileException {
        super(hostFile);
    }

    /**
     * Called by the host verification framework when the attempted connections
     * host has been denied access to this computer
     *
     * @param hostname the denied hostname
     */
    public void onDeniedHost(String hostname) {
        System.out.println("Access to the host " + hostname
                           + " is denied from this system");
    }

    /**
     * Called by the host verification framework when the attempted connections
     * host supplies a fingerprint that is different to the allowed
     * fingerprint. This implementation requests that the user either confirm
     * or deny access to the host via the System console.
     *
     * @param host the connected host
     * @param fingerprint the fingerprint supplied
     * @param actual the actual allowed fingerprint
     */
    public void onHostKeyMismatch(String host, String fingerprint, String actual) {
        try {
            System.out.println("The host key supplied by " + host + " is: "
                               + actual);
            System.out.println("The current allowed key for " + host + " is: "
                               + fingerprint);
            getResponse(host, actual);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called by the host verification framework when the host is unknown. This
     * implementation requests that the user either allow or deny the host
     * access, optionally recording the fingerprint for further verification.
     *
     * @param host the connected host
     * @param fingerprint the hosts public key fingerprint
     */
    public void onUnknownHost(String host, String fingerprint) {
        try {
            System.out.println("The host " + host
                               + " is currently unknown to the system");
            System.out.println("The host key fingerprint is: " + fingerprint);
            getResponse(host, fingerprint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Asks the user for either a Yes, No or Always response to allow the host
     * access.
     *
     * @param host the connected host to verify
     * @param fingerprint the hosts fingerprint
     *
     * @throws InvalidHostFileException if the host file does not exist or is
     *         invalid
     * @throws IOException if an IO error occurs
     */
    private void getResponse(String host, String fingerprint)
                      throws InvalidHostFileException, IOException {
        String response = "";
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));

        while (!(response.equalsIgnoreCase("YES")
                   || response.equalsIgnoreCase("NO")
                   || (response.equalsIgnoreCase("ALWAYS")
                   && isHostFileWriteable()))) {
            String options =
                (isHostFileWriteable() ? "Yes|No|Always" : "Yes|No");

            if (!isHostFileWriteable()) {
                System.out.println("Always option disabled, host file is not writeable");
            }

            System.out.print("Do you want to allow this host key? [" + options
                             + "]: ");

            response = reader.readLine();
        }

        if (response.equalsIgnoreCase("YES")) {
            allowHost(host, fingerprint, false);
        }

        if (response.equalsIgnoreCase("NO")) {
            System.out.println("Cannot continue without a valid host key");
            System.exit(1);
        }

        if (response.equalsIgnoreCase("ALWAYS") && isHostFileWriteable()) {
            allowHost(host, fingerprint, true);
        }
    }
}
