package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSPopUpButton;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSObject;
import com.apple.cocoa.foundation.NSSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Message;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;

/**
 * @version $Id$
 */
public class CDPathController extends NSObject implements Observer {
    private static Logger log = Logger.getLogger(CDPathController.class);

    private NSPopUpButton combo;
    private List items = new ArrayList();
    private Path workdir;

    public CDPathController(NSPopUpButton combo) {
        log.debug("CDPathController");
        this.combo = combo;
        this.combo.setTarget(this);
        this.combo.setAction(new NSSelector("selectionChanged", new Class[]{Object.class}));
    }

    public NSView view() {
        return this.combo;
    }

    public Path workdir() {
        return this.workdir;
    }

    public void update(final Observable o, final Object arg) {
        log.debug("update:" + o + "," + arg);
        if (o instanceof Session) {
            if (arg instanceof Path) {
                workdir = (Path)arg;
                removeAllItems();
                // current path has index 0
                addItem(workdir);
                // root path has index numberOfItems()-1
                Path p = workdir;
                while (!p.isRoot()) {
                    p = p.getParent();
                    addItem(p);
                }
            }
			else if (arg instanceof Message) {
				Message msg = (Message)arg;
				if (msg.getTitle().equals(Message.OPEN)) {
					this.removeAllItems();
				}
			}
        }
    }

    public int numberOfItems() {
        return items.size();
    }

    public void selectionChanged(Object sender) {
        Path p = (Path)items.get(combo.indexOfSelectedItem());
        p.list();
    }

    public void addItem(Path p) {
        this.items.add(p);
        this.combo.addItem(p.getAbsolute());
        if (p.isRoot()) {
            this.combo.itemAtIndex(this.combo.numberOfItems() - 1).setImage(NSImage.imageNamed("disk.tiff"));
        }
        else {
            this.combo.itemAtIndex(this.combo.numberOfItems() - 1).setImage(NSImage.imageNamed("folder16.tiff"));
        }
    }

    public Path getItem(int row) {
        return (Path)items.get(row);
    }

    public void removeAllItems() {
        this.items.clear();
        this.combo.removeAllItems();
    }
}
