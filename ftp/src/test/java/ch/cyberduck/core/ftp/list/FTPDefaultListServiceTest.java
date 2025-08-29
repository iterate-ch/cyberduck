package ch.cyberduck.core.ftp.list;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.ftp.AbstractFTPTest;
import ch.cyberduck.core.ftp.FTPDeleteFeature;
import ch.cyberduck.core.ftp.FTPTouchFeature;
import ch.cyberduck.core.ftp.FTPWorkdirService;
import ch.cyberduck.core.ftp.FTPWriteFeature;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class FTPDefaultListServiceTest extends AbstractFTPTest {

    @Test
    public void testListDefault() throws Exception {
        final ListService list = new FTPDefaultListService(session, new CompositeFileEntryParser(Collections.singletonList(new UnixFTPEntryParser())),
            FTPListService.Command.list);
        final Path directory = new FTPWorkdirService(session).find();
        final Path file = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPTouchFeature(session).touch(new FTPWriteFeature(session), file, new TransferStatus());
        assertTrue(list.list(directory, new DisabledListProgressListener()).contains(file));
        new FTPDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListDefaultFlag() throws Exception {
        final ListService list = new FTPDefaultListService(session, new CompositeFileEntryParser(Collections.singletonList(new UnixFTPEntryParser())),
            FTPListService.Command.lista);
        final Path directory = new FTPWorkdirService(session).find();
        final Path file = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPTouchFeature(session).touch(new FTPWriteFeature(session), file, new TransferStatus());
        assertTrue(list.list(directory, new DisabledListProgressListener()).contains(file));
        new FTPDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
