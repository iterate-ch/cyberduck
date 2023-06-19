package ch.cyberduck.core.smb;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class SMBTimestampFeatureTest extends AbstractSMBTest {

    @Test
    public void testTimestamp() throws Exception {
        final TransferStatus status = new TransferStatus();
        final Path home = new DefaultHomeFinderService(session).find();
        
        final Path path = new Path(home, new AlphanumericRandomStringService(9).random(), EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(path, new TransferStatus())
        .withAttributes(session.getFeature(AttributesFinder.class).find(path));
        
        // make sure timestamps are different
        long oldTime = session.getFeature(AttributesFinder.class).find(path).getAccessedDate();
        status.setTimestamp(oldTime + 2000);

        session.getFeature(Timestamp.class).setTimestamp(path, status);
        PathAttributes newAttributes = session.getFeature(AttributesFinder.class).find(path);
        assertEquals(status.getTimestamp().longValue(), newAttributes.getAccessedDate());
    }
}
