/* CDConnectedListView */

package ch.cyberduck.ui.cocoa;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Session;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDConnectedListView extends CDListView {

    private static Logger log = Logger.getLogger(CDConnectedListView.class);

    public CDConnectedListView() {
	super();
	log.debug("CDConnectedListView");
	Host h = new Host(Session.SFTP, "host.domain.tld", 22, "dkocher", "topsecret");
	NSView item = new CDConnectedItemView(h);
	this.addSubview(item);
	this.setNeedsDisplay(item.frame());
    }

    public CDConnectedListView(NSRect frameRect) {
	super(frameRect);
	log.debug("CDConnectedListView(NSRect)");
    }

    public void awakeFromNib() {
	log.debug("CDConnectedListView:awakeFromNib");
	Host h = new Host(Session.SFTP, "host.domain.tld", 22, "dkocher", "topsecret");
	NSView item = new CDConnectedItemView(h);
 	this.addSubview(item);
	this.setNeedsDisplay(item.frame());
    }
}