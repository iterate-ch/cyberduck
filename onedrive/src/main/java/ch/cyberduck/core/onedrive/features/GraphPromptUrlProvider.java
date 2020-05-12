package ch.cyberduck.core.onedrive.features;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import java.io.IOException;
import java.net.URI;

import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDrivePermission;
import org.nuxeo.onedrive.client.OneDriveSharingLink;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

public class GraphPromptUrlProvider implements PromptUrlProvider {
    private final GraphSession session;

    public GraphPromptUrlProvider(final GraphSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported(Path file, Type type) {
        if (Type.download != type) {
            return false;
        }
        return session.isAccessible(file, true);
    }

    @Override
    public DescriptiveUrl toDownloadUrl(Path file, Object options, PasswordCallback callback)
            throws BackgroundException {
        final OneDriveItem item = session.toItem(file);
        final OneDrivePermission.Metadata downloadLink;
        final URI webUrl;
        try {
            downloadLink = item.createSharedLink(OneDriveSharingLink.Type.VIEW);
            webUrl = URI.create(downloadLink.getLink().getWebUrl());
        } catch (IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failed creating download url", e, file);
        } catch (IllegalArgumentException e) {
            throw new DefaultExceptionMappingService().map("Failed creating download url", e);
        }
        return new DescriptiveUrl(webUrl);
    }

    @Override
    public DescriptiveUrl toUploadUrl(Path file, Object options, PasswordCallback callback) throws BackgroundException {
        return null;
    }

}