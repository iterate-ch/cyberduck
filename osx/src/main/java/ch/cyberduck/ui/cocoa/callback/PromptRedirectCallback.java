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
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.http.PreferencesRedirectCallback;
import ch.cyberduck.core.http.RedirectCallback;
import ch.cyberduck.ui.cocoa.controller.RedirectAlertController;

public class PromptRedirectCallback implements RedirectCallback {

    private final RedirectCallback preferences
            = new PreferencesRedirectCallback();

    private final ProxyController controller;

    private boolean suppressed;
    private boolean option;

    public PromptRedirectCallback(final ProxyController controller) {
        this.controller = controller;
    }

    @Override
    public boolean redirect(final String method) {
        if(suppressed) {
            return option;
        }
        if(preferences.redirect(method)) {
            // Allow if set defaults
            return true;
        }
        final AlertController alert = new RedirectAlertController(method);
        option = controller.alert(alert) == SheetCallback.DEFAULT_OPTION;
        if(alert.isSuppressed()) {
            suppressed = true;
        }
        return option;
    }

}
