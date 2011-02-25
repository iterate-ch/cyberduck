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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSTextField;

import org.rococoa.cocoa.foundation.NSRect;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class DownloadController extends AlertController {
    private static Logger log = Logger.getLogger(DownloadController.class);

    /**
     *
     */
    protected NSTextField urlField
            = NSTextField.textfieldWithFrame(new NSRect(0, 22));

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

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            Host host = Host.parse(urlField.stringValue());
            final Transfer transfer = new DownloadTransfer(
                    PathFactory.createPath(SessionFactory.createSession(host),
                            host.getDefaultPath(), Path.FILE_TYPE)
            );
            TransferController.instance().startTransfer(transfer);
        }
    }

    @Override
    protected boolean validateInput() {
        Host host = Host.parse(urlField.stringValue());
        return StringUtils.isNotBlank(host.getDefaultPath());
    }

    @Override
    protected void help() {
        StringBuilder site = new StringBuilder(Preferences.instance().getProperty("website.help"));
        site.append("/howto/download");
        openUrl(site.toString());
    }
}