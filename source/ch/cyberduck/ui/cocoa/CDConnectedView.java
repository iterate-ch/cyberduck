/* CDConnectedView */

package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class CDConnectedView extends NSTableView {

    public CDConnectedView(NSCoder decoder, long token) {
	super(decoder, token);
    }
    
    public CDConnectedView() {
	super();
    }

    public CDConnectedView(NSRect frame) {
	super(frame);
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }
    
}
