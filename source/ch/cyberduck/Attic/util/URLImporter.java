package ch.cyberduck.util;

/*
 *  ch.cyberduck.util.PreferencesDialog.java
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

import javax.swing.Action;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import ch.cyberduck.connection.Bookmark;

/**
 * Import URLs from any text file
 */
public class URLImporter extends Thread {
    File path;
    Action addBookmarkAction;
    
    public URLImporter(File path, Action addBookmarkAction) {
        this.path = path;
        this.addBookmarkAction = addBookmarkAction;
    }
    
    public void run() {
        FileReader fr = null;
        BufferedReader br = null;
        boolean eof = false;
        int url_hits = 0;
        int line_count = 0;
        try {
            br = new BufferedReader(fr = new FileReader(path));
            while (!eof) {
                String line = br.readLine();
                if (line == null) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Found " + url_hits + " URLs from " + line_count + "\n" +
                        "parsed lines.", 
                        "Import succeeded",
                        JOptionPane.INFORMATION_MESSAGE,
                        null
                    );
                    eof = true;
                }
                else {
                    line_count++;
                    boolean moreURLs = true;
                    URL url;
                    int index = 0;
                    while (moreURLs) {
                        int hit = line.indexOf("http://", index);
                        if (hit == -1) {
                            hit = line.indexOf("ftp://", index);
                        }
                        if(hit == -1)
                            moreURLs = false;
                        else {
                            url_hits++;
                            int end = line.indexOf(' ', hit);
                            if (end == - 1) {
                                url = new URL(line.substring(hit));
                                moreURLs = false;
                            }
                            else {
                                url = new URL(line.substring(hit, end));
                                index = end;
                            }
                            Bookmark bookmark = new Bookmark(url);
                            addBookmarkAction.actionPerformed(new ActionEvent(bookmark, ActionEvent.ACTION_PERFORMED, "Import"));
//                            table.addEntry(bookmark);
                        }
                    } // end of while (moreurls)
                }
            } // end of while(!eof)
	} // end of try
        catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to extract URLs from '" + path.getName() +  "':\n" + e.getMessage(), "Import failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
