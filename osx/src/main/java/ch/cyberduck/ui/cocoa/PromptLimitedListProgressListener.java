package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.binding.AlertController;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.LimitedListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ListCanceledException;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

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
            final AlertController alert = new AlertController() {
                @Override
                public void loadBundle() {
                    final NSAlert alert = NSAlert.alert();
                    alert.setMessageText(MessageFormat.format(LocaleFactory.localizedString("Listing directory {0}", "Status"), StringUtils.EMPTY));
                    alert.setInformativeText(MessageFormat.format(LocaleFactory.localizedString("Continue listing directory with more than {0} files.", "Alert"), e.getChunk().size()));
                    alert.addButtonWithTitle(LocaleFactory.localizedString("Continue", "Credentials"));
                    alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel"));
                    alert.setShowsSuppressionButton(true);
                    alert.suppressionButton().setTitle(LocaleFactory.localizedString("Always"));
                    super.loadBundle(alert);
                }
            };
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
