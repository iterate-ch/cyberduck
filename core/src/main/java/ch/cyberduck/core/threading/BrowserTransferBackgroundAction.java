package ch.cyberduck.core.threading;

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

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAdapter;
import ch.cyberduck.core.transfer.TransferCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferProgress;

public class BrowserTransferBackgroundAction extends TransferBackgroundAction {
    private final Transfer transfer;
    private final TransferCallback callback;

    public BrowserTransferBackgroundAction(final Controller controller, final SessionPool pool,
                                           final Transfer transfer, final TransferCallback callback) {
        super(controller, pool, pool, new TransferAdapter() {
            @Override
            public void progress(final TransferProgress status) {
                controller.message(status.getProgress());
                super.progress(status);
            }
        }, transfer, new TransferOptions());
        this.transfer = transfer;
        this.callback = callback;
    }

    @Override
    public void finish() {
        if(transfer.isComplete()) {
            callback.complete(transfer);
        }
        super.finish();
    }
}
