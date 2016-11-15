package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;

import java.io.File;

public class IRODSDownloadFeature implements Download {

    private final IRODSSession session;

    private final Preferences preferences = PreferencesFactory.get();

    public IRODSDownloadFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public void download(final Path file, final Local local, final BandwidthThrottle throttle,
                         final StreamListener listener, final TransferStatus status,
                         final ConnectionCallback callback) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.filesystem();
            final IRODSFile f = fs.getIRODSFileFactory().instanceIRODSFile(file.getAbsolute());
            if(f.exists()) {
                final TransferControlBlock block = DefaultTransferControlBlock.instance(StringUtils.EMPTY,
                        preferences.getInteger("connection.retry"));
                final TransferOptions options = new DefaultTransferOptionsConfigurer().configure(new TransferOptions());
                options.setUseParallelTransfer(session.getTransferType().equals(Host.TransferType.concurrent));
                block.setTransferOptions(options);
                final DataTransferOperations transfer = fs.getIRODSAccessObjectFactory()
                        .getDataTransferOperations(fs.getIRODSAccount());
                transfer.getOperation(f, new File(local.getAbsolute()),
                        new DefaultTransferStatusCallbackListener(status, listener, block),
                        block);
            }
            else {
                throw new NotfoundException(file.getAbsolute());
            }
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return false;
    }
}
