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
    
    private NSTextField fileDataField;
    public void setFileDataField(NSTextField fileDataField) {
	this.fileDataField = fileDataField;
    }

    private NSTextField totalDataField;
    public void setTotalDataField(NSTextField totalDataField) {
	this.totalDataField = totalDataField;
    }
    
    private NSProgressIndicator totalProgressBar;
    public void setTotalProgressBar(NSProgressIndicator totalProgressBar) {
	this.totalProgressBar = totalProgressBar;
	this.totalProgressBar.setIndeterminate(true);
	this.totalProgressBar.setUsesThreadedAnimation(true);
//	this.totalProgressBar.setDoubleValue(0);
    }

//    private NSProgressIndicator fileProgressBar;
//    public void setFileProgressBar(NSProgressIndicator fileProgressBar) {
//	this.fileProgressBar = fileProgressBar;
//	this.fileProgressBar.setIndeterminate(true);
//	this.fileProgressBar.setUsesThreadedAnimation(true);
//	this.fileProgressBar.setDoubleValue(0);
//    }

    private NSButton stopButton;
    public void setStopButton(NSButton stopButton) {
	this.stopButton = stopButton;
    }

    private NSButton resumeButton;
    public void setResumeButton(NSButton resumeButton) {
	this.resumeButton = resumeButton;
    }

    private NSButton reloadButton;
    public void setReloadButton(NSButton reloadButton) {
	this.reloadButton = reloadButton;
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
        if (false == NSApplication.loadNibNamed("Transfer", this)) {
            log.fatal("Couldn't load Transfer.nib");
            return;
        }
//@todo	this.init();
    }

    public CDTransferController(int kind) {
	this(null, kind);
    }

    public void setPath(Path file) {
	this.file = file;
    }
    
    private void init() {
	log.debug("init");
	this.urlField.setStringValue(file.getAbsolute()); //@todo url
	this.fileField.setStringValue(file.getLocal().toString());
	this.window().setTitle(file.getName());
	this.progressField.setStringValue("");
	this.fileDataField.setStringValue("");
	this.totalDataField.setStringValue("");
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

    public void finalize() throws Throwable {
	log.debug("finalize");
	super.finalize();
    }
    
    public void start(boolean resume) {
	this.init();
	this.queue = new Queue(kind);
	this.queue.addObserver(this);
	this.window().setTitle(file.getName());

	this.totalProgressBar.startAnimation(null);
//	this.fileProgressBar.startAnimation(null);

	this.window().makeKeyAndOrderFront(null);
	if(this.validate(resume))
	    this.start();
    }

    private void start() {
	switch(kind) {
	    case Queue.KIND_DOWNLOAD :
		file.fillDownloadQueue(queue, file.getSession().copy());
		if(file.isFile())
		    this.fileIconView.setImage(NSWorkspace.sharedWorkspace().iconForFileType(file.getExtension()));
		else
		    this.fileIconView.setImage(NSImage.imageNamed("folder.tiff"));
		break;
	    case Queue.KIND_UPLOAD:
		file.fillUploadQueue(queue, file.getSession().copy());
		if(file.getLocal().isFile())
		    this.fileIconView.setImage(NSWorkspace.sharedWorkspace().iconForFileType(file.getExtension()));
		else
		    this.fileIconView.setImage(NSImage.imageNamed("folder.tiff"));
		break;
	}
	if(queue.numberOfElements() > 1)
	    this.window().setTitle(file.getName()+" - "+queue.numberOfElements()+" files");
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
		    int currentQueue = queue.getCurrent();
		    
//		    this.fileProgressBar.setDoubleValue((double)status.getCurrent());
		    this.totalProgressBar.setDoubleValue((double)currentQueue);
//		    log.debug("File progress:"+status.getCurrent());
//		    log.debug("Total progress:"+queue.getCurrent());

//		    this.fileProgressBar.setMaxValue(status.getSize());
		    this.totalProgressBar.setMaxValue(queue.getSize());

		    this.fileDataField.setStringValue(msg.getDescription());
//		    this.fileDataField.sizeToFit();
		    this.totalDataField.setStringValue(currentQueue/queue.getSize()*100+"%");
//		    this.totalDataField.setStringValue(Status.parseDouble(currentQueue/1024)+" of "+Status.parseDouble(queue.getSize()/1024) + " kBytes.");
		    this.totalDataField.sizeToFit();
		}
		else if(msg.getTitle().equals(Message.CLOCK)) {
		    clockField.setStringValue(msg.getDescription());
		}
		else if(msg.getTitle().equals(Message.START)) {
		    this.totalProgressBar.setIndeterminate(false);
//		    this.fileProgressBar.setIndeterminate(false);

		    this.totalProgressBar.setMinValue(0);
//		    this.fileProgressBar.setMinValue(0);
		    
		    this.stopButton.setEnabled(true);
		    this.resumeButton.setEnabled(false);
		    this.reloadButton.setEnabled(false);
		}
		else if(msg.getTitle().equals(Message.STOP)) {
		    this.stopButton.setEnabled(false);
		    this.resumeButton.setEnabled(true);
		    this.reloadButton.setEnabled(true);
		}
		else if(msg.getTitle().equals(Message.COMPLETE)) {
//@todo		    if(queue.done()) {
			this.resumeButton.setEnabled(false);
			this.reloadButton.setEnabled(true);
			this.stopButton.setEnabled(false);
			this.progressField.setStringValue("Complete");
//		    }
		    if(Queue.KIND_DOWNLOAD == kind) {
			//@todo temp path name
			//path.getLocalTemp().renameTo(path.getLocal());
			if(Preferences.instance().getProperty("connection.download.postprocess").equals("true") && queue.numberOfElements() == 1) {
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
//	if(sender.title().equals("Resume"))
	this.start(true);
    }

    public void reloadButtonClicked(NSButton sender) {
	this.start(false);
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
					       null, // context
					       "Closing this window will stop the file transfer" // message
					       );
	    return false;	    
	}
	return true;
    }

    public void confirmSheetDidEnd(NSWindow sheet, int returncode, NSWindow main)  {
	sheet.orderOut(null);
	switch(returncode) {
	    case NSAlertPanel.DefaultReturn :
		this.stopButtonClicked(null);
		this.window().close();
		break;
	    case NSAlertPanel.AlternateReturn :
		break;
	}
    }
    
    private boolean validate(boolean resume) {
	//is upload
	if(Queue.KIND_UPLOAD == this.kind) {
	    this.file.status.setResume(false);
	    return true;
	}
	//is download
	if(resume) {
	    if(file.status.isComplete()) {
		NSAlertPanel.beginInformationalAlertSheet(
				       "Error", //title
				       "OK",// defaultbutton
				       "Cancel",//alternative button
				       null,//other button
				       this.window(), //docWindow
				       null, //modalDelegate
				       null, //didEndSelector
				       null, // dismiss selector
				       null, // context
				       "Download already complete." // message
				       );
		return false;
	    }
	    this.file.status.setResume(file.getLocal().exists());
//	    status.setCurrent(new Long(transfer.getLocalTempPath().length()).intValue());
	    return true;
	}
	else { //!resume
	    if(file.getLocal().exists()) {
		if(Preferences.instance().getProperty("connection.download.duplicate.ask").equals("true")) {
		    NSAlertPanel.beginCriticalAlertSheet(
					   "File exists", //title
					   "Resume",// defaultbutton
					   "Cancel",//alternative button
					   "Overwrite",//other button
					   this.window(),
					   this, //delegate
					   new NSSelector
					   (
	 "validateSheetDidEnd",
	 new Class[]
	 {
	     NSWindow.class, int.class, NSWindow.class
	 }
	 ),// end selector
					   null, // dismiss selector
					   null, // context
					   "The file "+file.getName()+" alredy exists in "+file.getLocal().getParent()+"." // message
					   );
		}
		return false;
	    }
	    return true;
	}
    }

    public void validateSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	sheet.orderOut(null);
	switch(returncode) {
	    case NSAlertPanel.DefaultReturn : //Resume
		this.file.status.setResume(true);
		this.start();
		break;
	    case NSAlertPanel.AlternateReturn : //Cancel
		this.totalProgressBar.stopAnimation(null);
//		this.fileProgressBar.stopAnimation(null);
		break;
	    case NSAlertPanel.OtherReturn : //Overwrite
		this.file.status.setResume(false);
		this.start();
		break;
	}
    }
}

