/* CDServerItemView */
package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import ch.cyberduck.core.Host;

import org.apache.log4j.Logger;

public class CDConnectedItemView extends NSView {
    private static Logger log = Logger.getLogger(CDConnectedItemView.class);

//    public NSTextField hostname; /* IBOutlet */
//    public NSTextField username; /* IBOutlet */
//    public NSImageView icon; /* IBOutlet */

    private Host host;

    public CDConnectedItemView(NSCoder decoder, long token) {
	super(decoder, token);
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }
    
    public CDConnectedItemView(Host host) {
	super();
	this.host = host;
//	NSTextField hostnameField = (NSTextField)this.viewWithTag(0);
//	hostnameField.toString();
//	hostnameField.setStringValue(host.getName());
//	hostnameField.toString();
	/*
	log.debug("CDConnectedItemView");
	this.setHostname(host.getName());
	this.setUsername(host.getUsername());
	NSTextField f = new NSTextField();
	f.setStringValue("you are connected to this host.");
	this.addSubview(f);
	 */
    }

    public CDConnectedItemView() {
	super();
	log.debug("CDConnectedItemView");
    }

    public CDConnectedItemView(NSRect rect) {
	super(rect);
	log.debug("CDConnectedItemView(NSRect)");
    }

    public void awakeFromNib() {
	NSView hostnameField = this.viewWithTag(0);
	hostnameField.toString();

    }

//    public void awakeFromNib() {
//	log.debug("CDConnectedItemView:awakeFromNib");

//	NSTextField f = new NSTextField();
//	f.setStringValue("you are connected to this host.");
//	this.addSubview(f);
//    }

    public void setHostname(String hostname) {
	log.debug("CDConnectedItemView:setHostname");
//	this.hostname.setStringValue(hostname);
    }

    public void setIcon(Object icon) {
	log.debug("CDConnectedItemView:setIcon");
    }

    public void setUsername(String username) {
	log.debug("CDConnectedItemView:setUsername");
//	this.username.setStringValue(username);
    }
}
