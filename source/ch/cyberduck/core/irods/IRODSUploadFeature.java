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
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.io.File;

/**
 * @version $Id$
 */
public class IRODSUploadFeature implements Upload<Void> {
    private static final Logger log = Logger.getLogger(IRODSUploadFeature.class);

    private IRODSSession session;

    private final Preferences preferences = PreferencesFactory.get();

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
            final TransferControlBlock block = DefaultTransferControlBlock.instance(StringUtils.EMPTY,
                    preferences.getInteger("connection.retry"));
            final TransferOptions options = new TransferOptions();
            options.setPutOption(TransferOptions.PutOptions.NORMAL);
            options.setForceOption(TransferOptions.ForceOption.ASK_CALLBACK_LISTENER);
            options.setComputeAndVerifyChecksumAfterTransfer(false);
            options.setComputeChecksumAfterTransfer(false);
            options.setMaxThreads(preferences.getInteger("queue.maxtransfers"));
            // Enable progress callbacks
            options.setIntraFileStatusCallbacks(true);
            options.setUseParallelTransfer(session.getTransferType().equals(Host.TransferType.concurrent));
            block.setTransferOptions(options);
            fs.getIRODSAccessObjectFactory().getDataTransferOperations(fs.getIRODSAccount())
                    .putOperation(new File(local.getAbsolute()), f, new TransferStatusCallbackListener() {
                        @Override
                        public FileStatusCallbackResponse statusCallback(final org.irods.jargon.core.transfer.TransferStatus t) throws JargonException {
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Progress with transfer status %s", t));
                            }
                            final long sent = t.getBytesTransfered() - status.getOffset();
                            status.progress(sent);
                            listener.sent(sent);
                            if(t.getTotalFilesTransferredSoFar() == t.getTotalFilesToTransfer()) {
                                status.setComplete();
                            }
                            if(status.isCanceled()) {
                                if(log.isDebugEnabled()) {
                                    log.debug(String.format("Set canceled for block %s", block));
                                }
                                return FileStatusCallbackResponse.SKIP;
                            }
                            return FileStatusCallbackResponse.CONTINUE;
                        }

                        @Override
                        public void overallStatusCallback(final org.irods.jargon.core.transfer.TransferStatus t) throws JargonException {
                            //
                        }

                        @Override
                        public CallbackResponse transferAsksWhetherToForceOperation(final String irodsAbsolutePath, final boolean isCollection) {
                            if(status.isCanceled()) {
                                return CallbackResponse.CANCEL;
                            }
                            if(status.isAppend()) {
                                return CallbackResponse.NO_THIS_FILE;
                            }
                            return CallbackResponse.YES_THIS_FILE;
                        }
                    }, block);
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
        return null;
    }
}
