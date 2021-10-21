package ch.cyberduck.core.profiles;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class ProfilesSynchronizeWorkerTest {

    @Test
    public void testRun() throws Exception {
        // Registry in temparary folder
        final ProtocolFactory protocols = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Host host = new HostParser(protocols, new S3Protocol()).get(PreferencesFactory.get().getProperty("profiles.discovery.updater.url"));
        final Session session = new S3Session(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledProxyFinder().find(host.getHostname()), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        // Local directory with oudated profile
        final Local localonlyprofile = LocalFactory.get(this.getClass().getResource("/test-localonly.cyberduckprofile").getPath());
        final Local outdatedprofile = LocalFactory.get(this.getClass().getResource("/test-outdated.cyberduckprofile").getPath());
        assertNotNull(new ProfilePlistReader(protocols).read(outdatedprofile));
        assertTrue(outdatedprofile.exists());
        final LocalProfileDescription localonlyProfileDescription = new LocalProfileDescription(protocols, localonlyprofile);
        assertTrue(localonlyProfileDescription.getProfile().isPresent());
        final LocalProfileDescription outdatedProfileDescription = new LocalProfileDescription(protocols, outdatedprofile);
        assertTrue(outdatedProfileDescription.getProfile().isPresent());
        final Local directory = outdatedprofile.getParent();
        final ProfilesSynchronizeWorker worker = new ProfilesSynchronizeWorker(protocols, directory, ProfilesFinder.Visitor.Noop);
        final Set<ProfileDescription> profiles = worker.run(session);
        assertFalse(profiles.isEmpty());
        profiles.forEach(d -> assertTrue(d.isLatest()));

        assertFalse(profiles.contains(outdatedProfileDescription));
        assertTrue(profiles.stream().filter(d -> d.getProfile().isPresent()).filter(d -> d.getProfile().get().getProvider().equals(outdatedProfileDescription.getProfile().get().getProvider())).findFirst().isPresent());
        assertTrue(profiles.stream().filter(d -> d.getProfile().isPresent()).filter(d -> d.getProfile().get().getProvider().equals(outdatedProfileDescription.getProfile().get().getProvider())).findFirst().get().isInstalled());
        assertTrue(profiles.stream().filter(d -> d.getProfile().isPresent()).filter(d -> d.getProfile().get().getProvider().equals(outdatedProfileDescription.getProfile().get().getProvider())).findFirst().get().isLatest());
        // Assert profile updated from remote
        assertNotEquals(outdatedProfileDescription, profiles.stream().filter(d -> d.getProfile().isPresent()).filter(d -> d.getProfile().get().getProvider().equals(outdatedProfileDescription.getProfile().get().getProvider())).findFirst().get());
        assertNotEquals(outdatedProfileDescription.getChecksum(), profiles.stream().filter(d -> d.getProfile().isPresent()).filter(d -> d.getProfile().get().getProvider().equals(outdatedProfileDescription.getProfile().get().getProvider())).findFirst().get().getChecksum());

        assertTrue(profiles.contains(localonlyProfileDescription));
        assertTrue(profiles.stream().filter(d -> d.getProfile().isPresent()).filter(d -> d.equals(localonlyProfileDescription)).findFirst().isPresent());
        assertTrue(profiles.stream().filter(d -> d.getProfile().isPresent()).filter(d -> d.equals(localonlyProfileDescription)).findFirst().get().isInstalled());
        assertTrue(profiles.stream().filter(d -> d.getProfile().isPresent()).filter(d -> d.equals(localonlyProfileDescription)).findFirst().get().isLatest());
        assertEquals(localonlyProfileDescription, profiles.stream().filter(d -> d.getProfile().isPresent()).filter(d -> d.equals(localonlyProfileDescription)).findFirst().get());
    }
}
