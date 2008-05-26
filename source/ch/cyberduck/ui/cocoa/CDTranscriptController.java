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

import com.apple.cocoa.application.NSFont;
import com.apple.cocoa.application.NSTextView;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.application.NSColor;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSRange;

import ch.cyberduck.core.TranscriptListener;

/**
 * @version $Id:$
 */
public class CDTranscriptController extends CDBundleController implements TranscriptListener {

    protected static final NSDictionary FIXED_WITH_FONT_REQUEST_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.userFixedPitchFontOfSize(9.0f)
            },
            new Object[]{
                    NSAttributedString.FontAttributeName
            }
    );

    public static NSDictionary FIXED_WITH_FONT_RESPONSE_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.userFixedPitchFontOfSize(9.0f),
                    NSColor.darkGrayColor()},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName
            }
    );

    private NSView logView;

    public void setLogView(NSView logView) {
        this.logView = logView;
    }

    public NSView getLogView() {
        return logView;
    }

    private NSTextView logTextView;

    public void setLogTextView(NSTextView logTextView) {
        this.logTextView = logTextView;
    }

    protected String getBundleName() {
        return "Transcript";
    }

    public CDTranscriptController() {
        this.loadBundle();
    }

    protected void awakeFromNib() {
        ;
    }

    public void log(final boolean request, final String transcript) {
        this.write(request ? FIXED_WITH_FONT_REQUEST_ATTRIBUTES : FIXED_WITH_FONT_RESPONSE_ATTRIBUTES, transcript);
    }

    public void write(final NSDictionary font, final String transcript) {
        logTextView.textStorage().appendAttributedString(
                new NSAttributedString(transcript+"\n", font)
        );
        logTextView.scrollRangeToVisible(
                new NSRange(logTextView.textStorage().length(), 0));
    }

    public void clear() {
        logTextView.setString("");
    }
}