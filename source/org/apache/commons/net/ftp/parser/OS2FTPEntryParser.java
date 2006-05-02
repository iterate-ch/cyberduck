package org.apache.commons.net.ftp.parser;

/*
 * Copyright 2001-2005 The Apache Software Foundation
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

import java.util.Calendar;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;

/**
 * Implementation of FTPFileEntryParser and FTPFileListParser for OS2 Systems.
 *
 * @author <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id$
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class OS2FTPEntryParser extends RegexFTPFileEntryParserImpl {
	/**
	 * this is the regular expression used by this parser.
	 */
	private static final String REGEX =
	    "(\\s+|[0-9]+)\\s*"
	    +"(\\s+|[A-Z]+)\\s*"
	    +"(DIR|\\s+)\\s*"
	    +"((?:0[1-9])|(?:1[0-2]))-"
	    +"((?:0[1-9])|(?:[1-2]\\d)|(?:3[0-1]))-"
	    +"(\\d\\d)\\s*"
	    +"(?:([0-1]\\d)|(?:2[0-3])):"
	    +"([0-5]\\d)\\s*"
	    +"(\\S.*)";

	/**
	 * The sole constructor for a OS2FTPEntryParser object.
	 *
	 * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not be seen
	 *                                  under normal conditions.  It it is seen, this is a sign that
	 *                                  <code>REGEX</code> is  not a valid regular expression.
	 */
	public OS2FTPEntryParser() {
		super(REGEX);
	}


	/**
	 * Parses a line of an OS2 FTP server file listing and converts it into a
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
		Path f = PathFactory.createPath(parent.getSession());

		if(matches(entry)) {
			String size = group(1);
			String attrib = group(2);
			String dirString = group(3);
			String mo = group(4);
			String da = group(5);
			String yr = group(6);
			String hr = group(7);
			String min = group(8);
			String name = group(9);
			if(null == name || name.equals("") || name.equals(".") || name.equals("..")) {
				return null;
			}

			//is it a DIR or a file
			if(dirString.trim().equals("DIR") || attrib.trim().equals("DIR")) {
				f.attributes.setType(Path.DIRECTORY_TYPE);
			}
			else {
				f.attributes.setType(Path.FILE_TYPE);
			}

			Calendar cal = Calendar.getInstance();


			//convert all the calendar stuff to ints
			int month = new Integer(mo).intValue()-1;
			int day = new Integer(da).intValue();
			int year = new Integer(yr).intValue()+2000;
			int hour = new Integer(hr).intValue();
			int minutes = new Integer(min).intValue();

			// Y2K stuff? this will break again in 2080 but I will
			// be sooooo dead anyways who cares.
			// SMC - IS OS2's directory date REALLY still not Y2K-compliant?
			if(year > 2080) {
				year -= 100;
			}

			//set the calendar
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, minutes);
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.DATE, day);
			cal.set(Calendar.MONTH, month);
			f.attributes.setTimestamp(cal.getTime().getTime());

			//set the name
			f.setPath(parent.getAbsolute(), name.trim());

			//set the size
			try {
				f.attributes.setSize(Double.parseDouble(size.trim()));
			}
			catch(NumberFormatException e) {
				// intentionally do nothing
			}

			return (f);
		}
		return null;

	}
}
