/* CDLogView */

package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.Observer;
import java.util.Observable;

import ch.cyberduck.core.Status;
import ch.cyberduck.core.Message;

import org.apache.log4j.Logger;

public class CDLogView extends NSScrollView implements Observer {
    
    private static Logger log = Logger.getLogger(CDLogView.class);

    private NSTextView documentView;

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
	this.setDocumentView(documentView = new NSTextView());
	this.setHasVerticalScroller(true);
	this.setHasHorizontalScroller(false);
    }

    public void update(Observable o, Object arg) {
//	log.debug("update");
	if(o instanceof Status) {
	    Message msg = (Message)arg;
	    if(msg.getTitle().equals(Message.TRANSCRIPT)) {
		log.debug("documentView.insertText()");
		documentView.insertText(msg.getDescription());
	    }
	}
    }
}
