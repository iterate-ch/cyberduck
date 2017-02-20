package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

@Category(IntegrationTest.class)
public class OneDriveListServiceTest extends OneDriveTest {
    private static final Logger log = Logger.getLogger(OneDriveListServiceTest.class);

    @Test
    public void testList() throws Exception {
        final AttributedList<Path> list = new OneDriveListService(session).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            log.info(f);
            assertEquals(new Path("/", EnumSet.of(Path.Type.directory)), f.getParent());
        }
    }

    @Test
    public void testListDriveChildren() throws Exception {
        ListService listService = new OneDriveListService(session);
        final AttributedList<Path> list = listService.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            log.info(f);
            final AttributedList<Path> children = listService.list(f, new DisabledListProgressListener());
            for(Path c : children) {
                log.info(c);
                assertEquals(f.getName(), c.getParent().getName());
                if(c.isDirectory()) {
                    final AttributedList<Path> subChildren = listService.list(c, new DisabledListProgressListener());
                    for(Path s : subChildren) {
                        log.info(s);
                        assertEquals(c.getName(), s.getParent().getName());
                    }
                }
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
