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

import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSFont;
import com.apple.cocoa.application.NSLayoutManager;
import com.apple.cocoa.application.NSPanel;
import com.apple.cocoa.application.NSTextContainer;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.application.NSTextView;
import com.apple.cocoa.foundation.*;

import java.util.Observable;
import java.util.Observer;

/**
 * @version $Id$
 */
public class CDCommandController extends CDWindowController implements TranscriptListener {

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

    private Session session;

    public CDCommandController(Session session) {
        this.session = session;
        this.session.addTranscriptListener(this);
        if (!NSApplication.loadNibNamed("Command", this)) {
            log.fatal("Couldn't load Command.nib");
        }
    }

    public void sendButtonClicked(NSButton sender) {
        String command = this.inputField.stringValue();
        if (command != null && command.length() > 0) {
            session.sendCommand(command);
        }
    }

    public void log(final String message) {
        this.invoke(new Runnable() {
            public void run() {
                responseField.textStorage().replaceCharactersInRange(new NSRange(responseField.textStorage().length(), 0),
                        new NSAttributedString(message + "\n", FIXED_WITH_FONT_ATTRIBUTES));
            }
        });
    }

    public void closeButtonClicked(NSButton sender) {
        this.endSheet(this.window(), sender.tag());
    }

    public void sheetDidEnd(NSPanel sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
        session.removeTranscriptListener(this);
        this.release();
    }
}