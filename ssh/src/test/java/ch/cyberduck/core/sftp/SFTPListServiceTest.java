package ch.cyberduck.core.sftp;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.unicode.NFDNormalizer;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPListServiceTest extends AbstractSFTPTest {

    @Test
    public void testList() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final String filename = String.format("%s%s", new AlphanumericRandomStringService().random(), new NFDNormalizer().normalize("ä"));
        final Path file = new Path(home, filename, EnumSet.of(Path.Type.file));
        final Path symlinkRelative = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        final Path symlinkAbsolute = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        final Path directory = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SFTPTouchFeature(session).touch(file, new TransferStatus());
        new SFTPSymlinkFeature(session).symlink(symlinkRelative, file.getName());
        new SFTPSymlinkFeature(session).symlink(symlinkAbsolute, file.getAbsolute());
        new SFTPDirectoryFeature(session).mkdir(directory, new TransferStatus());
        final Permission permission = new Permission(Permission.Action.read_write, Permission.Action.read_write, Permission.Action.read_write);
        new SFTPUnixPermissionFeature(session).setUnixPermission(file, permission);
        final AttributedList<Path> list = new SFTPListService(session).list(home, new DisabledListProgressListener());
        assertTrue(list.contains(file));
        // Not preserving Unicode normalization
        assertNotEquals(filename, list.find(new DefaultPathPredicate(file)).getName());
        assertEquals(permission, list.get(file).attributes().getPermission());
        assertTrue(list.contains(directory));
        assertTrue(list.contains(symlinkRelative));
        assertEquals(file, list.get(symlinkRelative).getSymlinkTarget());
        assertTrue(list.contains(symlinkAbsolute));
        assertEquals(file, list.get(symlinkAbsolute).getSymlinkTarget());
        new SFTPDeleteFeature(session).delete(Arrays.asList(file, symlinkAbsolute, symlinkRelative, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testInvalidSymlinkTarget() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path file = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, Path.Type.symboliclink));
        final String target = new AlphanumericRandomStringService().random();
        new SFTPSymlinkFeature(session).symlink(file, target);
        final AttributedList<Path> list = new SFTPListService(session).list(home, new DisabledListProgressListener());
        assertTrue(list.contains(file));
        assertEquals(new Path(home, target, EnumSet.of(Path.Type.file)), list.get(file).getSymlinkTarget());
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final Path f = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final SFTPListService service = new SFTPListService(session);
        service.list(f, new DisabledListProgressListener());
    }

    @Test(expected = NotfoundException.class)
    public void testListFile() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path f = new Path(home, "test", EnumSet.of(Path.Type.directory));
        final SFTPListService service = new SFTPListService(session);
        service.list(f, new DisabledListProgressListener());
    }

    @Test
    public void testListEmptyFolder() throws Exception {
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path folder = new SFTPDirectoryFeature(session).mkdir(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final AtomicBoolean callback = new AtomicBoolean();
        assertTrue(new SFTPListService(session).list(folder, new DisabledListProgressListener() {
            @Override
            public void chunk(final Path parent, final AttributedList<Path> list) {
                assertNotSame(AttributedList.EMPTY, list);
                callback.set(true);
            }
        }).isEmpty());
        assertTrue(callback.get());
        new SFTPDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
