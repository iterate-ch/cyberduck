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

import ch.cyberduck.core.Favorites;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Login;
import ch.cyberduck.core.Preferences;

import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Iterator;

/**
* @version $Id$
 */
public class CDFavoritesImpl extends Favorites { //implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDFavoritesImpl.class);

    private static Favorites instance;

    private static final File FAVORTIES_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/Favorites.plist"));

    static {
	FAVORTIES_FILE.getParentFile().mkdir();
    }

    private CDFavoritesImpl() {
	super();
    }

    public static Favorites instance() {
	if(null == instance) {
	    instance = new CDFavoritesImpl();
	}
	return instance;
    }
    
    private static final String HOSTNAME = "Hostname";
    private static final String NICKNAME = "Nickname";
    private static final String PORT = "Port";
    private static final String PROTOCOL = "Protocol";
    private static final String USERNAME = "Username";
    private static final String PATH = "Path";

    public void save() {
	log.debug("save");
	if(Preferences.instance().getProperty("favorites.save").equals("true")) {
	    try {
		NSMutableArray list = new NSMutableArray();
		Iterator i = super.getIterator();
		while(i.hasNext()) {
		    NSMutableDictionary element = new NSMutableDictionary();
		    Host next = (Host)i.next();
		    element.setObjectForKey(next.getNickname(), NICKNAME);
		    element.setObjectForKey(next.getHostname(), HOSTNAME);
		    element.setObjectForKey(next.getPort()+"", PORT);
		    element.setObjectForKey(next.getProtocol(), PROTOCOL);
		    element.setObjectForKey(next.getLogin().getUsername(), USERNAME);
		    element.setObjectForKey(next.getDefaultPath(), PATH);
		    list.addObject(element);
		}
		NSMutableData collection = new NSMutableData();
		collection.appendData(NSPropertyListSerialization.XMLDataFromPropertyList(list));
		
	    // data is written to a backup location, and then, assuming no errors occur, 
		// the backup location is renamed to the specified name
		if(collection.writeToURL(FAVORTIES_FILE.toURL(), true))
		    log.info("Favorites sucessfully saved in :"+FAVORTIES_FILE.toString());
		else
		    log.error("Error saving Favorites in :"+FAVORTIES_FILE.toString());
	    }
	    catch(java.net.MalformedURLException e) {
		log.error(e.getMessage());
	    }
	}
    }

    public void load() {
	log.debug("load");
	if(FAVORTIES_FILE.exists()) {
	    log.info("Found Favorites file: "+FAVORTIES_FILE.toString());
	    
	    NSData plistData = new NSData(FAVORTIES_FILE);
	    Object propertyListFromXMLData = NSPropertyListSerialization.propertyListFromXMLData(plistData);
	    log.info("Successfully read Favorites: "+propertyListFromXMLData);
	    if(propertyListFromXMLData instanceof NSArray) {
		NSArray entries = (NSArray)propertyListFromXMLData;
		java.util.Enumeration i = entries.objectEnumerator();
		Object element;
		while(i.hasMoreElements()) {
		    element = i.nextElement();
		    if(element instanceof NSMutableDictionary) { //new since 2.1
			NSMutableDictionary a = (NSMutableDictionary)element;
			this.addItem(
		new Host(
	   (String)a.objectForKey(PROTOCOL), 
	   (String)a.objectForKey(NICKNAME),
	   (String)a.objectForKey(HOSTNAME), 
	   Integer.parseInt((String)a.objectForKey(PORT)),
	   new Login((String)a.objectForKey(USERNAME)),
	   (String)a.objectForKey(PATH)
	   )
		);
		    }
		    if(element instanceof String) { //backward compatibilty <= 2.1beta5
			try {
			    this.addItem(new Host((String)element));
			}
			catch(java.net.MalformedURLException e) {
			    log.error(e.getMessage());
			}
		    }
		}
	    }
	}
    }
    
    // ----------------------------------------------------------
    // NSTableView.DataSource
    // ----------------------------------------------------------

    public int numberOfRowsInTableView(NSTableView tableView) {
	return this.values().size();
    }
    
    //getValue()
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
	log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
	String identifier = (String)tableColumn.identifier();
	if(identifier.equals("URL")) {
	    Host h = (Host)this.values().toArray()[row];
	     /*
	    NSDictionary attributes = new NSDictionary(new Object[]{NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName}, new Object[]{NSFont.fontWithNameAndSize("Monaco", 10), NSColor.greenColor()});
	    NSAttributedString text = new NSAttributedString(h.getURL(), attributes);
	    return text;
	     */
//	    return h.getURL();
	    return h.getNickname();
	}
	throw new IllegalArgumentException("Unknown identifier: "+identifier);
    }
}
