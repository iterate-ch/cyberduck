package ch.cyberduck.core.gmxcloud;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GmxcloudShareFeatureTest extends AbstractGmxcloudTest {

    @Test(expected = InteroperabilityException.class)
    public void testInvalidPin() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path sourceFolder = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final GmxcloudShareFeature feature = new GmxcloudShareFeature(session, fileid);
        try {
            feature.toDownloadUrl(sourceFolder, null, new DisabledPasswordCallback() {
                @Override
                public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                    return new Credentials(null, "test");
                }
            });
        }
        catch(InteroperabilityException e) {
            assertEquals("Pin does not match pin policy. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
        finally {
            new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(sourceFolder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testDownloadUrlForContainer() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path sourceFolder = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final GmxcloudShareFeature feature = new GmxcloudShareFeature(session, fileid);
        final DescriptiveUrl url = feature.toDownloadUrl(sourceFolder, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials(null, new AlphanumericRandomStringService().random());
            }
        });
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(sourceFolder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDownloadUrlForFile() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path sourceFolder = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new Path(sourceFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        createFile(file, RandomUtils.nextBytes(0));
        assertTrue(new GmxcloudFindFeature(session, fileid).find(file));
        final GmxcloudShareFeature feature = new GmxcloudShareFeature(session, fileid);
        final DescriptiveUrl url = feature.toDownloadUrl(file, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials(null, new AlphanumericRandomStringService().random());
            }
        });
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(sourceFolder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testUploadUrlForContainer() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path sourceFolder = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final GmxcloudShareFeature feature = new GmxcloudShareFeature(session, fileid);
        final DescriptiveUrl url = feature.toUploadUrl(sourceFolder, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials(null, new AlphanumericRandomStringService().random());
            }
        });
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(sourceFolder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testUploadUrlForFile() {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path file = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final GmxcloudShareFeature f = new GmxcloudShareFeature(session, fileid);
        assertFalse(f.isSupported(file, PromptUrlProvider.Type.upload));
    }

    @Test
    public void testUrlFormatting() {
        assertEquals("https://c.gmx.net/%401015156902205593160/YK8VCl2GSGmR_UwjZALpEA", GmxcloudShareFeature.toBrandedUri(
                "../../../../guest/%401015156902205593160/share/YK8VCl2GSGmR_UwjZALpEA/resourceAlias/ROOT", "c.gmx.net"));
    }
}
