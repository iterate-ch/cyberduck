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

import java.util.Observable;
import java.util.Observer;

import ch.cyberduck.core.*;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSSelector;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDTransferController implements Observer, Validator {
    private static Logger log = Logger.getLogger(CDTransferController.class);
	
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------
	
    private NSWindow window;
    public void setWindow(NSWindow window) {
		this.window = window;
    }
	
    public NSWindow window() {
		return this.window;
    }
    
    private NSTextField urlField;
    public void setUrlField(NSTextField urlField) {
		this.urlField = urlField;
    }
	
    private NSTextField clockField;
    public void setClockField(NSTextField clockField) {
		this.clockField = clockField;
		this.clockField.setObjectValue("00:00");
    }
    
    private NSTextField fileField;
    public void setFileField(NSTextField fileField) {
		this.fileField = fileField;
    }
	
    private NSTextField progressField;
    public void setProgressField(NSTextField progressField) {
		this.progressField = progressField;
		this.progressField.setObjectValue("");
    }
    
    private NSTextField fileDataField;
    public void setFileDataField(NSTextField fileDataField) {
		this.fileDataField = fileDataField;
		this.fileDataField.setObjectValue("");
    }
	
    private NSProgressIndicator totalProgressBar;
    public void setTotalProgressBar(NSProgressIndicator totalProgressBar) {
		this.totalProgressBar = totalProgressBar;
		this.totalProgressBar.setIndeterminate(true);
		this.totalProgressBar.setUsesThreadedAnimation(true);
    }
	
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
    private Path[] roots;
    private Path root;
    private Host host;
    private Queue queue;
	
    public CDTransferController(Path[] roots, int kind) {
		allDocuments.addObject(this);
		this.kind = kind;
		this.roots = roots;
		this.queue = new Queue(roots, this.kind, this);
		this.queue.addObserver(this);
		this.root = roots[0];
		this.host = root.getHost();
        if (false == NSApplication.loadNibNamed("Transfer", this)) {
            log.fatal("Couldn't load Transfer.nib");
            return;
        }
		this.host.getLogin().setController(new CDLoginController(this.window(), host.getLogin()));
    }	
    
    /**
		* @param kind Tag specifiying if it is a download or upload.
     */
    public CDTransferController(Path root, int kind) {
		this(new Path[]{root}, kind);
    }
	
    /**
		* Init the gui components according to the model
     */
    public void awakeFromNib() {
		log.debug("awakeFromNib");
		NSPoint origin = this.window.frame().origin();
		this.window.setFrameOrigin(new NSPoint(origin.x() + 16, origin.y() - 16));
		this.init();
		this.window().makeKeyAndOrderFront(null);
    }
	
    private void init() {
		this.window().setTitle(root.getName());
		this.window().display();
		this.urlField.setObjectValue(host.getURL()+root.getAbsolute());
		this.fileField.setObjectValue(root.getLocal().toString());
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
    }
	
    public void update(Observable o, Object arg) {
		//	log.debug("update:"+o+","+arg);
		if(arg instanceof Path) {
			log.debug("New root element:"+arg);
			this.root = (Path)arg;
			this.init();
		}
		else if(arg instanceof Message) {
			Message msg = (Message)arg;
			// CURRENT
			if(msg.getTitle().equals(Message.DATA)) {
				Status status = (Status)msg.getContent();
				if(queue.getCurrent() > 0 && queue.getSize() > 0) {
					this.totalProgressBar.setIndeterminate(false);
					this.totalProgressBar.setMinValue(0);
					this.totalProgressBar.setDoubleValue((double)queue.getCurrent());
					this.totalProgressBar.setMaxValue(queue.getSize());
					
				}
				else {
					this.totalProgressBar.setIndeterminate(true);
					this.totalProgressBar.startAnimation(null);
				}
				this.fileDataField.setObjectValue(
									  (status.getCurrent()/1024)+
									  " "+NSBundle.localizedString("of")+
									  " "+(status.getSize()/1024)+"kB ("+
									  (queue.getCurrent()/1024)+" of "+
									  (queue.getSize()/1024)+"kB "+NSBundle.localizedString("Total")+"), "+
									  Status.parseLong(queue.getSpeed()/1024) + "kB/s, "+queue.getTimeLeft()
									  );
				this.fileDataField.setNeedsDisplay(true);
			}
			// CLOCK
			else if(msg.getTitle().equals(Message.CLOCK)) {
				this.clockField.setObjectValue(msg.getContent());
				this.clockField.display();
			}
			// PROGRESS
			else if(msg.getTitle().equals(Message.PROGRESS)) {
				this.progressField.setObjectValue(msg.getContent());
				this.progressField.display();
			}
			// START
			else if(msg.getTitle().equals(Message.START)) {
				log.debug("START");
				this.totalProgressBar.setIndeterminate(true);
				this.totalProgressBar.startAnimation(null);
				
				this.stopButton.setEnabled(true);
				this.resumeButton.setEnabled(false);
				this.reloadButton.setEnabled(false);
				
				//		if(Queue.KIND_DOWNLOAD == kind) {
	//		    String creatorCodeString = (String)NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleSignature");
 //		    Integer creatorCode = new Integer(
 //					NSHFSFileTypes.hfsTypeCodeFromFileType("'"+creatorCodeString+"'"));
 //		    
 //		    NSPathUtilities.setFileAttributes(
 //					root.getLocal().toString(),
 //					new NSDictionary(creatorCode, NSPathUtilities.FileHFSCreatorCode));
 //		}
			}	    
			// STOP
			else if(msg.getTitle().equals(Message.STOP)) {
				log.debug("STOP");
				this.totalProgressBar.stopAnimation(null);
				this.stopButton.setEnabled(false);
				this.resumeButton.setEnabled(true);
				this.reloadButton.setEnabled(true);
			}
			// COMPLETE
			else if(msg.getTitle().equals(Message.COMPLETE)) {
				log.debug("COMPLETE");
				this.progressField.setObjectValue(NSBundle.localizedString("Complete"));
				this.progressField.setNeedsDisplay(true);
				if(0 == queue.remainingJobs()) {
					if(Preferences.instance().getProperty("transfer.close").equals("true")) {
						this.window.close();
					}
					if(Queue.KIND_DOWNLOAD == kind) {
						if(Preferences.instance().getProperty("connection.download.postprocess").equals("true")) {
							NSWorkspace.sharedWorkspace().openFile(root.getLocal().toString());
						}
					}
					//		    if(Queue.KIND_UPLOAD == kind) {
	 //			this.root.getParent().list();
  //		    }
				}
			}
			else if(msg.getTitle().equals(Message.ERROR)) {
				NSAlertPanel.beginCriticalAlertSheet(
										 NSBundle.localizedString("Error"), //title
										 NSBundle.localizedString("OK"),// defaultbutton
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
				this.totalProgressBar.stopAnimation(null);
				this.stopButton.setEnabled(false);
				this.resumeButton.setEnabled(true);
				this.reloadButton.setEnabled(true);
				this.progressField.setObjectValue(msg.getContent());
				this.progressField.setNeedsDisplay(true);
			}
		}
    }
	
    private boolean proceed;
    private boolean done;
	
    /**
		* @return true if validation suceeded, false if !proceed
     */
    public boolean validate(Path path, int kind) {
		boolean resume = path.status.isResume();
		this.done = false;
		this.proceed = false;
		log.debug("validate:"+path+","+resume);
		if(Queue.KIND_DOWNLOAD == kind) {
			log.debug("validating download");
			if(resume) {
				log.debug("resume:true");
				if(path.status.isComplete()) {
					log.debug("complete:true");
					//		    NSAlertPanel.beginInformationalAlertSheet(
	 //						"Error", //title
  //						"OK",// defaultbutton
  //						null,//alternative button
  //						null,//other button
  //						this.window(), //docWindow
  //						null, //modalDelegate
  //						null, //didEndSelector
  //						null, // dismiss selector
  //						null, // context
  //						"Cannot resume an already completed transfer." // message
  //						);
  //		    this.stopButton.setEnabled(false);
  //		    this.resumeButton.setEnabled(true);
  //		    this.reloadButton.setEnabled(true);
  //		    return false;
					log.debug("return:true");
					return true;
				}
				else if(! path.status.isComplete()) {
					log.debug("complete:false");
					path.status.setResume(path.getLocal().exists());
					log.debug("return:true");
					return true;
				}
			}
			if(!resume) {
				log.debug("resume:false");
				if(path.getLocal().exists()) {
					log.debug("local path exists:true");
					//		    if(Preferences.instance().getProperty("download.duplicate").equals("ask")) {
	 //			log.debug("download.duplicate:ask");
  //			NSAlertPanel.beginCriticalAlertSheet(
  //					NSBundle.localizedString("File exists"), //title
  //					NSBundle.localizedString("Resume"),// defaultbutton
  //					NSBundle.localizedString("Cancel"),//alternative button
  //					NSBundle.localizedString("Overwrite"),//other button
  //					this.window(),
  //					this, //delegate
  //					new NSSelector
  //					(
  //    "validateSheetDidEnd",
  //      new Class[]
  //      {
  //	  NSWindow.class, int.class, Object.class
  //      }
  //      ),// end selector
  //					null, // dismiss selector
  //					path, // context
  //					NSBundle.localizedString("The file")+" "+path.getName()+" "+NSBundle.localizedString("alredy exists in")+" "+path.getLocal().getParent()+"." // message
  //					);
  //			while(!done) {
  //			    try {
  //				log.debug("Sleeping...");
  //				Thread.sleep(500); //milliseconds
  //			    }
  //			    catch(InterruptedException e) {
  //				log.error(e.getMessage());
  //			    }
  //			}
  //			log.debug("return:"+proceed);
  //			return proceed;
  //		    }
					if(Preferences.instance().getProperty("download.duplicate").equals("similar")) {
						log.debug("download.duplicate:similar");
						path.status.setResume(false);
						String proposal = null;
						String parent = path.getLocal().getParent();
						String filename = path.getLocal().getName();
						int no = 1;
						int index = filename.lastIndexOf(".");
						while(path.getLocal().exists()) {
							if(index != -1)
								proposal = filename.substring(0, index)+"-"+no+filename.substring(index);
							else
								proposal = filename+"-"+no;
							path.setLocal(new java.io.File(parent, proposal));
							no++;
						}
						this.init();
						log.debug("return:true");
						return true;
					}
					else if(Preferences.instance().getProperty("download.duplicate").equals("resume")) {
						log.debug("download.duplicate:resume");
						path.status.setResume(true);
						log.debug("return:true");
						return true;
					}
					else if(Preferences.instance().getProperty("download.duplicate").equals("overwrite")) {
						log.debug("download.duplicate:overwrite");
						path.status.setResume(false);
						log.debug("return:true");
						return true;
					}
				}
				log.debug("local path exists:false");
				log.debug("return:true");
				return true;
			}
		}
		else if(Queue.KIND_UPLOAD == kind) {
			log.debug("Validating upload");
			path.status.setResume(false);
			log.debug("return:true");
			return true;
		}
		return false;
    }
	
    public void validateSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
		log.debug("validateSheetDidEnd:"+returncode+","+contextInfo);
		sheet.close();
		Path path = (Path)contextInfo;
		switch(returncode) {
			case NSAlertPanel.DefaultReturn : //Resume
				path.status.setResume(true);
				proceed = true;
				break;
			case NSAlertPanel.AlternateReturn : //Cancel
				this.stopButtonClicked(null);
				proceed = false;
				break;
			case NSAlertPanel.OtherReturn : //Overwrite
				path.status.setResume(false);
				proceed = true;
				break;
		}
		this.done = true;
    }
	
    /**
		* @param resume
     * @return boolean Return false if validation fails. I.e. the file already exists.
     */
    public void transfer() {
		this.queue.start();
    }
    
    public void windowWillClose(NSNotification notification) {
		this.window().setDelegate(null);
		allDocuments.removeObject(this);
    }
    
    public void resumeButtonClicked(NSButton sender) {
		this.stopButton.setEnabled(true);
		this.resumeButton.setEnabled(false);
		this.reloadButton.setEnabled(false);
		for(int i = 0; i < roots.length; i++) {
			roots[i].status.setResume(true);
		}
		this.transfer();
    }
	
    public void reloadButtonClicked(NSButton sender) {
		this.stopButton.setEnabled(true);
		this.resumeButton.setEnabled(false);
		this.reloadButton.setEnabled(false);
		for(int i = 0; i < roots.length; i++) {
			roots[i].status.setResume(false);
		}
		this.transfer();
    }
	
    public void stopButtonClicked(NSButton sender) {
		this.totalProgressBar.stopAnimation(null);
		this.stopButton.setEnabled(false);
		this.resumeButton.setEnabled(true);
		this.reloadButton.setEnabled(true);
		this.queue.cancel();
    }
	
    public void showInFinderClicked(NSButton sender) {
		NSWorkspace.sharedWorkspace().selectFile(root.getLocal().toString(), "");
    }
	
    public boolean windowShouldClose(Object sender) {
		log.debug("windowShouldClose");
		if(!queue.isStopped()) {
			NSAlertPanel.beginCriticalAlertSheet(
										NSBundle.localizedString("Cancel transfer?"), //title
										NSBundle.localizedString("Stop"),// defaultbutton
										NSBundle.localizedString("Cancel"),//alternative button
										null,//other button
										this.window(),
										this, //delegate
										new NSSelector
										(
		   "closeSheetDidEnd",
		   new Class[]
		   {
			   NSWindow.class, int.class, Object.class
		   }
		   ),// end selector
										null, // dismiss selector
										null, // context
										NSBundle.localizedString("Closing this window will stop the file transfer") // message
										);
			return false;	    
		}
		queue.deleteObserver(this);
		return true;
    }
	
    public void closeSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo)  {
		log.debug("closeSheetDidEnd");
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
}

