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

/**
* @version $Id$
 */
public class CDTransferController implements Observer {
    private static Logger log = Logger.getLogger(CDTransferController.class);

    private Path transfer;

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
    
    public CDTransferController(Path transfer) {
	super();
	this.transfer = transfer;
	transfer.addObserver(this);
	this.init();
    }

    private void init() {
	NSApplication.loadNibNamed("Transfer", this);

    }

    public void update(Observable o, Object arg) {
	if(o instanceof Path) {
	    
	}
    }

    public void download() {
	iconView.setImage(NSImage.imageNamed("download.tiff");
	this.transfer.download();
    }

    public void upload() {
	iconView.setImage(NSImage.imageNamed("upload.tiff");
	this.transfer.upload();
    }
}

