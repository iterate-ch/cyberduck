package ch.cyberduck.ui.dnd;

/*
 *  ch.cyberduck.ui.dnd.URLDragSourceListener.java
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

import ch.cyberduck.Cyberduck;

public class URLDragSourceListener implements DragSourceListener {
    
    public URLDragSourceListener() {
        super();
    }
    
    public void dragDropEnd(DragSourceDropEvent e) {
        Cyberduck.DEBUG("[URLDragSourceListener] dragDropEnd()");
        if(e.getDropSuccess()) {
            Cyberduck.DEBUG("[URLDragSourceListener] getDropSuccess()=true");
        }
        else {
            Cyberduck.DEBUG("[URLDragSourceListener] getDropSuccess()=false");
        }
    }
    public void dragExit(DragSourceEvent e) {
//        Cyberduck.DEBUG(e.toString());
    }
    public void dragEnter(DragSourceDragEvent e) {
//        Cyberduck.DEBUG(e.toString());
    }
    public void dragOver(DragSourceDragEvent e) {
//        Cyberduck.DEBUG(e.toString());
    }
    public void dropActionChanged(DragSourceDragEvent e) {
//        Cyberduck.DEBUG(e.toString());
    }
}
