package ch.cyberduck.core.dropbox;

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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxPasswordShareUrlProviderTest extends AbstractDropboxTest {

    @Test
    @Ignore
    public void testSharePasswordProtected() throws Exception {
        final Path file = new DropboxTouchFeature(session).touch(
            new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DropboxPasswordShareUrlProvider provider = new DropboxPasswordShareUrlProvider(session);
        final DescriptiveUrl url = provider.toDownloadUrl(file, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials().withPassword(new AlphanumericRandomStringService().random());
            }
        });
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(url, provider.toDownloadUrl(file, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials().withPassword(new AlphanumericRandomStringService().random());
            }
        }));
        new DropboxDeleteFeature(session).delete(Collections.singletonList(file), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testShareFileDownloadPublic() throws Exception {
        final Path file = new DropboxTouchFeature(session).touch(
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DropboxPasswordShareUrlProvider provider = new DropboxPasswordShareUrlProvider(session);
        final DescriptiveUrl url = provider.toDownloadUrl(file, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return bookmark.getCredentials();
            }
        });
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(url, provider.toDownloadUrl(file, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return bookmark.getCredentials();
            }
        }));
        new DropboxDeleteFeature(session).delete(Collections.singletonList(file), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testShareFileDownloadPassword() throws Exception {
        final Path file = new DropboxTouchFeature(session).touch(
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DropboxPasswordShareUrlProvider provider = new DropboxPasswordShareUrlProvider(session);
        assertThrows(InteroperabilityException.class, () -> provider.toDownloadUrl(file, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new Credentials().withPassword(new AlphanumericRandomStringService().random());
            }
        }));
        new DropboxDeleteFeature(session).delete(Collections.singletonList(file), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testShareDownloadFolderPublic() throws Exception {
        final Path folder = new DropboxDirectoryFeature(session).mkdir(
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DropboxPasswordShareUrlProvider provider = new DropboxPasswordShareUrlProvider(session);
        final DescriptiveUrl url = provider.toDownloadUrl(folder, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return bookmark.getCredentials();
            }
        });
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(url, provider.toDownloadUrl(folder, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return bookmark.getCredentials();
            }
        }));
        new DropboxDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testRequestFiles() throws Exception {
        final Path root = new DefaultHomeFinderService(session).find();
        assertFalse(new DropboxPasswordShareUrlProvider(session).isSupported(root, PromptUrlProvider.Type.upload));
        final Path folder = new DropboxDirectoryFeature(session).mkdir(
                new Path(root, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DropboxPasswordShareUrlProvider provider = new DropboxPasswordShareUrlProvider(session);
        assertNotEquals(DescriptiveUrl.EMPTY, provider.toUploadUrl(folder, null, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return bookmark.getCredentials();
            }
        }));
        new DropboxDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
