package ch.cyberduck.core.brick;

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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.local.BrowserLauncher;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.HashSet;

@Category(IntegrationTest.class)
public class BrickSessionTest extends AbstractBrickTest {

    @Test(expected = ConnectionCanceledException.class)
    public void testLoginInterrupt() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new BrickProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Brick.cyberduckprofile"));
        final Host host = new Host(profile, "mountainduck.files.com") {
            @Override
            public String getProperty(final String key) {
                switch(key) {
                    case "brick.pairing.interval.ms":
                        return String.valueOf(100L);
                    case "brick.pairing.interrupt.ms":
                        return String.valueOf(1000L);
                }
                return super.getProperty(key);
            }
        };
        final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        session.pair(host, new DisabledConnectionCallback(), new DisabledLoginCallback(), new DisabledCancelCallback(),
                "t", "m", new BrowserLauncher() {
                    @Override
                    public boolean open(final String url) {
                        return true;
                    }
                });
    }
}