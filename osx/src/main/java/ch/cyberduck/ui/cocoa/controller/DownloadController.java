package ch.cyberduck.ui.cocoa.controller;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.core.DefaultPathKindDetector;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathKindDetector;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.ProviderHelpServiceFactory;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferOptions;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.EnumSet;

public class DownloadController extends AlertController {
    private static final Logger log = LogManager.getLogger(DownloadController.class);

    protected final NSTextField urlField
        = NSTextField.textfieldWithFrame(new NSRect(0, 22));

    private final PathKindDetector detector = new DefaultPathKindDetector();
    private final String url;

    public DownloadController() {
        this(StringUtils.EMPTY);
    }

    public DownloadController(final String url) {
        this.url = url;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(LocaleFactory.localizedString("New Download", "Download"));
        alert.setInformativeText(LocaleFactory.localizedString("URL", "Download"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Download", "Download"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Download"));
        this.loadBundle(alert);
    }

    @Override
    public NSView getAccessoryView(final NSAlert alert) {
        urlField.cell().setWraps(false);
        urlField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(StringUtils.EMPTY, TRUNCATE_MIDDLE_ATTRIBUTES));
        return urlField;
    }

    @Override
    protected void focus(final NSAlert alert) {
        super.focus(alert);
        this.updateField(urlField, url);
        urlField.selectText(null);
    }

    @Override
    public void callback(final int returncode) {
        switch(returncode) {
            case DEFAULT_OPTION:
                try {
                    final Host host = HostParser.parse(urlField.stringValue());
                    final Path file = new Path(PathNormalizer.normalize(host.getDefaultPath()),
                        EnumSet.of(detector.detect(host.getDefaultPath())));
                    host.setDefaultPath(file.getParent().getAbsolute());
                    final Transfer transfer = new DownloadTransfer(host, file,
                        LocalFactory.get(PreferencesFactory.get().getProperty("queue.download.folder"), file.getName()));
                    TransferControllerFactory.get().start(transfer, new TransferOptions());
                    break;
                }
                catch(HostParserException e) {
                    log.warn(e);
                }
        }
    }

    @Override
    public boolean validate(final int option) {
        final Host host;
        try {
            host = HostParser.parse(urlField.stringValue());
        }
        catch(HostParserException e) {
            return false;
        }
        return StringUtils.isNotBlank(host.getDefaultPath());
    }

    @Override
    protected String help() {
        return ProviderHelpServiceFactory.get().help();
    }
}
