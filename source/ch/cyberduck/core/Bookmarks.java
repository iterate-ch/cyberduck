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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.NSWorkspace;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
* Keeps track of user bookmarks
 * The hosts are stored in a hashmap where host.getURL() is the key
 * @see ch.cyberduck.core.Host
 * @version $Id$
 */
public abstract class Bookmarks {
    private static Logger log = Logger.getLogger(Bookmarks.class);
    
	private static final String HOSTNAME = "Hostname";
    private static final String NICKNAME = "Nickname";
    private static final String PORT = "Port";
    private static final String PROTOCOL = "Protocol";
    private static final String USERNAME = "Username";
    private static final String PATH = "Path";
    private static final String KEYFILE = "Private Key File";	
	
    protected List data = new ArrayList();
	
	private Host getFromDictionary(NSDictionary dict) {
		Host h = new Host(
				  (String)dict.objectForKey(Bookmarks.PROTOCOL), 
				  (String)dict.objectForKey(Bookmarks.HOSTNAME), 
				  Integer.parseInt((String)dict.objectForKey(Bookmarks.PORT)),
				  new Login((String)dict.objectForKey(Bookmarks.USERNAME)),
				  (String)dict.objectForKey(Bookmarks.PATH),
				  (String)dict.objectForKey(Bookmarks.NICKNAME)
				  );
		h.getLogin().setPrivateKeyFile((String)dict.objectForKey(Bookmarks.KEYFILE));
		return h;
	}
	
	private NSDictionary getAsDictionary(Host bookmark) {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(bookmark.getNickname(), Bookmarks.NICKNAME);
		dict.setObjectForKey(bookmark.getHostname(), Bookmarks.HOSTNAME);
		dict.setObjectForKey(bookmark.getPort()+"", Bookmarks.PORT);
		dict.setObjectForKey(bookmark.getProtocol(), Bookmarks.PROTOCOL);
		dict.setObjectForKey(bookmark.getLogin().getUsername(), Bookmarks.USERNAME);
		dict.setObjectForKey(bookmark.getDefaultPath(), Bookmarks.PATH);
		if(bookmark.getLogin().getPrivateKeyFile() != null)
			dict.setObjectForKey(bookmark.getLogin().getPrivateKeyFile(), Bookmarks.KEYFILE);
		return dict;
	}
	
	public Host importBookmark(java.io.File file) {
		log.info("Importing bookmark from "+file);
		NSData plistData = new NSData(file);
		String[] errorString = new String[]{null};
		Object propertyListFromXMLData = 
			NSPropertyListSerialization.propertyListFromData(plistData, 
															 NSPropertyListSerialization.PropertyListImmutable,
															 new int[]{NSPropertyListSerialization.PropertyListXMLFormat}, 
															 errorString);
		if(errorString[0]!=null)
			log.error("Problem reading bookmark file: "+errorString[0]);
		else
			log.info("Successfully read bookmark file: "+propertyListFromXMLData);
		if(propertyListFromXMLData instanceof NSDictionary) {
			return getFromDictionary((NSDictionary)propertyListFromXMLData);
		}
		log.error("Invalid file format:"+file);
		return null;
	}		
	
	public void exportBookmark(Host bookmark, java.io.File file) {
		try {
			log.info("Exporting bookmark "+bookmark+" to "+file);
			NSMutableData collection = new NSMutableData();
//			public static NSData dataFromPropertyList(Object plist, int format, String[] errorString)
			String[] errorString = new String[]{null};
			collection.appendData(NSPropertyListSerialization.dataFromPropertyList(
												 this.getAsDictionary(bookmark),
												 NSPropertyListSerialization.PropertyListXMLFormat, 
																				   errorString)
					  );
			if(errorString[0]!=null)
				log.error("Problem writing bookmark file: "+errorString[0]);
				//collection.appendData(NSPropertyListSerialization.XMLDataFromPropertyList(this.getAsDictionary(bookmark)));
			if(collection.writeToURL(file.toURL(), true)) {
				log.info("Bookmarks sucessfully saved in :"+file.toString());
				NSWorkspace.sharedWorkspace().noteFileSystemChangedAtPath(file.getAbsolutePath());
			}
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
    public void save(java.io.File f) {
		log.debug("save");
		if(Preferences.instance().getProperty("favorites.save").equals("true")) {
			try {
				NSMutableArray list = new NSMutableArray();
				Iterator i = this.iterator();
				while(i.hasNext()) {
					Host bookmark = (Host)i.next();
					list.addObject(this.getAsDictionary(bookmark));
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
					log.error("Problem writing bookmark file: "+errorString[0]);
				
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
	
	/**
		* Deserialize all the bookmarks saved previously in the users's application support directory
	 */
    public void load(java.io.File f) {
		log.debug("load");
		if(f.exists()) {
			log.info("Found Bookmarks file: "+f.toString());			
			NSData plistData = new NSData(f);
			String[] errorString = new String[]{null};
			Object propertyListFromXMLData = 
				NSPropertyListSerialization.propertyListFromData(plistData, 
																 NSPropertyListSerialization.PropertyListImmutable,
																 new int[]{NSPropertyListSerialization.PropertyListXMLFormat}, 
																 errorString);
			if(errorString[0]!=null)
				log.error("Problem reading bookmark file: "+errorString[0]);
			else
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
	
	// ----------------------------------------------------------
	//	Data Manipulation
	// ----------------------------------------------------------
	
    public void addItem(Host item) {
		log.debug("addItem:"+item);
		this.data.add(item);
    }
	
	public void removeItem(int index) {
		this.data.remove(index);
	}
	
    public void removeItem(Host item) {
		log.debug("removeItem:"+item);
		this.removeItem(this.data.lastIndexOf(item));
    }
	
    /**
		* @param name the Key the host is stored with (ususally host.toString())
     */
    public Host getItem(int index) {
		Host result = (Host)this.data.get(index);
		if(null == result)
			throw new IllegalArgumentException("No host with index "+index+" in Bookmarks.");
		return result;
    }
	
    public Collection values() {
		return data;
    }
	
    public Iterator iterator() {
		return data.iterator();
    }
}
