package ch.cyberduck.ui.dnd;

/*
 *  ch.cyberduck.ui.dnd.URLDropTargetListener.java
 *  Cyberduck
 *
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://dewww.epfl.ch/~dkocher/
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
 *  dkocher@mac.com
 */
 
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.JTable;
import java.awt.event.ActionEvent;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.connection.Bookmark;

public class URLDropTargetListener implements DropTargetListener {

    private Action addBookmarkAction;
    private JTable table;
    
    public URLDropTargetListener(JTable table, Action addBookmarkAction) {
        super();
        this.table = table;
        this.addBookmarkAction = addBookmarkAction;
    }
    
    public synchronized void drop(DropTargetDropEvent e) {
        Cyberduck.DEBUG(e.toString());
        if(e.isLocalTransfer()) {
            Cyberduck.DEBUG("[URLDropTargetListener] isLocalTransfer=true)");
//            e.rejectDrop();
        }
        try {
            Transferable transferable = e.getTransferable();
            if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                Cyberduck.DEBUG("[URLDropTargetListener] DataFlavor.stringFlavor=true)");
                // int row = table.rowAtPoint(e.getLocation());
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                Bookmark b = new Bookmark();
                b.setAddress((String)transferable.getTransferData(DataFlavor.stringFlavor));
                addBookmarkAction.actionPerformed(new ActionEvent(b, ActionEvent.ACTION_PERFORMED, null));
                e.dropComplete(true);
            }
            else {
                Cyberduck.DEBUG("[URLDropTargetListener] DataFlavor.stringFlavor=false)");
                e.rejectDrop();
            }
        }
        catch(java.io.IOException ioe) {
            ioe.printStackTrace();
            e.rejectDrop();
        }
        catch(java.awt.datatransfer.UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
            e.rejectDrop();
        }
    }
    
    public void dragEnter(DropTargetDragEvent e) {
        //@todo paint border around table
        if(e.isDataFlavorSupported(DataFlavor.stringFlavor))
            table.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.red));
//        Cyberduck.DEBUG(e.toString());
    }
    public void dragExit(DropTargetEvent e) {
        table.setBorder(null);
//        Cyberduck.DEBUG(e.toString());
    }
    public void dragOver(DropTargetDragEvent e) {
//        Cyberduck.DEBUG(e.toString());
        int row = table.rowAtPoint(e.getLocation());
        if(row != -1) {
            ((JTable)e.getDropTargetContext().getComponent()).setRowSelectionInterval(row, row);
        }
    }
    public void dropActionChanged(DropTargetDragEvent e) {
        Cyberduck.DEBUG(e.toString());
        if(e.getDropAction() != DnDConstants.ACTION_COPY_OR_MOVE) {
            e.rejectDrag();
        }
    }
}
