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

import ch.cyberduck.core.AbstractPath;
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
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

public class AbstractDeepboxTest extends VaultTest {

    // deepbox.deepboxapp3.user
    // ORG1/Box1 (view): /deepBoxes/71fdd537-17db-4a8a-b959-64a1ab07774a/boxes/40062559-c1a3-4229-9b1b-77320821d0d5
    // ORG4/Box1 (organize): /deepBoxes/a548e68e-5584-42c1-b2bc-9e051dc78e5e/boxes/366a7117-0ad3-4dcb-9e79-a4270c3f6fb5

    protected final UUID ORG4 = UUID.fromString("a548e68e-5584-42c1-b2bc-9e051dc78e5e");
    protected final UUID ORG4_BOX1 = UUID.fromString("366a7117-0ad3-4dcb-9e79-a4270c3f6fb5");
    protected final UUID ORG1 = UUID.fromString("71fdd537-17db-4a8a-b959-64a1ab07774a");
    protected final UUID ORG1_BOX1 = UUID.fromString("40062559-c1a3-4229-9b1b-77320821d0d5");


    protected final Path deepBox = new Path("/Mountainduck Buddies", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));

    protected final Path box = new Path(deepBox, "My Box", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));

    protected final Path documents = new Path(box, "Documents", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));

    protected final Path trash = new Path(box, "Trash", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));

    protected final Path auditing = new Path(documents, "Auditing", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));

    protected DeepboxSession session;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        // TODO (16) remove personal account for integration testing
        setup("deepbox.user");
    }

    protected void setup(final String vaultUserKey) throws BackgroundException {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new DeepboxProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Deepbox.cyberduckprofile"));
        // deepbox-desktop-app-int (christian@iterate.ch) OAuth2 Access Token
        // deepbox-desktop-app-int (deepboxpeninna+deepboxapp1@gmail.com)  OAuth2 Access Token

        // TODO (16) remove personal account for integration testing
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(PROPERTIES.get(vaultUserKey)));
//        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(PROPERTIES.get("deepbox.deepboxapp1.user")));
//        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(PROPERTIES.get("deepbox.deepboxapp2.user")));
//        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(PROPERTIES.get("deepbox.deepboxapp3.user")));
//        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(PROPERTIES.get("deepbox.deepboxapp4.user")));
//        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(PROPERTIES.get("deepbox.deepboxappshare.user")));


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

    public static class TestPasswordStore extends DisabledPasswordStore {
        Map<String, String> map = Stream.of(
                        // TODO (16) remove personal account for integration testing
                        new AbstractMap.SimpleImmutableEntry<>("deepbox-desktop-app-int (christian@iterate.ch)", "deepbox"),
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
        public String getPassword(Scheme scheme, int port, String hostname, String user) {
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
