/* CDBrowserView */

package ch.cyberduck.ui.cocoa;

import java.util.Observer;
import java.util.Observable;

import ch.cyberduck.core.Path;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;
    
public class CDBrowserView extends NSTableView implements Observer {
    private static Logger log = Logger.getLogger(CDBrowserView.class);

    public CDBrowserView(NSCoder decoder, long token) {
	super(decoder, token);
    }

    public CDBrowserView() {
	super();
    }

    public CDBrowserView(NSRect frame) {
	super(frame);
    }

    public void awakeFromNib() {
	this.setTarget(this);
    }

    public void mouseUp(NSEvent event) {
	log.debug(event.toString());
	if(event.clickCount() == 2) {
	    // Double click
	    log.debug("I got double click!");
	}
    }
    
    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }

    public void update(Observable o, Object arg) {
	log.debug("update");
//	if(o instanceof Path) {
	    java.util.List files = (java.util.List)arg;
	    java.util.Iterator i = files.iterator();
	    CDBrowserTableDataSource browserTableDataSource = (CDBrowserTableDataSource)this.dataSource();
	    browserTableDataSource.clear();
	    while(i.hasNext()) {
		browserTableDataSource.addEntry(i.next());
		this.reloadData();
	    }
	    //this.reloadData();
//	}
    }
}
