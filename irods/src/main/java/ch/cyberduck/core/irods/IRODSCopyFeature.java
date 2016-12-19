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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

public class IRODSCopyFeature implements Copy {

    private final IRODSSession session;

    public IRODSCopyFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path copy) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.getClient();
            final DataTransferOperations transfer = fs.getIRODSAccessObjectFactory()
                    .getDataTransferOperations(fs.getIRODSAccount());
            transfer.copy(fs.getIRODSFileFactory().instanceIRODSFile(source.getAbsolute()),
                    fs.getIRODSFileFactory().instanceIRODSFile(copy.getAbsolute()), new TransferStatusCallbackListener() {
                        @Override
                        public FileStatusCallbackResponse statusCallback(final TransferStatus transferStatus) throws JargonException {
                            return FileStatusCallbackResponse.CONTINUE;
                        }

                        @Override
                        public void overallStatusCallback(final TransferStatus transferStatus) throws JargonException {
                            //
                        }

                        @Override
                        public CallbackResponse transferAsksWhetherToForceOperation(final String irodsAbsolutePath, final boolean isCollection) {
                            return CallbackResponse.YES_THIS_FILE;
                        }
                    }, DefaultTransferControlBlock.instance(StringUtils.EMPTY, PreferencesFactory.get().getInteger("connection.retry")));
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }
}
