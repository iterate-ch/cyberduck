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

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.File;

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
        catch (UnsatisfiedLinkError e) {
            log.error("Could not load the libDotMac.dylib library:" + e.getMessage());
        }
    }

    private native void downloadBookmarks(String path);

    public native void uploadBookmarks();

    public void downloadBookmarks() {
        File f = new File(NSPathUtilities.temporaryDirectory(), "Favorites.plist");
        this.downloadBookmarks(f.getAbsolutePath());
        if (f.exists()) {
            NSData plistData = new NSData(f);
            String[] errorString = new String[]{null};
            Object propertyListFromXMLData =
                    NSPropertyListSerialization.propertyListFromData(plistData,
                            NSPropertyListSerialization.PropertyListImmutable,
                            new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                            errorString);
            if (errorString[0] != null) {
                log.error("Problem reading bookmark file: " + errorString[0]);
            }
            if (propertyListFromXMLData instanceof NSArray) {
                NSArray entries = (NSArray) propertyListFromXMLData;
                java.util.Enumeration i = entries.objectEnumerator();
                Object element;
                while (i.hasMoreElements()) {
                    element = i.nextElement();
                    if (element instanceof NSDictionary) {
                        Host bookmark = new Host((NSDictionary) element);
                        if (!HostCollection.instance().contains(bookmark)) {
                            int choice = NSAlertPanel.runAlert((bookmark).getNickname(),
                                    NSBundle.localizedString("Add this bookmark to your existing bookmarks?", "IDisk", ""),
                                    NSBundle.localizedString("Add", "IDisk", ""), //default
                                    NSBundle.localizedString("Cancel", ""), //alternate
                                    NSBundle.localizedString("Skip", "IDisk", "")); //other
                            if (choice == CDSheetCallback.DEFAULT_OPTION) {
                                HostCollection.instance().add(bookmark);
                            }
                            if (choice == CDSheetCallback.ALTERNATE_OPTION) {
                                return;
                            }
                        }
                    }
                }
            }
        }
        f.delete();
    }
}