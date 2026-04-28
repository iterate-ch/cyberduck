package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OpenSshConfigTest {

    @Test
    public void testIncludeSpecificFile() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-include-specific"));
        final OpenSshConfig.Host host = config.lookup("include-host-a");
        assertEquals("host-a.example.com", host.getHostName());
        assertEquals("auser", host.getUser());
        assertEquals(2222, host.getPort());
    }

    @Test
    public void testIncludeHostFromMainConfigAlsoAvailable() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-include-specific"));
        final OpenSshConfig.Host host = config.lookup("main-host");
        assertEquals("main.example.com", host.getHostName());
        assertEquals("mainuser", host.getUser());
    }

    @Test
    public void testIncludeGlob() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-include-glob"));
        final OpenSshConfig.Host hostA = config.lookup("include-host-a");
        assertEquals("host-a.example.com", hostA.getHostName());
        assertEquals("auser", hostA.getUser());
        final OpenSshConfig.Host hostB = config.lookup("include-host-b");
        assertEquals("host-b.example.com", hostB.getHostName());
        assertEquals("buser", hostB.getUser());
    }

    @Test
    public void testIncludePrecedence() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-include-precedence"));
        final OpenSshConfig.Host host = config.lookup("include-host-a");
        // Main config definition must take precedence over included file
        assertEquals("override", host.getUser());
    }

    @Test
    public void testIncludeMissingFileIsIgnored() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-include-missing"));
        // Missing include must be silently ignored; remaining hosts are still loaded
        final OpenSshConfig.Host host = config.lookup("main-host");
        assertEquals("main.example.com", host.getHostName());
    }

    @Test
    public void testCircularIncludeDoesNotLoop() {
        // A config that includes itself must not cause infinite recursion
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-include-circular"));
        final OpenSshConfig.Host host = config.lookup("circular-host");
        assertEquals("circular.example.com", host.getHostName());
    }
}
