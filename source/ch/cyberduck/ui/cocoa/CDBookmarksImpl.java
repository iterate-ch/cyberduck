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

import ch.cyberduck.core.Bookmarks;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Login;
import ch.cyberduck.core.Preferences;

import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Iterator;

/**
* @version $Id$
 */
public class CDBookmarksImpl extends Bookmarks { //implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDBookmarksImpl.class);
	
    private static Bookmarks instance;
	
    private static final File FAVORTIES_FILE = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/Favorites.plist"));
	
    static {
		FAVORTIES_FILE.getParentFile().mkdir();
    }
	
    private CDBookmarksImpl() {
		super();
    }
	
    public static Bookmarks instance() {
		if(null == instance) {
			instance = new CDBookmarksImpl();
		}
		return instance;
    }
	
	private Host getFromDictionary(NSDictionary dict) {
		log.debug(dict);
		return new Host(
		   (String)dict.objectForKey(Bookmarks.PROTOCOL), 
		   (String)dict.objectForKey(Bookmarks.NICKNAME),
		   (String)dict.objectForKey(Bookmarks.HOSTNAME), 
		   Integer.parseInt((String)dict.objectForKey(Bookmarks.PORT)),
		   new Login((String)dict.objectForKey(Bookmarks.USERNAME)),
		   (String)dict.objectForKey(Bookmarks.PATH)
				  );
	}
	
	private NSDictionary getAsDictionary(Host bookmark) {
		NSMutableDictionary element = new NSMutableDictionary();
		element.setObjectForKey(bookmark.getNickname(), Bookmarks.NICKNAME);
		element.setObjectForKey(bookmark.getHostname(), Bookmarks.HOSTNAME);
		element.setObjectForKey(bookmark.getPort()+"", Bookmarks.PORT);
		element.setObjectForKey(bookmark.getProtocol(), Bookmarks.PROTOCOL);
		element.setObjectForKey(bookmark.getLogin().getUsername(), Bookmarks.USERNAME);
		element.setObjectForKey(bookmark.getDefaultPath(), Bookmarks.PATH);
		log.debug(element);
		return element;
	}
	
	public Host importBookmark(java.io.File file) {
		log.info("Importing bookmark from "+file);
		NSData plistData = new NSData(file);
		Object propertyListFromXMLData = NSPropertyListSerialization.propertyListFromXMLData(plistData);
		log.info("Successfully read bookmark file: "+propertyListFromXMLData);
		if(propertyListFromXMLData instanceof NSDictionary) {
			return getFromDictionary((NSDictionary)propertyListFromXMLData);
		}
		log.error("Invalid file format:"+file);
		return null;
	}		
	
	public void exportBookmark(Host bookmark, java.io.File file) {
		try {
			log.info("Importing bookmark "+bookmark+" to "+file);
			NSMutableData collection = new NSMutableData();
			collection.appendData(NSPropertyListSerialization.XMLDataFromPropertyList(this.getAsDictionary(bookmark)));
			// data is written to a backup location, and then, assuming no errors occur, 
   // the backup location is renamed to the specified name
			if(collection.writeToURL(file.toURL(), true))
				log.info("Bookmarks sucessfully saved in :"+file.toString());
			else
				log.error("Error saving Bookmarks in :"+file.toString());
		}
		catch(java.net.MalformedURLException e) {
			log.error(e.getMessage());
		}
	}
	
	/**
		* Saves this collection of bookmarks in to a file to the users's application support directory
	 * in a plist xml format
	 */
    public void save() {
		log.debug("save");
		if(Preferences.instance().getProperty("favorites.save").equals("true")) {
			try {
				NSMutableArray list = new NSMutableArray();
				Iterator i = super.getIterator();
				while(i.hasNext()) {
					Host bookmark = (Host)i.next();
					list.addObject(this.getAsDictionary(bookmark));
				}
				NSMutableData collection = new NSMutableData();
				collection.appendData(NSPropertyListSerialization.XMLDataFromPropertyList(list));
				
				if(collection.writeToURL(FAVORTIES_FILE.toURL(), true))
					log.info("Bookmarks sucessfully saved to :"+FAVORTIES_FILE.toString());
				else
					log.error("Error saving Bookmarks to :"+FAVORTIES_FILE.toString());
			}
			catch(java.net.MalformedURLException e) {
				log.error(e.getMessage());
			}
		}
    }
	
	/**
		* Deserialize all the bookmarks saved previously in the users's application support directory
	 */
    public void load() {
		log.debug("load");
		if(FAVORTIES_FILE.exists()) {
			log.info("Found Bookmarks file: "+FAVORTIES_FILE.toString());
			
			NSData plistData = new NSData(FAVORTIES_FILE);
			Object propertyListFromXMLData = NSPropertyListSerialization.propertyListFromXMLData(plistData);
			log.info("Successfully read Bookmarks: "+propertyListFromXMLData);
			if(propertyListFromXMLData instanceof NSArray) {
				NSArray entries = (NSArray)propertyListFromXMLData;
				java.util.Enumeration i = entries.objectEnumerator();
				Object element;
				while(i.hasMoreElements()) {
					element = i.nextElement();
					if(element instanceof NSDictionary) { //new since 2.1
						this.addItem(this.getFromDictionary((NSDictionary)element));
					}
					if(element instanceof String) { //backward compatibilty <= 2.1beta5 (deprecated)
						try {
							this.addItem(new Host((String)element));
						}
						catch(java.net.MalformedURLException e) {
							log.error("Bookmark has invalid URL: "+e.getMessage());
						}
					}
				}
			}
		}
    }
}
