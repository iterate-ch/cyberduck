package ch.cyberduck.core.sftp;

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
import ch.cyberduck.core.DefaultCredentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.NullLocal;

import org.junit.Test;
import org.spearce.jgit.transport.OpenSshConfig;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class OpenSSHCredentialsConfiguratorTest extends AbstractTestCase {

    @Test
    public void testNoConfigure() throws Exception {
        OpenSSHCredentialsConfigurator c = new OpenSSHCredentialsConfigurator(
                new OpenSshConfig(
                        new File(LocalFactory.createLocal("test/ch/cyberduck/core/sftp", "openssh/config").getAbsolute())));
        Credentials credentials = new DefaultCredentials("user", " ");
        credentials.setIdentity(new NullLocal(null, "t"));
        c.configure(new Host(new SFTPProtocol(), "t", credentials));
        assertEquals("t", credentials.getIdentity().getName());
    }

    @Test
    public void testConfigureKnownHost() throws Exception {
        OpenSSHCredentialsConfigurator c = new OpenSSHCredentialsConfigurator(
                new OpenSshConfig(
                        new File(LocalFactory.createLocal("test/ch/cyberduck/core/sftp", "openssh/config").getAbsolute())));
        final Credentials credentials = c.configure(new Host("alias"));
        assertNotNull(credentials.getIdentity());
        assertEquals(LocalFactory.createLocal("~/.ssh/version.cyberduck.ch-rsa"), credentials.getIdentity());
        assertEquals("root", credentials.getUsername());
    }

    @Test
    public void testConfigureDefaultKey() throws Exception {
        OpenSSHCredentialsConfigurator c = new OpenSSHCredentialsConfigurator(
                new OpenSshConfig(
                        new File(LocalFactory.createLocal("test/ch/cyberduck/core/sftp", "openssh/config").getAbsolute())));
        final Credentials credentials = c.configure(new Host("t"));
        // ssh.authentication.publickey.default.enable
        assertNull(credentials.getIdentity());
    }
}
