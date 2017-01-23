package ch.cyberduck.ui.cocoa.controller;

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
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

public class DonateAlertController extends AlertController {

    private final Preferences preferences = PreferencesFactory.get();

    private final NSApplication app;

    public DonateAlertController(final NSApplication app) {
        this.app = app;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setMessageText(LocaleFactory.localizedString("Thank you for using Cyberduck!", "Donate"));
        final StringAppender message = new StringAppender();
        message.append(LocaleFactory.localizedString("It has taken many nights to develop this application. If you enjoy using it, please consider a donation to the author of this software. It will help to make Cyberduck even better!", "Donate"));
        message.append(LocaleFactory.localizedString("As a contributor to Cyberduck, you receive a donation key that disables this prompt.", "Donate"));
        alert.setInformativeText(message.toString());
        alert.addButtonWithTitle(LocaleFactory.localizedString("Donate!", "Donate"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Later", "Donate"));
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setShowsSuppressionButton(true);
        alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't show again for this version.", "Donate"));
        this.loadBundle(alert);
    }

    @Override
    public void callback(final int returncode) {
        if(this.isSuppressed()) {
            preferences.setProperty("donate.reminder",
                    NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleShortVersionString").toString());
        }
        // Remember this reminder date
        preferences.setProperty("donate.reminder.date", System.currentTimeMillis());
        switch(returncode) {
            case DEFAULT_OPTION:
                BrowserLauncherFactory.get().open(preferences.getProperty("website.donate"));
                break;
        }
        this.terminate();
    }

    private void terminate() {
        if(this.isSuppressed()) {
            preferences.setProperty("donate.reminder",
                    NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleShortVersionString").toString());
        }
        // Remember this reminder date
        preferences.setProperty("donate.reminder.date", System.currentTimeMillis());
        // Quit again
        app.replyToApplicationShouldTerminate(true);
    }
}
