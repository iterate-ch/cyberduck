/* CDPathPopUpButton */

package ch.cyberduck.ui.cocoa;

import org.apache.log4j.Logger;

import java.util.Observer;
import java.util.Observable;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class CDPathPopUpButton extends NSPopUpButton implements Observer {
    private static Logger log = Logger.getLogger(CDPathPopUpButton.class);

    public void update(Observable o, Object arg) {
	//	log.debug("update:"+arg);
	if(o instanceof Host) {
	    Message msg = (Message)arg;
	    if(msg.getTitle().equals(Message.PROGRESS)) {
		this.setStringValue(msg.getDescription());
	    }
	}
    }
    
    public CDPathPopUpButton() {
	super();
	log.debug("CDPathPopUpButton");
    }

    public CDPathPopUpButton(NSRect frame) {
	super(frame);
	log.debug("CDPathPopUpButton");
    }

    public CDPathPopUpButton(NSCoder decoder, long token) {
	super(decoder, token);
	log.debug("CDPathPopUpButton");
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
	log.debug("encodeWithCoder");
    }    
}
