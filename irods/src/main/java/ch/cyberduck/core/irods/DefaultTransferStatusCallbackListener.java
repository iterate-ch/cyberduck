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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

public class DefaultTransferStatusCallbackListener implements TransferStatusCallbackListener {
    private static final Logger log = LogManager.getLogger(DefaultTransferStatusCallbackListener.class);

    private final TransferStatus status;
    private final BytecountStreamListener listener;
    private final TransferControlBlock block;

    public DefaultTransferStatusCallbackListener(final TransferStatus status, final StreamListener listener,
                                                 final TransferControlBlock block) {
        this.status = status;
        this.listener = new BytecountStreamListener(listener);
        this.block = block;
    }

    @Override
    public FileStatusCallbackResponse statusCallback(final org.irods.jargon.core.transfer.TransferStatus t) {
        log.debug("Progress with {}", t);
        final long bytes = t.getBytesTransfered() - listener.getSent();
        switch(t.getTransferType()) {
            case GET:
                listener.recv(bytes);
                break;
            case PUT:
                listener.sent(bytes);
                break;
        }
        try {
            status.validate();
            if(!t.isIntraFileStatusReport()) {
                if(t.getTotalFilesTransferredSoFar() == t.getTotalFilesToTransfer()) {
                    status.setComplete();
                }
            }
        }
        catch(ConnectionCanceledException e) {
            log.debug("Set canceled for block {}", block);
            block.setCancelled(true);
            return FileStatusCallbackResponse.SKIP;
        }
        return FileStatusCallbackResponse.CONTINUE;
    }

    @Override
    public void overallStatusCallback(final org.irods.jargon.core.transfer.TransferStatus t) {
        //
    }

    @Override
    public CallbackResponse transferAsksWhetherToForceOperation(final String irodsAbsolutePath, final boolean isCollection) {
        try {
            status.validate();
        }
        catch(ConnectionCanceledException e) {
            return CallbackResponse.CANCEL;
        }
        if(status.isAppend()) {
            return CallbackResponse.NO_THIS_FILE;
        }
        return CallbackResponse.YES_THIS_FILE;
    }
}
