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
package com.sshtools.j2ssh.transport;

import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * <p>
 * Implements the <code>AbstractKnownHostsKeyVerification</code> to provide
 * host key verification through the console.
 * </p>
 *
 * @author Lee David Painter
 * @version $Revision$
 *
 * @since 0.2.0
 */
public class ConsoleKnownHostsKeyVerification
    extends AbstractKnownHostsKeyVerification {
    /**
     * <p>
     * Constructs the verification instance with the default known_hosts file
     * from $HOME/.ssh/known_hosts.
     * </p>
     *
     * @throws InvalidHostFileException if the known_hosts file is invalid.
     *
     * @since 0.2.0
     */
    public ConsoleKnownHostsKeyVerification() throws InvalidHostFileException {
        super(new File(System.getProperty("user.home"),
                ".ssh" + File.separator + "known_hosts").getAbsolutePath());
    }

    /**
     * <p>
     * Constructs the verification instance with the specified known_hosts
     * file.
     * </p>
     *
     * @param knownhosts the path to the known_hosts file
     *
     * @throws InvalidHostFileException if the known_hosts file is invalid.
     *
     * @since 0.2.0
     */
    public ConsoleKnownHostsKeyVerification(String knownhosts)
        throws InvalidHostFileException {
        super(knownhosts);
    }

    /**
     * <p>
     * Prompts the user through the console to verify the host key.
     * </p>
     *
     * @param host the name of the host
     * @param pk the current public key of the host
     * @param actual the actual public key supplied by the host
     *
     * @since 0.2.0
     */
    public void onHostKeyMismatch(String host, SshPublicKey pk,
        SshPublicKey actual) {
        try {
            System.out.println("The host key supplied by " + host + " is: " +
                actual.getFingerprint());
            System.out.println("The current allowed key for " + host + " is: " +
                pk.getFingerprint());
            getResponse(host, pk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * Prompts the user through the console to verify the host key.
     * </p>
     *
     * @param host the name of the host
     * @param pk the public key supplied by the host
     *
     * @since 0.2.0
     */
    public void onUnknownHost(String host, SshPublicKey pk) {
        try {
            System.out.println("The host " + host +
                " is currently unknown to the system");
            System.out.println("The host key fingerprint is: " +
                pk.getFingerprint());
            getResponse(host, pk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getResponse(String host, SshPublicKey pk)
        throws InvalidHostFileException, IOException {
        String response = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    System.in));

        while (!(response.equalsIgnoreCase("YES") ||
                response.equalsIgnoreCase("NO") ||
                (response.equalsIgnoreCase("ALWAYS") && isHostFileWriteable()))) {
            String options = (isHostFileWriteable() ? "Yes|No|Always" : "Yes|No");

            if (!isHostFileWriteable()) {
                System.out.println(
                    "Always option disabled, host file is not writeable");
            }

            System.out.print("Do you want to allow this host key? [" + options +
                "]: ");
            response = reader.readLine();
        }

        if (response.equalsIgnoreCase("YES")) {
            allowHost(host, pk, false);
        }

        if (response.equalsIgnoreCase("NO")) {
            System.out.println("Cannot continue without a valid host key");
            System.exit(1);
        }

        if (response.equalsIgnoreCase("ALWAYS") && isHostFileWriteable()) {
            allowHost(host, pk, true);
        }
    }
}
