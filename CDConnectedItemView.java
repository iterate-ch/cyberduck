/* CDServerItemView */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDConnectedItemView extends NSView {
    private static Logger log = Logger.getLogger(CDConnectedItemView.class);

    public NSTextField hostname; /* IBOutlet */
    public NSTextField username; /* IBOutlet */
    public NSImageView icon; /* IBOutlet */

    public CDConnectedItemView(Host host) {
	super();
	//this.setHostname(host.getHostname());
	//this.setUsername(host.getUsername());
	log.debug("CDConnectedItemView");
    }

    public CDConnectedItemView() {
	super();
	log.debug("CDConnectedItemView");
    }

    public CDConnectedItemView(NSRect rect) {
	super(rect);
	log.debug("CDConnectedItemView");
    }

    public void awakeFromNib() {
	log.debug("CDConnectedItemView:awakeFromNib");
    }

    public void setHostname(String hostname) {
	log.debug("CDConnectedItemView:setHostname");
	this.hostname.setStringValue(hostname);
    }

    public void setIcon(Object icon) {
	log.debug("CDConnectedItemView:setIcon");
    }

    public void setUsername(String username) {
	log.debug("CDConnectedItemView:setUsername");
	this.username.setStringValue(username);
    }
}
