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
import ch.cyberduck.binding.DisabledSheetCallback;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.SheetInvoker;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.ui.cocoa.controller.InsecureLoginAlertController;
import ch.cyberduck.ui.cocoa.controller.LoginController;
import ch.cyberduck.ui.cocoa.controller.ProgressAlertController;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Rococoa;

import java.util.concurrent.CountDownLatch;

public final class PromptLoginCallback extends PromptPasswordCallback implements LoginCallback {
    private static final Logger log = LogManager.getLogger(PromptLoginCallback.class);

    private final Preferences preferences
        = PreferencesFactory.get();

    private final WindowController parent;

    @Outlet
    private NSOpenPanel select;

    public PromptLoginCallback(final WindowController parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void await(final CountDownLatch signal, final Host bookmark, final String title, final String message) throws ConnectionCanceledException {
        log.debug("Display progress alert for {}", bookmark);
        final AlertController alert = new ProgressAlertController(signal, title, message, bookmark.getProtocol());
        final int returnCode = alert.beginSheet(parent);
        switch(returnCode) {
            case SheetCallback.DEFAULT_OPTION:
                // User dismissed sheet
                throw new ConnectionCanceledException();
        }
    }

    @Override
    public void warn(final Host bookmark, final String title, final String message,
                     final String continueButton, final String disconnectButton, final String preference) throws ConnectionCanceledException {
        log.debug("Display insecure connection alert for {}", bookmark);
        final AlertController alert = new InsecureLoginAlertController(title, message, continueButton, disconnectButton,
                bookmark.getProtocol(), StringUtils.isNotBlank(preference));
        int option = alert.beginSheet(parent);
        if(alert.isSuppressed()) {
            // Never show again.
            preferences.setProperty(preference, true);
        }
        switch(option) {
            case SheetCallback.CANCEL_OPTION:
                throw new LoginCanceledException();
        }
        // Proceed nevertheless.
    }

    @Override
    public Credentials prompt(final Host bookmark, final String username,
                              final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
        log.debug("Prompt for credentials for {}", username);
        final Credentials credentials = new Credentials(username).withSaved(options.save);
        final LoginController controller = new LoginController(new Host(bookmark).withCredentials(credentials), title, reason, options);
        final SheetInvoker sheet = new SheetInvoker(controller, parent, controller);
        final int option = sheet.beginSheet();
        if(option == SheetCallback.CANCEL_OPTION) {
            throw new LoginCanceledException();
        }
        return credentials;
    }

    public Local select(final Local identity) throws LoginCanceledException {
        final SheetInvoker sheet = new SheetInvoker(new DisabledSheetCallback(), parent, new WindowController() {
            @Override
            protected String getBundleName() {
                return null;
            }

            @Override
            public NSWindow window() {
                return select;
            }
        }) {
            @Override
            public void beginSheet(final NSWindow window) {
                select = NSOpenPanel.openPanel();
                select.setCanChooseDirectories(false);
                select.setCanChooseFiles(true);
                select.setAllowsMultipleSelection(false);
                select.setMessage(LocaleFactory.localizedString("Select the private key in PEM or PuTTY format", "Credentials"));
                select.setPrompt(LocaleFactory.localizedString("Choose"));
                select.beginSheetForDirectory(LocalFactory.get("~/.ssh").getAbsolute(),
                    null, parent.window(), this.id(), Foundation.selector("sheetDidClose:returnCode:contextInfo:"), null);
            }
        };
        final int option = sheet.beginSheet();
        if(option == SheetCallback.DEFAULT_OPTION) {
            final NSObject selected = select.URLs().lastObject();
            if(selected != null) {
                return LocalFactory.get(Rococoa.cast(selected, NSURL.class).path());
            }
        }
        throw new LoginCanceledException();
    }
}
