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

//    private NSTextField totalDataField;
//    public void setTotalDataField(NSTextField totalDataField) {
//	this.totalDataField = totalDataField;
//    }

//    private NSTextField speedField;
//    public void setSpeedField(NSTextField speedField) {
//	this.speedField = speedField;
//    }
    
    private NSProgressIndicator totalProgressBar;
    public void setTotalProgressBar(NSProgressIndicator totalProgressBar) {
	this.totalProgressBar = totalProgressBar;
	this.totalProgressBar.setIndeterminate(true);
	this.totalProgressBar.setUsesThreadedAnimation(true);
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
    public CDTransferController(Path root, int kind) {
	allDocuments.addObject(this);
	this.root = root;
	this.host = root.getHost();
	this.kind = kind;
        if (false == NSApplication.loadNibNamed("Transfer", this)) {
            log.fatal("Couldn't load Transfer.nib");
            return;
        }
	this.host.getLogin().setController(new CDLoginController(this.window(), host.getLogin()));
//	this.init();
    }

    /**
	* Init the gui components according to the model
     */
    public void awakeFromNib() {
	log.debug("awakeFromNib");
	NSPoint origin = this.window().frame().origin();
	this.window().setFrameOrigin(new NSPoint(origin.x() + 16, origin.y() - 16));
	this.window().setTitle(root.getName());
	this.urlField.setAttributedStringValue(new NSAttributedString(host.getURL()+root.getAbsolute()));
	this.fileField.setAttributedStringValue(new NSAttributedString(root.getLocal().toString()));
	this.progressField.setAttributedStringValue(new NSAttributedString(""));
	this.fileDataField.setAttributedStringValue(new NSAttributedString(""));
//	this.totalDataField.setAttributedStringValue(new NSAttributedString(""));
//	this.speedField.setAttributedStringValue(new NSAttributedString(""));
	this.clockField.setAttributedStringValue(new NSAttributedString("00:00"));
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
		Status status = (Status)msg.getContent();
		this.totalProgressBar.setIndeterminate(false);
		this.totalProgressBar.setMinValue(0);
		this.totalProgressBar.setDoubleValue((double)queue.getCurrent());
		this.totalProgressBar.setMaxValue(queue.getSize());

		this.fileDataField.setAttributedStringValue(new NSAttributedString(
				  Status.parseDouble(status.getCurrent()/1024)+
				  " of "+Status.parseDouble(status.getSize()/1024)+"kB ("+
				  Status.parseDouble(queue.getCurrent()/1024)+" of "+Status.parseDouble(queue.getSize()/1024)+"kB Total), "+
					      Status.parseDouble(queue.getSpeed()/1024) + "kB/s, "+queue.getTimeLeft()
		    ));
		this.fileDataField.display();
//		this.fileDataField.setAttributedStringValue(new NSAttributedString((String)msg.getContent()));
//		this.fileDataField.setAttributedStringValue(new NSAttributedString((String)msg.getContent()));
//		this.fileDataField.display();
//@todo percent		this.totalDataField.setAttributedStringValue(new NSAttributedString(queue.getCurrent()/queue.getSize()*100+"%"));
//		    this.totalDataField.setAttributedStringValue(new NSAttributedString(Status.parseDouble(queue.getCurrent()/1024)+" of "+Status.parseDouble(queue.getSize()/1024) + " kBytes."));
//		    this.totalDataField.display();
	    }
//	    else if(msg.getTitle().equals(Message.SPEED)) {
//		this.fileDataField.setAttributedStringValue(new NSAttributedString((String)msg.getContent()));
//		this.speedField.setAttributedStringValue(new NSAttributedString((String)msg.getContent()));
//		this.speedField.display();
//	    }
	    else if(msg.getTitle().equals(Message.CLOCK)) {
		this.clockField.setAttributedStringValue(new NSAttributedString((String)msg.getContent()));
		this.clockField.display();
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
		this.progressField.setAttributedStringValue(new NSAttributedString("Interrupted"));
//		this.speedField.setAttributedStringValue(new NSAttributedString(""));
		this.stopButton.setEnabled(false);
		this.resumeButton.setEnabled(true);
		this.reloadButton.setEnabled(true);
		this.totalProgressBar.stopAnimation(null);
	    }
	    // COMPLETE
	    else if(msg.getTitle().equals(Message.COMPLETE)) {
		this.progressField.setAttributedStringValue(new NSAttributedString("Complete"));
		this.progressField.display();
		if(1 == queue.numberOfJobs()) {
		    if(Preferences.instance().getProperty("transfer.close").equals("true")) {
			this.window.close();
		    }
		    if(Queue.KIND_DOWNLOAD == kind) {
			if(Preferences.instance().getProperty("connection.download.postprocess").equals("true")) {
			    NSWorkspace.sharedWorkspace().openFile(root.getLocal().toString());
			}
		    }
		    if(Queue.KIND_UPLOAD == kind) {
			//todo refresh listing
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
		this.progressField.setAttributedStringValue(new NSAttributedString((String)msg.getContent()));
		this.progressField.display();
	    }
	    else if(msg.getTitle().equals(Message.PROGRESS)) {
		this.progressField.setAttributedStringValue(new NSAttributedString((String)msg.getContent()));
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
		 NSWindow.class, int.class, Object.class
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

    public void confirmSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo)  {
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
	this.queue = new Queue(root, this.kind);
	this.queue.addObserver(this);

	//todo show alert when resumption not possible because of upload or sftp protocol

	if(Queue.KIND_DOWNLOAD == kind) {
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
		else if(! root.status.isComplete()) {
		    resume = root.getLocal().exists();
//		this.file.status.setResume(file.getLocal().exists());
//	    return true;
		}
	    }
	    if(!resume) {
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
	  NSWindow.class, int.class, Object.class
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
	}
	if(Queue.KIND_UPLOAD == kind) {
	    resume = false;
	}
	this.queue.start(resume);
    }
    
//Preferences.instance().getProperty("connection.download.duplicate.ask").equals("true")
    public void validateSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
	sheet.orderOut(null);
	switch(returncode) {
	    case NSAlertPanel.DefaultReturn : //Resume
		this.queue.start(true);
		break;
	    case NSAlertPanel.AlternateReturn : //Cancel
		this.stopButtonClicked(null);
		break;
	    case NSAlertPanel.OtherReturn : //Overwrite
		this.queue.start(false);
		break;
	}
    }
}

