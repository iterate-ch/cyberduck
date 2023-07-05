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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveSharingLink;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

public class GraphPromptUrlProvider implements PromptUrlProvider {
    private final GraphSession session;

    public GraphPromptUrlProvider(final GraphSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported(Path file, Type type) {
        if(Type.download == type) {
            return session.isAccessible(file, true);
        }
        return false;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(Path file, final Sharee sharee, Object options, PasswordCallback callback)
            throws BackgroundException {
        final DriveItem item = session.getItem(file);
        try {
            return new DescriptiveUrl(URI.create(Files.createSharedLink(item, OneDriveSharingLink.Type.VIEW).getLink().getWebUrl()),
                    DescriptiveUrl.Type.signed, MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3")));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
        catch(IllegalArgumentException e) {
            throw new DefaultExceptionMappingService().map("Failed creating download url", e);
        }
    }

    @Override
    public DescriptiveUrl toUploadUrl(Path file, final Sharee sharee, Object options, PasswordCallback callback) throws BackgroundException {
        return DescriptiveUrl.EMPTY;
    }
}
