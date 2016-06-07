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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.diagnostics.ReachabilityFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.notification.NotificationAlertCallback;
import ch.cyberduck.core.threading.AlertCallback;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.cocoa.foundation.NSRect;

public class PanelAlertCallback implements AlertCallback {

    private final WindowController controller;

    private final FailureDiagnostics<Exception> diagnostics
            = new DefaultFailureDiagnostics();

    private final NotificationAlertCallback notification
            = new NotificationAlertCallback();

    public PanelAlertCallback(final WindowController controller) {
        this.controller = controller;
    }

    @Override
    public boolean alert(final Host host, final BackgroundException failure, final StringBuilder log) {
        notification.alert(host, failure, log);
        if(controller.isVisible()) {
            final NSAlert alert = NSAlert.alert(
                    null == failure.getMessage() ? LocaleFactory.localizedString("Unknown") : failure.getMessage(),
                    null == failure.getDetail() ? LocaleFactory.localizedString("Unknown") : failure.getDetail(),
                    LocaleFactory.localizedString("Try Again", "Alert"), // default button
                    diagnostics.determine(failure) == FailureDiagnostics.Type.network
                            ? LocaleFactory.localizedString("Network Diagnostics", "Alert") : null, //other button
                    LocaleFactory.localizedString("Cancel", "Alert") // alternate button
            );
            alert.setShowsHelp(true);
            final AlertController c = new AlertController(controller, alert) {
                @Override
                public void callback(final int returncode) {
                    if(returncode == ALTERNATE_OPTION) {
                        ReachabilityFactory.get().diagnose(host);
                    }
                }

                @Override
                protected void help() {
                    new DefaultProviderHelpService().help(host.getProtocol());
                }
            };
            if(!StringUtils.isBlank(log)) {
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
            if(c.returnCode() == SheetCallback.DEFAULT_OPTION) {
                return true;
            }
        }
        return false;
    }
}
