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
import ch.cyberduck.core.sftp.openssh.OpenSSHAgentAuthenticator;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

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

    @Test
    public void filterIdentitiesMatch() {
        final SFTPAgentAuthentication authentication = new SFTPAgentAuthentication(new SSHClient(), new OpenSSHAgentAuthenticator(new AgentProxy(null)));
        final Credentials credentials = new Credentials("user").withIdentity(new Local("mykey") {
            @Override
            public boolean exists() {
                return true;
            }
        });

        final List<Identity> identities = new ArrayList<>();
        final Identity nomatch = mock(Identity.class);
        when(nomatch.getComment()).thenReturn(StringUtils.getBytes("mykey2", StandardCharsets.UTF_8));
        final Identity match = mock(Identity.class);
        when(match.getComment()).thenReturn(StringUtils.getBytes("mykey", StandardCharsets.UTF_8));

        identities.add(nomatch);
        identities.add(match);

        final Collection<Identity> filtered = authentication.filter(credentials, identities);
        assertEquals(1, filtered.size());
        assertArrayEquals(match.getComment(), filtered.iterator().next().getComment());
    }

    @Test
    public void filterIdentitiesNoMatch() {
        final SFTPAgentAuthentication authentication = new SFTPAgentAuthentication(new SSHClient(), new OpenSSHAgentAuthenticator(new AgentProxy(null)));
        final Credentials credentials = new Credentials("user").withIdentity(new Local("mykey") {
            @Override
            public boolean exists() {
                return true;
            }
        });

        final List<Identity> identities = new ArrayList<>();
        final Identity nomatch = mock(Identity.class);
        when(nomatch.getComment()).thenReturn(StringUtils.getBytes("comment1", StandardCharsets.UTF_8));

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
        when(nomatch.getComment()).thenReturn(StringUtils.getBytes("comment1", StandardCharsets.UTF_8));

        identities.add(nomatch);
        identities.add(nomatch);

        final Collection<Identity> filtered = authentication.filter(credentials, identities);
        assertEquals(2, filtered.size());
    }
}