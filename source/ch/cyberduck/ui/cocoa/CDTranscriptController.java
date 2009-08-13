package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.ui.cocoa.application.NSColor;
import ch.cyberduck.ui.cocoa.application.NSFont;
import ch.cyberduck.ui.cocoa.application.NSTextView;
import ch.cyberduck.ui.cocoa.application.NSView;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSRange;

import org.rococoa.cocoa.foundation.NSUInteger;

/**
 * @version $Id$
 */
public class CDTranscriptController extends CDBundleController implements TranscriptListener {

    protected static final NSDictionary FIXED_WITH_FONT_REQUEST_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(
                    NSFont.userFixedPitchFontOfSize(9.0f)
            ),
            NSArray.arrayWithObjects(
                    NSAttributedString.FontAttributeName
            )
    );

    public static NSDictionary FIXED_WITH_FONT_RESPONSE_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(
                    NSFont.userFixedPitchFontOfSize(9.0f),
                    NSColor.darkGrayColor()),
            NSArray.arrayWithObjects(
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName
            )
    );

    @Outlet
    private NSView logView;

    public void setLogView(NSView logView) {
        this.logView = logView;
    }

    public NSView getLogView() {
        return logView;
    }

    @Outlet
    private NSTextView logTextView;

    public void setLogTextView(NSTextView logTextView) {
        this.logTextView = logTextView;
    }

    @Override
    protected String getBundleName() {
        return "Transcript";
    }

    public CDTranscriptController() {
        this.loadBundle();
    }

    public void log(final boolean request, final String transcript) {
        this.write(request ? FIXED_WITH_FONT_REQUEST_ATTRIBUTES : FIXED_WITH_FONT_RESPONSE_ATTRIBUTES, transcript);
    }

    public void write(final NSDictionary font, final String transcript) {
        logTextView.textStorage().appendAttributedString(
                NSAttributedString.attributedStringWithAttributes(transcript + "\n", font)
        );
        logTextView.scrollRangeToVisible(NSRange.NSMakeRange(logTextView.textStorage().length(), new NSUInteger(0)));
    }

    public void clear() {
        logTextView.setString("");
    }
}