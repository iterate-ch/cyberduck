package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.nextcloud.NextcloudWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.tus.TusCapabilities;
import ch.cyberduck.core.tus.TusUploadFeature;
import ch.cyberduck.core.tus.TusWriteFeature;

import org.apache.commons.lang3.ArrayUtils;

import java.text.MessageFormat;
import java.util.EnumSet;

import static ch.cyberduck.core.tus.TusCapabilities.TUS_VERSION;

/**
 * Deny uploading files to virtual folders
 */
public class OwncloudWriteFeature<Reply> implements Write<Reply> {

    private final OwncloudPathContainerService containerService;
    private final Write<Reply> proxy;
    /**
     * Used for uploads of a segment to an existing upload URL
     */
    private final Write<Reply> resumable;

    public OwncloudWriteFeature(final OwncloudSession session, final Write<Reply> proxy) {
        this(session, proxy, proxy);
    }

    /**
     * Use resumable upload for segments if supported by server
     */
    public OwncloudWriteFeature(final OwncloudSession session) {
        this(session, (Write<Reply>) new NextcloudWriteFeature(session),
                (Write<Reply>) (ArrayUtils.contains(session.tus.versions, TUS_VERSION)
                        && session.tus.extensions.contains(TusCapabilities.Extension.creation)
                        ? new TusWriteFeature(session, session.tus) : new NextcloudWriteFeature(session)));
    }

    /**
     * @param proxy     Direct write of the complete content
     * @param resumable Write of a segment to the upload URL given in the status parameters
     */
    public OwncloudWriteFeature(final OwncloudSession session, final Write<Reply> proxy, final Write<Reply> resumable) {
        this.containerService = new OwncloudPathContainerService(session.ocs);
        this.proxy = proxy;
        this.resumable = resumable;
    }

    private Write<Reply> delegate(final TransferStatus status) {
        return status.getParameters().containsKey(TusUploadFeature.UPLOAD_URL) ? resumable : proxy;
    }

    @Override
    public StatusOutputStream<Reply> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        return this.delegate(status).write(file, status, callback);
    }

    @Override
    public boolean random(final Path file) {
        return proxy.random(file);
    }

    @Override
    public boolean timestamp(final Path file) {
        return proxy.timestamp(file);
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        if(containerService.isVirtual(file.getParent())) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString(
                    "Upload {0} failed", "Error"), file.getName())).withFile(file.getParent());
        }
        proxy.preflight(file);
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return proxy.features(file);
    }
}
