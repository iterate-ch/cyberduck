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

import ch.cyberduck.core.Message;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.Preferences;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;

import java.util.Observable;
import java.util.Observer;

/**
* @version $Id$
 */
public class CDTransferController implements Observer {
    private static Logger log = Logger.getLogger(CDTransferController.class);

    private int kind;
    private Path file;
    private Queue queue;

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

    private NSTextField clockField;
    public void setClockField(NSTextField clockField) {
	this.clockField = clockField;
    }
    
    private NSTextField fileField;
    public void setFileField(NSTextField fileField) {
	this.fileField = fileField;
    }

    private NSTextField progressField;
    public void setProgressField(NSTextField progressField) {
	this.progressField = progressField;
    }

    private NSTextField dataField;
    public void setDataField(NSTextField dataField) {
	this.dataField = dataField;
    }
    
    private NSProgressIndicator progressBar;
    public void setProgressBar(NSProgressIndicator progressBar) {
	this.progressBar = progressBar;
	this.progressBar.setIndeterminate(true);
	this.progressBar.setUsesThreadedAnimation(true);
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


    /**
	* @param kind Tag specifiying if it is a download or upload.
     */
    public CDTransferController(Path file, int kind) {
	super();
	this.file = file;
	this.kind = kind;
	//register for events
	
        if (false == NSApplication.loadNibNamed("Transfer", this)) {
            log.fatal("Couldn't load Transfer.nib");
            return;
        }
	this.init();
    }

    private void init() {
	log.debug("init");
	this.fileIconView.setImage(NSWorkspace.sharedWorkspace().iconForFileType(file.getExtension()));
	this.urlField.setStringValue(file.getAbsolute()); //@todo url
	this.fileField.setStringValue(file.getLocal().toString());
	this.window().setTitle(file.getName());
	this.progressField.setStringValue("");
	this.dataField.setStringValue("");
	this.clockField.setStringValue("00:00");
	switch(kind) {
	    case Queue.KIND_DOWNLOAD:
		iconView.setImage(NSImage.imageNamed("download.tiff"));
		break;
	    case Queue.KIND_UPLOAD:
		iconView.setImage(NSImage.imageNamed("upload.tiff"));
		break;
	}
    }

    public void start() {
//	file.getDownloadSession().addObserver(this);
	this.queue = new Queue(kind);
	this.queue.addObserver(this);
	switch(kind) {
	    case Queue.KIND_DOWNLOAD:
	//@todo check if file exists in download folder
		file.fillDownloadQueue(queue, file.getSession().copy());
		break;
	    case Queue.KIND_UPLOAD:
	//@todo check if file exists on server
		file.fillUploadQueue(queue, file.getSession().copy());
		break;
	}
//	this.progressBar.setMaxValue(queue.size());
	this.window().setTitle(file.getName()+" - "+queue.numberOfElements()+" files");
	this.window().makeKeyAndOrderFront(null);
	queue.start();
    }

    public void update(Observable o, Object arg) {
//	log.debug("update:"+o+","+arg);
	if(o instanceof Status) {
	    //@todo get session messages
	    if(arg instanceof Message) {
		Status status = (Status)o;
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.DATA)) {
		    this.progressBar.setIndeterminate(false);
		    this.progressBar.setDoubleValue((double)status.getCurrent());
		    this.progressBar.setMaxValue(status.getSize());
		    this.dataField.setStringValue(msg.getDescription());
		}
		else if(msg.getTitle().equals(Message.CLOCK)) {
		    clockField.setStringValue(msg.getDescription());
		}
		else if(msg.getTitle().equals(Message.START)) {
		    this.resumeButton.setTitle("Resume");
		    this.progressBar.startAnimation(null);
		    this.stopButton.setEnabled(true);
		    this.resumeButton.setEnabled(false);
		    this.progressBar.setMinValue(0);
		}
		else if(msg.getTitle().equals(Message.STOP)) {
		    this.progressBar.stopAnimation(null);
		    this.stopButton.setEnabled(false);
		    this.resumeButton.setEnabled(true);
		}
		else if(msg.getTitle().equals(Message.COMPLETE)) {
		    this.progressBar.setDoubleValue((double)status.getCurrent());
		    this.resumeButton.setTitle("Reload");
		    this.stopButton.setEnabled(false);
		    this.resumeButton.setEnabled(true);
		    this.progressField.setStringValue("Complete");
		    if(Queue.KIND_DOWNLOAD == kind) {
			//@todo temp path name
			//path.getLocalTemp().renameTo(path.getLocal());
			if(Preferences.instance().getProperty("connection.download.postprocess").equals("true")) {
			    NSWorkspace.sharedWorkspace().openFile(file.getLocal().toString());
			}
		    }
		}
	    }
	}
	if(o instanceof Session) {
	    if(arg instanceof Message) {
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.ERROR)) {
		    NSAlertPanel.beginAlertSheet(
				   "Error", //title
				   "OK",// defaultbutton
				   null,//alternative button
				   null,//other button
				   this.window(), //docWindow
				   null, //modalDelegate
				   null, //didEndSelector
				   null, // dismiss selector
				   null, // context
				   msg.getDescription() // message
				   );
		    this.stopButtonClicked(null);
		    this.progressField.setStringValue(msg.getDescription());

		}
		else if(msg.getTitle().equals(Message.PROGRESS)) {
		    this.progressField.setStringValue(msg.getDescription());
		}
	    }
	}
    }

    public NSWindow window() {
	return this.window;
    }

    public void resumeButtonClicked(NSButton sender) {
	if(sender.title().equals("Resume"))
	    this.file.status.setResume(true);
	this.start();
    }

    public void stopButtonClicked(NSButton sender) {
	this.queue.cancel();
    }

    public void showInFinderClicked(NSButton sender) {
	NSWorkspace.sharedWorkspace().selectFile(file.getLocal().toString(), "");
    }

    public boolean windowShouldClose(Object sender) {
	if(!this.file.status.isStopped()) {
	    NSAlertPanel.beginCriticalAlertSheet(
					       "Cancel transfer?", //title
					       "Stop",// defaultbutton
					       "Cancel",//alternative button
					       null,//other button
					       this.window(),
					       this, //delegate
					       new NSSelector
					       (
	     "confirmSheetDidEnd",
	     new Class[]
	     {
		 NSWindow.class, int.class, NSWindow.class
	     }
	     ),// end selector
					       null, // dismiss selector
					       this, // context
					       "Closing this window will stop the file transfer" // message
					       );
	    return false;	    
	}
	return true;
    }

    public void confirmSheetDidEnd(NSWindow sheet, int returncode, NSWindow main)  {
	sheet.orderOut(null);
	if(returncode == NSAlertPanel.DefaultReturn) {
	    this.stopButtonClicked(null);
	    this.window().close();
	}
	if(returncode == NSAlertPanel.AlternateReturn) {
	    //
	}
    }
}

