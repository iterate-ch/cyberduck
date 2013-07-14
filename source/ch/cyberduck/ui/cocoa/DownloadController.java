package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.core.DefaultPathKindDetector;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathKindDetector;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.download.DownloadTransfer;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSTextField;

import org.apache.commons.lang.StringUtils;
import org.rococoa.cocoa.foundation.NSRect;

/**
 * @version $Id$
 */
public class DownloadController extends AlertController {

    protected NSTextField urlField
            = NSTextField.textfieldWithFrame(new NSRect(0, 22));

    private PathKindDetector detector = new DefaultPathKindDetector();

    @Override
    public void beginSheet() {
        this.setAccessoryView(urlField);
        this.updateField(urlField, url);
        alert.setShowsHelp(true);
        super.beginSheet();
    }

    private String url;

    public DownloadController(final WindowController parent) {
        this(parent, StringUtils.EMPTY);
    }

    public DownloadController(final WindowController parent, final String url) {
        super(parent, NSAlert.alert(
                Locale.localizedString("New Download", "Download"),
                Locale.localizedString("URL", "Download"),
                Locale.localizedString("Download", "Download"),
                null,
                Locale.localizedString("Cancel", "Download")
        ), NSAlert.NSInformationalAlertStyle);
        this.url = url;
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            final Host host = HostParser.parse(urlField.stringValue());
            final Session session = SessionFactory.createSession(host);
            final Transfer transfer = new DownloadTransfer(session,
                    new Path(host.getDefaultPath(), detector.detect(host.getDefaultPath()))
            );
            TransferControllerFactory.get().startTransfer(transfer);
        }
    }

    @Override
    protected void focus() {
        // Focus accessory view.
        urlField.selectText(null);
        this.window().makeFirstResponder(urlField);
    }

    @Override
    protected boolean validateInput() {
        Host host = HostParser.parse(urlField.stringValue());
        return StringUtils.isNotBlank(host.getDefaultPath());
    }

    @Override
    protected void help() {
        StringBuilder site = new StringBuilder(Preferences.instance().getProperty("website.help"));
        site.append("/howto/download");
        openUrl(site.toString());
    }
}