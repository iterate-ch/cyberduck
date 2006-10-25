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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

/**
 * @version $Id$
 */
public class CDCommandController extends CDSheetController implements TranscriptListener {

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

    public void layoutManagerDidCompleteLayoutForTextContainer(NSLayoutManager layoutManager,
                                                               NSTextContainer textContainer,
                                                               boolean finished) {
        if (finished && this.responseField.window().isVisible()) {
            this.responseField.scrollRangeToVisible(new NSRange(this.responseField.textStorage().length(), 0));
        }
    }

    private Session session;

    public CDCommandController(final CDWindowController parent, final Session session) {
        super(parent);
        this.session = session;
        this.session.addTranscriptListener(this);
        synchronized(parent) {
            if (!NSApplication.loadNibNamed("Command", this)) {
                log.fatal("Couldn't load Command.nib");
            }
        }
    }

    public void sendButtonClicked(final Object sender) {
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

    protected boolean validateInput() {
        return true;
    }

    public void callback(final int returncode) {
        ;
    }

    protected void invalidate() {
        session.removeTranscriptListener(this);
        super.invalidate();
    }
}