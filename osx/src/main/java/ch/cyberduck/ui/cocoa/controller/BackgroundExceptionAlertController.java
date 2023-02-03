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
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.ProviderHelpServiceFactory;
import ch.cyberduck.core.diagnostics.ReachabilityDiagnosticsFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;

public class BackgroundExceptionAlertController extends AlertController {

    private final BackgroundException failure;
    private final Host host;
    private final String defaultButton;
    private final String cancelButton;

    public BackgroundExceptionAlertController(final BackgroundException failure, final Host host) {
        this(failure, host, LocaleFactory.localizedString("Try Again", "Alert"), LocaleFactory.localizedString("Cancel", "Alert"));
    }

    public BackgroundExceptionAlertController(final BackgroundException failure, final Host host, final String defaultButton, final String cancelButton) {
        this.failure = failure;
        this.host = host;
        this.defaultButton = defaultButton;
        this.cancelButton = cancelButton;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setMessageText(String.format("%s (%s)", null == failure.getMessage() ? LocaleFactory.localizedString("Unknown") : failure.getMessage(),
                BookmarkNameProvider.toString(host)));
        alert.setInformativeText(null == failure.getDetail() ? LocaleFactory.localizedString("Unknown") : failure.getDetail());
        alert.addButtonWithTitle(defaultButton);
        alert.addButtonWithTitle(cancelButton);
        final FailureDiagnostics.Type type = new DefaultFailureDiagnostics().determine(failure);
        switch(type) {
            case network:
                if(new ReachabilityDiagnosticsFactory().isAvailable()) {
                    alert.addButtonWithTitle(LocaleFactory.localizedString("Network Diagnostics", "Alert"));
                }
                break;
            case quota:
                alert.addButtonWithTitle(LocaleFactory.localizedString("Help", "Main"));
                break;
        }
        this.loadBundle(alert);
    }

    @Override
    protected String help() {
        return ProviderHelpServiceFactory.get().help(host.getProtocol());
    }
}
