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

import ch.cyberduck.core.History;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Preferences;
import com.apple.cocoa.application.NSComboBox;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Iterator;

/**
* @version $Id$
 */
public class CDHistoryImpl extends History { //implements NSComboBox.DataSource {
    private static Logger log = Logger.getLogger(CDHistoryImpl.class);
	
    private static History instance;
	
    private static final File HISTORY_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/History.plist"));
	
    static {
		HISTORY_FILE.getParentFile().mkdir();
    }
	
    private CDHistoryImpl() {
		super();
    }
	
    public static History instance() {
		if(null == instance) {
			instance = new CDHistoryImpl();
		}
		return instance;
    }
	
    public void save() {
		if(Preferences.instance().getProperty("history.save").equals("true")) {
			log.debug("save");
			try {
				Iterator i = super.getIterator();
				
				NSMutableArray data = new NSMutableArray();
				int no = 0;
				int size = Integer.parseInt(Preferences.instance().getProperty("history.size"));
				while(i.hasNext() && no < size) {
					data.addObject(((Host)i.next()).getURL());
					no++;
				}
				NSMutableData collection = new NSMutableData();
				collection.appendData(NSPropertyListSerialization.XMLDataFromPropertyList(data));
				
				// data is written to a backup location, and then, assuming no errors occur, the backup location is renamed to the specified name
				if(collection.writeToURL(HISTORY_FILE.toURL(), true))
					log.info("History sucessfully saved in :"+ HISTORY_FILE.toString());
				else
					log.error("Error saving History in :"+ HISTORY_FILE.toString());
			}
			catch(java.net.MalformedURLException e) {
				log.error(e.getMessage());
			}
		}
    }
    
    public void load() {
		log.debug("load");
		if(HISTORY_FILE.exists()) {
			log.info("Found History file: "+ HISTORY_FILE.toString());
			
			NSData plistData = new NSData(HISTORY_FILE);
			NSArray entries = (NSArray)NSPropertyListSerialization.propertyListFromXMLData(plistData);
			log.info("Successfully read History: "+entries);
			java.util.Enumeration i = entries.objectEnumerator();
			while(i.hasMoreElements()) {
				try {
					this.addItem(new Host((String)i.nextElement()));
				}
				catch(java.net.MalformedURLException e) {
					log.error(e.getMessage());
				}
			}
		}
    }
	
    public Host getItemAtIndex(int row) {
		return (Host)this.values().toArray()[row];
    }
	
    // ----------------------------------------------------------
    // NSComboBox.DataSource
    // ----------------------------------------------------------
	
    public int numberOfItemsInComboBox(NSComboBox combo) {
		return this.values().size();
    }
	
    public Object comboBoxObjectValueForItemAtIndex(NSComboBox combo, int row) {
		Host h = (Host)this.values().toArray()[row];
		return h.getHostname();
    }
    
    // ----------------------------------------------------------
    // NSTableView.DataSource
    // ----------------------------------------------------------
	
    public int numberOfRowsInTableView(NSTableView tableView) {
		return this.values().size();
    }
    
    //getValue()
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
		String identifier = (String)tableColumn.identifier();
		if(identifier.equals("URL")) {
			Host h = (Host)this.values().toArray()[row];
			return h.getHostname();
		}
		throw new IllegalArgumentException("Unknown identifier: "+identifier);
    }
}
