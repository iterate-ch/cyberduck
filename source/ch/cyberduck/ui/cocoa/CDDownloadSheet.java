package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.http.*;
import ch.cyberduck.core.ftp.*;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDDownloadSheet {
    private static Logger log = Logger.getLogger(CDDownloadSheet.class);

    private NSWindow sheet;
    public void setSheet(NSWindow sheet) {
	this.sheet = sheet;
    }

    private NSTextField urlLabel;
    public void setUrlLabel(NSTextField urlLabel) {
	this.urlLabel = urlLabel;
    }

    private NSPopUpButton protocolPopup;
    public void setProtocolPopup(NSPopUpButton protocolPopup) {
	this.protocolPopup = protocolPopup;
    }

    private NSTextField hostField;
    public void setHostField(NSTextField hostField) {
	this.hostField = hostField;
    }

    private NSTextField pathField;
    public void setPathField(NSTextField pathField) {
	this.pathField = pathField;
    }

    private NSTextField portField;
    public void setPortField(NSTextField portField) {
	this.portField = portField;
    }
    
    public CDDownloadSheet() {
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
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  hostField);
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  pathField);
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  portField);
	this.portField.setIntValue(protocolPopup.selectedItem().tag());
    }

    public void finalize() throws Throwable {
	super.finalize();
        NSNotificationCenter.defaultCenter().removeObserver(this);
    }

    public void protocolSelectionChanged(Object sender) {
	NSMenuItem selectedItem = protocolPopup.selectedItem();
	if(selectedItem.tag() == Session.FTP_PORT)
	    portField.setIntValue(Session.FTP_PORT);
	if(selectedItem.tag() == Session.HTTP_PORT)
	    portField.setIntValue(Session.HTTP_PORT);
	//@todo HTTPS
	this.textInputDidChange(null);
    }

    public void textInputDidChange(NSNotification sender) {
	NSMenuItem selectedItem = protocolPopup.selectedItem();
	String protocol = null;
	if(selectedItem.tag() == Session.FTP_PORT)
	    protocol = Session.FTP+"://";
	else if(selectedItem.tag() == Session.HTTP_PORT)
	    protocol = Session.HTTP+"://";
	urlLabel.setStringValue(protocol+hostField.stringValue()+":"+portField.stringValue()+"/"+pathField.stringValue());
    }
    
    public void closeSheet(NSButton sender) {
	this.window().close();
	//@ todo url field
	switch(sender.tag()) {
	    case(NSAlertPanel.DefaultReturn):
		int tag = protocolPopup.selectedItem().tag();
		Path file = null;
		Session session = null;
		switch(tag) {
		    case(Session.FTP_PORT):
			session = new FTPSession(new Host(Session.FTP, hostField.stringValue(), Session.FTP_PORT, new CDLoginController(this.window())));
			file = new FTPPath((FTPSession)session, pathField.stringValue());
			break;
		    case(Session.HTTP_PORT):
			session = new HTTPSession(new Host(Session.HTTP, hostField.stringValue(), Session.HTTP_PORT, new CDLoginController(this.window())));
			file = new HTTPPath((HTTPSession)session, pathField.stringValue());
			break;
		}
		    	//@todo keep reference?
		CDTransferController controller = new CDTransferController(file, Queue.KIND_DOWNLOAD);
		controller.start();
//		controller.window().makeKeyAndOrderFront(null);
//		file.download();
	    case(NSAlertPanel.AlternateReturn):
		//
	}
    }
}