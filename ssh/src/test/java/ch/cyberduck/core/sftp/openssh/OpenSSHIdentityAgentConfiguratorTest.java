package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OpenSSHIdentityAgentConfiguratorTest {

    private final OpenSSHIdentityAgentConfigurator configurator = new OpenSSHIdentityAgentConfigurator(
        new OpenSshConfig(new Local("src/test/resources", "openssh/config-wildcard")));

    @Test
    public void testNoneDisablesAgent() {
        assertNull(configurator.getIdentityAgent("x"));
    }

    @Test
    public void testConfigured() {
        assertEquals("/other.sock", configurator.getIdentityAgent("y"));
    }

    @Test
    public void testNotConfiguredUsesDefault() {
        final String expected;
        if(null != System.getenv("SSH_AUTH_SOCK")) {
            expected = System.getenv("SSH_AUTH_SOCK");
        }
        else {
            switch(Factory.Platform.getDefault()) {
                case windows:
                    expected = WindowsOpenSSHAgentAuthenticator.SSH_AGENT_PIPE;
                    break;
                default:
                    expected = null;
                    break;
            }
        }
        assertEquals(expected, new OpenSSHIdentityAgentConfigurator(
            new OpenSshConfig(new Local("src/test/resources", "openssh/included"))).getIdentityAgent("unknown"));
    }
}
