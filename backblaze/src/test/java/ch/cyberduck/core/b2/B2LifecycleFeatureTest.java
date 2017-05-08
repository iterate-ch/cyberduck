package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class B2LifecycleFeatureTest {

    @Test
    public void setConfiguration() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path bucket = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new B2DirectoryFeature(session).mkdir(bucket, null, new TransferStatus());
        assertEquals(LifecycleConfiguration.empty(), new B2LifecycleFeature(session).getConfiguration(bucket));
        new B2LifecycleFeature(session).setConfiguration(bucket, new LifecycleConfiguration(1, null, 30));
        assertEquals(30, new B2LifecycleFeature(session).getConfiguration(bucket).getExpiration(), 0L);
        assertEquals(1, new B2LifecycleFeature(session).getConfiguration(bucket).getTransition(), 0L);
        new B2LifecycleFeature(session).setConfiguration(bucket, LifecycleConfiguration.empty());
        assertEquals(LifecycleConfiguration.empty(), new B2LifecycleFeature(session).getConfiguration(bucket));
        new B2DeleteFeature(session).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}