/* CDBrowserView */

package ch.cyberduck.ui.cocoa;

import java.util.Observer;
import java.util.Observable;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;
    
public class CDBrowserView extends NSTableView implements Observer, NSDraggingDestination {
    private static Logger log = Logger.getLogger(CDBrowserView.class);

//    public Object transferController;
    
    public CDBrowserView() {
	super();
    }

    public CDBrowserView(NSRect frame) {
	super(frame);
    }

    public CDBrowserView(NSCoder decoder, long token) {
	super(decoder, token);
    }
    
    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }
    
    public void awakeFromNib() {
	this.setDelegate(this);
	this.setTarget(this);
	this.setAutoresizesAllColumnsToFit(true);
	//By setting the drop row to -1, the entire table is highlighted instead of just a single row.
	this.setDropRowAndDropOperation(-1, NSTableView.DropOn);

	//	this.setIndicatorImage(NSImage.imageNamed("file.tiff"), this.tableColumnWithIdentifier("FILENAME"))

    }

    public void mouseUp(NSEvent event) {
	log.debug(event.toString());
	if(event.clickCount() == 2) { //double click
	    
	}
    }

    public Object dataSource() {
	return super.dataSource();
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

    public void sort(final String columnIdentifier, final boolean ascending) {
	final int higher;
	final int lower;
	if(ascending) {
	    higher = 1;
	    lower = -1;
	}
	else {
	    higher = -1;
	    lower = 1;
	}
	if(columnIdentifier.equals("FILENAME")) {
	    Collections.sort((List)this.dataSource(),
		      new Comparator() {
			  public int compare(Object o1, Object o2) {
			      Path p1 = (Path) o1;
			      Path p2 = (Path) o2;
			      if(ascending) {
				  return p1.getName().compareTo(p2.getName());
			      }
			      else {
				  return -p1.getName().compareTo(p2.getName());
			      }
			  }
		      }
		      );
	}
    }


    // ----------------------------------------------------------
    // NSDraggingDestination interface methods
    // ----------------------------------------------------------

    /**
	* Invoked when a dragged image enters the destination. Specifically, this method is invoked when the mouse
     * pointer enters the destination's bounds rectangle (if it is a view object) or its frame
     * rectangle (if it is a window object).
     */
    public int draggingEntered(NSDraggingInfo sender) {
	log.debug("draggingEntered");
	return NSDraggingInfo.DragOperationCopy;
    }

    public int draggingUpdated(NSDraggingInfo sender) {
	log.debug("draggingUpdated");
	return NSDraggingInfo.DragOperationCopy;
    }

    public void draggingEnded(NSDraggingInfo sender) {
	log.debug("draggingEnded");
	//
    }

    public void draggingExited(NSDraggingInfo sender) {
	log.debug("draggingExited");
	//
    }

    /**
	* Invoked when the image is released, if the most recent draggingEntered or draggingUpdated message
     * returned an acceptable drag-operation value. Returns true if the receiver agrees to perform the drag operation
     * and false if not. Use sender to obtain details about the dragging operation.
     */
    public boolean prepareForDragOperation(NSDraggingInfo sender)  {
	log.debug("prepareForDragOperation");
	NSPasteboard pasteboard = sender.draggingPasteboard();
	if(NSPasteboard.FileContentsPboardType.equals(pasteboard.availableTypeFromArray(new NSArray(NSPasteboard.FileContentsPboardType))))
	    return true;
	return false;
    }

    /**
	* Invoked after the released image has been removed from the screen and the previous prepareForDragOperation message
     * has returned true. The destination should implement this method to do the real work of importing the pasteboard data
     * represented by the image. If the destination accepts the data, it returns true; otherwise it returns false. The default is
     * to return false. Use sender to obtain details about the dragging operation.
     */
    public boolean performDragOperation(NSDraggingInfo sender) {
	log.debug("performDragOperation");
	NSPasteboard pasteboard = sender.draggingPasteboard();
//	NSData data = pasteboard.dataForType(NSPasteboard.FileContentsPboardType);
//	if(data == null) {
//	    // error sheet
//	}
	NSFileWrapper wrapper = pasteboard.readFileWrapper();
	String file = wrapper.filename();
	log.debug("performDragOperation:"+file);
//	transferController.upload(file);

	
//	if(wrapper.isRegularFile())
//	if(wrapper.isDirectory())
	
	return true;
    }

    /**
	* Invoked when the dragging operation is complete and the previous performDragOperation returned true. The destination
     * implements this method to perform any tidying up that it needs to do, such as updating its visual representation
     * now that it has incorporated the dragged data. This message is the last message sent from sender to the destination
     * during a dragging session.
     */
    public void concludeDragOperaton(NSDraggingInfo sender) {
	log.debug("concludeDragOperaton");
	//
    }
}