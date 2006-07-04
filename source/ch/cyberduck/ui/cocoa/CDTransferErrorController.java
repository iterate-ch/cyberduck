package ch.cyberduck.ui.cocoa;

import ch.cyberduck.core.Host;

import com.apple.cocoa.application.NSView;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.*;

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

/**
 * @version $Id$
 */
public class CDTransferErrorController extends CDErrorController {

    /**
     * @param container
     * @param failure
     * @param host
     */
    public CDTransferErrorController(NSView container, Exception failure, Host host) {
        super(container, failure, host);
        if(!NSApplication.loadNibNamed("TransferError", this)) {
            log.fatal("Couldn't load TransferError.nib");
        }
    }

    public void display() {
        this.errorField.setAttributedStringValue(new NSAttributedString(this.getErrorText()));
        view.setFrameSize(
                new NSSize(container.frame().size().width(), view.frame().size().height())
        );
        container.setFrameSize(
                new NSSize(container.frame().width(), container.frame().height() + view.frame().height())
        );
        container.addSubview(view, NSWindow.Above, null);
        container.setNeedsDisplay(true);
        this.resizeParentTableViewIfAny(container);
    }

    protected void viewWillClose() {
        container.setFrame(new NSRect(
                container.frame().origin().x(),
                container.frame().origin().y() + view.frame().size().height(),
                container.frame().size().width(),
                container.frame().size().height() - view.frame().size().height())
        );
    }

    public void viewDidClose() {
        this.resizeParentTableViewIfAny(container);
    }

    /**
     *
     * @param parent
     */
    private void resizeParentTableViewIfAny(NSView parent) {
        while(parent != null) {
            if(parent instanceof NSTableView) {
                NSSelector noteHeightOfRowsWithIndexesChangedSelector
                        = new NSSelector("noteHeightOfRowsWithIndexesChanged", new Class[]{NSIndexSet.class});
                if(noteHeightOfRowsWithIndexesChangedSelector.implementedByClass(NSTableView.class)) {
                    int selected = 0;
                    if(((NSTableView)parent).numberOfSelectedRows() > 0) {
                        selected = ((NSTableView)parent).selectedRow();
                    }
                    ((NSTableView)parent).noteHeightOfRowsWithIndexesChanged(
                            new NSIndexSet(new NSRange(selected, ((NSTableView)parent).numberOfRows() - selected))
                    );
                    return;
                }
                else {
                    //What is the behavior on < 10.4?
                }
            }
            parent = parent.superview();
        }
    }
}
