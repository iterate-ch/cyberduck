package ch.cyberduck.connection.ftp;

/*
 *  ch.cyberduck.connection.ftp.Parser.java
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

import java.util.*;

import com.enterprisedt.net.ftp.FTPException;

import ch.cyberduck.Preferences;
import ch.cyberduck.connection.Path;
import ch.cyberduck.connection.Permission;

/*
 * @version $Id$
 */
public class FTPParser {

    private FTPParser() {
        super();
    }

    private static final String months[] = {
        "JAN", "FEB", "MAR",
        "APR", "MAY", "JUN",
        "JUL", "AUG", "SEP",
        "OCT", "NOV", "DEC"
    };

    public static List parseList(String parent, String[] list) throws FTPException {
//        Cyberduck.DEBUG("[FTPParser] parseList(" + parent + "," + list + ")");
        List parsedList = new ArrayList();
        boolean showHidden = Preferences.instance().getProperty("ftp.showHidden").equals("true");
        for(int i = 0; i < list.length; i++) {
            int index = 0;
            String line = list[i].trim();
            if(isValidLine(line)) {
                Path p = FTPParser.parseListLine(parent, line);
                String filename = p.getName();
                if(!(filename.equals(".") || filename.equals(".."))) {
                    if(!showHidden) {
                        if(filename.charAt(0) == '.') {
                            p.setVisible(false);
                        }
                    }
                    parsedList.add(p);
                }
            }
        }
        return parsedList;
    }


    /**
        If the file name is a link, it may include a pointer to the original, in which case it is in the form "name -> link"
     */
    public static String parseLink(String link) {
        if(!isValidLink(link)) {
            return null;
        }
        return link.substring(jumpWhiteSpace(link, link.indexOf("->")) + 3).trim();
    }

    public static boolean isFile(String c) {
        return c.charAt(0) == '-';
    }

    public static boolean isLink(String c) {
        return c.charAt(0) == 'l';
    }

    public static boolean isDirectory(String c) {
//        Cyberduck.DEBUG("[FTPParser] isDirectory(" + c + ")");
        return c.charAt(0) == 'd';
    }

    private static Path parseListLine(String parent, String line) throws FTPException {
//        Cyberduck.DEBUG("[FTPParser] parseListLine("+ parent+","+line+")");
        // unix list format never strarts with number
        if("0123456789".indexOf(line.charAt(0)) < 0) {
            return FTPParser.parseUnixListLine(parent, line);
        }
        // windows list format always starts with number
        else {
            return FTPParser.parseWinListLine(parent, line);
        }
    }


    private static Path parseWinListLine(String path, String line) throws FTPException {
//        Cyberduck.DEBUG("[FTPParser] parseWinListLine("+ path+","+line+")");
        
        // 10-16-01  11:35PM                 1479 file
        // 10-16-01  11:37PM       <DIR>          awt  *
        Path p = null;
        try {
            StringTokenizer toker = new StringTokenizer(line);
            long date = parseWinListDate (toker.nextToken(),  toker.nextToken());// time
                String size2dir  = toker.nextToken();  // size or dir
                String access;
                int size = 0;
                if(size2dir.equals("<DIR>")) {
                    access = "d?????????";
                }
                else {
                    access = "-?????????";
                }
                String name = toker.nextToken("").trim();
                String owner = "";
                String group = "";

            if(FTPParser.isDirectory(access) && !(name.charAt(name.length()-1) == '/')) {
                    name = name + "/";
                }
                p = new Path(path, name);
                p.setOwner(owner);
                p.setModified(date);
                p.setMode(access);
                p.setPermission(new Permission(access));
                p.setSize(size);
                return p;
        }
        catch(NumberFormatException e) {
            throw new FTPException("Invalid server response : "+e.getMessage());
        }
        catch(StringIndexOutOfBoundsException e) {
            throw new FTPException("Invalid server response : "+e.getMessage());
        }
    }

    private static long parseWinListDate(String date, String time) throws NumberFormatException {
        //10-16-01    11:35PM
        //10-16-2001  11:35PM
        Calendar c = Calendar.getInstance();
        StringTokenizer toker = new StringTokenizer(date,"-");
        int m = Integer.parseInt(toker.nextToken()),
            d = Integer.parseInt(toker.nextToken()),
            y = Integer.parseInt(toker.nextToken());
        if(y >= 70) y += 1900; else y += 2000;
        toker = new StringTokenizer(time,":APM");
        c.set(y,m,d,(time.endsWith("PM")?12:0)+
              Integer.parseInt(toker.nextToken()),
              Integer.parseInt(toker.nextToken()));
        return c.getTime().getTime();
    }

    private static Path parseUnixListLine(String path, String line) throws FTPException{
//        Cyberduck.DEBUG("[FTPParser] parseUnixListLine("+ path+","+line+")");
        
        //drwxr-xr-x  33 root     wheel       1078 Mar 15 16:18 bin
        //lrwxrwxr-t   1 root     admin         13 Mar 16 13:38 cores -> private/cores
        //dr-xr-xr-x   2 root     wheel        512 Mar 16 02:38 dev
        //lrwxrwxr-t   1 root     admin         11 Mar 16 13:38 etc -> private/etc
        //lrwxrwxr-t   1 root     admin          9 Mar 16 13:38 mach -> /mach.sym
        //-r--r--r--   1 root     admin     563812 Mar 16 02:38 mach.sym
        //-rw-r--r--   1 root     wheel    3156580 Jan 25 07:06 mach_kernel
        //drwxr-xr-x   7 root     wheel        264 Jul 10  2001 private
        //drwxr-xr-x  59 root     wheel       1962 Mar 15 16:18 sbin
        //lrwxrwxr-t   1 root     admin         11 Mar 16 13:38 tmp -> private/tmp
        //drwxr-xr-x  11 root     wheel        330 Jan 31 08:15 usr
        //lrwxrwxr-t   1 root     admin         11 Mar 16 13:38 var -> private/var
        
        Path p = null;
        try {
            String link = null;
            if(isLink(line)) {
                link = parseLink(line);
                line = line.substring(0, line.indexOf("->")).trim();
            }
            StringTokenizer toker = new StringTokenizer(line);
            String access = toker.nextToken();  // access
            toker.nextToken();  // links
            String owner = toker.nextToken();  // owner
            String group = toker.nextToken();  // group
            String size = toker.nextToken();  // size
            if(size.endsWith(","))
                size = size.substring(0,size.indexOf(","));
            String uu = size;
            if(access.startsWith("c"))
                uu = toker.nextToken();             // device
                                                    // if uu.charAt(0) is not digit try uu_file format
            if("0123456789".indexOf(uu.charAt(0)) < 0) {
                size = group;
                group = "";
            }
            long date = parseUnixListDate(("0123456789".indexOf(uu.charAt(0)) < 0 ?uu
                                                                                  :toker.nextToken()), // month
                                          toker.nextToken(),  // day
                                          toker.nextToken()); // time or year
            String name = toker.nextToken("").trim(); // name

	    /*
	     //@ verify
            if(FTPParser.isDirectory(access) && !(name.charAt(name.length()-1) == '/')) {
                name = name + "/";
            }
	     */
            
            p = new Path(path, name);
	    p.setOwner(owner);
            p.setGroup(group);
            p.setModified(date);
            p.setMode(access);
            p.setPermission(new Permission(access));
            p.setSize(Integer.parseInt(size));


	    // @todo implement this in Path.class
	    /*
            if(isLink(line)) {
                // the following lines are the most ugly. I just don't know how I can be sure
                // a link is a directory or a file. Now I look if there's a '.' and if we have execute rights.
                
                //good chance it is a directory if everyone can execute
                boolean execute = p.getPermission().getOtherPermissions()[Permission.EXECUTE];
                boolean point = false;
                if(link.length() >= 4) {
                    if(link.charAt(link.length()-3) == '.' || link.charAt(link.length()-4) == '.')
                        point = true;
                }
                boolean directory = false;
                if(!point && execute)
                    directory = true;

                if(directory) {
                    //Cyberduck.DEBUG("***Parsing link as directory:"+link);
                    if(!(link.charAt(link.length()-1) == '/'))
                        link = link+"/";
                    if(link.charAt(0) == '/')
                        p.setPath(link);
                    else
                        p.setPath(path + link);
                }
                else {
                    //Cyberduck.DEBUG("***Parsing link as file:"+link);
                    if(link.charAt(0) == '/')
                        p.setPath(link);
                    else
                        p.setPath(path + link);
                }
            }
	     */
            return p;
        }
        catch(NoSuchElementException e) {
            throw new FTPException("Invalid server response : "+e.getMessage());
        }
        catch(StringIndexOutOfBoundsException e) {
            throw new FTPException("Invalid server response : "+e.getMessage());
        }
    }


    private static long parseUnixListDate(String month, String day, String year2time) throws NumberFormatException {
        
        // Nov  9  1998
        // Nov 12 13:51
        Calendar c = Calendar.getInstance();
        month = month.toUpperCase();
        for(int m=0;m<12;m++) {
            if(month.equals(months[m])) {
                if(year2time.indexOf(':')!= -1) {
                    // current year
                    c.setTime(new Date(System.currentTimeMillis()));
                    StringTokenizer toker = new StringTokenizer(year2time,":");
                    // date and time
                    c.set(c.get(Calendar.YEAR), m,
                          Integer.parseInt(day),
                          Integer.parseInt(toker.nextToken()),
                          Integer.parseInt(toker.nextToken()));
                }
                else {
                    // date
                    c.set(Integer.parseInt(year2time), m, Integer.parseInt(day),0,0);
                }
                break;
            }
        }
        return c.getTime().getTime();
    }

    // UTILITY METHODS

    private static boolean isValidLink(String link) {
        return link.indexOf("->") != -1;
    }

    private static boolean isValidLine(String l) {
        String line = l.trim();
        if(line.equals("")) {
            return false;
        }
        /* When decoding, it is important to note that many implementations include a line at the start like "total <number>". Clients should ignore any lines that don't match the described format.
            */
        if( line.indexOf("total") != -1 ) {
            try {
                Integer.parseInt(line.substring(line.lastIndexOf(' ') + 1));
                return false;
            }
            catch(NumberFormatException e) {
                // return true // total must be name of real file
            }
        }
        return true;
    }

    private static int jumpWhiteSpace(String line, int index) {
        while(line.substring(index, index + 1).equals(" ")) {
            index++;
        }
        return index;
    }
}
