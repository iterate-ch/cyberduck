/* CDLoginSheet */

package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDLoginSheet extends NSWindow {
    private static Logger log = Logger.getLogger(CDLoginSheet.class);

    public NSTextField userField;
    public NSSecureTextField passField;

    public CDLoginSheet() {
	super();
    }

    public CDLoginSheet(NSRect contentRect, int styleMask, int backingType, boolean defer) {
	super(contentRect, styleMask, backingType, defer);
    }

    public CDLoginSheet(NSRect contentRect, int styleMask, int bufferingType, boolean defer, NSScreen aScreen) {
	super(contentRect, styleMask, bufferingType, defer, aScreen);
    }
    
    public void awakeFromNib() {
	//
    }

    public void closeSheet(NSObject sender) {
	log.debug("closeSheet");
	// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
	NSButton button = (NSButton)sender;
	if(button.title().equals("Login")) {
	    NSApplication.sharedApplication().endSheet(this, NSAlertPanel.DefaultReturn);
	}
	else
	    NSApplication.sharedApplication().endSheet(this, NSAlertPanel.AlternateReturn);
    }

    public String getUser() {
	log.debug("getUser");
	return userField.stringValue();
    }

    public String getPass() {
	log.debug("getPass");
	return passField.stringValue();
    }
}
