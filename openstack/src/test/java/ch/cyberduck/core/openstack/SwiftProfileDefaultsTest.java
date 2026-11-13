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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpVersion;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;

import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.method.Authentication20UsernamePasswordRequest;
import ch.iterate.openstack.swift.method.Authentication3UsernamePasswordProjectRequest;
import ch.iterate.openstack.swift.method.AuthenticationRequest;
import ch.iterate.openstack.swift.model.Container;
import ch.iterate.openstack.swift.model.Region;

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
    public void testQuotaFromMetadata() {
        final Quota.Space space = SwiftContainerQuotaFeature.toSpace(14680064L, "42949672960");
        assertEquals(Long.valueOf(14680064L), space.used);
        assertEquals(Long.valueOf(42949672960L - 14680064L), space.available);
    }

    @Test
    public void testQuotaWithoutQuotaMetadata() {
        final Quota.Space space = SwiftContainerQuotaFeature.toSpace(1024L, null);
        assertEquals(Long.valueOf(1024L), space.used);
        assertEquals(Long.valueOf(Long.MAX_VALUE), space.available);
        assertEquals(Long.valueOf(Long.MAX_VALUE), SwiftContainerQuotaFeature.toSpace(1024L, "invalid").available);
    }

    @Test
    public void testQuotaExceeded() {
        assertEquals(Long.valueOf(0L), SwiftContainerQuotaFeature.toSpace(2048L, "1024").available);
    }

    private static LoginCallback prompted(final AtomicInteger count) {
        return new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                count.incrementAndGet();
                return new Credentials("prompted");
            }
        };
    }

    @Test
    public void testKeystone3DefaultProjectPromptsForDomainOnly() throws Exception {
        final Host host = new Host(protocol("/v3/auth/tokens", properties("openstack.authentication.tenant", "storage")),
                "auth.example.net", new Credentials("user@example.net", "p"));
        final AtomicInteger count = new AtomicInteger();
        new SwiftAuthenticationService().getRequest(host, prompted(count));
        assertEquals(1, count.get());
        assertEquals("storage:prompted:user@example.net", host.getCredentials().getUsername());
    }

    @Test
    public void testKeystone3DefaultDomainPromptsForProjectOnly() throws Exception {
        final Host host = new Host(protocol("/v3/auth/tokens", properties("openstack.authentication.domain", "Default")),
                "auth.example.net", new Credentials("user@example.net", "p"));
        final AtomicInteger count = new AtomicInteger();
        new SwiftAuthenticationService().getRequest(host, prompted(count));
        assertEquals(1, count.get());
        assertEquals("prompted:Default:user@example.net", host.getCredentials().getUsername());
    }

    /**
     * @param status Status code in reply to listing containers
     */
    private static SwiftSession session(final Map<String, String> properties, final int status) {
        final Client stub = new Client((CloseableHttpClient) null) {
            @Override
            public Set<Region> getRegions() {
                return Collections.singleton(new Region("r", URI.create("https://storage.example.net/v1/AUTH_a"), null));
            }

            @Override
            public List<Container> listContainers(final Region region, final int limit, final String marker) throws IOException {
                throw new GenericException("failure", new Header[0], new BasicStatusLine(HttpVersion.HTTP_1_1, status, "failure"));
            }
        };
        final Host host = new Host(protocol("/v2.0/tokens", properties), "auth.example.net", new Credentials("alice", "p"));
        return new SwiftSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            public Client getClient() {
                return stub;
            }
        };
    }

    private static AttributedList<Path> listRoot(final SwiftSession session) throws Exception {
        return new SwiftListService(session, new SwiftRegionService(session)).list(Home.root(), new DisabledListProgressListener());
    }

    @Test
    public void testListRootForbiddenReturnsHomeContainer() throws Exception {
        final AttributedList<Path> list = listRoot(session(properties("openstack.container.home", "{0}", "openstack.list.container.limit", "100"), 403));
        assertEquals(1, list.size());
        assertEquals("/alice", list.get(0).getAbsolute());
    }

    @Test(expected = AccessDeniedException.class)
    public void testListRootForbiddenWithoutHomeContainer() throws Exception {
        listRoot(session(properties("openstack.list.container.limit", "100"), 403));
    }

    @Test
    public void testListRootTransientFailureNotReplacedByHomeContainer() throws Exception {
        for(int status : new int[]{429, 500, 406}) {
            try {
                listRoot(session(properties("openstack.container.home", "{0}", "openstack.list.container.limit", "100"), status));
                fail("Expected failure for status " + status);
            }
            catch(AccessDeniedException e) {
                assertFalse(SwiftExceptionMappingService.isForbidden(e));
            }
        }
    }

    @Test
    public void testDistributionLoaderSkipsForbidden() throws Exception {
        assertTrue(new SwiftDistributionConfigurationLoader(session(properties("openstack.list.container.limit", "100"), 403))
                .operate(new DisabledPasswordCallback()).isEmpty());
    }

    @Test(expected = RetriableAccessDeniedException.class)
    public void testDistributionLoaderPropagatesTransientFailure() throws Exception {
        new SwiftDistributionConfigurationLoader(session(properties("openstack.list.container.limit", "100"), 429))
                .operate(new DisabledPasswordCallback());
    }
}
