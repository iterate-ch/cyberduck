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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import org.apache.log4j.Logger;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.Message;
import java.util.Observable;
import java.util.Observer;

/**
* @version $Id$
 */
public class CDTransferController implements Observer {
    private static Logger log = Logger.getLogger(CDTransferController.class);

    private Path file;
//@todo open new session for transfer
    //private Session session;

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSWindow window;
    public void setWindow(NSWindow window) {
	this.window = window;
    }
    
    private NSTextField urlField;
    public void setUrlField(NSTextField urlField) {
	this.urlField = urlField;
    }
    
    private NSTextField fileField;
    public void setFileField(NSTextField fileField) {
	this.fileField = fileField;
    }

    private NSTextField progressField;
    public void setProgressField(NSTextField progressField) {
	this.progressField = progressField;
    }

    private NSProgressIndicator progressBar;
    public void setProgressField(NSProgressIndicator progressBar) {
	this.progressBar = progressBar;
    }

    private NSButton stopButton;
    public void setStopButton(NSButton stopButton) {
	this.stopButton = stopButton;
    }

    private NSButton resumeButton;
    public void setResumeButton(NSButton resumeButton) {
	this.resumeButton = resumeButton;
    }

    public NSImageView iconView;
    public void setIconView(NSImageView iconView) {
	this.iconView = iconView;
    }

    public NSImageView fileIconView;
    public void setFileIconView(NSImageView fileIconView) {
	this.fileIconView = fileIconView;
    }
    
    public CDTransferController(Path file) {
	super();
	this.file = file;
	//register for events
	file.status.addObserver(this);
        if (false == NSApplication.loadNibNamed("Transfer", this)) {
            log.error("Couldn't load Transfer.nib");
            return;
        }
	this.init();
    }

    private void init() {
	this.fileIconView.setImage(NSWorkspace.sharedWorkspace().iconForFileType(file.getExtension()));
	this.urlField.setStringValue(file.getAbsolute());
	this.progressBar.setMinValue(0);
	this.progressBar.setMaxValue(file.status.getSize());
	
	//@todo
    }

    public void update(Observable o, Object arg) {
	if(o instanceof Status) {
	    if(arg instanceof Message) {
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.DATA)) {
		    this.progressField.setStringValue(msg.getDescription());
		    return;
		}
		if(msg.getTitle().equals(Message.PROGRESS)) {
		    //@todo
		    return;
		}
		if(msg.getTitle().equals(Message.ERROR)) {

		    return;
		}
		if(msg.getTitle().equals(Message.START)) {

		    return;
		}
		if(msg.getTitle().equals(Message.STOP)) {

		    return;
		}
		if(msg.getTitle().equals(Message.COMPLETE)) {

		    return;
		}
	    }
	}
    }

    public void download() {
	iconView.setImage(NSImage.imageNamed("download.tiff"));
	this.file.download();
    }

    public void upload() {
	iconView.setImage(NSImage.imageNamed("upload.tiff"));
//@todo	this.file.upload();
    }

    public NSWindow window() {
	return this.window;
    }

    public void resumeButtonClicked(NSObject sender) {
	this.stopButton.setEnabled(true);
	this.resumeButton.setEnabled(false);
    }

    public void stopButtonClicked(NSObject sender) {
	this.stopButton.setEnabled(false);
	this.resumeButton.setEnabled(true);
    }
}

