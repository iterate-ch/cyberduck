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

import com.apple.cocoa.application.NSFont;
import com.apple.cocoa.application.NSLayoutManager;
import com.apple.cocoa.application.NSTextContainer;
import com.apple.cocoa.application.NSTextView;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSRange;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Message;

/**
 * @version $Id$
 */
public class CDTranscriptImpl extends CDController implements Observer {
	private static Logger log = Logger.getLogger(CDTranscriptImpl.class);

	private NSTextView textView;

	public CDTranscriptImpl(NSTextView textView) {
		this.textView = textView;
        super.awakeFromNib();
	}

	private static final NSDictionary FIXED_WITH_FONT_ATTRIBUTES = new NSDictionary(new Object[]{NSFont.userFixedPitchFontOfSize(9.0f)},
	    new Object[]{NSAttributedString.FontAttributeName});

	public void layoutManagerDidCompleteLayoutForTextContainer(NSLayoutManager layoutManager,
	                                                           NSTextContainer textContainer,
	                                                           boolean finished) {
		if(finished && this.textView.window().isVisible()) {
			this.textView.scrollRangeToVisible(new NSRange(this.textView.textStorage().length(), 0));
		}
	}

    public void update(final Observable o, final Object arg) {
        if (arg instanceof Message) {
            final Message msg = (Message) arg;
            if (msg.getTitle().equals(Message.TRANSCRIPT)) {
                log.info(msg.getContent());
                this.invoke(new Runnable() {
                    public void run() {
                        // textView.layoutManager().setDelegate(CDTranscriptImpl.this);

                        // Replaces the characters in aRange with aString. For a rich text object, the text of aString is assigned the
                        // formatting attributes of the first character of the text it replaces, or of the character immediately
                        // before aRange if the range's length is 0. If the range's location is 0, the formatting
                        // attributes of the first character in the receiver are used.
                        textView.textStorage().replaceCharactersInRange(new NSRange(textView.textStorage().length(), 0),
                                new NSAttributedString(msg.getContent()+"\n", FIXED_WITH_FONT_ATTRIBUTES));
                    }
                });
            }
        }
	}
}