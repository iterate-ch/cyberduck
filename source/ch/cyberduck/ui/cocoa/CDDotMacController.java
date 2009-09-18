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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.serializer.HostReaderFactory;
import ch.cyberduck.ui.cocoa.application.NSAlert;

/**
 * @version $Id$
 */
public class CDDotMacController extends CDController {

    private static CDDotMacController instance;

    public static CDDotMacController instance() {
        if(null == instance) {
            instance = new CDDotMacController();
        }
        return instance;
    }

    private static boolean JNI_LOADED = false;

    /**
     * Load native library extensions
     *
     * @return
     */
    private static boolean loadNative() {
        if(!JNI_LOADED) {
            JNI_LOADED = Native.load("DotMac");
        }
        return JNI_LOADED;
    }

    private CDDotMacController() {
        ;
    }

    public boolean isAvailable() {
        return loadNative();
    }

    /**
     * @return Member name of the MobileMe account configured in System Preferences
     */
    public String getAccountName() {
        if(!loadNative()) {
            return null;
        }
        return getAccountNameNative();
    }

    protected native String getAccountNameNative();

    /**
     *
     */
    public void downloadBookmarks() {
        if(!loadNative()) {
            return;
        }
        final Local f = LocalFactory.createLocal(Preferences.instance().getProperty("tmp.dir"), "Favorites.plist");
        this.downloadBookmarksNative(f.getAbsolute());
        if(f.exists()) {
            final Collection<Host> collection = HostReaderFactory.instance().readCollection(f);
            for(Host bookmark : collection) {
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
                    if(choice == CDSheetCallback.OTHER_OPTION) {
                        continue;
                    }
                    if(choice == CDSheetCallback.ALTERNATE_OPTION) {
                        return;
                    }
                }
            }
        }
        f.delete();
    }

    protected native void downloadBookmarksNative(String path);

    /**
     *
     */
    public void uploadBookmarks() {
        if(!loadNative()) {
            return;
        }
        this.uploadBookmarksNative();
    }

    public native void uploadBookmarksNative();
}