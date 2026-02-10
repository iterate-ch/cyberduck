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
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.test.VaultTest;

import org.junit.After;
import org.junit.Before;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.fail;

public class AbstractDeepboxTest extends VaultTest {

    protected final String ORG4 = "3ce7505e-6497-4723-b5c6-e381d8b8cbad";
    protected final String ORG4_DEEPBOX4 = "a548e68e-5584-42c1-b2bc-9e051dc78e5e";
    protected final String ORG4_DEEPBOX4_BOX1 = "366a7117-0ad3-4dcb-9e79-a4270c3f6fb5";
    protected final String ORG1 = "9df0bd54-c4c6-43b5-b622-34988af31f78";
    protected final String ORG1_DEEPBOX1 = "71fdd537-17db-4a8a-b959-64a1ab07774a";
    protected final String ORG1_DEEPBOX1_BOX1 = "40062559-c1a3-4229-9b1b-77320821d0d5";
    protected final String SHARED_DEEPBOX = "18e8c231-0c6f-462c-85ea-67b7c7942fe3";
    protected final String SHARED_DEEPBOX_BOX = "540e2462-7e82-432d-ae2a-5d7f9edf9f3e";


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
        session = setup("deepbox.deepboxapp3.user");
    }

    public DeepboxSession setup(final String user) throws BackgroundException {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new DeepboxProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Deepbox Integration.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(PROPERTIES.get(user))) {
            @Override
            public String getProperty(final String key) {
                if("deepbox.listing.box.trash".equals(key)) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        DeepboxSession session = new DeepboxSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
                new TestPasswordStore(), new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
        return session;
    }
}
