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

import com.apple.cocoa.application.NSFont;
import com.apple.cocoa.application.NSTextView;
import com.apple.cocoa.foundation.NSRange;

import ch.cyberduck.core.Transcript;

/**
 * @version $Id$
 */
public class CDTranscriptImpl implements Transcript {

    private NSTextView textView;

    public CDTranscriptImpl(NSTextView textView) {
        this.textView = textView;
    }

    public void awakeFromNib() {
        this.textView.setEditable(true);
        this.textView.setSelectable(true);
		this.textView.setUsesFontPanel(false);
		this.textView.setRichText(false);
    }

    public void log(final String message) {
//        ThreadUtilities.instance().invokeLater(new Runnable() {
//            public void run() {
// Replaces the characters in aRange with aString. For a rich text object, the text of aString is assigned the
// formatting attributes of the first character of the text it replaces, or of the character immediately
// before aRange if the range's length is 0. If the range's location is 0, the formatting
// attributes of the first character in the receiver are used.
		textView.textStorage().replaceCharactersInRange(new NSRange(textView.textStorage().length(), 0), 
														message + "\n"); // @warning very bad performance
		textView.setFont(NSFont.userFixedPitchFontOfSize(9.0f));
		//textView.scrollRangeToVisible(new NSRange(textView.textStorage().length()-1, 0));
	}
}
