package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;
import ch.cyberduck.core.test.NullLocal;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class OpenSSHCredentialsConfiguratorTest extends AbstractTestCase {

    @Test
    public void testNoConfigure() throws Exception {
        OpenSSHCredentialsConfigurator c = new OpenSSHCredentialsConfigurator(
                new OpenSshConfig(
                        new Local("test/ch/cyberduck/core/sftp", "openssh/config")));
        Credentials credentials = new Credentials("user", " ");
        credentials.setIdentity(new NullLocal("t"));
        assertEquals("t", c.configure(new Host(new SFTPProtocol(), "t", credentials)).getIdentity().getName());
    }

    @Test
    public void testConfigureKnownHost() throws Exception {
        OpenSSHCredentialsConfigurator c = new OpenSSHCredentialsConfigurator(
                new OpenSshConfig(
                        new Local("test/ch/cyberduck/core/sftp", "openssh/config")));
        final Host host = new Host("alias");
        final Credentials credentials = c.configure(host);
        assertSame(host.getCredentials(), credentials);
        assertNotNull(credentials.getIdentity());
        assertEquals(new Local("~/.ssh/version.cyberduck.ch-rsa"), credentials.getIdentity());
        assertEquals("root", credentials.getUsername());
    }

    @Test
    public void testConfigureDefaultKey() throws Exception {
        OpenSSHCredentialsConfigurator c = new OpenSSHCredentialsConfigurator(
                new OpenSshConfig(
                        new Local("test/ch/cyberduck/core/sftp", "openssh/config")));
        final Credentials credentials = c.configure(new Host("t"));
        // ssh.authentication.publickey.default.enable
        assertNull(credentials.getIdentity());
    }

    @Test
    public void testNullHostname() throws Exception {
        OpenSSHCredentialsConfigurator c = new OpenSSHCredentialsConfigurator(
                new OpenSshConfig(
                        new Local("test/ch/cyberduck/core/sftp", "openssh/config")));
        assertNotNull(c.configure(new Host(ProtocolFactory.SFTP, null)));
    }
}
