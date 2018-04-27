package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.fail;

public class AbstractB2Test {

    protected final PathCache cache = new PathCache(100);
    protected B2Session session;

    @After
    public void disconnect() throws Exception {
        session.close();
        cache.clear();
    }

    @Before
    public void setup() throws Exception {
        session = new B2Session(
            new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                new Credentials(
                    System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                )));
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, PathCache.empty(), new DisabledCancelCallback());
    }
}
