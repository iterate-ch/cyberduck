package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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
import ch.cyberduck.core.ftp.FTPPath;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.http.HTTPPath;
import ch.cyberduck.core.http.HTTPSession;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSPoint;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDDownloadController {
    private static Logger log = Logger.getLogger(CDDownloadController.class);
	
    private NSWindow window;
    public void setWindow(NSWindow window) {
		this.window = window;
    }
	
    private NSTextField urlField;
    public void setUrlField(NSTextField urlField) {
		this.urlField = urlField;
    }
	
    private static NSMutableArray allDocuments = new NSMutableArray();
	
    public CDDownloadController() {
		allDocuments.addObject(this);
        if (false == NSApplication.loadNibNamed("Download", this)) {
            log.fatal("Couldn't load Download.nib");
            return;
        }
    }
	
    public NSWindow window() {
		return this.window;
    }
	
    public void awakeFromNib() {
		log.debug("awakeFromNib");
		NSPoint origin = this.window.frame().origin();
		this.window.setFrameOrigin(new NSPoint(origin.x() + 16, origin.y() - 16));
    }
	
    public void windowWillClose(NSNotification notification) {
		this.window().setDelegate(null);
		allDocuments.removeObject(this);
    }
    
    public void closeWindow(NSButton sender) {
		switch(sender.tag()) {
			case(NSAlertPanel.DefaultReturn):
				URL url = null;
				try {
					url = new URL(urlField.stringValue());
					Host host = new Host(url.getProtocol(), url.getHost(), url.getPort(), new Login(url.getUserInfo()));
					Session session = host.createSession();
					Path path = null;
					String file = url.getFile();
					if(file.length() > 1) {
						if(host.getProtocol().equals(Session.FTP)) {
							path = new FTPPath((FTPSession)session, file);
						}
						else if(host.getProtocol().equals(Session.HTTP)) {
							path = new HTTPPath((HTTPSession)session, file);
						}
						this.window().orderOut(null);
						CDQueueController.instance().addTransfer(path, Queue.KIND_DOWNLOAD);
//						CDTransferController controller = new CDTransferController(path, Queue.KIND_DOWNLOAD);
//						controller.transfer();
					}
					else
						throw new MalformedURLException("URL must contain reference to a file");
				}
					catch(MalformedURLException e) {
						NSAlertPanel.beginCriticalAlertSheet(
										   "Error", //title
										   "OK",// defaultbutton
										   null,//alternative button
										   null,//other button
										   this.window(), //docWindow
										   null, //modalDelegate
										   null, //didEndSelector
										   null, // dismiss selector
										   null, // context
										   e.getMessage() // message
										   );
						
					}
					break;
			case(NSAlertPanel.AlternateReturn):
				this.window().orderOut(null);
				break;
		}
    }
}