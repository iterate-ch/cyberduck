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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Validator;

import java.util.Observer;
import java.util.Observable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDQueueController implements Observer, Validator {
    private static Logger log = Logger.getLogger(CDQueueController.class);

	private static CDQueueController instance;

	private NSToolbar toolbar;

    private NSWindow window; // IBOutlet
    public void setWindow(NSWindow window) {
		this.window = window;
    }
	
	public NSWindow window() {
		return this.window;
    }
	
	private CDQueueTableDataSource queueModel;
    private NSTableView queueTable; // IBOutlet
    public void setQueueTable(NSTableView queueTable) {
		this.queueTable = queueTable;
		this.queueTable.setTarget(this);
		this.queueTable.setDataSource(this.queueModel = new CDQueueTableDataSource());
		this.queueTable.setDelegate(this.queueModel);
		this.queueTable.tableColumnWithIdentifier("REMOVE").setDataCell(new NSActionCell());
//		this.queueTable.tableColumnWithIdentifier("ICON").setDataCell(new CDImageCell());
		this.queueTable.tableColumnWithIdentifier("DATA").setDataCell(new CDQueueCell());
		this.queueTable.tableColumnWithIdentifier("PROGRESS").setDataCell(new CDProgressCell());
		this.queueTable.setDoubleAction(new NSSelector("revealButtonClicked", new Class[] {Object.class}));
    }
	
	public void addTransfer(Path root, int kind) {
		List l = new ArrayList(); 
		l.add(root);
		this.addTransfer(l, kind);
	}

	/**
		* @param kind Tag specifiying if it is a download or upload.
     */
	public void addTransfer(List roots, int kind) {
		this.window().makeKeyAndOrderFront(null);
		Queue queue = new Queue(roots, kind, this);
		this.queueModel.addEntry(queue);
		this.queueTable.reloadData();
		queue.addObserver(this);
		queue.start();
		//todo		this.host.getLogin().setController(new CDLoginController(this.window(), host.getLogin()));
    }
	
	public void update(Observable o, Object arg) {
		this.queueTable.reloadData();
		if(arg instanceof Message) {
			Message msg = (Message)arg;
			if(msg.getTitle().equals(Message.ERROR)) {
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
			}
		}
	}
	
	public static CDQueueController instance() {
		if(null == instance) {
			instance = new CDQueueController();
		}
		return instance;
    }
	
	private CDQueueController() {
        if (false == NSApplication.loadNibNamed("Queue", this)) {
            log.fatal("Couldn't load Queue.nib");
        }
	}
	
	public void awakeFromNib() {
		this.window().setTitle("Transfer Queue");
		this.toolbar = new NSToolbar("Queue Toolbar");
		this.toolbar.setDelegate(this);
		this.toolbar.setAllowsUserCustomization(true);
		this.toolbar.setAutosavesConfiguration(true);
		this.window().setToolbar(toolbar);
	}		
	
	// ----------------------------------------------------------
 	// Toolbar Delegate
 	// ----------------------------------------------------------
    
	public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {		
		NSToolbarItem item = new NSToolbarItem(itemIdentifier);
		if (itemIdentifier.equals(NSBundle.localizedString("Stop"))) {
			item.setLabel(NSBundle.localizedString("Stop"));
			item.setPaletteLabel(NSBundle.localizedString("Stop"));
			item.setImage(NSImage.imageNamed("stop.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("stopButtonClicked", new Class[] {Object.class}));
		}
		if (itemIdentifier.equals(NSBundle.localizedString("Resume"))) {
			item.setLabel(NSBundle.localizedString("Resume"));
			item.setPaletteLabel(NSBundle.localizedString("Resume"));
			item.setImage(NSImage.imageNamed("resume.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("resumeButtonClicked", new Class[] {Object.class}));
		}
		if (itemIdentifier.equals(NSBundle.localizedString("Reload"))) {
			item.setLabel(NSBundle.localizedString("Reload"));
			item.setPaletteLabel(NSBundle.localizedString("Reload"));
			item.setImage(NSImage.imageNamed("reload.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("reloadButtonClicked", new Class[] {Object.class}));
		}
		if (itemIdentifier.equals(NSBundle.localizedString("Reveal in Finder"))) {
			item.setLabel(NSBundle.localizedString("Reveal in Finder"));
			item.setPaletteLabel(NSBundle.localizedString("Reveal in Finder"));
			item.setImage(NSImage.imageNamed("reveal.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("revealButtonClicked", new Class[] {Object.class}));
		}
		return item;
	}
	
	public void stopButtonClicked(Object sender) {
		Queue item = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
		item.cancel();
	}
	
	public void resumeButtonClicked(Object sender) {
		Queue item = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
		Iterator i = item.getRoots().iterator();
		while(i.hasNext()) {
			Path p = (Path)i.next();
			p.status.setResume(true);
		}
		item.start();
	}
	
	public void reloadButtonClicked(Object sender) {
		Queue item = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
		Iterator i = item.getRoots().iterator();
		while(i.hasNext()) {
			Path p = (Path)i.next();
			p.status.setResume(false);
		}
		item.start();
	}
	
	public void revealButtonClicked(Object sender) {
		Queue item = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
		if(item.isInitialized())
			NSWorkspace.sharedWorkspace().selectFile(item.getCurrentJob().getLocal().toString(), "");
    }	
	
	public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
		return new NSArray(new Object[] {
			NSBundle.localizedString("Stop"),
			NSBundle.localizedString("Resume"),
			NSBundle.localizedString("Reload"),
			NSBundle.localizedString("Reveal in Finder")
		});
	}

	public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
		return new NSArray(new Object[] {
			NSBundle.localizedString("Stop"),
			NSBundle.localizedString("Resume"),
			NSBundle.localizedString("Reload"),
			NSBundle.localizedString("Reveal in Finder")
		});
	}
	
    public boolean validateToolbarItem(NSToolbarItem item) {
//		log.debug("validateToolbarItem:"+item.label());
		String label = item.label();
		if(label.equals(NSBundle.localizedString("Stop"))) {
			if(this.queueTable.selectedRow() != -1) {
				Queue queue = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
				return !queue.isCanceled();
			}
			return false;
		}
		if(label.equals(NSBundle.localizedString("Resume"))) {
			if(this.queueTable.selectedRow() != -1) {
				Queue queue = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
				return !queue.isRunning() && !(queue.remainingJobs() == 0);
			}
			return false;
		}
		if(label.equals(NSBundle.localizedString("Reload"))) {
			if(this.queueTable.selectedRow() != -1) {
				Queue queue = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
				return !queue.isRunning();
			}
			return false;
		}
		if(label.equals(NSBundle.localizedString("Reveal in Finder"))) {
			return this.queueTable.selectedRow() != -1;
		}
		return true;
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
					if(Preferences.instance().getProperty("download.duplicate").equals("ask")) {
						log.debug("download.duplicate:ask");
						NSAlertPanel.beginCriticalAlertSheet(
										   NSBundle.localizedString("File exists"), //title
										   NSBundle.localizedString("Resume"),// defaultbutton
										   NSBundle.localizedString("Cancel"),//alternative button
										   NSBundle.localizedString("Overwrite"),//other button
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
										   path, // context
										   NSBundle.localizedString("The file")+" "+path.getName()+" "+NSBundle.localizedString("alredy exists in")+" "+path.getLocal().getParent()+"." // message
										   );
						while(!done) {
							try {
								log.debug("Sleeping...");
								Thread.sleep(500); //milliseconds
							}
							catch(InterruptedException e) {
								log.error(e.getMessage());
							}
						}
						log.debug("return:"+proceed);
						return proceed;
					}
					else if(Preferences.instance().getProperty("download.duplicate").equals("similar")) {
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
							path.setLocal(new Local(parent, proposal));
							no++;
						}
						//todo						this.init();
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
		Path item = (Path)contextInfo;
		switch(returncode) {
			case NSAlertPanel.DefaultReturn : //Resume
				item.status.setResume(true);
				proceed = true;
				break;
			case NSAlertPanel.AlternateReturn : //Cancel
//				item.cancel();
				proceed = false;
				break;
			case NSAlertPanel.OtherReturn : //Overwrite
				item.status.setResume(false);
				proceed = true;
				break;
		}
		this.done = true;
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