package ch.cyberduck.ui.cocoa.threading;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.ReachabilityFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.AlertCallback;
import ch.cyberduck.core.threading.NetworkFailureDiagnostics;
import ch.cyberduck.core.threading.SessionBackgroundAction;
import ch.cyberduck.ui.cocoa.AlertController;
import ch.cyberduck.ui.cocoa.SheetCallback;
import ch.cyberduck.ui.cocoa.TranscriptController;
import ch.cyberduck.ui.cocoa.WindowController;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSView;

import org.rococoa.cocoa.foundation.NSRect;

/**
 * @version $Id$
 */
public class PanelAlertCallback implements AlertCallback {

    private final WindowController controller;

    private final NetworkFailureDiagnostics diagnostics
            = new NetworkFailureDiagnostics();

    public PanelAlertCallback(final WindowController controller) {
        this.controller = controller;
    }

    @Override
    public void alert(final SessionBackgroundAction<?> action,
                      final BackgroundException failure, final StringBuilder log) {
        if(controller.isVisible()) {
            final NSAlert alert = NSAlert.alert(
                    null == failure.getMessage() ? LocaleFactory.localizedString("Unknown") : failure.getMessage(),
                    null == failure.getDetail() ? LocaleFactory.localizedString("Unknown") : failure.getDetail(),
                    LocaleFactory.localizedString("Try Again", "Alert"), // default button
                    diagnostics.isNetworkFailure(failure) ? LocaleFactory.localizedString("Network Diagnostics") : null, //other button
                    LocaleFactory.localizedString("Cancel") // alternate button
            );
            alert.setShowsHelp(true);
            final AlertController c = new AlertController(controller, alert) {
                @Override
                public void callback(final int returncode) {
                    if(returncode == SheetCallback.ALTERNATE_OPTION) {
                        for(Session session : action.getSessions()) {
                            ReachabilityFactory.get().diagnose(session.getHost());
                        }
                    }
                    if(returncode == SheetCallback.DEFAULT_OPTION) {
                        // Re-run the action with the previous lock used
                        controller.background(action);
                    }
                }

                @Override
                protected void help() {
                    new DefaultProviderHelpService().help(action.getSessions().iterator().next().getHost().getProtocol());
                }
            };
            if(log.length() > 0) {
                final TranscriptController transcript = new TranscriptController() {
                    @Override
                    public boolean isOpen() {
                        return true;
                    }
                };
                transcript.log(true, log.toString());
                final NSView view = transcript.getLogView();
                view.setFrame(new NSRect(0, 100d));
                c.setAccessoryView(view);
            }
            c.beginSheet();
        }
    }
}
