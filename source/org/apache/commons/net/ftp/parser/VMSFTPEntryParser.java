package org.apache.commons.net.ftp.parser;

/*
 * Copyright 2001-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Calendar;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;

/**
 * Implementation FTPFileEntryParser and FTPFileListParser for VMS Systems.
 * This is a sample of VMS LIST output
 * <p/>
 * "1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 * "1-JUN.LIS;2              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 * "DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 * <P><B>
 * Note: VMSFTPEntryParser can only be instantiated through the
 * DefaultFTPParserFactory by classname.  It will not be chosen
 * by the autodetection scheme.
 * </B>
 * <P>
 *
 * @author <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @author <a href="sestegra@free.fr">Stephane ESTE-GRACIAS</a>
 * @version $Id$
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
 */
public class VMSFTPEntryParser extends RegexFTPFileEntryParserImpl {

	private static Logger log = Logger.getLogger(VMSFTPEntryParser.class);

	/**
	 * months abbreviations looked for by this parser.  Also used
	 * to determine <b>which</b> month has been matched by the parser.
	 */
	private static final String MONTHS =
	    "(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)";

	/**
	 * this is the regular expression used by this parser.
	 */
	private static final String REGEX =
	    "(.*;[0-9]+)\\s*"
	    +"(\\d+)/\\d+\\s*"
	    +"(\\d{1,2})-"
	    +MONTHS
	    +"-([0-9]{4})\\s*"
	    +"((?:[01]\\d)|(?:2[0-3])):([012345]\\d):([012345]\\d)\\s*"
	    +"\\[(([0-9$A-Za-z_]+)|([0-9$A-Za-z_]+),([0-9$a-zA-Z_]+))\\]?\\s*"
	    +"\\([a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*\\)";

	/**
	 * Constructor for a VMSFTPEntryParser object.
	 *
	 * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not be seen
	 *                                  under normal conditions.  It it is seen, this is a sign that
	 *                                  <code>REGEX</code> is  not a valid regular expression.
	 */
	public VMSFTPEntryParser() {
		super(REGEX);
	}

	/**
	 * Parses a line of a VMS FTP server file listing and converts it into a
	 * usable format in the form of an <code> FTPFile </code> instance.  If the
	 * file listing line doesn't describe a file, <code> null </code> is
	 * returned, otherwise a <code> FTPFile </code> instance representing the
	 * files in the directory is returned.
	 * <p/>
	 *
	 * @param entry A line of text from the file listing
	 * @return An FTPFile instance corresponding to the supplied entry
	 */
	public Path parseFTPEntry(Path parent, String entry) {
		//one block in VMS equals 512 bytes
		long longBlock = 512;

		if(matches(entry)) {
			Path f = PathFactory.createPath(parent.getSession());
			String name = group(1);
			if(null == name || name.equals("") || name.equals(".") || name.equals("..")) {
				return null;
			}
			String size = group(2);
			String day = group(3);
			String mo = group(4);
			String yr = group(5);
			String hr = group(6);
			String min = group(7);
			//            String sec = group(8);
			//            String owner = group(9);
			/*
						String grp;
						String user;
						StringTokenizer t = new StringTokenizer(owner, ",");
						switch (t.countTokens()) {
							case 1:
								grp  = null;
								user = t.nextToken();
								break;
							case 2:
								grp  = t.nextToken();
								user = t.nextToken();
								break;
							default:
								grp  = null;
								user = null;
						}
			 */

			if(name.lastIndexOf(".DIR") != -1) {
				f.attributes.setType(Path.DIRECTORY_TYPE);
			}
			else {
				f.attributes.setType(Path.FILE_TYPE);
			}
			name = name.substring(0, name.lastIndexOf(";"));
			f.setPath(parent.getAbsolute(), name);
			//size is retreived in blocks and needs to be put in bytes
			//for us humans and added to the FTPFile array
			try {
				Long theSize = new Long(size);
				long sizeInBytes = theSize.longValue()*longBlock;
				f.attributes.setSize(sizeInBytes);
			}
			catch(NumberFormatException e) {
				// intentionally do nothing
			}

			//set the date
			Calendar cal = Calendar.getInstance();
			cal.clear();

			cal.set(Calendar.DATE, new Integer(day).intValue());
			cal.set(Calendar.MONTH, MONTHS.indexOf(mo)/4);
			cal.set(Calendar.YEAR, new Integer(yr).intValue());
			cal.set(Calendar.HOUR_OF_DAY, new Integer(hr).intValue());
			cal.set(Calendar.MINUTE, new Integer(min).intValue());
			//            cal.set(Calendar.SECOND, new Integer(sec).intValue());
			f.attributes.setTimestamp(cal.getTime());

			//            f.attributes.setGroup(grp);
			//            f.attributes.setOwner(user);
			//set group and owner
			//Since I don't need the persmissions on this file (RWED), I'll
			//leave that for further development. 'Cause it will be a bit
			//elaborate to do it right with VMSes World, Global and so forth.
			return f;
		}
		return null;
	}


	/**
	 * Reads the next entry using the supplied BufferedReader object up to
	 * whatever delemits one entry from the next.   This parser cannot use
	 * the default implementation of simply calling BufferedReader.readLine(),
	 * because one entry may span multiple lines.
	 *
	 * @param reader The BufferedReader object from which entries are to be
	 *               read.
	 * @return A string representing the next ftp entry or null if none found.
	 * @throws IOException thrown on any IO Error reading from the reader.
	 */
	public String readNextEntry(BufferedReader reader) throws IOException {
		String line = reader.readLine();
		StringBuffer entry = new StringBuffer();
		while(line != null) {
			if(line.startsWith("Directory") || line.startsWith("Total")) {
				line = reader.readLine();
				continue;
			}

			entry.append(line);
			if(line.trim().endsWith(")")) {
				break;
			}
			line = reader.readLine();
		}
		return (entry.length() == 0 ? null : entry.toString());
	}

	protected boolean isVersioning() {
		return false;
	}
}