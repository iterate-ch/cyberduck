package ch.cyberduck.ui.cocoa.datasource;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.transfer.SyncTransfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.cocoa.controller.TransferPromptController;

public class SyncPromptDataSource extends TransferPromptDataSource {

    private final SyncTransfer transfer;

    public SyncPromptDataSource(final TransferPromptController c, final SessionPool session,
                                final SyncTransfer transfer, final Cache<TransferItem> cache) {
        super(c, session, transfer, cache);
        this.transfer = transfer;
    }

    public enum Column {
        /**
         * A column indicating if the file will be uploaded or downloaded
         */
        sync,
        /**
         * A column indicating if the file is missing and will be created
         */
        create
    }

    @Override
    protected NSObject objectValueForItem(final TransferItem item, final String identifier) {
        if(identifier.equals(Column.sync.name())) {
            final Comparison compare = transfer.compare(item);
            if(compare.equals(Comparison.remote)) {
                return IconCacheFactory.<NSImage>get().iconNamed("transfer-download.tiff", 16);
            }
            if(compare.equals(Comparison.local)) {
                return IconCacheFactory.<NSImage>get().iconNamed("transfer-upload.tiff", 16);
            }
            return null;
        }
        if(identifier.equals(Column.create.name())) {
            final TransferStatus status = this.getStatus(item);
            if(!status.isExists()) {
                return IconCacheFactory.<NSImage>get().iconNamed("plus.tiff", 16);
            }
            return null;
        }
        return super.objectValueForItem(item, identifier);
    }
}