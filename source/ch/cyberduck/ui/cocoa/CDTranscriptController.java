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

import com.apple.cocoa.application.NSTextView;
import com.apple.cocoa.application.NSFont;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSRange;
import com.apple.cocoa.foundation.NSDictionary;

/**
 * @version $Id:$
 */
public class CDTranscriptController extends CDBundleController {

    protected static final NSDictionary FIXED_WITH_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{NSFont.userFixedPitchFontOfSize(9.0f)},
            new Object[]{NSAttributedString.FontAttributeName}
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

    public void write(final String transcript) {
        logTextView.textStorage().appendAttributedString(
                new NSAttributedString(transcript+"\n", FIXED_WITH_FONT_ATTRIBUTES)
        );
        logTextView.scrollRangeToVisible(
                new NSRange(logTextView.textStorage().length(), 0));
    }

    public void clear() {
        logTextView.textStorage().setAttributedString(
                new NSAttributedString("", FIXED_WITH_FONT_ATTRIBUTES)
        );
    }
}