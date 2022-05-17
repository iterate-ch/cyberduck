package ch.cyberduck.ui.cocoa.controller;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.threading.BrowserTransferBackgroundAction;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.ui.quicklook.QuickLook;

import java.util.ArrayList;
import java.util.List;

public class QuicklookTransferBackgroundAction extends BrowserTransferBackgroundAction {

    private final QuickLook quicklook;
    private final List<TransferItem> downloads;

    public QuicklookTransferBackgroundAction(final Controller controller, final QuickLook quicklook, final SessionPool session,
                                             final Transfer transfer, final List<TransferItem> items) {
        super(controller, session, transfer, new TransferCallback() {
            @Override
            public void complete(final Transfer transfer) {
                //
            }
        }, new TransferPrompt() {
            @Override
            public TransferAction prompt(final TransferItem item) {
                return TransferAction.comparison;
            }

            @Override
            public boolean isSelected(final TransferItem file) {
                return true;
            }

            @Override
            public void message(final String message) {
                controller.message(message);
            }
        });
        this.quicklook = quicklook;
        this.downloads = items;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if(!this.hasFailed()) {
            final List<Local> previews = new ArrayList<>();
            for(TransferItem download : downloads) {
                previews.add(download.local);
            }
            // Change files in Quick Look
            quicklook.select(previews);
            // Open Quick Look Preview Panel
            quicklook.open();
        }
    }

    @Override
    public String getActivity() {
        return LocaleFactory.localizedString("Quick Look", "Status");
    }
}
