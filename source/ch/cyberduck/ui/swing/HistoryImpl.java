package ch.cyberduck.ui.swing;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
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

import ch.cyberduck.core.History;
import ch.cyberduck.core.Host;
import org.apache.log4j.Logger;

import java.io.*;

public class HistoryImpl extends History {
    private static Logger log = Logger.getLogger(HistoryImpl.class);
    private static File PREFS_DIRECTORY = new File(System.getProperty("user.home"), ".cyberduck");
    private static final String HISTORY_FILE = "cyberduck.history";

    public void save() {
        log.debug("save");
        FileOutputStream st1 = null;
        ObjectOutputStream st2 = null;
        try {
            st1 = new FileOutputStream(new File(PREFS_DIRECTORY, HISTORY_FILE));
            st2 = new ObjectOutputStream(st1);
            java.util.Iterator iterator = this.iterator();
            while (iterator.hasNext()) {
                st2.writeObject(iterator.next());
            }
        }
        catch(IOException e) {
            log.error("Problem saving transfer history: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            try {
                if (st1 != null)
                    st1.close();
                if (st2 != null)
                    st2.close();
            }
            catch(IOException e) {
                log.error("Problem closing output stream: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void load() {
        log.debug("restore");
//        java.util.List l = new java.util.ArrayList();
        FileInputStream st1 = null;
        ObjectInputStream st2 = null;
        if (PREFS_DIRECTORY.exists()) {
            try {
                st1 = new FileInputStream(new File(PREFS_DIRECTORY, HISTORY_FILE));
                st2 = new ObjectInputStream(st1);
                while(true) {
                    try {
                        Host h = (Host)st2.readObject();
                        this.add(h);
                    }
                    catch(ClassNotFoundException e) {
                        log.error(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            catch(EOFException e) {
                //actually no exception. Just the end of the file.
            }
            catch(IOException e) {
                log.error("Error while reading from file: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                try {
                    if (st1 != null)
                        st1.close();
                    if (st2 != null)
                        st2.close();
                }
                catch(IOException e) {
                    log.error("Error while closing output stream: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
}
