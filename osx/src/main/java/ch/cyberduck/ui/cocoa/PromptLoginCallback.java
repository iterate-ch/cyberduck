package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.DisabledSheetCallback;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.SheetInvoker;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.ui.cocoa.controller.LoginController;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;

public final class PromptLoginCallback implements LoginCallback {
    private static final Logger log = Logger.getLogger(PromptLoginCallback.class);

    private final NSNotificationCenter notificationCenter
            = NSNotificationCenter.defaultCenter();

    private final Preferences preferences
            = PreferencesFactory.get();

    private final WindowController parent;

    @Outlet
    private NSOpenPanel select;

    public PromptLoginCallback(final WindowController parent) {
        this.parent = parent;
    }

    @Override
    public void warn(final Protocol protocol, final String title, final String message,
                     final String continueButton, final String disconnectButton, final String preference)
            throws LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Display insecure connection alert for %s", protocol));
        }
        final NSAlert alert = NSAlert.alert(title, message,
                continueButton, // Default Button
                null, // Alternate button
                disconnectButton // Other
        );
        alert.setShowsHelp(true);
        alert.setShowsSuppressionButton(true);
        alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't show again", "Credentials"));
        alert.setAlertStyle(NSAlert.NSWarningAlertStyle);
        final StringBuilder site = new StringBuilder(preferences.getProperty("website.help"));
        site.append("/").append(protocol.getScheme().name());
        int option = parent.alert(alert, site.toString());
        if(alert.suppressionButton().state() == NSCell.NSOnState) {
            // Never show again.
            preferences.setProperty(preference, true);
        }
        switch(option) {
            case SheetCallback.CANCEL_OPTION:
                throw new LoginCanceledException();
        }
        //Proceed nevertheless.
    }

    @Override
    public void prompt(final Host bookmark, final Credentials credentials,
                       final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Prompt for credentials for %s", bookmark));
        }
        final LoginController controller = new LoginController(title, reason, bookmark, credentials, options);
        final SheetInvoker sheet = new SheetInvoker(new DisabledSheetCallback(), parent, controller);
        final int option = sheet.beginSheet();
        if(option == SheetCallback.CANCEL_OPTION) {
            throw new LoginCanceledException();
        }
    }

    public Local select(final Local identity) throws LoginCanceledException {
        final SheetInvoker sheet = new SheetInvoker(new DisabledSheetCallback(), parent, select) {
            @Override
            public int beginSheet(final NSWindow window) {
                select = NSOpenPanel.openPanel();
                select.setCanChooseDirectories(false);
                select.setCanChooseFiles(true);
                select.setAllowsMultipleSelection(false);
                select.setMessage(LocaleFactory.localizedString("Select the private key in PEM or PuTTY format", "Credentials"));
                select.setPrompt(LocaleFactory.localizedString("Choose"));
                select.beginSheetForDirectory(LocalFactory.get("~/.ssh").getAbsolute(),
                        null, parent.window(), this.id(), Foundation.selector("sheetDidClose:returnCode:contextInfo:"), null);
                return this.getSelectedOption();
            }

            @Override
            public void invalidate() {
                notificationCenter.removeObserver(this.id());
                super.invalidate();
            }
        };
        final int option = sheet.beginSheet();
        if(option == SheetCallback.DEFAULT_OPTION) {
            final NSObject selected = select.filenames().lastObject();
            if(selected != null) {
                return LocalFactory.get(selected.toString());
            }
        }
        throw new LoginCanceledException();
    }
}
