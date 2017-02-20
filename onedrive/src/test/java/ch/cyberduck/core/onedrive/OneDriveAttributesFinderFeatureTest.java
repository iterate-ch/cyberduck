package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.test.IntegrationTest;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Category(IntegrationTest.class)
public class OneDriveAttributesFinderFeatureTest extends OneDriveTest {
    private static final Logger log = Logger.getLogger(OneDriveListServiceTest.class);

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final OneDriveAttributesFinderFeature f = new OneDriveAttributesFinderFeature(session);
        try {
            f.find(new Path(UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file)));
        }
        catch(NotfoundException e) {
            assertEquals("Unexpected response (404 Not Found). Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testFind() throws Exception {
        final Path file = new Path("/", EnumSet.of(Path.Type.directory));
        OneDriveAttributesFinderFeature attributesFinderFeature = new OneDriveAttributesFinderFeature(session);
        final AttributedList<Path> list = new OneDriveListService(session).list(file, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            log.info(f);
            attributesFinderFeature.find(f);
        }
    }

    @Test
    public void testFindHierarchy() throws Exception {
        final Path path = new Path("/", EnumSet.of(Path.Type.directory));
        OneDriveAttributesFinderFeature attributesFinderFeature = new OneDriveAttributesFinderFeature(session);
        OneDriveListService listService = new OneDriveListService(session);
        final AttributedList<Path> list = listService.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            log.info(f);
            final AttributedList<Path> children = listService.list(f, new DisabledListProgressListener());
            for(Path c : children) {
                attributesFinderFeature.find(c);
            }
        }
    }

    @Override
    protected String getHostname() {
        return "api.onedrive.com";
    }

    @Override
    protected Credentials getCredentials() {
        return new Credentials();
    }
}
