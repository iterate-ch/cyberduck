/* CDConnectedTableDataSource */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDConnectedTableDataSource extends NSObject {//implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDServerTableDataSource.class);

    private NSMutableArray data;

    public CDConnectedTableDataSource() {
	super();
	this.data = new NSMutableArray();
	log.debug("CDConnectedTableDataSource");
    }

    public void awakeFromNib() {
	log.debug("CDConnectedTableDataSource:awakeFromNib");
	CDServerItemView item = new CDServerItemView();
	item.setHostname("hostname.domain.tld");
	item.setUsername("anonymous");
	this.data.addObject(item);
    }

    public int numberOfRowsInTableView(NSTableView tableView) {
//	log.debug("CDServerTableDataSource:numberOfRowsInTableView");
	return data.count();
    }

    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
	log.debug("CDConnectedTableDataSource:tableViewObjectValueForLocation");
	    
        String identifier = (String)tableColumn.identifier();
	if(identifier.equals("SERVER")) {
	    NSCell cell = new NSCell();
	    cell.setObjectValue(new CDServerItemView());
	    tableColumn.setDataCell(cell);
	    return data.objectAtIndex(row);
	}
	throw new IllegalArgumentException("Unknown identifier: "+identifier);
    }
    
    public void tableViewSetObjectValueForLocation(NSTableView tableView, Object object, NSTableColumn tableColumn, int row) {
	log.debug("CDConnectedTableDataSource:tableViewSetObjectValueForLocation() not implemented.");
    }
}
