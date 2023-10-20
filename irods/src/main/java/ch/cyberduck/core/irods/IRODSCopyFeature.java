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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.util.EnumSet;

public class IRODSCopyFeature implements Copy {

    private final IRODSSession session;

    public IRODSCopyFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Path copy(final Path source, final Path target, final ch.cyberduck.core.transfer.TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.getClient();
            final DataTransferOperations transfer = fs.getIRODSAccessObjectFactory()
                .getDataTransferOperations(fs.getIRODSAccount());
            transfer.copy(fs.getIRODSFileFactory().instanceIRODSFile(source.getAbsolute()),
                fs.getIRODSFileFactory().instanceIRODSFile(target.getAbsolute()), new TransferStatusCallbackListener() {
                    @Override
                    public FileStatusCallbackResponse statusCallback(final TransferStatus transferStatus) {
                        return FileStatusCallbackResponse.CONTINUE;
                    }

                    @Override
                    public void overallStatusCallback(final TransferStatus transferStatus) {
                        switch(transferStatus.getTransferState()) {
                            case OVERALL_COMPLETION:
                                listener.sent(status.getLength());
                        }
                    }

                    @Override
                    public CallbackResponse transferAsksWhetherToForceOperation(final String irodsAbsolutePath, final boolean isCollection) {
                        return CallbackResponse.YES_THIS_FILE;
                    }
                }, DefaultTransferControlBlock.instance(StringUtils.EMPTY,
                    new HostPreferences(session.getHost()).getInteger("connection.retry")));
            return target;
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }
}
