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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class OneDriveTouchFeatureTest extends AbstractOneDriveTest {
    private static final Logger log = Logger.getLogger(OneDriveTouchFeatureTest.class);

    @Test
    public void testTouch() throws Exception {
        final Touch touch = new OneDriveTouchFeature(session);
        final Delete delete = new OneDriveDeleteFeature(session);
        final AttributesFinder attributesFinder = new OneDriveAttributesFinderFeature(session);

        final AttributedList<Path> list = new OneDriveListService(session).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path file : list) {
            if(file.isDirectory()) {
                Path touchedFile = new Path(file, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
                touch.touch(touchedFile, new TransferStatus().mime("x-application/cyberduck"));
                assertNotNull(attributesFinder.find(touchedFile));
                delete.delete(Collections.singletonList(touchedFile), new DisabledLoginCallback(), new Delete.DisabledCallback());
            }
        }
    }
}
