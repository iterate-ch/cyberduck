package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.NSDraggingInfo;
import com.apple.cocoa.application.NSPasteboard;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.foundation.*;

import java.io.File;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class CDQueueTableDataSource extends Collection {
	private static Logger log = Logger.getLogger(CDQueueTableDataSource.class);

	private static final File QUEUE_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/Queue.plist"));

	static {
		QUEUE_FILE.getParentFile().mkdir();
	}

	private static CDQueueTableDataSource instance;

	public static CDQueueTableDataSource instance() {
		if(instance == null) {
			instance = new CDQueueTableDataSource();
		}
		return instance;
	}

	private int queuePboardChangeCount = NSPasteboard.pasteboardWithName("QueuePBoard").changeCount();

	private CDQueueTableDataSource() {
		this.load();
	}

	public void sort(NSTableColumn tableColumn, final boolean ascending) {
		//
	}
	
	public int numberOfRowsInTableView(NSTableView tableView) {
		return this.size();
	}

	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
		if(row < numberOfRowsInTableView(tableView)) {
			String identifier = (String)tableColumn.identifier();
			if(identifier.equals("ICON")) {
				return this.get(row);
			}
			if(identifier.equals("PROGRESS")) {
				return this.getController(row).view();
			}
			if(identifier.equals("TYPEAHEAD")) {
				return ((Queue)this.get(row)).getName();
			}
			throw new IllegalArgumentException("Unknown identifier: "+identifier);
		}
		return null;
	}

	// ----------------------------------------------------------
	// Drop methods
	// ----------------------------------------------------------

	public int tableViewValidateDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
		log.debug("tableViewValidateDrop:row:"+row+",operation:"+operation);
		if(info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.StringPboardType)) != null) {
			tableView.setDropRowAndDropOperation(row, NSTableView.DropAbove);
			return NSDraggingInfo.DragOperationCopy;
		}
		NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
		if(this.queuePboardChangeCount < pboard.changeCount()) {
			if(pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
				tableView.setDropRowAndDropOperation(row, NSTableView.DropAbove);
				return NSDraggingInfo.DragOperationCopy;
			}
		}
		log.debug("tableViewValidateDrop:DragOperationNone");
		return NSDraggingInfo.DragOperationNone;
	}

	/**
	 * Invoked by tableView when the mouse button is released over a table view that previously decided to allow a drop.
	 *
	 * @param info  contains details on this dragging operation.
	 * @param index The proposed location is row and action is operation.
	 *              The data source should
	 *              incorporate the data from the dragging pasteboard at this time.
	 */
	public boolean tableViewAcceptDrop(NSTableView tableView, NSDraggingInfo info, int index, int operation) {
		log.debug("tableViewAcceptDrop:row:"+index+",operation:"+operation);
		int row = index;
		if(row < 0) {
			row = 0;
		}
		if(info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.StringPboardType)) != null) {
			String droppedText = info.draggingPasteboard().stringForType(NSPasteboard.StringPboardType);// get the data from paste board
			if(droppedText != null) {
				log.info("NSPasteboard.StringPboardType:"+droppedText);
				try {
					Host h = Host.parse(droppedText);
					String file = h.getDefaultPath();
					if(file.length() > 1) {
						Path p = PathFactory.createPath(SessionFactory.createSession(h), file);
						Queue q = new DownloadQueue();
						q.addRoot(p);
						CDQueueController.instance().startItem(q);
						return true;
					}
				}
				catch(java.net.MalformedURLException e) {
					log.error(e.getMessage());
				}
			}
		}
		else {
			// we are only interested in our private pasteboard with a description of the queue
			// encoded in as a xml.
			NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
			if(this.queuePboardChangeCount < pboard.changeCount()) {
				log.debug("availableTypeFromArray:QueuePBoardType: "+pboard.availableTypeFromArray(new NSArray("QueuePBoardType")));
				if(pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
					Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
					log.debug("tableViewAcceptDrop:"+o);
					if(o != null) {
						NSArray elements = (NSArray)o;
						for(int i = 0; i < elements.count(); i++) {
							NSDictionary dict = (NSDictionary)elements.objectAtIndex(i);
							this.add(row, Queue.createQueue(dict));
							tableView.reloadData();
							tableView.selectRow(row, false);
						}
						this.queuePboardChangeCount++;
						pboard.setPropertyListForType(null, "QueuePBoardType");
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// ----------------------------------------------------------
	//	Data Manipulation
	// ----------------------------------------------------------

	public void save() {
		this.save(QUEUE_FILE);
	}

	private void save(java.io.File f) {
		log.debug("save");
		if(Preferences.instance().getBoolean("queue.save")) {
			try {
				NSMutableArray list = new NSMutableArray();
				for(int i = 0; i < this.size(); i++) {
					list.addObject(((Queue)this.get(i)).getAsDictionary());
				}
				NSMutableData collection = new NSMutableData();
				String[] errorString = new String[]{null};
				collection.appendData(NSPropertyListSerialization.dataFromPropertyList(list,
				    NSPropertyListSerialization.PropertyListXMLFormat,
				    errorString));
				if(errorString[0] != null) {
					log.error("Problem writing queue file: "+errorString[0]);
				}

				if(collection.writeToURL(f.toURL(), true)) {
					log.info("Queue sucessfully saved to :"+f.toString());
				}
				else {
					log.error("Error saving queue to :"+f.toString());
				}
			}
			catch(java.net.MalformedURLException e) {
				log.error(e.getMessage());
			}
		}
	}

	public void load() {
		this.load(QUEUE_FILE);
	}

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
			if(errorString[0] != null) {
				log.error("Problem reading queue file: "+errorString[0]);
			}
			if(propertyListFromXMLData instanceof NSArray) {
				NSArray entries = (NSArray)propertyListFromXMLData;
				java.util.Enumeration i = entries.objectEnumerator();
				Object element;
				while(i.hasMoreElements()) {
					element = i.nextElement();
					if(element instanceof NSDictionary) {
						this.add(Queue.createQueue((NSDictionary)element));
					}
				}
			}
		}
	}

	public boolean add(Object queue) {
		super.add(new CDProgressController((Queue)queue));
		this.save();
        return true;
	}

	public void add(int row, Object queue) {
		super.add(row, new CDProgressController((Queue)queue));
		this.save();
	}

	public boolean remove(Object item) {
		for(int i = 0; i < this.size(); i++) {
			CDProgressController c = this.getController(i);
			if(c.getQueue().equals(item)) {
				super.remove(i);
			}
		}
        return true;
	}

    /**
     * Get the queue at index row
     * @param row
     * @return The @see ch.cyberduck.core.Queue object at this index
     */
	public Object get(int row) {
		if(row < this.size()) {
			return ((CDProgressController)super.get(row)).getQueue();
		}
		return null;
	}

    /**
     * Get the progress controller at index row
     * @param row
     * @return the @see ch.cyberduck.ui.cocoa.CDProgressController at this index
     */
	public CDProgressController getController(int row) {
		if(row < this.size()) {
			return (CDProgressController)super.get(row);
		}
		return null;
	}
}