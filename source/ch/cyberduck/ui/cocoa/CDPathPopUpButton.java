/* CDPathPopUpButton */

package ch.cyberduck.ui.cocoa;

import org.apache.log4j.Logger;

import java.util.Observer;
import java.util.Observable;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class CDPathPopUpButton extends NSPopUpButton implements Observer {
    private static Logger log = Logger.getLogger(CDPathPopUpButton.class);

    public void update(Observable o, Object arg) {
	//	log.debug("update:"+arg);
	if(o instanceof Host) {
	    if(arg instanceof Path) {
		Path p = (Path)arg;
		this.removeAllItems();
		this.addItem(p.getPath());
		while(!p.isRoot()) {
		    p = p.getParent();
		    this.addItem(p.getPath());
		}
	    }
	}
    }

    public void awakeFromNib() {
	this.removeAllItems();
    }

    public CDPathPopUpButton() {
	super();
	log.debug("CDPathPopUpButton");
    }

    public CDPathPopUpButton(NSRect rect) {
	super(rect);
	log.debug("CDPathPopUpButton");
    }

    public CDPathPopUpButton( NSRect rect, boolean flag) {
	super(rect, flag);
    }	

    public CDPathPopUpButton(NSCoder decoder, long token) {
	super(decoder, token);
	log.debug("CDPathPopUpButton");
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
	log.debug("encodeWithCoder");
    }    
}
