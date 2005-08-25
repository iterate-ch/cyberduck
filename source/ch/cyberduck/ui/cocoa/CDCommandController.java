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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import java.util.Observable;
import java.util.Observer;

import ch.cyberduck.core.Message;
import ch.cyberduck.core.Session;

/**
 * @version $Id$
 */
public class CDCommandController extends CDWindowController implements Observer {

    private static NSMutableArray instances = new NSMutableArray();

    private NSTextField inputField; //IBOutlet
    private NSTextView responseField; //IBOUtltet

    public void setInputField(NSTextField inputField) {
        this.inputField = inputField;
    }

    public void setResponseField(NSTextView responseField) {
        this.responseField = responseField;
        this.responseField.setEditable(false);
        this.responseField.setSelectable(true);
        this.responseField.setUsesFontPanel(false);
        this.responseField.setRichText(false);
        this.responseField.layoutManager().setDelegate(this);
    }

    private static final NSDictionary FIXED_WITH_FONT_ATTRIBUTES = new NSDictionary(new Object[]{NSFont.userFixedPitchFontOfSize(9.0f)},
            new Object[]{NSAttributedString.FontAttributeName});

    public void layoutManagerDidCompleteLayoutForTextContainer(NSLayoutManager layoutManager,
                                                               NSTextContainer textContainer,
                                                               boolean finished) {
        if (finished && this.responseField.window().isVisible()) {
            this.responseField.scrollRangeToVisible(new NSRange(this.responseField.textStorage().length(), 0));
        }
    }

    public void awakeFromNib() {
        super.awakeFromNib();
        this.window().setReleasedWhenClosed(true);
    }

    private Session session;

    public CDCommandController(Session session) {
        instances.addObject(this);
        this.session = session;
        this.session.addObserver(this);
        if(!NSApplication.loadNibNamed("Command", this)) {
            log.fatal("Couldn't load Command.nib");
        }
    }

    public void windowWillClose(NSNotification notification) {
        session.deleteObserver(this);
        instances.removeObject(this);
    }

    public void sendButtonClicked(NSButton sender) {
        String command = this.inputField.stringValue();
        if (command != null && command.length() > 0) {
            session.sendCommand(command);
        }
    }

    public void update(final Observable o, final Object arg) {
        if (arg instanceof Message) {
            final Message msg = (Message) arg;
            if (msg.getTitle().equals(Message.TRANSCRIPT)) {
                this.invoke(new Runnable() {
                    public void run() {
                        responseField.textStorage().replaceCharactersInRange(new NSRange(responseField.textStorage().length(), 0),
                                new NSAttributedString(msg.getContent() + "\n", FIXED_WITH_FONT_ATTRIBUTES));
                    }
                });
            }
        }
    }

    public void closeButtonClicked(NSButton sender) {
        this.endSheet(this.window(), sender.tag());
    }

    public void sheetDidEnd(NSPanel sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
    }
}