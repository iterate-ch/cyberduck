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

import ch.cyberduck.core.Codec;
import ch.cyberduck.core.Session;
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
import java.io.File;

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
		this.queueTable.setRowHeight(50f);

		NSTableColumn dataColumn = new NSTableColumn();
		dataColumn.setIdentifier("DATA");
		dataColumn.setMinWidth(200f);
		dataColumn.setWidth(350f);
		dataColumn.setMaxWidth(1000f);
		dataColumn.setResizable(true);
		dataColumn.setDataCell(new CDQueueCell());
		this.queueTable.addTableColumn(dataColumn);

		NSTableColumn progressColumn = new NSTableColumn();
		progressColumn.setIdentifier("DATA");
		progressColumn.setMinWidth(50f);
		progressColumn.setWidth(250f);
		progressColumn.setMaxWidth(1000f);
		progressColumn.setResizable(true);
		progressColumn.setDataCell(new CDProgressCell());
		this.queueTable.addTableColumn(progressColumn);
		
//		this.queueTable.tableColumnWithIdentifier("DATA").setDataCell(new CDQueueCell());
//		this.queueTable.tableColumnWithIdentifier("PROGRESS").setDataCell(new CDProgressCell());
		this.queueTable.setDoubleAction(new NSSelector("revealButtonClicked", new Class[] {Object.class}));
		this.queueTable.sizeToFit();
    }
	
//	public void addTransfer(Path root, int kind) {
//		List l = new ArrayList(); 
//		l.add(root);
//		this.addTransfer(l, kind);
//	}

	/**
		* @param kind Tag specifiying if it is a download or upload.
     */
//	public void addTransfer(List roots, int kind) {
//		this.window().makeKeyAndOrderFront(null);
//		Queue queue = new Queue(roots, kind, this);
////		CDQueueElementController elementController = new CDQueueElementController(queue);
////		this.queueModel.addEntry(elementController);
////		queue.addObserver(elementController);
//		this.queueModel.addEntry(queue);
//		this.queueTable.reloadData();
//		queue.addObserver(this);
//		queue.start(this);
//		//		this.host.getLogin().setController(new CDLoginController(this.window(), host.getLogin()));
//    }
	
	public void addItem(Queue queue) {
		this.window().makeKeyAndOrderFront(null);
		this.queueModel.addEntry(queue);
		this.queueTable.reloadData();
	}
	
	public void addItemAndStart(Queue queue) {
		this.addItem(queue);
		this.startItem(queue);
	}
	
	public void startItem(Queue queue) {
		queue.addObserver(this);
		queue.getRoot().getHost().getLogin().setController(new CDLoginController(this.window(), queue.getRoot().getHost().getLogin()));
		if(queue.getRoot().getHost().getProtocol().equals(Session.SFTP)) {
			try {
				queue.getRoot().getHost().setHostKeyVerificationController(new CDHostKeyController(this.window()));
			}
			catch(com.sshtools.j2ssh.transport.InvalidHostFileException e) {
				//This exception is thrown whenever an exception occurs open or reading from the host file.
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
													 NSBundle.localizedString("Could not open or read the host file")+": "+e.getMessage() // message
													 );
			}
		}
		queue.start(this);
	}
	
	public void update(Observable observable, Object arg) {
		if(arg instanceof Message) {
			Message msg = (Message)arg;
//			if(msg.getTitle().equals(Message.CLOCK)
//			   || msg.getTitle().equals(Message.DATA)
//			   || msg.getTitle().equals(Message.PROGRESS)) {
			this.queueTable.reloadData();
//			}
			if(msg.getTitle().equals(Message.START)) {
				this.toolbar.validateVisibleItems();
			}
			else if(msg.getTitle().equals(Message.STOP)) {
				this.toolbar.validateVisibleItems();
				if(observable instanceof Queue) {
					Queue queue = (Queue)observable;
					if(queue.isEmpty()) {
						if(Preferences.instance().getProperty("queue.removeItemWhenComplete").equals("true")) {
							this.queueModel.removeEntry(queue);
							this.queueTable.reloadData();
						}
						if(Queue.KIND_DOWNLOAD == queue.kind()) {
							if(Preferences.instance().getProperty("connection.download.postprocess").equals("true")) {
								NSWorkspace.sharedWorkspace().openFile(queue.getRoot().getLocal().toString());
							}
						}
					}
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
		this.load(QUEUE_FILE);
	}		
	
	private static final File QUEUE_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/Queue.plist"));

    private void load(java.io.File f) {
		log.debug("load");
		if(f.exists()) {
			log.info("Found Queue file: "+f.toString());			
			NSData plistData = new NSData(f);
			String[] errorString = new String[]{null};
			Object propertyListFromXMLData = 
				NSPropertyListSerialization.propertyListFromData(plistData, 
																 NSPropertyListSerialization.PropertyListImmutable,
																 new int[]{NSPropertyListSerialization.PropertyListXMLFormat}, 
																 errorString);
			if(errorString[0]!=null)
				log.error("Problem reading queue file: "+errorString[0]);
			else
				log.info("Successfully read Queue: "+propertyListFromXMLData);
			if(propertyListFromXMLData instanceof NSArray) {
				NSArray entries = (NSArray)propertyListFromXMLData;
				java.util.Enumeration i = entries.objectEnumerator();
				Object element;
				while(i.hasMoreElements()) {
					element = i.nextElement();
					if(element instanceof NSDictionary) {
						this.addItem(new Queue((NSDictionary)element));
					}
				}
			}
		}
    }
	
	protected void finalize() throws Throwable {
		super.finalize();
		this.save(QUEUE_FILE);
	}
	
	public void save() {
		this.save(QUEUE_FILE);
	}
	
	private void save(java.io.File f) {
		log.debug("save");
		if(Preferences.instance().getProperty("queue.save").equals("true")) {
			try {
				NSMutableArray list = new NSMutableArray();
				for(int i = 0; i < queueModel.numberOfRowsInTableView(this.queueTable); i++) {
					list.addObject(queueModel.getEntry(i).getAsDictionary());
				}
				NSMutableData collection = new NSMutableData();
				String[] errorString = new String[]{null};
				collection.appendData(NSPropertyListSerialization.dataFromPropertyList(
																					   list,
																					   NSPropertyListSerialization.PropertyListXMLFormat, 
																					   errorString)
									  );
				//				collection.appendData(NSPropertyListSerialization.XMLDataFromPropertyList(list));
				if(errorString[0]!=null)
					log.error("Problem writing queue file: "+errorString[0]);
				
				if(collection.writeToURL(f.toURL(), true))
					log.info("Queue sucessfully saved to :"+f.toString());
				else
					log.error("Error saving Queue to :"+f.toString());
			}
			catch(java.net.MalformedURLException e) {
				log.error(e.getMessage());
			}
		}
    }
	
	// ----------------------------------------------------------
 	// Toolbar Delegate
 	// ----------------------------------------------------------
    
	public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {		
		NSToolbarItem item = new NSToolbarItem(itemIdentifier);
		if (itemIdentifier.equals("Stop")) {
			item.setLabel(NSBundle.localizedString("Stop"));
			item.setPaletteLabel(NSBundle.localizedString("Stop"));
			item.setImage(NSImage.imageNamed("stop.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("stopButtonClicked", new Class[] {Object.class}));
		}
		if (itemIdentifier.equals("Resume")) {
			item.setLabel(NSBundle.localizedString("Resume"));
			item.setPaletteLabel(NSBundle.localizedString("Resume"));
			item.setImage(NSImage.imageNamed("resume.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("resumeButtonClicked", new Class[] {Object.class}));
		}
		if (itemIdentifier.equals("Reload")) {
			item.setLabel(NSBundle.localizedString("Reload"));
			item.setPaletteLabel(NSBundle.localizedString("Reload"));
			item.setImage(NSImage.imageNamed("reload.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("reloadButtonClicked", new Class[] {Object.class}));
		}
		if (itemIdentifier.equals("Reveal")) {
			item.setLabel(NSBundle.localizedString("Reveal"));
			item.setPaletteLabel(NSBundle.localizedString("Reveal in Finder"));
			item.setImage(NSImage.imageNamed("reveal.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("revealButtonClicked", new Class[] {Object.class}));
		}
		if (itemIdentifier.equals("Remove")) {
			item.setLabel(NSBundle.localizedString("Remove"));
			item.setPaletteLabel(NSBundle.localizedString("Remove"));
			item.setImage(NSImage.imageNamed("clean.tiff"));
			item.setTarget(this);
			item.setAction(new NSSelector("removeButtonClicked", new Class[] {Object.class}));
		}
		return item;
	}
	
	public void stopButtonClicked(Object sender) {
		NSEnumerator enum = queueTable.selectedRowEnumerator();
		while(enum.hasMoreElements()) {
			Queue item = (Queue)this.queueModel.getEntry(((Integer)enum.nextElement()).intValue());
			item.cancel();
		}
	}
	
	public void resumeButtonClicked(Object sender) {
		//		Queue item = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
		NSEnumerator enum = queueTable.selectedRowEnumerator();
		while(enum.hasMoreElements()) {
			Queue item = (Queue)this.queueModel.getEntry(((Integer)enum.nextElement()).intValue());
//			Iterator i = item.getRoots().iterator();
//			while(i.hasNext()) {
//				Path p = (Path)i.next();
//				p.status.setResume(true);
//			}
			item.getRoot().status.setResume(true);
			this.startItem(item);
		}
	}
	
	public void reloadButtonClicked(Object sender) {
		//		Queue item = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
		NSEnumerator enum = queueTable.selectedRowEnumerator();
		while(enum.hasMoreElements()) {
			Queue item = (Queue)this.queueModel.getEntry(((Integer)enum.nextElement()).intValue());
//			Iterator i = item.getRoots().iterator();
//			while(i.hasNext()) {
//				Path p = (Path)i.next();
//				p.status.setResume(false);
//			}
			item.getRoot().status.setResume(false);
			this.startItem(item);
		}
	}

	public void revealButtonClicked(Object sender) {
		//		Queue item = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
		NSEnumerator enum = queueTable.selectedRowEnumerator();
		while(enum.hasMoreElements()) {
			Queue item = (Queue)this.queueModel.getEntry(((Integer)enum.nextElement()).intValue());
			if(!NSWorkspace.sharedWorkspace().selectFile(item.getRoot().getLocal().toString(), "")) {
				NSAlertPanel.beginCriticalAlertSheet(
													 NSBundle.localizedString("Could not show the file in the Finder"), //title
													 NSBundle.localizedString("OK"),// defaultbutton
													 null,//alternative button
													 null,//other button
													 this.window(), //docWindow
													 null, //modalDelegate
													 null, //didEndSelector
													 null, // dismiss selector
													 null, // context
													 "Could not show the file \""+item.getRoot().getLocal().toString()+"\" in the Finder because it moved since you downloaded it." // message
													 );
			}
		}
    }	

	public void removeButtonClicked(Object sender) {
//		Queue item = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
		NSEnumerator enum = queueTable.selectedRowEnumerator();
		int i = 0;
		while(enum.hasMoreElements()) {
//			Queue item = (Queue)this.queueModel.getEntry(((Integer)enum.nextElement()).intValue());
			this.queueModel.removeEntry(((Integer)enum.nextElement()).intValue()-i);
			i++;
		}
		this.queueTable.reloadData();
    }	
	
	public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
		return new NSArray(new Object[] {
			"Resume",
			NSBundle.localizedString("Reload"),
			"Stop",
			"Remove",
			NSToolbarItem.FlexibleSpaceItemIdentifier,
			"Reveal"
		});
	}

	public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
		return new NSArray(new Object[] {
			"Resume",
			"Reload",
			"Stop",
			"Remove",
			"Reveal",
			NSToolbarItem.CustomizeToolbarItemIdentifier, 
			NSToolbarItem.SpaceItemIdentifier, 
			NSToolbarItem.SeparatorItemIdentifier, 
			NSToolbarItem.FlexibleSpaceItemIdentifier
		});
	}
	
    public boolean validateToolbarItem(NSToolbarItem item) {
//		log.debug("validateToolbarItem:"+item.label());
		String label = item.label();
		if(label.equals(NSBundle.localizedString("Stop"))) {
			if(this.queueTable.selectedRow() != -1) {
				Queue queue = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
				return queue.isRunning();
			}
			return false;
		}
		if(label.equals(NSBundle.localizedString("Resume"))) {
			if(this.queueTable.selectedRow() != -1) {
				Queue queue = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
				return queue.isCanceled() && !(queue.remainingJobs() == 0);
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
		if(label.equals(NSBundle.localizedString("Reveal"))) {
			return this.queueTable.numberOfSelectedRows() == 1;
		}
		if(label.equals(NSBundle.localizedString("Remove"))) {
			if(this.queueTable.selectedRow() != -1) {
				Queue queue = (Queue)this.queueModel.getEntry(this.queueTable.selectedRow());
				return queue.isCanceled();
			}
			return false;
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
										   NSBundle.localizedString("The file")+" "+Codec.encode(path.getName())+" "+NSBundle.localizedString("alredy exists in")+" "+path.getLocal().getParent() // message
										   );
						while(!done) {
							try {
								log.debug("Sleeping...");
								Thread.sleep(1000); //milliseconds
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