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

import ch.cyberduck.core.Message;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.Host;
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

    private NSTextField speedField;
    public void setSpeedField(NSTextField speedField) {
	this.speedField = speedField;
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

    private static NSMutableArray allDocuments = new NSMutableArray();

    private int kind;
    private Path root;
    private Host host;
    private Queue queue;
    
    /**
	* @param kind Tag specifiying if it is a download or upload.
     */
    public CDTransferController(Host host, Path root, int kind) {
	this.host = host;
	this.root = root;
	this.kind = kind;
	this.host.getLogin().setController(new CDLoginController(this.window(), host.getLogin()));
        if (false == NSApplication.loadNibNamed("Transfer", this)) {
            log.fatal("Couldn't load Transfer.nib");
            return;
        }
	this.init();
	allDocuments.addObject(this);
    }

    /**
	* Initiliaze the gui components according to the model
     */
    private void init() {
	log.debug("init");
	this.window().setTitle(root.getName());
	this.urlField.setStringValue(host.getURL()+root.getAbsolute());
	this.fileField.setStringValue(root.getLocal().toString());
	this.progressField.setStringValue("");
	this.fileDataField.setStringValue("");
	this.totalDataField.setStringValue("");
	this.speedField.setStringValue("");
	this.clockField.setStringValue("00:00");
	switch(kind) {
	    case Queue.KIND_DOWNLOAD:
		iconView.setImage(NSImage.imageNamed("download.tiff"));
		if(root.isFile())
		    fileIconView.setImage(NSWorkspace.sharedWorkspace().iconForFileType(root.getExtension()));
		else
		    fileIconView.setImage(NSImage.imageNamed("folder.tiff"));
		break;
	    case Queue.KIND_UPLOAD:
		iconView.setImage(NSImage.imageNamed("upload.tiff"));
		if(root.getLocal().isFile())
		    fileIconView.setImage(NSWorkspace.sharedWorkspace().iconForFileType(root.getExtension()));
		else
		    fileIconView.setImage(NSImage.imageNamed("folder.tiff"));
		break;
	}
//	this.totalProgressBar.startAnimation(null);
	this.window().makeKeyAndOrderFront(null);
    }

    public void update(Observable o, Object arg) {
//	log.debug("update:"+o+","+arg);
	if(arg instanceof Message) {
	    Message msg = (Message)arg;
	    // CURRENT
	    if(msg.getTitle().equals(Message.DATA)) {
		this.totalProgressBar.setIndeterminate(false);
		this.totalProgressBar.setMinValue(0);
		this.totalProgressBar.setDoubleValue((double)queue.getCurrent());
		this.totalProgressBar.setMaxValue(queue.getSize());
		
		this.fileDataField.setStringValue((String)msg.getContent());
		this.fileDataField.display();
//@todo percent		this.totalDataField.setAttributedStringValue(new NSAttributedString(queue.getCurrent()/queue.getSize()*100+"%"));
		    this.totalDataField.setStringValue(Status.parseDouble(queue.getCurrent()/1024)+" of "+Status.parseDouble(queue.getSize()/1024) + " kBytes.");
	    }
	    else if(msg.getTitle().equals(Message.SPEED)) {
		this.speedField.setStringValue((String)msg.getContent());
		//this.speedField.display();
	    }
	    else if(msg.getTitle().equals(Message.CLOCK)) {
		this.clockField.setStringValue((String)msg.getContent());
		//this.clockField.display();
	    }
	    // START
	    else if(msg.getTitle().equals(Message.START)) {
		this.totalProgressBar.setIndeterminate(true);
		this.totalProgressBar.startAnimation(null);

		//this.totalProgressBar.setIndeterminate(false);
//		this.totalProgressBar.setMinValue(0);
//		this.totalProgressBar.setDoubleValue((double)queue.getCurrent());
//		this.totalProgressBar.setMaxValue(queue.getSize());
		this.stopButton.setEnabled(true);
		this.resumeButton.setEnabled(false);
		this.reloadButton.setEnabled(false);
	    }
	    
	    // STOP
	    else if(msg.getTitle().equals(Message.STOP)) {
		this.stopButton.setEnabled(false);
		this.resumeButton.setEnabled(true);
		this.reloadButton.setEnabled(true);
		this.totalProgressBar.stopAnimation(null);
//		this.fileProgressBar.stopAnimation(null);
	    }
	    // COMPLETE
	    else if(msg.getTitle().equals(Message.COMPLETE)) {
		this.progressField.setStringValue("Complete");
		if(Queue.KIND_DOWNLOAD == kind) {
		    if(1 == queue.numberOfJobs()) {
			if(Preferences.instance().getProperty("connection.download.postprocess").equals("true")) {
			    NSWorkspace.sharedWorkspace().openFile(root.getLocal().toString());
			}
			if(Preferences.instance().getProperty("transfer.close").equals("true")) {
			    this.window.close();
			}
		    }
		}
	    }
	    else if(msg.getTitle().equals(Message.ERROR)) {
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
				       (String)msg.getContent() // message
				       );
		this.queue.cancel();
		this.progressField.setStringValue((String)msg.getContent());
	    }
	    else if(msg.getTitle().equals(Message.PROGRESS)) {
		this.progressField.setStringValue((String)msg.getContent());
		this.progressField.display();
	    }
	}
    }

    public NSWindow window() {
	return this.window;
    }

    public void windowWillClose(NSNotification notification) {
	this.window().setDelegate(null);
	allDocuments.removeObject(this);
    }
    
    public void resumeButtonClicked(NSButton sender) {
	this.stopButton.setEnabled(true);
	this.resumeButton.setEnabled(false);
	this.reloadButton.setEnabled(false);
	this.transfer(true);
    }

    public void reloadButtonClicked(NSButton sender) {
	this.stopButton.setEnabled(true);
	this.resumeButton.setEnabled(false);
	this.reloadButton.setEnabled(false);
	this.transfer(false);
    }
    
    public void stopButtonClicked(NSButton sender) {
	this.stopButton.setEnabled(false);
	this.resumeButton.setEnabled(true);
	this.reloadButton.setEnabled(true);
	this.queue.cancel();
    }

    public void showInFinderClicked(NSButton sender) {
	NSWorkspace.sharedWorkspace().selectFile(root.getLocal().toString(), "");
    }

    public boolean windowShouldClose(Object sender) {
	if(!queue.isStopped()) {
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

    /**
	* @param resume
     * @return boolean Return false if validation fails. I.e. the file already exists.
     */
    public void transfer(boolean resume) {
//	this.totalProgressBar.setIndeterminate(true);
//	this.totalProgressBar.startAnimation(null);

	if(null == this.queue) {
//	    this.queue = root.getQueue(this.kind);
	    this.queue = new Queue(root, this.kind);
	    this.queue.addObserver(this);
	}

//is upload
//	if(Queue.KIND_UPLOAD == this.kind) {
//	    this.file.status.setResume(resume);
//	    return true;
//	}
	//is download
	if(resume) {
	    if(root.status.isComplete()) {
		NSAlertPanel.beginInformationalAlertSheet(
				       "Error", //title
				       "OK",// defaultbutton
				       null,//alternative button
				       null,//other button
				       this.window(), //docWindow
				       null, //modalDelegate
				       null, //didEndSelector
				       null, // dismiss selector
				       null, // context
				       "Cannot resume an already completed transfer." // message
				       );
		this.stopButton.setEnabled(false);
		this.resumeButton.setEnabled(true);
		this.reloadButton.setEnabled(true);
//		return false;
		return;
	    }
	    if(! root.status.isComplete()) {
		resume = root.getLocal().exists();
//		this.file.status.setResume(file.getLocal().exists());
//	    return true;
	    }
	}
	if(! resume) {
	    if(root.getLocal().exists()) {
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
					   "The file "+root.getName()+" alredy exists in "+root.getLocal().getParent()+"." // message
					   );
		    return;
		}
		else if(Preferences.instance().getProperty("connection.download.duplicate.resume").equals("true")) {
		    resume = true;
		}
		else if(Preferences.instance().getProperty("connection.download.duplicate.overwrite").equals("true")) {
		    resume = false;
		}
	    }
	}
	this.queue.start(resume);
    }




//Preferences.instance().getProperty("connection.download.duplicate.ask").equals("true")
    public void validateSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	sheet.orderOut(null);
	switch(returncode) {
	    case NSAlertPanel.DefaultReturn : //Resume
//		this.file.status.setResume(true);
//		this.transfer();
		this.queue.start(true);
		break;
	    case NSAlertPanel.AlternateReturn : //Cancel
		this.stopButtonClicked(null);
		break;
	    case NSAlertPanel.OtherReturn : //Overwrite
//		this.file.status.setResume(false);
//		this.transfer();
		this.queue.start(false);
		break;
	}
    }
}

