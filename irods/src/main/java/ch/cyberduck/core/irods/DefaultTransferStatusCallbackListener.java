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

import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

public class DefaultTransferStatusCallbackListener implements TransferStatusCallbackListener {
    private static final Logger log = Logger.getLogger(DefaultTransferStatusCallbackListener.class);

    private final TransferStatus status;
    private final StreamListener listener;
    private final TransferControlBlock block;

    public DefaultTransferStatusCallbackListener(final TransferStatus status, final StreamListener listener,
                                                 final TransferControlBlock block) {
        this.status = status;
        this.listener = listener;
        this.block = block;
    }

    @Override
    public FileStatusCallbackResponse statusCallback(final org.irods.jargon.core.transfer.TransferStatus t) throws JargonException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Progress with %s", t));
        }
        final long bytes = t.getBytesTransfered() - status.getOffset();
        status.progress(bytes);
        switch(t.getTransferType()) {
            case GET:
                listener.recv(bytes);
                break;
            case PUT:
                listener.sent(bytes);
                break;
        }
        if(status.isCanceled()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Set canceled for block %s", block));
            }
            block.setCancelled(true);
            return FileStatusCallbackResponse.SKIP;
        }
        else {
            if(!t.isIntraFileStatusReport()) {
                if(t.getTotalFilesTransferredSoFar() == t.getTotalFilesToTransfer()) {
                    status.setComplete();
                }
            }
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
}
