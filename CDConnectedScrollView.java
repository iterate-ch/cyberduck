/* CDConnectedScrollView */
package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDConnectedScrollView extends NSScrollView {
    private static Logger log = Logger.getLogger(CDConnectedListView.class);

    public CDConnectedScrollView() {
	super();
	log.debug("CDConnectedScrollView");
    }

    public CDConnectedScrollView(NSRect frameRect) {
	super(frameRect);
	log.debug("CDConnectedScrollView(NSRect)");
    }

    public void awakeFromNib() {
	log.debug("CDConnectedScrollView:awakeFromNib");
	this.setDocumentView(new CDConnectedListView());
    }
}
