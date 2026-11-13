package ch.cyberduck.core.sftp.openssh;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OpenSSHSecurityKeyProviderConfiguratorTest {

    private static OpenSSHSecurityKeyProviderConfigurator configurator(final String value) {
        return new OpenSSHSecurityKeyProviderConfigurator(
                new OpenSshConfig(new Local("src/test/resources", "openssh/config-securitykey-env"))) {
            @Override
            protected String getEnv(final String variable) {
                return "SSH_SK_PROVIDER_TEST".equals(variable) ? value : null;
            }
        };
    }

    @Test
    public void testProviderFromPath() {
        assertEquals("/opt/homebrew/lib/libsk-libfido2.dylib",
                configurator(null).getProvider("path.example.com"));
    }

    /**
     * A value beginning with <code>$</code> is the name of an environment variable
     */
    @Test
    public void testProviderFromEnvironmentVariable() {
        assertEquals("/usr/local/lib/libsk-libfido2.so",
                configurator("/usr/local/lib/libsk-libfido2.so").getProvider("variable.example.com"));
    }

    @Test
    public void testProviderFromUnsetEnvironmentVariable() {
        assertNull(configurator("/usr/local/lib/libsk-libfido2.so").getProvider("missing.example.com"));
    }

    @Test
    public void testNoConfiguration() {
        assertNull(configurator(null).getProvider("other.example.com"));
    }
}
