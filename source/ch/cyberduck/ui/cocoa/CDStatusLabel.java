/* CDStatusLabel */
package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

import java.util.Observer;
import java.util.Observable;

import ch.cyberduck.core.Status;
import ch.cyberduck.core.Message;

public class CDStatusLabel extends NSTextField implements Observer {
    private static Logger log = Logger.getLogger(CDStatusLabel.class);

    public void update(Observable o, Object arg) {
//	log.debug("update:"+arg);
	if(o instanceof Status) {
	    Message msg = (Message)arg;
	    if(msg.getTitle().equals(Message.PROGRESS)) {
		this.setStringValue(msg.getDescription());
	    }
	}
    }
    
    public void awakeFromNib() {
	log.debug("awakeFromNib");
	this.setStringValue("Idle");
    }
    
    public CDStatusLabel() {
	super();
	log.debug("CDStatusLabel");
    }

    public CDStatusLabel(NSCoder decoder, long token) {
	super(decoder, token);
	log.debug("CDStatusLabel");
    }
    
    public CDStatusLabel(NSRect frameRect) {
	super(frameRect);
	log.debug("CDStatusLabel");
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
	log.debug("encodeWithCoder");
    }
}
