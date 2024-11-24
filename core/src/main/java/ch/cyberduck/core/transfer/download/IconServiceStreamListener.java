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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.io.DelegateStreamListener;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.local.IconService;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.transfer.TransferStatus;

public class IconServiceStreamListener extends DelegateStreamListener {

    private final IconService icon;
    private final TransferStatus status;
    private final Local file;

    public IconServiceStreamListener(final IconService icon, final StreamListener delegate, final TransferStatus status, final Local file) {
        super(delegate);
        this.icon = icon;
        this.status = status;
        this.file = file;
    }

    @Override
    public void sent(final long bytes) {
        super.sent(bytes);
        icon.set(file, new TransferProgress(status.getLength(), bytes));
    }
}
