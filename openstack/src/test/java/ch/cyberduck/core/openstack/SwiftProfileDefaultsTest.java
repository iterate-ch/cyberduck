package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2026 AiNET and Blomp. All rights reserved.
 * https://ai.net/
 * https://www.blomp.com/
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Quota;

import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpResponse;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.method.Authentication20UsernamePasswordRequest;
import ch.iterate.openstack.swift.method.Authentication3UsernamePasswordProjectRequest;
import ch.iterate.openstack.swift.method.AuthenticationRequest;

import static org.junit.Assert.*;

public class SwiftProfileDefaultsTest {

    private static SwiftProtocol protocol(final String context, final Map<String, String> properties) {
        return new SwiftProtocol() {
            @Override
            public String getContext() {
                return context;
            }

            @Override
            public Map<String, String> getProperties() {
                return properties;
            }
        };
    }

    private static Map<String, String> properties(final String... pairs) {
        final Map<String, String> map = new HashMap<>();
        for(int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    @Test
    public void testKeystone2DefaultTenantWithoutPrompt() throws Exception {
        final Host host = new Host(protocol("/v2.0/tokens", properties("openstack.authentication.tenant", "storage")),
                "auth.example.net", new Credentials("user@example.net", "p"));
        // DisabledLoginCallback fails when prompted for tenant
        final AuthenticationRequest request = new SwiftAuthenticationService().getRequest(host, new DisabledLoginCallback()).iterator().next();
        assertEquals(Client.AuthVersion.v20, request.getVersion());
        assertEquals(Authentication20UsernamePasswordRequest.class, request.getClass());
        // Tenant is not saved as prefix in username
        assertEquals("user@example.net", host.getCredentials().getUsername());
    }

    @Test
    public void testKeystone2ExplicitTenantPrefixWins() throws Exception {
        final Host host = new Host(protocol("/v2.0/tokens", properties("openstack.authentication.tenant", "storage")),
                "auth.example.net", new Credentials("other:user@example.net", "p"));
        assertEquals(Client.AuthVersion.v20, new SwiftAuthenticationService().getRequest(host, new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals("other:user@example.net", host.getCredentials().getUsername());
    }

    @Test
    public void testKeystone3DefaultProjectAndDomainWithoutPrompt() throws Exception {
        final Host host = new Host(protocol("/v3/auth/tokens", properties(
                "openstack.authentication.tenant", "storage", "openstack.authentication.domain", "Default")),
                "auth.example.net", new Credentials("user@example.net", "p"));
        final AuthenticationRequest request = new SwiftAuthenticationService().getRequest(host, new DisabledLoginCallback()).iterator().next();
        assertEquals(Client.AuthVersion.v3, request.getVersion());
        assertEquals(Authentication3UsernamePasswordProjectRequest.class, request.getClass());
        assertEquals("user@example.net", host.getCredentials().getUsername());
    }

    @Test
    public void testKeystone3DefaultDomainWithProjectPrefix() throws Exception {
        final Host host = new Host(protocol("/v3/auth/tokens", properties("openstack.authentication.domain", "Default")),
                "auth.example.net", new Credentials("project:user@example.net", "p"));
        assertEquals(Client.AuthVersion.v3, new SwiftAuthenticationService().getRequest(host, new DisabledLoginCallback()).iterator().next().getVersion());
        assertEquals("project:user@example.net", host.getCredentials().getUsername());
    }

    @Test
    public void testToUser() {
        assertEquals("u", SwiftAuthenticationService.toUser("u"));
        assertEquals("u@example.net", SwiftAuthenticationService.toUser("t:u@example.net"));
        assertEquals("u@example.net", SwiftAuthenticationService.toUser("p:d:u@example.net"));
        assertNull(SwiftAuthenticationService.toUser(null));
    }

    @Test
    public void testHomeContainer() {
        final Host host = new Host(protocol("/v3/auth/tokens", properties("openstack.container.home", "{0}")),
                "auth.example.net", new Credentials("storage:Default:user@example.net", "p"));
        final Path home = new SwiftHomeFeature(host).find();
        assertNotNull(home);
        assertEquals("/user@example.net", home.getAbsolute());
        assertTrue(home.isVolume());
        assertTrue(home.isDirectory());
    }

    @Test
    public void testHomeContainerPattern() {
        final Host host = new Host(protocol("/v2.0/tokens", properties("openstack.container.home", "home-{0}")),
                "auth.example.net", new Credentials("alice", "p"));
        assertEquals("/home-alice", new SwiftHomeFeature(host).find().getAbsolute());
    }

    @Test
    public void testNoHomeContainer() {
        assertNull(new SwiftHomeFeature(new Host(protocol("/v2.0/tokens", Collections.emptyMap()),
                "auth.example.net", new Credentials("alice", "p"))).find());
        assertNull(new SwiftHomeFeature(new Host(protocol("/v2.0/tokens", properties("openstack.container.home", "{0}")),
                "auth.example.net", new Credentials(null, "p"))).find());
    }

    @Test
    public void testQuotaFromHeaders() {
        // Header names in lower case as sent by some reverse proxies
        final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 204, "No Content");
        response.addHeader("x-container-bytes-used", "14680064");
        response.addHeader("x-container-meta-quota-bytes", "42949672960");
        final Quota.Space space = SwiftContainerQuotaFeature.toSpace(response);
        assertEquals(Long.valueOf(14680064L), space.used);
        assertEquals(Long.valueOf(42949672960L - 14680064L), space.available);
    }

    @Test
    public void testQuotaWithoutQuotaHeader() {
        final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 204, "No Content");
        response.addHeader("X-Container-Bytes-Used", "1024");
        final Quota.Space space = SwiftContainerQuotaFeature.toSpace(response);
        assertEquals(Long.valueOf(1024L), space.used);
        assertEquals(Long.valueOf(Long.MAX_VALUE), space.available);
        response.addHeader("X-Container-Meta-Quota-Bytes", "invalid");
        assertEquals(Long.valueOf(Long.MAX_VALUE), SwiftContainerQuotaFeature.toSpace(response).available);
    }

    @Test
    public void testQuotaExceeded() {
        final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 204, "No Content");
        response.addHeader("X-Container-Bytes-Used", "2048");
        response.addHeader("X-Container-Meta-Quota-Bytes", "1024");
        assertEquals(Long.valueOf(0L), SwiftContainerQuotaFeature.toSpace(response).available);
    }
}
