/* CDFavoriteTableDataSource */

package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDFavoriteTableDataSource extends NSObject {
    private static Logger log = Logger.getLogger(CDFavoriteTableDataSource.class);

    private NSMutableArray data;

    public CDFavoriteTableDataSource() {
	super();
	this.data = new NSMutableArray();
	log.debug("CDFavoriteTableDataSource");
    }

    public void awakeFromNib() {
	log.debug("CDFavoriteTableDataSource:awakeFromNib");
    }

    public int numberOfRowsInTableView(NSTableView tableView) {
//	log.debug("CDFavoriteTableDataSource:numberOfRowsInTableView");
	return data.count();
    }

    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
	log.debug("CDFavoriteTableDataSource:tableViewObjectValueForLocation");
        String identifier = (String)tableColumn.identifier();
	throw new IllegalArgumentException("Unknown identifier: "+identifier);
    }

    public void tableViewSetObjectValueForLocation(NSTableView tableView, Object object, NSTableColumn tableColumn, int row) {
	log.debug("CDFavoriteTableDataSource:tableViewSetObjectValueForLocation() not implemented.");
    }
}
