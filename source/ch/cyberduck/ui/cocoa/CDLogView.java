/* CDLogView */

package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.Observer;
import java.util.Observable;

import ch.cyberduck.core.Status;
import ch.cyberduck.core.Message;

import org.apache.log4j.Logger;

public class CDLogView extends NSTextView implements Observer {
    
    private static Logger log = Logger.getLogger(CDLogView.class);

//    private NSTextView documentView;

    public CDLogView() {
	super();
	log.debug("CDLogView");
    }

    public CDLogView(NSRect frameRect) {
	super(frameRect);
	log.debug("CDLogView(NSRect)");
    }

    public void awakeFromNib() {
	log.debug("awakeFromNib");
//	this.setDocumentView(documentView = new NSTextView());
	this.setEditable(false);
	this.setSelectable(true);
//	this.setHasVerticalScroller(true);
//	this.setHasHorizontalScroller(false);
    }

    public void update(Observable o, Object arg) {
//	log.debug("update");
	if(o instanceof Status) {
	    Message msg = (Message)arg;
	    if(msg.getTitle().equals(Message.TRANSCRIPT)) {
                log.debug("update");

		/**
		* Replaces the characters in aRange with aString. For a rich text object, the text of aString is assigned the
		 * formatting attributes of the first character of the text it replaces, or of the character immediately
		 * before aRange if the range's length is 0. If the range's location is 0, the formatting
		 * attributes of the first character in the receiver are used.
*/
		if(msg.getDescription() != null) {
		    this.replaceCharactersInRange(new NSRange(this.string().length(), 0), msg.getDescription());
//		    this.replaceCharactersInRange(new NSRange(0, this.string().length()), msg.getDescription());
//		    documentView.replaceCharactersInRange(new NSRange(documentView.string().length(), 0), msg.getDescription());
//		documentView.scrollRangeToVisible(new NSRange(documentView.string().length()-1, documentView.string().length()-1));
                }
		
	    }
	}
    }
    
        // ----------------------------------------------------------
    // NSCoding
    // ----------------------------------------------------------
    
    public CDLogView(NSCoder decoder, long token) {
	super(decoder, token);
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }

}
