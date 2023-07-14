package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveSharingUrlProviderTest extends AbstractDriveTest {

    @Test
    public void toDownloadUrl() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path test = new DriveTouchFeature(session, fileid).touch(
            new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus().withMime("x-application/cyberduck"));
        final DriveSharingUrlProvider provider = new DriveSharingUrlProvider(session, fileid);
        // Set web view link
        test.setAttributes(new DriveAttributesFinderFeature(session, fileid).find(test));
        assertFalse(provider.isSupported(test, Share.Type.upload));
        assertTrue(provider.isSupported(test, Share.Type.download));
        final DescriptiveUrl url = provider.toDownloadUrl(test, Share.Sharee.world, null, new DisabledPasswordCallback());
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertNotNull(url.getUrl());
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
