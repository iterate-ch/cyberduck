package ch.cyberduck.ui.cocoa.delegate;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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
import ch.cyberduck.core.Rendezvous;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDMainController;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSMenu;
import com.apple.cocoa.application.NSMenuItem;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSObject;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class RendezvousMenuDelegate extends MenuDelegate {
    private static Logger log = Logger.getLogger(RendezvousMenuDelegate.class);

    /**
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public int numberOfItemsInMenu(NSMenu menu) {
        synchronized(Rendezvous.instance()) {
            int n = Rendezvous.instance().numberOfServices();
            if(n > 0) {
                return n;
            }
            return 1;
        }
    }

    /**
     * Called to let you update a menu item before it is displayed. If your
     * numberOfItemsInMenu delegate method returns a positive value,
     * then your menuUpdateItemAtIndex method is called for each item in the menu.
     * You can then update the menu title, image, and so forth for the menu item.
     * Return true to continue the process. If you return false, your menuUpdateItemAtIndex
     * is not called again. In that case, it is your responsibility to trim any extra items from the menu.
     * @see com.apple.cocoa.application.NSMenu.Delegate
     */
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem sender, int index, boolean shouldCancel) {
        synchronized(Rendezvous.instance()) {
            if(Rendezvous.instance().numberOfServices() == 0) {
                sender.setTitle(NSBundle.localizedString("No Bonjour services available", ""));
                sender.setEnabled(false);
                return !shouldCancel;
            }
            else {
                if(index >= this.numberOfItemsInMenu(menu)) {
                    log.warn("Invalid index in menuUpdateItemAtIndex:" + index);
                    return false;
                }
                String title = Rendezvous.instance().getDisplayedName(index);
                sender.setTitle(title);
                sender.setTarget(this);
                sender.setEnabled(true);
                sender.setAction(new NSSelector("rendezvousMenuClicked", new Class[]{NSMenuItem.class}));
                sender.setRepresentedObject(Rendezvous.instance().getServiceWithDisplayedName(title));
                return !shouldCancel;
            }
        }
    }

    public void rendezvousMenuClicked(NSMenuItem sender) {
        log.debug("rendezvousMenuClicked:" + sender);
        CDBrowserController controller
                = ((CDMainController) (NSApplication.sharedApplication().delegate())).newDocument();
        controller.mount((Host) sender.representedObject());
    }
}
