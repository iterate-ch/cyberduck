package ch.cyberduck.ui.cocoa.callback;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.AlertController;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.LimitedListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.ui.cocoa.controller.LimitedListAlertController;

public class PromptLimitedListProgressListener extends LimitedListProgressListener {

    private final WindowController controller;

    private boolean suppressed;

    public PromptLimitedListProgressListener(final WindowController controller) {
        super(controller);
        this.controller = controller;
    }

    @Override
    public void chunk(final Path parent, final AttributedList<Path> list) throws ListCanceledException {
        if(suppressed) {
            return;
        }
        try {
            super.chunk(parent, list);
        }
        catch(ListCanceledException e) {
            final AlertController alert = new LimitedListAlertController(e);
            final int returncode = alert.beginSheet(controller);
            if(alert.isSuppressed()) {
                this.disable();
            }
            switch(returncode) {
                case SheetCallback.CANCEL_OPTION:
                    throw e;
            }
        }
    }

    @Override
    protected void disable() {
        super.disable();
        suppressed = true;
    }
}
