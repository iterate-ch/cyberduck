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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostCollection;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.apache.log4j.Logger;
import org.rococoa.Rococoa;

import java.io.File;
import java.io.IOException;

/**
 * @version $Id$
 */
public class CDDotMacController extends CDController {

    private static Logger log = Logger.getLogger(CDDotMacController.class);

    static {
        // Ensure native odb library is loaded
        try {
            NSBundle bundle = NSBundle.mainBundle();
            String lib = bundle.resourcePath() + "/Java/" + "libDotMac.dylib";
            log.info("Locating libDotMac.dylib at '" + lib + "'");
            System.load(lib);
            log.info("libDotMac.dylib loaded");
        }
        catch(UnsatisfiedLinkError e) {
            log.error("Could not load the libDotMac.dylib library:" + e.getMessage());
            throw e;
        }
    }

    /**
     * @return Member name of the MobileMe account configured in System Preferences
     */
    public native String getAccountName();

    /**
     * @param path
     */
    private native void downloadBookmarks(String path);

    /**
     *
     */
    public void downloadBookmarks() {
        File f = new File(Preferences.instance().getProperty("tmp.dir"), "Favorites.plist");
        this.downloadBookmarks(f.getAbsolutePath());
        if(f.exists()) {
            NSData plistData = NSData.dataWithContentsOfURL(NSURL.fileURLWithPath(f.getAbsolutePath()));
            try {
                NSArray propertyListFromXMLData = Rococoa.cast(NSPropertyListSerialization.propertyListFromData(plistData), NSArray.class);
                NSArray entries = (NSArray) propertyListFromXMLData;
                final NSEnumerator i = entries.objectEnumerator();
                NSObject next;
                while(((next = i.nextObject()) != null)) {
                    final Host bookmark = new Host(Rococoa.cast(next, NSDictionary.class));
                    if(!HostCollection.defaultCollection().contains(bookmark)) {
                        final NSAlert alert = NSAlert.alert((bookmark).getNickname(),
                                Locale.localizedString("Add this bookmark to your existing bookmarks?", "IDisk"),
                                Locale.localizedString("Add", "IDisk"), //default
                                Locale.localizedString("Cancel"), //alternate
                                Locale.localizedString("Skip", "IDisk"));
                        int choice = alert.runModal(); //other
                        if(choice == CDSheetCallback.DEFAULT_OPTION) {
                            HostCollection.defaultCollection().add(bookmark);
                        }
                        if(choice == CDSheetCallback.ALTERNATE_OPTION) {
                            return;
                        }
                    }
                }
            }
            catch(IOException e) {
                log.error("Problem reading bookmark file: " + e.getMessage());
            }
        }
        f.delete();
    }

    /**
     *
     */
    public native void uploadBookmarks();
}