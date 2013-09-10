package ch.cyberduck.core.editor;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;
import ch.cyberduck.ui.growl.Growl;
import ch.cyberduck.ui.growl.GrowlFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public abstract class BrowserBackgroundEditor extends AbstractEditor {
    private static final Logger log = Logger.getLogger(BrowserBackgroundEditor.class);

    private Controller controller;

    private Growl growl = GrowlFactory.get();

    /**
     * @param controller  Browser
     * @param application Editor
     * @param path        Remote file
     */
    public BrowserBackgroundEditor(final Controller controller, final Session session,
                                   final Application application, final Path path) {
        super(application, session, path);
        this.controller = controller;
    }

    /**
     * Open the file in the parent directory
     */
    @Override
    public void open(final TransferCallable download) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Open %s in %s", edited.getLocal().getAbsolute(), this.getApplication()));
        }
        controller.background(new BrowserBackgroundAction((BrowserController) controller) {
            @Override
            public Boolean run() throws BackgroundException {
                final Transfer transfer = download.call();
                growl.notify(transfer.isComplete() ?
                        String.format("%s complete", StringUtils.capitalize(transfer.getType().name())) : "Transfer incomplete", transfer.getName());
                return true;
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(LocaleFactory.localizedString("Downloading {0}", "Status"),
                        edited.getName());
            }
        });
    }

    /**
     * Upload the edited file to the server
     */
    @Override
    public void save(final TransferCallable upload) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Save changes from %s for %s", this.getApplication().getIdentifier(), edited.getLocal().getAbsolute()));
        }
        controller.background(new BrowserBackgroundAction((BrowserController) controller) {
            @Override
            public Boolean run() throws BackgroundException {
                final Transfer transfer = upload.call();
                growl.notify(transfer.isComplete() ?
                        String.format("%s complete", StringUtils.capitalize(transfer.getType().name())) : "Transfer incomplete", transfer.getName());
                return true;
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(LocaleFactory.localizedString("Uploading {0}", "Status"),
                        edited.getName());
            }
        });
    }
}
