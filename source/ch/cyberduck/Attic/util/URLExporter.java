package ch.cyberduck.util;

/*
 *  ch.cyberduck.util.Export.java
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
 
import javax.swing.JOptionPane;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.ui.model.BookmarkTableModel;

/**
 * Export bookmarks as tab delimited text file
 */
public class URLExporter extends Thread {
    File file;
    BookmarkTableModel tableModel;
    
    public URLExporter(File file, BookmarkTableModel tableModel) {
        Cyberduck.DEBUG("[URLExporter] URLExporter(" + file.toString() + ")");
        this.file = file;
        this.tableModel = tableModel;
    }
    
    public void run() {
        Cyberduck.DEBUG("[URLExporter] exportAll(" + tableModel.toString() + ")");
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(fw = new FileWriter(file));
            Iterator iterator = tableModel.iterator();
            Bookmark bookmark = null;
//            bw.write("Host\tPort\tPath\tAddress");
            while(iterator.hasNext()) {
                bookmark = (Bookmark)iterator.next();
                /*
                bw.write(bookmark.getHost());
                bw.write("\t");
                bw.write(bookmark.getPort());
                bw.write("\t");
                if(bookmark.isDownload()) {
                    bw.write(bookmark.getLocalFilename());
                }
                else if(bookmark.isDownload()) {
                    bw.write(bookmark.getServerDirectoryAsString());
                }
                bw.write("\t");
                 */
                bw.write(bookmark.getAddressAsString());
                bw.newLine();
            }
            bw.flush();
        }
        catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to export table content\n" + "to file " + file.toString() + ".\n" + e.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
        }
        finally {
            try {
                if(bw != null)
                    bw.close();
                if(fw != null)
                    fw.close();
            }
            catch(IOException close) {
                System.err.println("[Log] Error: " + close.getMessage());
                close.printStackTrace();
            }
        }
    }
}
