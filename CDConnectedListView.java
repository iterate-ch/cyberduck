/* CDConnectedListView */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDConnectedListView extends CDListView {
    private static Logger log = Logger.getLogger(CDConnectedListView.class);

    public CDConnectedListView() {
	super();
	log.debug("CDConnectedListView");
    }

    public CDConnectedListView(NSRect frameRect) {
	super(frameRect);
	log.debug("CDConnectedListView");
    }

    public void awakeFromNib() {
	log.debug("***superview of CDConnectedListView:"+this.superview().toString());
	Host h = new Host(Host.SFTP, "host.domain.tld", 22, "dkocher", "topsecret");
	NSView item = new CDConnectedItemView(h);
	this.addSubview(item);
	this.setNeedsDisplay(item.frame());
    }
}
