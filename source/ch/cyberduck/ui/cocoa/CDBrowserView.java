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

    public CDBrowserView() {
	super();
    }

    public CDBrowserView(NSRect frame) {
	super(frame);
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
