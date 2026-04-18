package ch.cyberduck.core.transfer.download;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.local.IconService;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.transfer.TransferStatus;

public class IconServiceStreamListener extends BytecountStreamListener {

    private final TransferStatus status;
    private final IconService.Icon icon;
    private final Transfer.Type transferType;

    public IconServiceStreamListener(final TransferStatus status, final IconService.Icon icon, final StreamListener delegate,
                                     final Transfer.Type transferType) {
        super(delegate);
        this.status = status;
        this.icon = icon;
        this.transferType = transferType;
    }

    /**
     * Finder / NSProgress updates must follow the same byte direction as {@link ch.cyberduck.core.transfer.TransferStreamListener}
     * (downloads count {@code recv}, uploads count {@code sent}). Previously only {@code sent} was used, so download folder
     * progress on the parent path never advanced and was never cleared by Finder.
     */
    private void updateIcon() {
        final long transferred;
        switch(this.transferType) {
            case download:
                transferred = this.getRecv();
                break;
            case sync:
                transferred = this.getRecv() + this.getSent();
                break;
            default:
                transferred = this.getSent();
                break;
        }
        icon.update(new TransferProgress(status.getLength(), transferred));
    }

    @Override
    public void sent(final long bytes) {
        super.sent(bytes);
        switch(this.transferType) {
            case download:
                break;
            default:
                this.updateIcon();
                break;
        }
    }

    @Override
    public void recv(final long bytes) {
        super.recv(bytes);
        switch(this.transferType) {
            case download:
            case sync:
                this.updateIcon();
                break;
            default:
                break;
        }
    }
}
