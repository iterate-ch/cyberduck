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

import org.apache.commons.io.FileUtils;
import org.apache.kerby.kerberos.kerb.client.KrbClient;
import org.apache.kerby.kerberos.kerb.server.SimpleKdcServer;
import org.apache.kerby.util.NetworkUtil;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Kerberos KDC running in this JVM issuing a ticket for the test user saved to a credentials cache. The JVM-wide
 * Kerberos configuration points to the KDC until closed. The configuration has no default realm and no mapping of
 * hosts to realms.
 */
public final class KerberosTestKdc implements AutoCloseable {

    public static final String REALM = "EXAMPLE.COM";
    public static final String PRINCIPAL = String.format("test@%s", REALM);

    private final File directory;
    private final SimpleKdcServer kdc;
    /**
     * Previous JVM-wide Kerberos configuration to restore
     */
    private final String configuration;

    public KerberosTestKdc() throws Exception {
        directory = Files.createTempDirectory("kdc").toFile();
        final int port = NetworkUtil.getServerPort();
        kdc = new SimpleKdcServer();
        kdc.setWorkDir(directory);
        kdc.setKdcHost("localhost");
        kdc.setKdcRealm(REALM);
        kdc.setAllowUdp(false);
        kdc.setAllowTcp(true);
        kdc.setKdcTcpPort(port);
        kdc.init();
        kdc.start();
        final String password = UUID.randomUUID().toString();
        kdc.createPrincipal(PRINCIPAL, password);
        final File config = new File(directory, "client-krb5.conf");
        try(PrintWriter w = new PrintWriter(config)) {
            w.println("[libdefaults]");
            w.println("    udp_preference_limit = 1");
            w.println("    dns_lookup_kdc = false");
            w.println("    dns_lookup_realm = false");
            // Service ticket must be requested for the hostname connected to matching the keytab
            w.println("    dns_canonicalize_hostname = false");
            w.println("    rdns = false");
            w.println("[realms]");
            w.println("    " + REALM + " = {");
            w.println("        kdc = localhost:" + port);
            w.println("    }");
        }
        configuration = System.getProperty("java.security.krb5.conf");
        System.setProperty("java.security.krb5.conf", config.getAbsolutePath());
        // Equivalent to kinit
        final KrbClient client = kdc.getKrbClient();
        client.storeTicket(client.requestTgt(PRINCIPAL, password), this.ticketCache());
    }

    /**
     * @return Credentials cache with ticket granting ticket for {@link #PRINCIPAL}
     */
    public File ticketCache() {
        return new File(directory, "krb5cc");
    }

    /**
     * @param hostname Hostname of SSH server
     * @return Keytab with new service principal <code>host/hostname@EXAMPLE.COM</code>
     */
    public File keytab(final String hostname) throws Exception {
        final File keytab = new File(directory, String.format("%s.keytab", UUID.randomUUID()));
        kdc.createAndExportPrincipals(keytab, service(hostname));
        return keytab;
    }

    /**
     * @return Service principal for SSH server
     */
    public static String service(final String hostname) {
        return String.format("host/%s@%s", hostname, REALM);
    }

    @Override
    public void close() throws Exception {
        try {
            kdc.stop();
        }
        finally {
            if(null == configuration) {
                System.clearProperty("java.security.krb5.conf");
            }
            else {
                System.setProperty("java.security.krb5.conf", configuration);
            }
            FileUtils.deleteQuietly(directory);
        }
    }
}
