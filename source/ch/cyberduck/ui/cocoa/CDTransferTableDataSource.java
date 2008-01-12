package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;

import com.apple.cocoa.application.NSDraggingInfo;
import com.apple.cocoa.application.NSPasteboard;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSDictionary;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Id$
 */
public class CDTransferTableDataSource extends CDController {
    private static Logger log = Logger.getLogger(CDTransferTableDataSource.class);

    public static final String ICON_COLUMN = "ICON";
    public static final String PROGRESS_COLUMN = "PROGRESS";
    // virtual column to implement keyboard selection
    protected static final String TYPEAHEAD_COLUMN = "TYPEAHEAD";

    /**
     *
     */
    private final Map controllers = new HashMap();

    public CDTransferTableDataSource() {
        TransferCollection.instance().addListener(new AbstractCollectionListener() {
            public void collectionItemAdded(Object item) {
                assert item instanceof Transfer;
                controllers.put(item, new CDProgressController((Transfer)item));
            }

            public void collectionItemRemoved(Object item) {
                assert item instanceof Transfer;
                controllers.remove(item);
            }
        });
        TransferCollection.instance().load();
    }

    /**
     *
     */
    private TransferFilter filter = new NullTransferFilter();

    /**
     *
     * @param searchString
     */
    public void setFilter(final String searchString) {
        if(null == searchString || searchString.length() == 0) {
            // Revert to the default filter
            this.filter = new NullTransferFilter();
        }
        else {
            // Setting up a custom filter
            this.filter = new TransferFilter() {
                public boolean accept(Transfer transfer) {
                    // Match for pathnames and hostname
                    return transfer.getName().indexOf(searchString) != -1
                            || transfer.getHost().getHostname().indexOf(searchString) != -1;
                }
            };
        }
    }

    private Collection filter(Collection c) {
        if(null == filter) {
            return c;
        }
        Collection filtered = new Collection(c);
        Transfer t = null;
        for(Iterator i = filtered.iterator(); i.hasNext();) {
            if(!filter.accept(t = (Transfer) i.next())) {
                //temporarly remove the t from the collection
                i.remove();
            }
        }
        return filtered;
    }

    /**
     *
     * @param view
     */
    public int numberOfRowsInTableView(NSTableView view) {
        return this.filter(TransferCollection.instance()).size();
    }

    /**
     *
     * @param view
     * @param tableColumn
     * @param row
     */
    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        if (row < numberOfRowsInTableView(view)) {
            final String identifier = (String) tableColumn.identifier();
            if (identifier.equals(ICON_COLUMN)) {
                return (Transfer) this.filter(TransferCollection.instance()).get(row);
            }
            if (identifier.equals(PROGRESS_COLUMN)) {
                return ((CDProgressController)controllers.get(this.filter(TransferCollection.instance()).get(row)));
            }
            if (identifier.equals(TYPEAHEAD_COLUMN)) {
                return ((Transfer) this.filter(TransferCollection.instance()).get(row)).getName();
            }
            throw new IllegalArgumentException("Unknown identifier: " + identifier);
        }
        return null;
    }

    // ----------------------------------------------------------
    // Drop methods
    // ----------------------------------------------------------

    public int tableViewValidateDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
        log.debug("tableViewValidateDrop:row:" + row + ",operation:" + operation);
        if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.StringPboardType)) != null) {
            tableView.setDropRowAndDropOperation(row, NSTableView.DropAbove);
            return NSDraggingInfo.DragOperationCopy;
        }
        NSPasteboard pboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
        if (pboard.availableTypeFromArray(new NSArray(CDPasteboards.TransferPasteboardType)) != null) {
            tableView.setDropRowAndDropOperation(row, NSTableView.DropAbove);
            return NSDraggingInfo.DragOperationCopy;
        }
        log.debug("tableViewValidateDrop:DragOperationNone");
        return NSDraggingInfo.DragOperationNone;
    }

    /**
     * Invoked by tableView when the mouse button is released over a table view that previously decided to allow a drop.
     *
     * @param info  contains details on this dragging operation.
     * @param index The proposed location is row and action is operation.
     *              The data source should
     *              incorporate the data from the dragging pasteboard at this time.
     */
    public boolean tableViewAcceptDrop(NSTableView tableView, NSDraggingInfo info, int index, int operation) {
        log.debug("tableViewAcceptDrop:row:" + index + ",operation:" + operation);
        int row = index;
        if (row < 0) {
            row = 0;
        }
        if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.StringPboardType)) != null) {
            String droppedText = info.draggingPasteboard().stringForType(NSPasteboard.StringPboardType);// get the data from paste board
            if (droppedText != null) {
                log.info("NSPasteboard.StringPboardType:" + droppedText);
                try {
                    Host h = Host.parse(droppedText);
                    String file = h.getDefaultPath();
                    if (file.length() > 1) {
                        final Transfer q = new DownloadTransfer(
                                PathFactory.createPath(SessionFactory.createSession(h), file)
                        );
                        CDTransferController.instance().startTransfer(q);
                        return true;
                    }
                }
                catch (java.net.MalformedURLException e) {
                    log.error(e.getMessage());
                }
            }
        }
        else {
            // we are only interested in our private pasteboard with a description of the queue
            // encoded in as a xml.
            NSPasteboard pboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
            log.debug("availableTypeFromArray:TransferPasteboardType: " + pboard.availableTypeFromArray(new NSArray(CDPasteboards.TransferPasteboardType)));
            if (pboard.availableTypeFromArray(new NSArray(CDPasteboards.TransferPasteboardType)) != null) {
                Object o = pboard.propertyListForType(CDPasteboards.TransferPasteboardType);// get the data from paste board
                log.debug("tableViewAcceptDrop:" + o);
                if (o != null) {
                    NSArray elements = (NSArray) o;
                    for (int i = 0; i < elements.count(); i++) {
                        NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                        TransferCollection.instance().add(row, TransferFactory.create(dict));
                        tableView.reloadData();
                        tableView.selectRow(row, false);
                        tableView.scrollRowToVisible(row);
                    }
                    pboard.setPropertyListForType(null, CDPasteboards.TransferPasteboardType);
                    return true;
                }
            }
        }
        return false;
    }

    public void setHighlighted(Transfer transfer, boolean highlighted) {
        ((CDProgressController)controllers.get(transfer)).setHighlighted(highlighted);
    }
}