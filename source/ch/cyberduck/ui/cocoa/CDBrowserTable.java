package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import org.apache.log4j.Logger;
import java.util.*;
import ch.cyberduck.core.*;


public class CDBrowserTable extends NSTableView {
    private static Logger log = Logger.getLogger(CDBrowserTable.class);
    
    private static final NSColor TABLE_CELL_SHADED_COLOR = NSColor.colorWithCalibratedRGB(0.929f, 0.953f, 0.996f, 1.0f);

//    private static NSMutableArray allDocuments = new NSMutableArray();

    private CDBrowserTableDataSource browserModel;
    private CDBrowserTableDelegate browserDelegate;

    public CDBrowserTable() {
	super();
	log.debug("CDBrowserTable");
    }

    public CDBrowserTable(NSRect frame) {
	super(frame);
	log.debug("CDBrowserTable:"+frame);
    }

    public CDBrowserTable(NSCoder decoder, long token) {
	super(decoder, token);
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }    

    public void awakeFromNib() {
	log.debug("awakeFromNib");
//	allDocuments.addObject(this);
	this.setDataSource(this.browserModel = new CDBrowserTableDataSource());
	this.setDelegate(this.browserDelegate = new CDBrowserTableDelegate());
	this.setDrawsGrid(false);
	this.setAutoresizesAllColumnsToFit(true);
	this.setAutosaveTableColumns(true);
	this.tableColumnWithIdentifier("TYPE").setDataCell(new NSImageCell());
	this.registerForDraggedTypes(new NSArray(NSPasteboard.FilenamesPboardType));
    }

    public void keyUp(NSEvent e) {
	log.debug(e);
    }


        // ----------------------------------------------------------
    // Drag methods
    // ----------------------------------------------------------

    public void startedDraggingImage(NSImage image, NSPoint point) {
	log.debug("startedDraggingImage:"+point);
    }

    public void finishedDraggingImage(NSImage image, NSPoint point, boolean accepted) {
	log.debug("finishedDraggingImage:"+accepted);
    }

    public void finishedDraggingImage(NSImage image, NSPoint point, int operation) {
	log.debug("finishedDraggingImage:"+operation);
	Path p = (Path)browserModel.getEntry(this.selectedRow());
	p.setLocal(new java.io.File(dropDestination.getPath(), p.getName()));
	CDTransferController controller = new CDTransferController(p, Queue.KIND_DOWNLOAD);
	controller.transfer(p.status.isResume());
    }
    
    public boolean ignoreModifierKeysWhileDragging() {
//	log.debug("ignoreModifierKeysWhileDragging");
	return true;
    }

    public boolean verticalMotionCanBeginDrag() {
	log.debug("verticalMotionCanBeginDrag");
	return true;
    }

    public NSImage dragImageForRows(NSArray dragRows, NSEvent dragEvent, NSMutablePoint dragImageOffset) {
	log.debug("dragImageForRows:"+dragRows);
	if(dragRows.count() == 1) {
	    Path p = (Path)browserModel.getEntry(((Integer)dragRows.objectAtIndex(0)).intValue());
	    return NSWorkspace.sharedWorkspace().iconForFileType(p.getExtension());
	}
	return null;
    }
    
    public int draggingSourceOperationMaskForLocal(boolean local) {
	log.debug("draggingSourceOperationMaskForLocal:"+local);
	if(local)
	    return NSDraggingInfo.DragOperationNone;
	else
	    return NSDraggingInfo.DragOperationMove | NSDraggingInfo.DragOperationCopy;

    }

//    public void mouseDragged(NSEvent event) {
//	super.mouseDragged(event);
//	log.debug("mouseDragged:"+event);
//	if(selectedRow() != -1) {
//	    Path p = (Path)browserModel.getEntry(selectedRow());
//	    NSPoint dragPosition = this.convertPointFromView(event.locationInWindow(), null);
//	    NSRect imageRect = new NSRect(new NSPoint(dragPosition.x()-16, dragPosition.y()-16), new NSSize(32, 32));
//	    this.dragPromisedFilesOfTypes(new NSArray(p.getExtension()), imageRect, this, true, event);
//	}
//    }

    /**
	* Implemented by the owner (previously declared in a declareTypes message) to provide promised data.
	    * The owner receives a pasteboardProvideDataForType message from the sender pasteboard when the data is
	    * required for a paste operation; type gives the type of data being requested. The requested data should
	    * be written to sender using the setDataForType, setPropertyListForType, or setStringForType methods.
	    */
    public void pasteboardProvideDataForType(NSPasteboard pboard, String type) {
	log.debug("pasteboardProvideDataForType:"+type);
	if(type.equals(NSPasteboard.FilesPromisePboardType)) {
	    Path p = (Path)browserModel.getEntry(selectedRow());
	    pboard.setStringForType(p.getName(), NSPasteboard.FilesPromisePboardType);
	}
    }

    private java.net.URL dropDestination;

    /**
	@return the names (not full paths) of the files that the receiver promises to create at dropDestination.
     * This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
     * Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
     * you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
     * blocking the destination application.
     */
    public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
	log.debug("namesOfPromisedFilesDroppedAtDestination:"+dropDestination);
	this.dropDestination = dropDestination;
	Path p = (Path)browserModel.getEntry(this.selectedRow());

	log.debug("namesOfPromisedFilesDroppedAtDestination:return:"+p.getName());
	return new NSArray(new String[]{p.getName()});
    }

    
    /**
     *  Invoked after the released image has been removed from the screen and the previous prepareForDragOperation
     * message has returned true. draggingInfo contains information about the dragging operation. This method
     * should do the real work of importing the pasteboard data represented by the image. If the receiver
     * accepts the data, returns true, otherwise returns false.
     */
    public boolean performDragOperation(NSDraggingInfo draggingInfo) {
	log.debug("performDragOperation:"+draggingInfo);
	NSPasteboard pboard = draggingInfo.draggingPasteboard();
	if(pboard.types().containsObject(NSPasteboard.FilesPromisePboardType)) {
	    NSArray filenames = draggingInfo.namesOfPromisedFilesDroppedAtDestination(dropDestination);
	    log.debug(filenames);

//	    Path p = (Path)this.getEntry(((Integer)rows.objectAtIndex(this.selectedRow())).intValue());
//	    p.setLocal(new File(dropDestination.getPath(), p.getName()));
//	    CDTransferController controller = new CDTransferController(host, p, Queue.KIND_DOWNLOAD);
//	    controller.transfer(p.status.isResume());

	    return true;
	}
	return false;
    }
    
        // ----------------------------------------------------------
    // BrowserTable delegate methods
    // ----------------------------------------------------------

    private class CDBrowserTableDelegate {

	boolean sortAscending = true;
	NSTableColumn lastClickedColumn;

	public void tableViewDidClickTableColumn(NSTableView tableView, NSTableColumn tableColumn) {
	    log.debug("tableViewDidClickTableColumn");
	    if(lastClickedColumn == tableColumn) {
		sortAscending = !sortAscending;
	    }
	    else {
		if(lastClickedColumn != null)
		    tableView.setIndicatorImage(null, lastClickedColumn);
		lastClickedColumn = tableColumn;
		tableView.setHighlightedTableColumn(tableColumn);
	    }

	    tableView.setIndicatorImage(sortAscending ? NSImage.imageNamed("NSAscendingSortIndicator") : NSImage.imageNamed("NSDescendingSortIndicator"), tableColumn);

	    final int higher = sortAscending ? 1 : -1 ;
	    final int lower = sortAscending ? -1 : 1;
	    final boolean ascending = sortAscending;
	    if(tableColumn.identifier().equals("TYPE")) {
		Collections.sort(browserModel.list(),
		   new Comparator() {
		       public int compare(Object o1, Object o2) {
			   Path p1 = (Path) o1;
			   Path p2 = (Path) o2;
			   if(p1.isDirectory() && p2.isDirectory())
			       return 0;
			   if(p1.isFile() && p2.isFile())
			       return 0;
			   if(p1.isFile())
			       return higher;
			   return lower;
		       }
		   }
		   );
	    }
	    else if(tableColumn.identifier().equals("FILENAME")) {
		Collections.sort(browserModel.list(),
		   new Comparator() {
		       public int compare(Object o1, Object o2) {
			   Path p1 = (Path)o1;
			   Path p2 = (Path)o2;
			   if(ascending)
			       return p1.getName().compareToIgnoreCase(p2.getName());
			   else
			       return -p1.getName().compareToIgnoreCase(p2.getName());
		       }
		   }
		   );
	    }
	    else if(tableColumn.identifier().equals("SIZE")) {
		Collections.sort(browserModel.list(),
		   new Comparator() {
		       public int compare(Object o1, Object o2) {
			   int p1 = ((Path)o1).status.getSize();
			   int p2 = ((Path)o2).status.getSize();
			   if (p1 > p2)
			       return lower;
			   else if (p1 < p2)
			       return higher;
			   else if (p1 == p2)
			       return 0;
			   return 0;
		       }
		   }
		   );
	    }
	    else if(tableColumn.identifier().equals("MODIFIED")) {
		Collections.sort(browserModel.list(),
		   new Comparator() {
		       public int compare(Object o1, Object o2) {
			   Path p1 = (Path) o1;
			   Path p2 = (Path) o2;
			   if(ascending)
			       return p1.attributes.getModifiedDate().compareTo(p2.attributes.getModifiedDate());
			   else
			       return -p1.attributes.getModifiedDate().compareTo(p2.attributes.getModifiedDate());
		       }
		   }
		   );
	    }
	    else if(tableColumn.identifier().equals("OWNER")) {
		Collections.sort(browserModel.list(),
		   new Comparator() {
		       public int compare(Object o1, Object o2) {
			   Path p1 = (Path) o1;
			   Path p2 = (Path) o2;
			   if(ascending)
			       return p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
			   else
			       return -p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
		       }
		   }
		   );
	    }
	    reloadData();
	}

	public void tableViewWillDisplayCell(NSTableView view, Object cell, NSTableColumn column, int row) {
	    if(cell instanceof NSTextFieldCell) {
		if (! (view == null || cell == null || column == null)) {
		    if (row % 2 == 0) {
			((NSTextFieldCell)cell).setDrawsBackground(true);
			((NSTextFieldCell)cell).setBackgroundColor(TABLE_CELL_SHADED_COLOR);
		    }
		    else
			((NSTextFieldCell)cell).setBackgroundColor(view.backgroundColor());
		}
	    }
	}

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
	    return false;
	}
    }

        // ----------------------------------------------------------
    // Model
    // ----------------------------------------------------------

    class CDBrowserTableDataSource {//implements NSTableView.DataSource {
	private List data;

	public CDBrowserTableDataSource() {
	    super();
	    this.data = new ArrayList();
	    log.debug("CDBrowserTableDataSource");
	}

	public int numberOfRowsInTableView(NSTableView tableView) {
	    return data.size();
	}
	
    //getValue()
	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
//	log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
	    String identifier = (String)tableColumn.identifier();
	    Path p = (Path)data.get(row);
	    if(identifier.equals("TYPE")) {
		if(p.isDirectory())
		    return NSImage.imageNamed("folder.tiff");
		return NSWorkspace.sharedWorkspace().iconForFileType(p.getExtension());
	    }
	    if(identifier.equals("FILENAME"))
		return p.getName();
	    else if(identifier.equals("SIZE"))
		return p.status.getSizeAsString();
	    else if(identifier.equals("MODIFIED"))
		return p.attributes.getModified();
	    else if(identifier.equals("OWNER"))
		return p.attributes.getOwner();
	    else if(identifier.equals("PERMISSION"))
		return p.attributes.getPermission().toString();
	    throw new IllegalArgumentException("Unknown identifier: "+identifier);
	}
	
    //setValue()
	public void tableViewSetObjectValueForLocation(NSTableView tableView, Object value, NSTableColumn tableColumn, int row) {
	    log.debug("tableViewSetObjectValueForLocation:"+row);
	    Path p = (Path)data.get(row);
	    p.rename((String)value);
	}

	
    // ----------------------------------------------------------
    // Drop methods
    // ----------------------------------------------------------

	/**
	    * Used by tableView to determine a valid drop target. info contains details on this dragging
	 * operation. The proposed
	 * location is row is and action is operation. Based on the mouse position, the table view
	 * will suggest a proposed drop location.
	 * This method must return a value that indicates which dragging operation the data source will
	 * perform. The data source may
	 * "retarget" a drop if desired by calling setDropRowAndDropOperation and returning something other than
	 * NSDraggingInfo.
	 * DragOperationNone. One may choose to retarget for various reasons (e.g. for better visual
								       * feedback when inserting into a sorted position).
	 */
	public int tableViewValidateDrop( NSTableView tableView, NSDraggingInfo info, int row, int operation) {
	    log.debug("tableViewValidateDrop");
	    tableView.setDropRowAndDropOperation(-1, NSTableView.DropOn);
	//tableView.setDropRowAndDropOperation(tableView.numberOfRows(), NSTableView.DropAbove);
//	    return NSTableView.DropOn;
	    return NSTableView.DropAbove;
	}

	/**
	    * Invoked by tableView when the mouse button is released over a table view that previously decided to allow a drop.
	 * @param info contains details on this dragging operation.
	 * @param row The proposed location is row and action is operation.
	 * The data source should
	 * incorporate the data from the dragging pasteboard at this time.
	 */
	public boolean tableViewAcceptDrop( NSTableView tableView, NSDraggingInfo info, int row, int operation) {
	    log.debug("tableViewAcceptDrop:"+row+","+operation);
	// Get the drag-n-drop pasteboard
	    NSPasteboard pasteboard = info.draggingPasteboard();
	// What type of data are we going to allow to be dragged?  The pasteboard
 // might contain different formats
	    NSArray formats = new NSArray(NSPasteboard.FilenamesPboardType);
	    
	// find the best match of the types we'll accept and what's actually on the pasteboard
	// In the file format type that we're working with, get all data on the pasteboard
	    NSArray filesList = (NSArray)pasteboard.propertyListForType(pasteboard.availableTypeFromArray(formats));
	    int i = 0;
	    for(i = 0; i < filesList.count(); i++) {
		log.debug(filesList.objectAtIndex(i));
		String filename = (String)filesList.objectAtIndex(i);
//		    Session session = host.getSession().copy();
		
		    //TODO TODO TODO TODO TODO
//		    Path parent = (Path)pathController.getItem(0);
//		    Path path = parent.copy();
//		    path.setLocal(new java.io.File(filename));
		    //TODO TODO TODO TODO TODO
		
//		    if(session instanceof ch.cyberduck.core.ftp.FTPSession) {
//			path = new FTPPath((FTPSession)session, parent.getAbsolute(), new java.io.File(filename));
//		    }
//		    else if(session instanceof ch.cyberduck.core.sftp.SFTPSession) {
//			path = new SFTPPath((SFTPSession)session, parent.getAbsolute(), new java.io.File(filename));
//		    }
		
		    //TODO TODO TODO TODO TODO
		    //CDTransferController controller = new CDTransferController(host, path, Queue.KIND_UPLOAD);
		    //controller.transfer(path.status.isResume());
		    //TODO TODO TODO TODO TODO
	    }
	    tableView.reloadData();
	    tableView.setNeedsDisplay(true);
// Select the last song.
	    tableView.selectRow(row+i-1, false);
	    return true;
	}

	
	    // ----------------------------------------------------------
    // Drag methods
    // ----------------------------------------------------------
	
	/**    Invoked by tableView after it has been determined that a drag should begin, but before the drag has been started.
	    * The drag image and other drag-related information will be set up and provided by the table view once this call
	    * returns with true.
	    * @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard
	    * (data, owner, and so on).
	    *@param rows is the list of row numbers that will be participating in the drag.
	    */
	public boolean tableViewWriteRowsToPasteboard(NSTableView tableView, NSArray rows, NSPasteboard pboard) {
	    log.debug("tableViewWriteRowsToPasteboard:"+rows);
	    Path p = (Path)this.getEntry(selectedRow());

//	    pboard.declareTypes(new NSArray(NSPasteboard.FilesPromisePboardType), CDBrowserTable.this);
//	    pboard.setStringForType(p.getName(), NSPasteboard.FilesPromisePboardType);

	    NSRect rowRect = tableView.convertRectFromView(tableView.rectOfRow(tableView.selectedRow()), tableView);
	    NSPoint dragPosition = rowRect.origin();	

//	    NSPoint dragPosition = tableView.convertPointFromView(rectOfRow(selectedRow()).origin(), null);
	    NSRect imageRect = new NSRect(new NSPoint(dragPosition.x()-16, dragPosition.y()-16), new NSSize(32, 32));
	    //• 	The typeArray argument is the list of file types being promised. The array elements can consist of file extensions and HFS types encoded with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory of files, only include the top directory in the array.

	    CDBrowserTable.this.dragPromisedFilesOfTypes(new NSArray(p.getExtension()), imageRect, CDBrowserTable.this, false, null);
	    return false;
	}


    // ----------------------------------------------------------
    // Data access
    // ----------------------------------------------------------

	public void clear() {
	    log.debug("clear");
	    this.data.clear();
	}

	public void addEntry(Path entry, int row) {
//	log.debug("addEntry:"+entry);
	    this.data.add(row, entry);
	}

	public void addEntry(Path entry) {
//	log.debug("addEntry:"+entry);
	    if(entry.attributes.isVisible())
		this.data.add(entry);
	}

	public Object getEntry(int row) {
	    return this.data.get(row);
	}

	public void removeEntry(Path o) {
	    data.remove(data.indexOf(o));
	}

	public void removeEntry(int row) {
	    data.remove(row);
	}

	public int indexOf(Path o) {
	    return data.indexOf(o);
	}

	public List list() {
	    return this.data;
	}
    }    
}
