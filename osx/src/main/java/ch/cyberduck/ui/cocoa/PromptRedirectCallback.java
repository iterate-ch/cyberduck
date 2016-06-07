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

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.http.PreferencesRedirectCallback;
import ch.cyberduck.core.http.RedirectCallback;

import org.apache.log4j.Logger;

public class PromptRedirectCallback implements RedirectCallback {
    private static final Logger log = Logger.getLogger(PromptRedirectCallback.class);

    private RedirectCallback preferences
            = new PreferencesRedirectCallback();

    private WindowController parent;

    public PromptRedirectCallback(final WindowController parent) {
        this.parent = parent;
    }

    @Override
    public boolean redirect(final String method) {
        if(preferences.redirect(method)) {
            // Allow if set defaults
            return true;
        }
        NSAlert alert = NSAlert.alert("Redirect", //title
                LocaleFactory.localizedString(String.format("Allow redirect for method %s", method), "Alert"),
                LocaleFactory.localizedString("Allow"), // defaultbutton
                LocaleFactory.localizedString("Cancel", "Alert"), //alternative button
                null //other button
        );
        alert.setShowsSuppressionButton(true);
        alert.suppressionButton().setTitle(LocaleFactory.localizedString("Always"));
        SheetController c = new AlertController(parent, alert) {
            @Override
            public void callback(final int returncode) {
                if(returncode == DEFAULT_OPTION) {

                }
                else {
                    log.warn("Cannot continue without a valid host key");
                }
            }
        };
        c.beginSheet();
        return c.returnCode() == SheetCallback.DEFAULT_OPTION;
    }
}
