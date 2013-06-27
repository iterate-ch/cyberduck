package ch.cyberduck.ui.cocoa.threading;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import ch.cyberduck.core.HostKeyControllerFactory;
import ch.cyberduck.core.LoginControllerFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.threading.RepeatableBackgroundAction;
import ch.cyberduck.ui.cocoa.AlertController;
import ch.cyberduck.ui.cocoa.SheetCallback;
import ch.cyberduck.ui.cocoa.WindowController;
import ch.cyberduck.ui.cocoa.application.NSAlert;

/**
 * @version $Id$
 */
public abstract class AlertRepeatableBackgroundAction extends RepeatableBackgroundAction {

    private WindowController controller;

    public AlertRepeatableBackgroundAction(final WindowController controller) {
        super(LoginControllerFactory.get(controller), HostKeyControllerFactory.get(controller));
        this.controller = controller;
    }

    @Override
    public void finish() throws BackgroundException {
        super.finish();
        // If there was any failure, display the summary now
        if(this.hasFailed() && !this.isCanceled()) {
            // Display alert if the action was not canceled intentionally
            this.alert();
        }
        this.reset();
    }

    private void callback(final int returncode) {
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            //Try Again
            this.reset();
            // Re-run the action with the previous lock used
            controller.background(this);
        }
    }

    /**
     * Display an alert dialog with a summary of all failed tasks
     */
    protected void alert() {
        if(controller.isVisible()) {
            final BackgroundException failure = this.getException();
            NSAlert alert = NSAlert.alert(
                    failure.getMessage(), //title
                    failure.getDetail(),
                    Locale.localizedString("Try Again", "Alert"), // default button
                    AlertRepeatableBackgroundAction.this.isNetworkFailure() ? Locale.localizedString("Network Diagnostics") : null, //other button
                    Locale.localizedString("Cancel") // alternate button
            );
            alert.setShowsHelp(true);
            final AlertController c = new AlertController(AlertRepeatableBackgroundAction.this.controller, alert) {
                @Override
                public void callback(final int returncode) {
                    if(returncode == SheetCallback.ALTERNATE_OPTION) {
                        AlertRepeatableBackgroundAction.this.diagnose();
                    }
                    if(returncode == SheetCallback.DEFAULT_OPTION) {
                        AlertRepeatableBackgroundAction.this.callback(returncode);
                    }
                }

                @Override
                protected void help() {
                    StringBuilder site = new StringBuilder(Preferences.instance().getProperty("website.help"));
                    if(null != failure.getPath()) {
                        site.append("/").append(getSessions().iterator().next().getHost().getProtocol().getProvider());
                    }
                    controller.openUrl(site.toString());
                }
            };
            c.beginSheet();
        }
    }
}