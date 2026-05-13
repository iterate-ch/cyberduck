package ch.cyberduck.core.sftp.auth;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.sftp.openssh.OpenSSHAgentAuthenticator;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jcraft.jsch.agentproxy.AgentProxy;
import com.jcraft.jsch.agentproxy.Identity;
import net.schmizz.sshj.SSHClient;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SFTPAgentAuthenticationTest {

    // "AQID" is Base64 for {1, 2, 3}
    private static final byte[] MATCH_BLOB = {1, 2, 3};
    private static final byte[] NO_MATCH_BLOB = {4, 5, 6};
    private static final String PUBLIC_KEY_LINE = "ssh-rsa AQID comment";

    @Test
    public void filterIdentitiesMatch() {
        final SFTPAgentAuthentication authentication = new SFTPAgentAuthentication(new SSHClient(), new OpenSSHAgentAuthenticator(new AgentProxy(null)));
        final Credentials credentials = new Credentials("user").setIdentity(new Local("mykey") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public InputStream getInputStream() throws AccessDeniedException {
                return new ByteArrayInputStream(PUBLIC_KEY_LINE.getBytes(StandardCharsets.UTF_8));
            }
        });

        final List<Identity> identities = new ArrayList<>();
        final Identity nomatch = mock(Identity.class);
        when(nomatch.getBlob()).thenReturn(NO_MATCH_BLOB);
        final Identity match = mock(Identity.class);
        when(match.getBlob()).thenReturn(MATCH_BLOB);

        identities.add(nomatch);
        identities.add(match);

        final Collection<Identity> filtered = authentication.filter(credentials, identities);
        assertEquals(1, filtered.size());
        assertArrayEquals(MATCH_BLOB, filtered.iterator().next().getBlob());
    }

    @Test
    public void filterIdentitiesNoMatch() {
        final SFTPAgentAuthentication authentication = new SFTPAgentAuthentication(new SSHClient(), new OpenSSHAgentAuthenticator(new AgentProxy(null)));
        final Credentials credentials = new Credentials("user").setIdentity(new Local("mykey") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(PUBLIC_KEY_LINE.getBytes(StandardCharsets.UTF_8));
            }
        });

        final List<Identity> identities = new ArrayList<>();
        final Identity nomatch = mock(Identity.class);
        when(nomatch.getBlob()).thenReturn(NO_MATCH_BLOB);

        identities.add(nomatch);
        identities.add(nomatch);

        final Collection<Identity> filtered = authentication.filter(credentials, identities);
        assertEquals(2, filtered.size());
    }

    @Test
    public void filterIdentitiesNoKeySet() {
        final SFTPAgentAuthentication authentication = new SFTPAgentAuthentication(new SSHClient(), new OpenSSHAgentAuthenticator(new AgentProxy(null)));
        final Credentials credentials = new Credentials("user");

        final List<Identity> identities = new ArrayList<>();
        final Identity nomatch = mock(Identity.class);
        when(nomatch.getBlob()).thenReturn(NO_MATCH_BLOB);

        identities.add(nomatch);
        identities.add(nomatch);

        final Collection<Identity> filtered = authentication.filter(credentials, identities);
        assertEquals(2, filtered.size());
    }
}