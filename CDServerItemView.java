/* CDServerItemView */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDServerItemView extends NSView {
    private static Logger log = Logger.getLogger(CDServerItemView.class);

    public NSTextField hostname = new NSTextField(); /* IBOutlet */
    public NSTextField username = new NSTextField(); /* IBOutlet */
    public NSImageView icon; /* IBOutlet */

    public CDServerItemView() {
	super();
	log.debug("CDServerItemView");
    }

    public CDServerItemView(NSRect rect) {
	super(rect);
	log.debug("CDServerItemView");
    }

    public void awakeFromNib() {
	//
    }
    
    public void setHostname(String hostname) {
	log.debug("CDServerItemView:setHostname");
	this.hostname.setStringValue(hostname);
    }

    public void setIcon(Object icon) {
	log.debug("CDServerItemView:setIcon");
    }

    public void setUsername(String username) {
	log.debug("CDServerItemView:setUsername");
	this.username.setStringValue(username);
    }
}
