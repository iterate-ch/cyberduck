package ch.cyberduck.core.ftp;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;

import com.enterprisedt.net.ftp.FTPException;

import java.util.*;

/**
 * @version $Id$
 */
public class FTPParser {
	private static FTPParser instance;

	private FTPParser() {
		super();
	}

	public static FTPParser instance() {
		if (null == instance)
			instance = new FTPParser();
		return instance;
	}

	private static final String months[] = {
		"JAN", "FEB", "MAR",
		"APR", "MAY", "JUN",
		"JUL", "AUG", "SEP",
		"OCT", "NOV", "DEC"
	};

	public List parseList(Path parent, String[] list, boolean showHidden) throws FTPException {
		//        log.debug("[FTPParser] parseList(" + parent + "," + list + ")");
		List parsedList = new ArrayList();
		//	    boolean showHidden = Preferences.instance().getProperty("browser.showHidden").equals("true");
		for (int i = 0; i < list.length; i++) {
			int index = 0;
			String line = list[i].trim();
			if (isValidLine(line)) {
				Path p = parseListLine(parent, line);
//				Path p = parseListLine(parent, new String(line.getBytes("ISO-8859-1"), "UTF-8").toString());
				String filename = p.getName();
				if (!(filename.equals(".") || filename.equals(".."))) {
					if (!showHidden && filename.charAt(0) == '.') {
						p.attributes.setVisible(false);
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
	public String parseLink(String link) {
		if (!isValidLink(link)) {
			return null;
		}
		return link.substring(jumpWhiteSpace(link, link.indexOf("->")) + 3).trim();
	}

	public boolean isFile(String c) {
		return c.charAt(0) == '-';
	}

	public boolean isLink(String c) {
		return c.charAt(0) == 'l';
	}

	public boolean isDirectory(String c) {
		//        log.debug("[FTPParser] isDirectory(" + c + ")");
		return c.charAt(0) == 'd';
	}

	private Path parseListLine(Path parent, String line) throws FTPException {
		//        log.debug("[FTPParser] parseListLine("+ parent+","+line+")");
		// unix list format never strarts with number
		if ("0123456789".indexOf(line.charAt(0)) < 0) {
			return parseUnixListLine(parent, line);
		}
		// windows list format always starts with number
		else {
			return parseWinListLine(parent, line);
		}
	}


	private Path parseWinListLine(Path parent, String line) throws FTPException {
		//        log.debug("[FTPParser] parseWinListLine("+ parent+","+line+")");

		// 10-16-01  11:35PM                 1479 file
		// 10-16-01  11:37PM       <DIR>          awt  *
		Path p = null;
		try {
			StringTokenizer toker = new StringTokenizer(line);
			long date = parseWinListDate(toker.nextToken(), toker.nextToken());// time
			String size2dir = toker.nextToken();  // size or dir
			String access;
			int size = 0;
			if (size2dir.equals("<DIR>")) {
				access = "d?????????";
			}
			else {
				access = "-?????????";
			}
			String name = toker.nextToken("").trim();
			String owner = "";
			String group = "";

			if (isDirectory(access) && !(name.charAt(name.length() - 1) == '/')) {
				name = name + "/";
			}
			p = new FTPPath((FTPSession) parent.getSession(), parent.getAbsolute(), name);
			p.attributes.setOwner(owner);
			p.attributes.setModified(date);
//			p.attributes.setMask(access);
			p.attributes.setPermission(new Permission(access));
			p.status.setSize(size);
			return p;
		}
		catch (NumberFormatException e) {
			throw new FTPException("Invalid server response : " + e.getMessage());
		}
		catch (StringIndexOutOfBoundsException e) {
			throw new FTPException("Invalid server response : " + e.getMessage());
		}
	}

	private long parseWinListDate(String date, String time) throws NumberFormatException {
		//10-16-01    11:35PM
		//10-16-2001  11:35PM
		Calendar c = Calendar.getInstance();
		StringTokenizer toker = new StringTokenizer(date, "-");
		int m = Integer.parseInt(toker.nextToken()),
		    d = Integer.parseInt(toker.nextToken()),
		    y = Integer.parseInt(toker.nextToken());
		if (y >= 70) y += 1900; else y += 2000;
		toker = new StringTokenizer(time, ":APM");
		c.set(y, m, d, (time.endsWith("PM") ? 12 : 0) +
		    Integer.parseInt(toker.nextToken()),
		    Integer.parseInt(toker.nextToken()));
		return c.getTime().getTime();
	}

	private Path parseUnixListLine(Path parent, String line) throws FTPException {
		//        log.debug("[FTPParser] parseUnixListLine("+ parent+","+line+")");

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
			if (isLink(line)) {
				link = parseLink(line);
				line = line.substring(0, line.indexOf("->")).trim();
			}
			StringTokenizer toker = new StringTokenizer(line);
			String access = toker.nextToken();  // access
			toker.nextToken();  // links
			String owner = toker.nextToken();  // owner
			String group = toker.nextToken();  // group
			String size = toker.nextToken();  // size
			if (size.endsWith(","))
				size = size.substring(0, size.indexOf(","));
			String uu = size;
			if (access.startsWith("c"))
				uu = toker.nextToken();             // device
			// if uu.charAt(0) is not digit try uu_file format
			if ("0123456789".indexOf(uu.charAt(0)) < 0) {
				size = group;
				group = "";
			}
			long date = parseUnixListDate(("0123456789".indexOf(uu.charAt(0)) < 0 ? uu
			    : toker.nextToken()), // month
			    toker.nextToken(), // day
			    toker.nextToken()); // time or year
			String name = toker.nextToken("").trim(); // name

			p = new FTPPath((FTPSession) parent.getSession(), parent.getAbsolute(), name);
			p.attributes.setOwner(owner);
			p.attributes.setGroup(group);
			p.attributes.setModified(date);
//			p.attributes.setMask(access);
			p.attributes.setPermission(new Permission(access));
			p.status.setSize(Integer.parseInt(size));
			return p;
		}
		catch (NoSuchElementException e) {
			throw new FTPException("Invalid server response : " + e.getMessage());
		}
		catch (StringIndexOutOfBoundsException e) {
			throw new FTPException("Invalid server response : " + e.getMessage());
		}
	}


	private long parseUnixListDate(String month, String day, String year2time) throws NumberFormatException {

		// Nov  9  1998
		// Nov 12 13:51
		Calendar c = Calendar.getInstance();
		month = month.toUpperCase();
		for (int m = 0; m < 12; m++) {
			if (month.equals(months[m])) {
				if (year2time.indexOf(':') != -1) {
					// current year
					c.setTime(new Date(System.currentTimeMillis()));
					StringTokenizer toker = new StringTokenizer(year2time, ":");
					// date and time
					c.set(c.get(Calendar.YEAR), m,
					    Integer.parseInt(day),
					    Integer.parseInt(toker.nextToken()),
					    Integer.parseInt(toker.nextToken()));
				}
				else {
					// date
					c.set(Integer.parseInt(year2time), m, Integer.parseInt(day), 0, 0);
				}
				break;
			}
		}
		return c.getTime().getTime();
	}

	// UTILITY METHODS

	private boolean isValidLink(String link) {
		return link.indexOf("->") != -1;
	}

	private boolean isValidLine(String l) {
		String line = l.trim();
		if (line.equals("")) {
			return false;
		}
		/* When decoding, it is important to note that many implementations include a line at the start like "total <number>". Clients should ignore any lines that don't match the described format.
			*/
		if (line.indexOf("total") != -1) {
			try {
				Integer.parseInt(line.substring(line.lastIndexOf(' ') + 1));
				return false;
			}
			catch (NumberFormatException e) {
				// return true // total must be name of real file
			}
		}
		return true;
	}

	private int jumpWhiteSpace(String line, int index) {
		while (line.substring(index, index + 1).equals(" ")) {
			index++;
		}
		return index;
	}
}
