package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Trash;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.test.VaultTest;

import org.junit.After;
import org.junit.Before;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

public class AbstractDeepboxTest extends VaultTest {

    protected final String ORG4 = "a548e68e-5584-42c1-b2bc-9e051dc78e5e";
    protected final String ORG4_BOX1 = "366a7117-0ad3-4dcb-9e79-a4270c3f6fb5";
    protected final String ORG1 = "71fdd537-17db-4a8a-b959-64a1ab07774a";
    protected final String ORG1_BOX1 = "40062559-c1a3-4229-9b1b-77320821d0d5";

    protected DeepboxSession session;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    /**
     * deepbox.deepboxapp3.user
     * - ORG1/Box1 (view): /deepBoxes/71fdd537-17db-4a8a-b959-64a1ab07774a/boxes/40062559-c1a3-4229-9b1b-77320821d0d5
     * - ORG4/Box1 (organize): /deepBoxes/a548e68e-5584-42c1-b2bc-9e051dc78e5e/boxes/366a7117-0ad3-4dcb-9e79-a4270c3f6fb5
     * - ORG4/Box2 (no access to Trash/Inbox, Documents partially)
     */
    @Before
    public void setup() throws BackgroundException {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new DeepboxProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Deepbox Integration.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(PROPERTIES.get("deepbox.deepboxapp3.user")));
        session = new DeepboxSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
                new TestPasswordStore(), new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
    }

    protected void deleteAndPurge(final Path file) throws BackgroundException {
        if(new DeepboxPathContainerService(session).isInTrash(file)) {
            session.getFeature(Delete.class).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        else {
            session.getFeature(Trash.class).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
            final Path trash = new Path(new DeepboxPathContainerService(session).getBoxPath(file).withAttributes(new PathAttributes()),
                    new DeepboxPathContainerService(session).getPinnedLocalization(DeepboxListService.TRASH), EnumSet.of(Path.Type.directory, Path.Type.volume));
            final Path fileInTrash = new Path(trash, file.getName(), file.getType());
            session.getFeature(Delete.class).delete(Collections.singletonList(fileInTrash), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    public static class TestPasswordStore extends DisabledPasswordStore {
        final Map<String, String> map = Stream.of(
                        new AbstractMap.SimpleImmutableEntry<>("deepbox-desktop-app-int (deepboxpeninna+deepboxapp1@gmail.com)", "deepbox.deepboxapp1"),
                        new AbstractMap.SimpleImmutableEntry<>("deepbox-desktop-app-int (deepboxpeninna+deepboxapp2@gmail.com)", "deepbox.deepboxapp2"),
                        new AbstractMap.SimpleImmutableEntry<>("deepbox-desktop-app-int (deepboxpeninna+deepboxapp3@gmail.com)", "deepbox.deepboxapp3"),
                        new AbstractMap.SimpleImmutableEntry<>("deepbox-desktop-app-int (deepboxpeninna+deepboxapp4@gmail.com)", "deepbox.deepboxapp4"),
                        new AbstractMap.SimpleImmutableEntry<>("deepbox-desktop-app-int (deepboxpeninna+deepboxappshare@gmail.com)", "deepbox.deepboxappshare"))
                .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));

        @Override
        public String getPassword(final String serviceName, final String accountName) {
            if(accountName.endsWith("OAuth2 Token Expiry")) {
                final String prefix = accountName.replace(" OAuth2 Token Expiry", "");
                return PROPERTIES.get(String.format("%s.tokenexpiry", map.get(prefix)));
            }
            return null;
        }

        @Override
        public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
            if(user.endsWith("OAuth2 Access Token")) {
                final String prefix = user.replace(" OAuth2 Access Token", "");
                return PROPERTIES.get(String.format("%s.accesstoken", map.get(prefix)));
            }
            if(user.endsWith("OAuth2 Refresh Token")) {
                final String prefix = user.replace(" OAuth2 Refresh Token", "");
                return PROPERTIES.get(String.format("%s.refreshtoken", map.get(prefix)));
            }
            return null;
        }

        @Override
        public void addPassword(final String serviceName, final String accountName, final String password) {
            if(accountName.endsWith("OAuth2 Token Expiry")) {
                final String prefix = accountName.replace(" OAuth2 Token Expiry", "");
                VaultTest.add(String.format("%s.tokenexpiry", map.get(prefix)), password);
            }
        }

        @Override
        public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) {
            if(user.endsWith("OAuth2 Access Token")) {
                final String prefix = user.replace(" OAuth2 Access Token", "");
                VaultTest.add(String.format("%s.accesstoken", map.get(prefix)), password);
            }
            if(user.endsWith("OAuth2 Refresh Token")) {
                final String prefix = user.replace(" OAuth2 Refresh Token", "");
                VaultTest.add(String.format("%s.refreshtoken", map.get(prefix)), password);
            }
        }
    }
}
