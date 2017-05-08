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
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;

public class BackgroundExceptionAlertController extends AlertController {
    private final BackgroundException failure;
    private final Host host;

    public final String defaultButton;
    public final String cancelButton;

    public BackgroundExceptionAlertController(final BackgroundException failure, final Host host) {
        this.failure = failure;
        this.host = host;
        this.defaultButton = LocaleFactory.localizedString("Try Again", "Alert");
        this.cancelButton = LocaleFactory.localizedString("Cancel", "Alert");
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
        alert.setMessageText(null == failure.getMessage() ? LocaleFactory.localizedString("Unknown") : failure.getMessage());
        alert.setInformativeText(null == failure.getDetail() ? LocaleFactory.localizedString("Unknown") : failure.getDetail());
        alert.addButtonWithTitle(defaultButton);
        alert.addButtonWithTitle(cancelButton);
        if(new DefaultFailureDiagnostics().determine(failure) == FailureDiagnostics.Type.network) {
            alert.addButtonWithTitle(LocaleFactory.localizedString("Network Diagnostics", "Alert"));
        }
        this.loadBundle(alert);
    }

    @Override
    protected String help() {
        return new DefaultProviderHelpService().help(host.getProtocol());
    }
}
