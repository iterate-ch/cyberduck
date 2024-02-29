package ch.cyberduck.core.features;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

/**
 * Allow to invoke any action required before or after a file transfer
 */
@Optional
public interface Bulk<R> {
    /**
     * Prior transfer is started
     *
     * @param type     Transfer Type
     * @param files    Map of files with status
     * @param callback Callback to user
     * @return Upload Id from server
     */
    R pre(Transfer.Type type, Map<TransferItem, TransferStatus> files, ConnectionCallback callback) throws BackgroundException;

    /**
     * After transfer is complete
     *
     * @param type     Transfer Type
     * @param files    Map of files with status
     * @param callback Callback to user
     */
    void post(Transfer.Type type, Map<TransferItem, TransferStatus> files, ConnectionCallback callback) throws BackgroundException;

    Bulk<R> withDelete(Delete delete);
}
