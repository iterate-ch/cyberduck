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

import ch.cyberduck.core.DownloadTransfer;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.Transfer;

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

import java.net.MalformedURLException;

/**
 * @version $Id$
 */
public class CDDownloadController extends CDSheetController {
    private static Logger log = Logger.getLogger(CDDownloadController.class);

    private NSTextField urlField;

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
    }

    public CDDownloadController(final CDWindowController parent) {
        super(parent);
    }

    protected String getBundleName() {
        return "Download";
    }

    public void callback(final int returncode) {
        if (returncode == DEFAULT_OPTION) {
            try {
                Host host = Host.parse(urlField.stringValue());
                final Transfer transfer = new DownloadTransfer(
                        PathFactory.createPath(SessionFactory.createSession(host),
                                host.getDefaultPath())
                );
                CDTransferController.instance().startTransfer(transfer);
            }
            catch (MalformedURLException e) {
                log.error(e.getMessage());
            }
        }
    }

    protected boolean validateInput() {
        try {
            Host host = Host.parse(urlField.stringValue());
            return host.hasReasonableDefaultPath();
        }
        catch (MalformedURLException e) {
            this.alert(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Error", "Alert sheet title"),
                    e.getMessage(), // message
                    NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
                    null, //alternative button
                    null //other button
            ));
            return false;
        }
    }
}