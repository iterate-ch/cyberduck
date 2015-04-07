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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.io.File;

/**
 * @version $Id$
 */
public class IRODSUploadFeature implements Upload<Void> {

    private IRODSSession session;

    public IRODSUploadFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public boolean pooled() {
        return true;
    }

    @Override
    public Void upload(final Path file, final Local local, final BandwidthThrottle throttle,
                       final StreamListener listener, final TransferStatus status,
                       final ConnectionCallback callback) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.filesystem();
            final IRODSFile f = fs.getIRODSFileFactory().instanceIRODSFile(file.getAbsolute());
            fs.getIRODSAccessObjectFactory().getDataTransferOperations(fs.getIRODSAccount())
                    .putOperation(new File(local.getAbsolute()), f, new TransferStatusCallbackListener() {
                        @Override
                        public FileStatusCallbackResponse statusCallback(final org.irods.jargon.core.transfer.TransferStatus t) throws JargonException {
                            listener.sent(t.getBytesTransfered() - status.getOffset());
                            if(status.isCanceled()) {
                                return FileStatusCallbackResponse.SKIP;
                            }
                            return FileStatusCallbackResponse.CONTINUE;
                        }

                        @Override
                        public void overallStatusCallback(final org.irods.jargon.core.transfer.TransferStatus transferStatus) throws JargonException {
                            //
                        }

                        @Override
                        public CallbackResponse transferAsksWhetherToForceOperation(final String irodsAbsolutePath, final boolean isCollection) {
                            if(status.isAppend()) {
                                return CallbackResponse.NO_THIS_FILE;
                            }
                            return CallbackResponse.YES_THIS_FILE;
                        }
                    }, DefaultTransferControlBlock.instance(StringUtils.EMPTY, PreferencesFactory.get().getInteger("connection.retry")));
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
        return null;
    }
}
