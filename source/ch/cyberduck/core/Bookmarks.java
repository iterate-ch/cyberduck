package ch.cyberduck.core;

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

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.apple.cocoa.foundation.*;

/**
* Keeps track of user bookmarks
 * The hosts are stored in a hashmap where host.getURL() is the key
 * @see ch.cyberduck.core.Host
 * @version $Id$
 */
public abstract class Bookmarks {
    private static Logger log = Logger.getLogger(Bookmarks.class);
    
	public static final String HOSTNAME = "Hostname";
    public static final String NICKNAME = "Nickname";
    public static final String PORT = "Port";
    public static final String PROTOCOL = "Protocol";
    public static final String USERNAME = "Username";
    public static final String PATH = "Path";
	
    private Map data = new HashMap();
	
    public Bookmarks() {
		this.load();
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
	
	public void save() {
		this.save(FAVORTIES_FILE);
	}
	
	/**
		* Saves this collection of bookmarks in to a file to the users's application support directory
	 * in a plist xml format
	 */
    public void save(java.io.File f) {
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
				
				if(collection.writeToURL(f.toURL(), true))
					log.info("Bookmarks sucessfully saved to :"+f.toString());
				else
					log.error("Error saving Bookmarks to :"+f.toString());
			}
			catch(java.net.MalformedURLException e) {
				log.error(e.getMessage());
			}
		}
    }
	
	public void load() {
		this.load(FAVORTIES_FILE);
	}
	
	/**
		* Deserialize all the bookmarks saved previously in the users's application support directory
	 */
    public void load(java.io.File f) {
		log.debug("load");
		if(f.exists()) {
			log.info("Found Bookmarks file: "+f.toString());			
			NSData plistData = new NSData(f);
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
	
    public void addItem(Host h) {
		log.debug("addItem:"+h);
		this.data.put(h.getURL(), h);
    }
	
    public void removeItem(String key) {
		log.debug("removeItem:"+key);
		this.data.remove(key);
    }
	
    /**
		* @param name the Key the host is stored with (ususally host.toString())
     */
    public Host getItem(String name) {
		Host result =  (Host)this.data.get(name);
		if(null == result)
			throw new IllegalArgumentException("Host "+name+" not found in Bookmarks.");
		return result;
    }
	
//	public int indexOf(Host bookmark) {
//		return data.values().indexOf(bookmark);
//	}
	
    public Collection values() {
		return data.values();
    }
	
    public Iterator getIterator() {
		return data.values().iterator();
    }
	
	public Host getItemAtIndex(int row) {
		return (Host)this.values().toArray()[row];
    }
}
