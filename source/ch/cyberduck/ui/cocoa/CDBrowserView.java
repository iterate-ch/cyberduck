/* CDBrowserView */

package ch.cyberduck.ui.cocoa;

import java.util.Observer;
import java.util.Observable;

import ch.cyberduck.core.Host;
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
	if(event.clickCount() == 2) { //double click
	    
	}
    }
    
    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }

    public void update(Observable o, Object arg) {
	log.debug("update");
	if(o instanceof Host) {
	    if(arg instanceof java.util.List) {
		java.util.List files = (java.util.List)arg;
		java.util.Iterator i = files.iterator();
		CDBrowserTableDataSource browserTableDataSource = (CDBrowserTableDataSource)this.dataSource();
		browserTableDataSource.clear();
		while(i.hasNext()) {
		    browserTableDataSource.addEntry(i.next());
		    this.reloadData();
		}
	    }
	    //this.reloadData();
	}
    }

    /**	Informs the delegate that aTableView will display the cell at rowIndex in aTableColumn using aCell.
	* The delegate can modify the display attributes of a cell to alter the appearance of the cell.
	* Because aCell is reused for every row in aTableColumn, the delegate must set the display attributes both when drawing special cells and when drawing normal cells.
	*/
    /*
    public void tableViewWillDisplayCell(NSTableView browserTable, Object c, NSTableColumn tableColumn, int row) {
	String identifier = (String)tableColumn.identifier();
	CDBrowserTableDataSource ds = (CDBrowserTableDataSource)this.dataSource();
//@todo throws null pointer fo ds ???
        Path p = (Path)ds.tableViewObjectValueForLocation(browserTable, tableColumn, row);
	NSTextFieldCell cell = (NSTextFieldCell)c;
	cell.setDrawsBackground(true);
	if (row%2 == 0) {
	    cell.setBackgroundColor(NSColor.blueColor());
	}
	if(identifier.equals("TYPE")) {
	    if(p.isFile())
	       cell.setImage(NSImage.imageNamed("file.tiff"));
	    if(p.isDirectory())
		cell.setImage(NSImage.imageNamed("folder.tiff"));
	}
    }
     */

    /**	Returns true to permit aTableView to select the row at rowIndex, false to deny permission.
	* The delegate can implement this method to disallow selection of particular rows.
	*/
    public  boolean tableViewShouldSelectRow( NSTableView aTableView, int rowIndex) {
	return true;
    }

	
    /**	Returns true to permit aTableView to edit the cell at rowIndex in aTableColumn, false to deny permission.
	*The delegate can implemen this method to disallow editing of specific cells.
	*/
    public boolean tableViewShouldEditLocation( NSTableView view, NSTableColumn tableColumn, int row) {
        String identifier = (String)tableColumn.identifier();
	if(identifier.equals("FILENAME"))
	    return true;
	return false;
    }

    public void tableViewSelectionDidChange(NSNotification notification) {
	log.debug("tableViewSelectionDidChange");
	//	NSTableView table = (NSTableView)notification.object(); // Returns the object associated with the receiver. This is often the object that posted this notification
    }
}
