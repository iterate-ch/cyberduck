package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSControl;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.cocoa.NSRect;

public abstract class CDAlertController extends CDSheetController {

    /**
     * If using alert and no custom window
     */
    protected NSAlert alert;

    /**
     * @param parent
     * @param alert
     */
    public CDAlertController(final CDWindowController parent, NSAlert alert) {
        this(parent, alert, NSAlert.NSWarningAlertStyle);
    }

    /**
     * @param parent
     * @param alert
     * @param style
     */
    public CDAlertController(final CDWindowController parent, NSAlert alert, int style) {
        super(parent);
        this.alert = alert;
        this.alert.setAlertStyle(style);
    }

    protected void setAccessoryView(NSControl view) {
        view.sizeToFit();
        view.setFrame(new NSRect(300, view.frame().size.height.doubleValue()));
        alert.setAccessoryView(view);
    }

    public void beginSheet() {
        alert.layout();
        alert.beginSheet(parent.window(), this.id(), Foundation.selector("alertDidEnd:returnCode:contextInfo:"), null);
    }

    protected void callback(int returnCode, ID context) {
        if(DEFAULT_OPTION == returnCode) {
            if(!this.validateInput()) {
//                NSApplication.beep();
                return;
            }
        }
        super.callback(returnCode, context);
    }

    public void alertDidEnd_returnCode_contextInfo(NSAlert alert, int returnCode, ID context) {
        this.callback(returnCode, context);
    }
}