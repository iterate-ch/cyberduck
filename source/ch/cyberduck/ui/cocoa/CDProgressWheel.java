/* CDProgressWheel */

package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.util.Observer;
import java.util.Observable;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Status;
import ch.cyberduck.core.Message;

public class CDProgressWheel extends NSProgressIndicator implements Observer {

    private static Logger log = Logger.getLogger(CDProgressWheel.class);

    public CDProgressWheel() {
	super();
    }
    
    public CDProgressWheel(NSRect frame) {
	super(frame);
    }

    public void update(Observable o, Object arg) {
//	log.debug("update:"+arg);
	if(o instanceof Status) {
	    Message msg = (Message)arg;
	    if(msg.getTitle().equals(Message.ACTIVE)) {
		this.startAnimation(this);
	    }
	    if(msg.getTitle().equals(Message.STOP)) {
		this.stopAnimation(this);
	    }
	}
    }

    public CDProgressWheel(NSCoder decoder, long token) {
	super(decoder, token);
	log.debug("CDProgressWheel");
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }
}
