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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OpenSSHHostnameConfiguratorTest {

    @Test
    public void testLookup() {
        OpenSSHHostnameConfigurator c = new OpenSSHHostnameConfigurator(
                new OpenSshConfig(
                        new Local("src/test/resources", "openssh/config")));
        assertEquals("cyberduck.ch", c.getHostname("alias"));
    }

    @Test
    public void testJumpHostAlias() {
        OpenSSHHostnameConfigurator c = new OpenSSHHostnameConfigurator(
                new OpenSshConfig(
                        new Local("src/test/resources", "openssh/config")));
        final Host jumpHost = c.getJumphost("remote-host-nickname");
        assertEquals("bastion-hostname", jumpHost.getHostname());
        assertEquals("", jumpHost.getCredentials().getUsername());
        assertEquals(22, jumpHost.getPort());
    }

    @Test
    public void testJumpHost() {
        OpenSSHHostnameConfigurator c = new OpenSSHHostnameConfigurator(
                new OpenSshConfig(
                        new Local("src/test/resources", "openssh/config")));
        final Host jumpHost = c.getJumphost("server2");
        assertEquals("jumphost1.example.org", jumpHost.getHostname());
        assertEquals("user1", jumpHost.getCredentials().getUsername());
        assertEquals(22, jumpHost.getPort());
    }

    @Test
    public void testPort() {
        OpenSSHHostnameConfigurator c = new OpenSSHHostnameConfigurator(
                new OpenSshConfig(
                        new Local("src/test/resources", "openssh/config")));
        assertEquals(555, c.getPort("portalias"));
        assertEquals(-1, c.getPort(null));
    }
}
