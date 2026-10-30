package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.date.MDTMSecondsDateFormatter;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class FTPMDTMTimestampFeatureTest extends AbstractFTPTest {

    @Test
    public void testSetTimestamp() throws Exception {
        this.assumeFeature("MDTM");
        final Path home = new FTPWorkdirService(session).find();
        final long modified = System.currentTimeMillis();
        final Path test = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new FTPTouchFeature(session).touch(new FTPWriteFeature(session), test, new TransferStatus());
        new FTPMDTMTimestampFeature(session).setTimestamp(test, modified);
        // Listing has minute precision and is in server timezone
        assertEquals(new MDTMSecondsDateFormatter().format(modified, TimeZone.getTimeZone("UTC")),
                session.getClient().getModificationTime(test.getAbsolute()));
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }
}