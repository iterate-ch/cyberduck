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
	
	public void import(java.io.File file) {
		NSData plistData = new NSData(FAVORTIES_FILE);
		Object propertyListFromXMLData = NSPropertyListSerialization.propertyListFromXMLData(plistData);
		log.info("Successfully read Favorites: "+propertyListFromXMLData);

	}
	
	public void export(Host favorite, java.io.File file) {
		NSMutableData collection = new NSMutableData();
		collection.appendData(NSPropertyListSerialization.XMLDataFromPropertyList(this.getAsDictionary(favorite)));
		
		this.writeToFile(collection, file);
	}
	
	private NSDictionary getAsDictionary(Host favorite) {
		NSMutableDictionary element = new NSMutableDictionary();
		element.setObjectForKey(favorite.getNickname(), Favorites.NICKNAME);
		element.setObjectForKey(favorite.getHostname(), Favorites.HOSTNAME);
		element.setObjectForKey(favorite.getPort()+"", Favorites.PORT);
		element.setObjectForKey(favorite.getProtocol(), Favorites.PROTOCOL);
		element.setObjectForKey(favorite.getLogin().getUsername(), Favorites.USERNAME);
		element.setObjectForKey(favorite.getDefaultPath(), Favorites.PATH);
		return element;
	}
	
	private Host readFromFile(java.io.File file) {
		NSData plistData = new NSData(f);
		Object propertyListFromXMLData = NSPropertyListSerialization.propertyListFromXMLData(plistData);
		log.info("Successfully read file: "+propertyListFromXMLData);
		if(propertyListFromXMLData instanceof NSDictionary) {
			NSDictionary a = (NSDictionary)propertyListFromXMLData;
			Host host = new Host(
						(String)a.objectForKey(Favorites.PROTOCOL), 
						(String)a.objectForKey(Favorites.NICKNAME),
						(String)a.objectForKey(Favorites.HOSTNAME), 
						Integer.parseInt((String)a.objectForKey(Favorites.PORT)),
						new Login((String)a.objectForKey(Favorites.USERNAME)),
						(String)a.objectForKey(Favorites.PATH)
						);
			return host;
		}
	}		
	
	private void writeToFile(NSData xml, java.io.File file) {
		// data is written to a backup location, and then, assuming no errors occur, 
  // the backup location is renamed to the specified name
		if(collection.writeToURL(file.toURL(), true))
			log.info("Favorites sucessfully saved in :"+file.toString());
		else
			log.error("Error saving Favorites in :"+file.toString());
		
	}
    	
    public void save() {
		log.debug("save");
		if(Preferences.instance().getProperty("favorites.save").equals("true")) {
			try {
				NSMutableArray list = new NSMutableArray();
				Iterator i = super.getIterator();
				while(i.hasNext()) {
					Host favorite = (Host)i.next();
					list.addObject(this.getAsDictionary(favorite));
				}
				NSMutableData collection = new NSMutableData();
				collection.appendData(NSPropertyListSerialization.XMLDataFromPropertyList(list));
				
				this.writeToFile(collection, FAVORTIES_FILE);
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
					if(element instanceof NSDictionary) { //new since 2.1
						NSDictionary a = (NSDictionary)element;
						this.addItem(
				   new Host(
				(String)a.objectForKey(Favorites.PROTOCOL), 
				(String)a.objectForKey(Favorites.NICKNAME),
				(String)a.objectForKey(Favorites.HOSTNAME), 
				Integer.parseInt((String)a.objectForKey(Favorites.PORT)),
				new Login((String)a.objectForKey(Favorites.USERNAME)),
				(String)a.objectForKey(Favorites.PATH)
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
}
