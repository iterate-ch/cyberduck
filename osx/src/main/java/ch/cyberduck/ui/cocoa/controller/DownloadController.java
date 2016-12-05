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
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.core.DefaultPathKindDetector;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathKindDetector;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.EnumSet;

public class DownloadController extends AlertController {

    protected final NSTextField urlField
            = NSTextField.textfieldWithFrame(new NSRect(0, 22));

    private final PathKindDetector detector = new DefaultPathKindDetector();

    public DownloadController(final WindowController parent) {
        this(parent, StringUtils.EMPTY);
    }

    public DownloadController(final WindowController parent, final String url) {
        super(NSAlert.alert(
                LocaleFactory.localizedString("New Download", "Download"),
                LocaleFactory.localizedString("URL", "Download"),
                LocaleFactory.localizedString("Download", "Download"),
                null,
                LocaleFactory.localizedString("Cancel", "Download")
        ), NSAlert.NSInformationalAlertStyle);
        this.updateField(urlField, url);
        this.alert.setShowsHelp(true);
    }

    @Override
    public NSView getAccessoryView() {
        return urlField;
    }

    @Override
    protected void focus() {
        super.focus();
        urlField.selectText(null);
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            final Host host = HostParser.parse(urlField.stringValue());
            final Path file = new Path(PathNormalizer.normalize(host.getDefaultPath(), true),
                    EnumSet.of(detector.detect(host.getDefaultPath())));
            host.setDefaultPath(file.getParent().getAbsolute());
            final Transfer transfer = new DownloadTransfer(host, file,
                    LocalFactory.get(PreferencesFactory.get().getProperty("queue.download.folder"), file.getName()));
            TransferControllerFactory.get().start(transfer);
        }
    }

    @Override
    public boolean validate() {
        Host host = HostParser.parse(urlField.stringValue());
        return StringUtils.isNotBlank(host.getDefaultPath());
    }

    @Override
    protected void help() {
        StringBuilder site = new StringBuilder(PreferencesFactory.get().getProperty("website.help"));
        site.append("/howto/download");
        BrowserLauncherFactory.get().open(site.toString());
    }
}