package ch.cyberduck.ui.common;

/*
 *  ch.cyberduck.ui.common.DraggableLabel.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;

/**
 * Enhanded JLabel implementing Drag and drop.
 */
public class DraggableLabel extends JLabel implements DragSourceListener, DragGestureListener {
    DragSource dragSource;
    public DraggableLabel(String s, Font font) {
        super (s);
        this.setFont(font);
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(
                                                      this, DnDConstants.ACTION_COPY, this);
    }
    public void dragGestureRecognized(DragGestureEvent e) {
        StringSelection text = new StringSelection(this.getText());
        dragSource.startDrag(
                             e,
                             DragSource.DefaultCopyDrop,
                             text,
                             this);
    }
    public void dragDropEnd (DragSourceDropEvent e) {}
    public void dragEnter (DragSourceDragEvent e) {}
    public void dragExit (DragSourceEvent e) {}
    public void dragOver (DragSourceDragEvent e) {}
    public void dropActionChanged (DragSourceDragEvent e) {}
}
