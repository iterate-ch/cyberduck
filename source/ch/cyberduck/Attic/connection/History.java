package ch.cyberduck.connection;

/*
 *  ch.cyberduck.connection.History.java
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



/**
 * Not yet implemented
 * @version $Id$
 */
public class History extends java.util.ArrayList {

    public History() {
        //@todo impl
    }

    /*
    public void save() {
        Cyberduck.DEBUG("[History] save()");
        FileOutputStream st1 = null;
        ObjectOutputStream st2 = null;
        try {
            st1 = new FileOutputStream(new File(Cyberduck.PREFS_DIRECTORY, Cyberduck.HISTORY_FILE));
            st2 = new ObjectOutputStream(st1);
            java.util.Iterator iterator = this.iterator();
            while (iterator.hasNext()) {
                st2.writeObject(iterator.next());
            }
        }
        catch(IOException e) {
            System.err.println("[History] Problem saving transfer history: " + e.getMessage());
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
                System.err.println("[History] Problem closing output stream: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    */

    /*
    public static java.util.List restore() {
        Cyberduck.DEBUG("[History] restore()");
        java.util.List l = new java.util.ArrayList();
        FileInputStream st1 = null;
        ObjectInputStream st2 = null;
        File path = new File(Cyberduck.PREFS_DIRECTORY, Cyberduck.HISTORY_FILE);
        if (path.exists()) {
            try {
                st1 = new FileInputStream(path);
                st2 = new ObjectInputStream(st1);
                while(true) {
                    try {
                        Bookmark bookmark = (Bookmark)st2.readObject();
                        l.add(bookmark);
                    }
                    catch(ClassNotFoundException e) {
                        System.err.println("[History] " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            catch(EOFException e) {
                //actually no error. Just the end of the file.
            }
            catch(IOException e) {
                System.err.println("[History] Error while reading from '" + Cyberduck.TABLE_FILE + "':  " + e.getMessage());
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
                    System.err.println("[History] Error while closing output stream: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return l;
    }
     */
}
