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
import ch.cyberduck.core.RendezvousListener;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDIconCache;
import ch.cyberduck.ui.cocoa.CDMainController;
import ch.cyberduck.ui.cocoa.application.NSMenu;
import ch.cyberduck.ui.cocoa.application.NSMenuItem;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSInteger;

/**
 * @version $Id$
 */
public class RendezvousMenuDelegate extends AbstractMenuDelegate implements RendezvousListener {
    private static Logger log = Logger.getLogger(RendezvousMenuDelegate.class);

    public RendezvousMenuDelegate() {
        Rendezvous.instance().addListener(this);
    }

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        int n = Rendezvous.instance().numberOfServices();
        if(n > 0) {
            return new NSInteger(n);
        }
        return new NSInteger(1);
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean shouldCancel) {
        if(shouldCancel) {
            return false;
        }
        if(super.shouldSkipValidation(menu, index.intValue())) {
            return false;
        }
        if(Rendezvous.instance().numberOfServices() == 0) {
            item.setTitle(Locale.localizedString("No Bonjour services available"));
            item.setEnabled(false);
            return !shouldCancel;
        }
        else {
            final String title = Rendezvous.instance().getDisplayedName(index.intValue());
            final Host h = Rendezvous.instance().getServiceWithDisplayedName(title);
            item.setTitle(title);
            item.setTarget(this.id());
            item.setEnabled(true);
            item.setImage(CDIconCache.iconNamed(h.getProtocol().icon(), 16));
            item.setAction(Foundation.selector("rendezvousMenuClicked:"));
            item.setRepresentedObject(h.getNickname());
            return !shouldCancel;
        }
    }

    public void rendezvousMenuClicked(NSMenuItem sender) {
        log.debug("rendezvousMenuClicked:" + sender);
        CDBrowserController controller = CDMainController.newDocument();
        controller.mount(Rendezvous.instance().getServiceWithDisplayedName(sender.representedObject()));
    }

    @Override
    protected void invalidate() {
        Rendezvous.instance().removeListener(this);
        super.invalidate();
    }

    public void serviceResolved(String servicename, String hostname) {
        this.setNeedsUpdate();
    }

    public void serviceLost(String servicename) {
        this.setNeedsUpdate();
    }
}
