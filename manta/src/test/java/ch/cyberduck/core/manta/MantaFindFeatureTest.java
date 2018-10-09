package ch.cyberduck.core.manta;

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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class MantaFindFeatureTest extends AbstractMantaTest {

    @Test
    public void testFindFileNotFound() throws Exception {
        final MantaFindFeature f = new MantaFindFeature(session);
        assertFalse(f.find(new Path(
            new MantaAccountHomeInfo(session.getHost().getCredentials().getUsername(), session.getHost().getDefaultPath()).getAccountPrivateRoot(),
            UUID.randomUUID().toString(),
            EnumSet.of(Path.Type.file)), new DisabledListProgressListener()));
    }

    @Test
    public void testFindPrivate() throws Exception {
        final MantaFindFeature f = new MantaFindFeature(session);
        assertTrue(f.find(new Path(
            new MantaAccountHomeInfo(session.getHost().getCredentials().getUsername(), session.getHost().getDefaultPath()).getAccountRoot(),
            MantaAccountHomeInfo.HOME_PATH_PRIVATE,
            EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()));
    }

    @Test
    public void testFindPublic() throws Exception {
        final MantaFindFeature f = new MantaFindFeature(session);
        assertTrue(f.find(new Path(
            new MantaAccountHomeInfo(session.getHost().getCredentials().getUsername(), session.getHost().getDefaultPath()).getAccountRoot(),
            MantaAccountHomeInfo.HOME_PATH_PUBLIC,
            EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()));
    }
}
