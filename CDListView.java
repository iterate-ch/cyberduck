/* CDListView */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDListView extends NSView {
    private static Logger log = Logger.getLogger(CDListView.class);

    public CDListView() {
	super();
    }

    public CDListView(NSRect frameRect) {
	super(frameRect);
    }

    public void awakeFromNib() {
	log.debug("***superview of CDListView:"+this.superview().toString());
    }    
}
