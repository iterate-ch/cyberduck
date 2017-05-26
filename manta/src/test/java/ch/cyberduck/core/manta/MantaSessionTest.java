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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MantaSessionTest {

    @Test
    public void testFeatures() throws Exception {
        final MantaSession session = new MantaSession(new Host(new MantaProtocol()));
        assertTrue(session.getFeature(Read.class) instanceof MantaReadFeature);
        assertTrue(session.getFeature(Write.class) instanceof MantaWriteFeature);
        assertTrue(session.getFeature(Directory.class) instanceof MantaDirectoryFeature);
        assertTrue(session.getFeature(Touch.class) instanceof MantaTouchFeature);
        assertTrue(session.getFeature(Delete.class) instanceof MantaDeleteFeature);
        assertTrue(session.getFeature(UrlProvider.class) instanceof MantaUrlProviderFeature);
        assertTrue(session.getFeature(AttributesFinder.class) instanceof MantaAttributesFinderFeature);
    }

    @Test
    public void testUserOwnerIdentification() {
        final MantaSession session = new MantaSession(
                new Host(new MantaProtocol(), null, new Credentials("owner")));

        // assertTrue(session);

    }

}
