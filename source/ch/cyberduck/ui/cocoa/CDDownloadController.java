package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSObject;
import com.apple.cocoa.foundation.NSSelector;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class CDDownloadController extends NSObject implements CDController {
    private static Logger log = Logger.getLogger(CDDownloadController.class);

    private static NSMutableArray instances = new NSMutableArray();

    private NSWindow window;

    public void setWindow(NSWindow window) {
        this.window = window;
        this.window.setDelegate(this);
    }

    public NSWindow window() {
        return this.window;
    }

    private NSTextField urlField;

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
    }

    public CDDownloadController() {
        instances.addObject(this);
        if (false == NSApplication.loadNibNamed("Download", this)) {
            log.fatal("Couldn't load Download.nib");
        }
    }

    public void awakeFromNib() {
        log.debug("awakeFromNib");
        CDQueueController controller = CDQueueController.instance();
        controller.window().makeKeyAndOrderFront(null);
        NSApplication.sharedApplication().beginSheet(this.window, //sheet
                controller.window(),
                this, //modalDelegate
                new NSSelector("downloadSheetDidEnd",
                        new Class[]{NSWindow.class, int.class, Object.class}), // did end selector
                null); //contextInfo
    }

    public boolean windowShouldClose(NSWindow sender) {
        return true;
    }

    public void windowWillClose(NSNotification notification) {
        instances.removeObject(this);
    }

    public void downloadSheetDidEnd(NSWindow sheet, int returncode, Object context) {
        this.window().orderOut(null);
    }

    public void cancelButtonClicked(Object sender) {
        NSApplication.sharedApplication().endSheet(this.window(), ((NSButton)sender).tag());
    }

    public void downloadButtonClicked(Object sender) {
        log.debug("downloadButtonClicked");
        try {
            URL url = new URL(URLDecoder.decode(urlField.stringValue(), "UTF-8"));
            Host host = new Host(url.getProtocol(),
                    url.getHost(),
                    url.getPort(),
                    new Login(url.getHost(), url.getUserInfo(), null));
            Session session = SessionFactory.createSession(host);
            String file = url.getFile();
            if (file.length() > 1) {
                Path path = PathFactory.createPath(SessionFactory.createSession(host), file);
				Queue queue = new DownloadQueue();
                queue.addRoot(path);
				CDQueueController.instance().addItem(queue);
//                QueueList.instance().addItem(queue);
                CDQueueController.instance().startItem(queue);
            }
            else {
                throw new MalformedURLException("URL must contain reference to a file");
            }
            NSApplication.sharedApplication().endSheet(this.window(), ((NSButton)sender).tag());
        }
		catch (java.io.UnsupportedEncodingException e) {
            log.error(e.getMessage());
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