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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.http.*;
import ch.cyberduck.core.ftp.*;
import ch.cyberduck.core.Preferences;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;
import java.net.URL;
import java.net.MalformedURLException;

/**
* @version $Id$
 */
public class CDDownloadController {
    private static Logger log = Logger.getLogger(CDDownloadController.class);

    private NSWindow sheet;
    public void setSheet(NSWindow sheet) {
	this.sheet = sheet;
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
	this.init();
    }

    public NSWindow window() {
	return this.sheet;
    }

    private void init() {
	//
    }

    public void finalize() throws Throwable {
	log.debug("finalize");
	super.finalize();
    }

    public void windowWillClose(NSNotification notification) {
	this.window().setDelegate(null);
	allDocuments.removeObject(this);
    }
    
    public void closeSheet(NSButton sender) {
	//@ todo url field
	switch(sender.tag()) {
	    case(NSAlertPanel.DefaultReturn):
		URL url = null;
		try {
		    url = new URL(urlField.stringValue());
		    this.window().orderOut(null);
		    String protocol = url.getProtocol();
		    String host = url.getHost();
		    String file = url.getPath();
		    Path path = null;
		    Session session = null;
		    CDTransferController controller = new CDTransferController(Queue.KIND_DOWNLOAD);
		    if(protocol.equals(Session.FTP)) {
			String userinfo = url.getUserInfo();
			String user = Preferences.instance().getProperty("ftp.anonymous.name");
			String pass = Preferences.instance().getProperty("ftp.anonymous.pass");
			if(userinfo != null) {
			    int i = userinfo.indexOf(':');
			    if(i != -1) {
				user = userinfo.substring(0, i);
				pass = userinfo.substring(i + 1);
			    }
			}

			//@todo attach logincontroller to transfer window
			session = new FTPSession(new Host(Session.FTP, host, url.getPort(), new CDLoginController(controller.window(), user, pass)));
			path = new FTPPath((FTPSession)session, file);
		    }
		    else if (protocol.equals(Session.HTTP)) {
//@todo			this.setServerPath(a.getPath() + "?" + a.getQuery());
			session = new HTTPSession(new Host(Session.HTTP, host, url.getPort(), new CDLoginController(this.window())));
			path = new HTTPPath((HTTPSession)session, file);
		    }
			//@todo HTTPS
			//@todo SCP
		    controller.setPath(path);
		    controller.transfer(path.status.isResume());
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