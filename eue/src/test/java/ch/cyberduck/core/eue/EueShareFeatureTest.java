package ch.cyberduck.core.eue;

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
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class EueShareFeatureTest extends AbstractEueSessionTest {

    @Test(expected = InteroperabilityException.class)
    public void testInvalidPin() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path sourceFolder = new EueDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final EueShareFeature feature = new EueShareFeature(session, fileid);
        try {
            feature.toDownloadUrl(sourceFolder, Share.Sharee.world, null, new DisabledPasswordCallback() {
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
            new EueDeleteFeature(session, fileid).delete(Collections.singletonList(sourceFolder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testDownloadUrlForContainer() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path sourceFolder = new EueDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final EueShareFeature feature = new EueShareFeature(session, fileid);
        final DescriptiveUrl url = feature.toDownloadUrl(sourceFolder, Share.Sharee.world, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials(null, new AlphanumericRandomStringService().random());
            }
        });
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        // Test returning same share
        assertEquals(url, feature.toDownloadUrl(sourceFolder, Share.Sharee.world, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials(null, new AlphanumericRandomStringService().random());
            }
        }));
        assertEquals(url, new EueShareUrlProvider(session.getHost(), session.userShares()).toUrl(sourceFolder).find(DescriptiveUrl.Type.signed));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(sourceFolder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDownloadUrlForFile() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path sourceFolder = new EueDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new Path(sourceFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        createFile(fileid, file, RandomUtils.nextBytes(0));
        assertTrue(new EueFindFeature(session, fileid).find(file));
        final EueShareFeature feature = new EueShareFeature(session, fileid);
        final DescriptiveUrl url = feature.toDownloadUrl(file, Share.Sharee.world, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials(null, new AlphanumericRandomStringService().random());
            }
        });
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        // Test returning same share
        assertEquals(url, feature.toDownloadUrl(file, Share.Sharee.world, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials(null, new AlphanumericRandomStringService().random());
            }
        }));
        assertEquals(url, new EueShareUrlProvider(session.getHost(), session.userShares()).toUrl(file).find(DescriptiveUrl.Type.signed));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(sourceFolder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testUploadUrlForContainer() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path sourceFolder = new EueDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final EueShareFeature feature = new EueShareFeature(session, fileid);
        final DescriptiveUrl url = feature.toUploadUrl(sourceFolder, Share.Sharee.world, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials(null, new AlphanumericRandomStringService().random());
            }
        });
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        // Test returning same share
        assertEquals(url, feature.toUploadUrl(sourceFolder, Share.Sharee.world, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials(null, new AlphanumericRandomStringService().random());
            }
        }));
        assertEquals(url, new EueShareUrlProvider(session.getHost(), session.userShares()).toUrl(sourceFolder).find(DescriptiveUrl.Type.signed));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(sourceFolder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testUploadUrlForFile() {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path file = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final EueShareFeature f = new EueShareFeature(session, fileid);
        assertFalse(f.isSupported(file, Share.Type.upload));
    }

    @Test
    public void testUrlFormatting() {
        assertEquals("https://c.gmx.net/%401015156902205593160/YK8VCl2GSGmR_UwjZALpEA", EueShareFeature.toBrandedUri(
                "../../../../guest/%401015156902205593160/share/YK8VCl2GSGmR_UwjZALpEA/resourceAlias/ROOT", "c.gmx.net"));
    }
}
