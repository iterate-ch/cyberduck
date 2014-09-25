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

import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSButton;
import ch.cyberduck.ui.cocoa.application.NSPanel;
import ch.cyberduck.ui.cocoa.application.NSView;
import ch.cyberduck.ui.cocoa.foundation.NSEnumerator;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSRect;

public abstract class AlertController extends SheetController {

    /**
     * If using alert and no custom window
     */
    protected NSAlert alert;

    public AlertController(final WindowController parent, final NSAlert alert) {
        this(parent, alert, NSAlert.NSWarningAlertStyle);
    }

    public AlertController(final WindowController parent, final NSAlert alert, final int style) {
        super(parent);
        this.alert = alert;
        this.alert.setAlertStyle(style);
        this.alert.setDelegate(this.id());
        this.setWindow(this.alert.window());
    }

    public void setAccessoryView(final NSView view) {
        view.setFrame(new NSRect(alert.window().contentView().frame().size.width.floatValue(),
                view.frame().size.height.floatValue()));
        alert.setAccessoryView(view);
    }

    @Override
    public void beginSheet() {
        super.beginSheet();
        this.focus();
    }

    @Override
    protected void beginSheetImpl() {
        parent.window().makeKeyAndOrderFront(null);
        alert.layout();
        NSEnumerator buttons = alert.buttons().objectEnumerator();
        NSObject button;
        while(((button = buttons.nextObject()) != null)) {
            final NSButton b = Rococoa.cast(button, NSButton.class);
            b.setTarget(this.id());
            b.setAction(Foundation.selector("closeSheet:"));
        }
        alert.beginSheet(parent.window(), this.id(), Foundation.selector("alertDidEnd:returnCode:contextInfo:"), null);
    }

    protected void focus() {
        //
    }

    protected void setTitle(final String title) {
        alert.setMessageText(title);
    }

    protected void setMessage(final String message) {
        alert.setInformativeText(message);
    }

    /**
     * Message the alert sends to modalDelegate after the user responds but before the sheet is dismissed.
     *
     * @param alert       Alert window
     * @param returnCode  Button code
     * @param contextInfo Context
     */
    public void alertDidEnd_returnCode_contextInfo(final NSAlert alert, final int returnCode, final ID contextInfo) {
        this.sheetDidClose_returnCode_contextInfo(alert.window(), returnCode, contextInfo);
    }

    @Override
    protected int getCallbackOption(final NSButton selected) {
        if(selected.tag() == NSPanel.NSAlertDefaultReturn) {
            return SheetCallback.DEFAULT_OPTION;
        }
        else if(selected.tag() == NSPanel.NSAlertAlternateReturn) {
            return SheetCallback.ALTERNATE_OPTION;
        }
        else if(selected.tag() == NSPanel.NSAlertOtherReturn) {
            return SheetCallback.CANCEL_OPTION;
        }
        return SheetCallback.DEFAULT_OPTION;
    }

    /**
     * Open help page.
     */
    protected void help() {
        new DefaultProviderHelpService().help();
    }

    /**
     * When the help button is pressed, the alert delegate (delegate) is first sent a alertShowHelp: message.
     *
     * @param alert Alert window
     * @return True if help request was handled.
     */
    public boolean alertShowHelp(final NSAlert alert) {
        this.help();
        return true;
    }
}