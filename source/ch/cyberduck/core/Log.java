package ch.cyberduck.core;

/*
 *  ch.cyberduck.core.Log.java
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

import java.io.*;

import ch.cyberduck.core.Preferences;
import org.apache.log4j.Logger;


/**
 * I am the logger of a connection.
 * @version $Id$
 */
public class Log {

    private static Logger log = Logger.getLogger(Log.class);

    StringBuffer buffer;

    public Log () {
        log.debug("[Log] new Log()");
        buffer = new StringBuffer();
    }
    
    public void append(String text) {
//        log.debug("[Log] append(" + text + ")");
        buffer.append(text + System.getProperty("line.separator"));
    }
    
    public void save() {
        log.debug("[Log] save()");
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            File prefFile = new File(Preferences.PREFS_DIRECTORY, Preferences.instance().getProperty("connection.log.file"));
            bw = new BufferedWriter(fw = new FileWriter(prefFile.toString(), true));
            String logtext = buffer.toString();
            if (logtext != null) {
//                bw.write("////////////////////////////////////////////////");
                bw.newLine();
//                bw.write(Cyberduck.SEPARATOR + logtext + Cyberduck.SEPARATOR);
                bw.write(logtext);
                bw.newLine();
                log.debug("[Log] " + logtext);
            }
            bw.flush();
        }
        catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            try {
                if(fw != null)
                    fw.close();
                if(bw != null)
                    bw.close();
            }
            catch(IOException close) {
                System.err.println("[Log] Error: " + close.getMessage());
                close.printStackTrace();
            }
        }
    }
    
    public static String open() {
        log.debug("[Log] open()");
	StringBuffer logtext = new StringBuffer();
	File path = new File(Preferences.PREFS_DIRECTORY, Preferences.instance().getProperty("connection.log.file"));
	if(path.exists()) {
            try {
                FileReader fr = new FileReader(path);
                BufferedReader br = new BufferedReader(fr);
                boolean eof = false;
                while(!eof) {
                    String line = br.readLine();
                    if (line == null) {
                        eof = true;
                    }
                    else {
                        logtext.append(line + System.getProperty("line.separator"));
                    }
                }
            }
            catch (IOException e) {
                System.err.println("[Log] Could not open log file." + e.getMessage());
            }
        }
	return logtext.toString();
    }
    
    public static void delete() {
        log.debug("[Log] delete()");
        try {
	    File path = new File(Preferences.PREFS_DIRECTORY, Preferences.instance().getProperty("connection.log.file"));
	    if (path.exists())
		path.delete();
        }
        catch(SecurityException e) {
            System.err.println("[Log] Could not delete log file: " + e.getMessage());
        }
    }
}
