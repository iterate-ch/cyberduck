/* CDListView */

package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDListView extends NSView {
    private static Logger log = Logger.getLogger(CDListView.class);

    public CDListView() {
	super();
	log.debug("CDListView");
    }

    public CDListView(NSRect frameRect) {
	super(frameRect);
	log.debug("CDListView(NSRect)");
    }

    public void awakeFromNib() {
	log.debug("CDListView:awakeFromNib");
    }    
}
