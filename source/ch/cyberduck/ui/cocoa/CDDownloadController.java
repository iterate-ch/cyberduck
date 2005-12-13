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

import ch.cyberduck.core.DownloadQueue;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.odb.Editor;

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.application.NSPanel;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSSelector;
import com.apple.cocoa.foundation.NSNotificationCenter;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * @version $Id$
 */
public class CDDownloadController extends CDWindowController {
    private static Logger log = Logger.getLogger(CDDownloadController.class);

    private NSTextField urlField;

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
    }

    public CDDownloadController() {
        if (!NSApplication.loadNibNamed("Download", this)) {
            log.fatal("Couldn't load Download.nib");
        }
    }

    public void awakeFromNib() {
        super.awakeFromNib();

        CDQueueController controller = CDQueueController.instance();
        controller.window().makeKeyAndOrderFront(null);
        controller.beginSheet(this.window(), //sheet
                this, //modal delegate
                new NSSelector("sheetDidEnd",
                        new Class[]{NSPanel.class, int.class, Object.class}), // did end selector
                null); //contextInfo
    }

    public void sheetDidEnd(NSPanel sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
        switch (returncode) {
            case (NSAlertPanel.DefaultReturn): //Download
                break;
            case (NSAlertPanel.OtherReturn): //Cancel
                break;
            case (NSAlertPanel.AlternateReturn): //Cancel
                break;
        }
        NSNotificationCenter.defaultCenter().removeObserver(this);
        instances.removeObject(this);
    }


    public void cancelButtonClicked(NSButton sender) {
        this.endSheet(this.window(), sender.tag());
    }

    public void downloadButtonClicked(NSButton sender) {
        try {
            Host host = Host.parse(urlField.stringValue());
            String file = host.getDefaultPath();
            if (file.length() > 1) {
                Path path = PathFactory.createPath(SessionFactory.createSession(host), file);
                try {
                    path.cwdir();
                    CDBrowserController controller = new CDBrowserController();
                    controller.mount(host);
                }
                catch (IOException e) {
                    Queue queue = new DownloadQueue();
                    queue.addRoot(path);
                    CDQueueController.instance().startItem(queue);
                }
                this.endSheet(this.window(), sender.tag());
            }
            else {
                throw new MalformedURLException("URL must contain reference to a file");
            }
        }
        catch (MalformedURLException e) {
            NSAlertPanel.beginCriticalAlertSheet("Error", //title
                    "OK", // defaultbutton
                    null, //alternative button
                    null, //other button
                    this.window(), //docWindow
                    null, //modalDelegate
                    null, //didEndSelector
                    null, // dismiss selector
                    null, // context
                    e.getMessage() // message
            );
        }
    }
}