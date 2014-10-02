package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SwiftDeleteFeatureTest extends AbstractTestCase {

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundBucket() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        new SwiftDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginController(), new DisabledProgressListener());
    }

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundKey() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController(), new DisabledProgressListener());
    }

    @Test
    public void testDeleteNoParentPlaceholder() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path placeholder = new Path(new Path(container, "t", EnumSet.of(Path.Type.directory, Path.Type.placeholder)),
                "placeholder-" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final Path test = new Path(placeholder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session).touch(test);
        final SwiftFindFeature find = new SwiftFindFeature(session);
        assertFalse(find.find(placeholder));
        final SwiftObjectListService list = new SwiftObjectListService(session);
        assertTrue(list.list(placeholder.getParent(), new DisabledListProgressListener()).contains(placeholder));
        assertFalse(list.list(placeholder.getParent(), new DisabledListProgressListener()).contains(test));
        assertTrue(list.list(placeholder, new DisabledListProgressListener()).contains(test));
        assertTrue(find.find(test));
        new SwiftDeleteFeature(session).delete(Arrays.asList(placeholder, test), new DisabledLoginController(), new DisabledProgressListener());
        assertFalse(find.find(test));
        assertFalse(find.find(placeholder));
        session.close();
    }

    @Test
    public void testDeletePlaceholder() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), session);
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path placeholder = new Path(container,
                "placeholder-" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        new SwiftDirectoryFeature(session).mkdir(placeholder);
        final SwiftFindFeature find = new SwiftFindFeature(session);
        assertTrue(find.find(placeholder));
        new SwiftDeleteFeature(session).delete(Arrays.asList(placeholder), new DisabledLoginController(), new DisabledProgressListener());
        assertFalse(find.find(placeholder));
        session.close();
    }
}
