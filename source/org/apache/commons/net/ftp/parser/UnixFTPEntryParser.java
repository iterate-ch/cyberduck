package org.apache.commons.net.ftp.parser;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.Calendar;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.PathFactory;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

import org.apache.log4j.Logger;

/**
 * Implementation FTPFileEntryParser and FTPFileListParser for standard 
 * Unix Systems.
 * 
 * This class is based on the logic of Daniel Savarese's
 * DefaultFTPListParser, but adapted to use regular expressions and to fit the
 * new FTPFileEntryParser interface.
 * @author <a href="mailto:scohen@ignitesports.com">Steve Cohen</a>
 * @version $Id$
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class UnixFTPEntryParser extends FTPFileEntryParserImpl 
{

	private static Logger log = Logger.getLogger(UnixFTPEntryParser.class);

	/**
     * months abbreviations looked for by this parser.  Also used
     * to determine which month is matched by the parser
     */
    private static final String MONTHS =
        "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
    
    /**
     * this is the regular expression used by this parser.
     */
    private static final String REGEX =
        "([bcdlf-])"
        + "(((r|-)(w|-)(x|-))((r|-)(w|-)(x|-))((r|-)(w|-)(x|-)))\\s+"
        + "(\\d+)\\s+"
        + "(\\S+)\\s+"
        + "(?:(\\S+)\\s+)?"
        + "(\\d+)\\s+"
        + MONTHS + "\\s+"
        + "((?:[0-9])|(?:[0-2][0-9])|(?:3[0-1]))\\s+"
        + "((\\d\\d\\d\\d)|((?:[01]\\d)|(?:2[0123])):([012345]\\d))\\s"
        + "(\\S+)(\\s*.*)";

    
    /**
     * The sole constructor for a UnixFTPEntryParser object.
     * 
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen 
     * under normal conditions.  It it is seen, this is a sign that 
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public UnixFTPEntryParser() 
    {
        super(REGEX);
    }

    /**
     * Parses a line of a unix (standard) FTP server file listing and converts 
     * it into a usable format in the form of an <code> FTPFile </code> 
     * instance.  If the file listing line doesn't describe a file, 
     * <code> null </code> is returned, otherwise a <code> FTPFile </code> 
     * instance representing the files in the directory is returned.
     * <p>
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public Path parseFTPEntry(Path parent, String entry)
    {

		Path f = PathFactory.createPath(parent.getSession());
        int type;
        boolean isDevice = false;

        if (matches(entry))
        {
			log.debug(group(0));
            String typeStr = group(1);
			String permStr = group(2);
            String hardLinkCount = group(15);
            String usr = group(16);
            String grp = group(17);
            String filesize = group(18);
            String mo = group(19);
            String da = group(20);
            String yr = group(22);
            String hr = group(23);
            String min = group(24);
            String name = group(25);
			log.debug("name:"+name);
            String endtoken = group(26);

            switch (typeStr.charAt(0))
            {
				case 'd':
					type = Path.DIRECTORY_TYPE;
					break;
				case 'l':
					type = Path.SYMBOLIC_LINK_TYPE;
					break;
				case 'b':
				case 'c':
					isDevice = true;
					// break; - fall through
				default:
					type = Path.FILE_TYPE;
            }
			
			f.attributes.setType(type);
			f.attributes.setPermission(new Permission(typeStr+permStr));

//			int g = 4;
//          for (int access = 0; access < 3; access++, g += 4)
//            {
//              // Use != '-' to avoid having to check for suid and sticky bits
//                f.setPermission(access, FTPFile.READ_PERMISSION,
//                                   (!group(g).equals("-")));
//                f.setPermission(access, FTPFile.WRITE_PERMISSION,
//                                   (!group(g + 1).equals("-")));
//                f.setPermission(access, FTPFile.EXECUTE_PERMISSION,
//                                   (!group(g + 2).equals("-")));
//			}
//
//            if (!isDevice)
//            {
//                try
//                {
//                    f.setHardLinkCount(Integer.parseInt(hardLinkCount));
//                }
//              catch (NumberFormatException e)
//                {
//                    // intentionally do nothing
//                }
//            }

            f.attributes.setOwner(usr);
            f.attributes.setGroup(grp);

            try
            {
				f.status.setSize(Integer.parseInt(filesize));
            }
            catch (NumberFormatException e)
            {
                // intentionally do nothing
            }

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);

            try
            {
                int pos = MONTHS.indexOf(mo);
                int month = pos / 4;

                if (null != yr)
                {
                    // it's a year
                    cal.set(Calendar.YEAR, Integer.parseInt(yr));
                }
                else
                {
                    // it must be  hour/minute or we wouldn't have matched
                    int year = cal.get(Calendar.YEAR);
                    // if the month we're reading is greater than now, it must
                    // be last year
                    if (cal.get(Calendar.MONTH) < month)
                    {
                        year--;
                    }
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hr));
                    cal.set(Calendar.MINUTE, Integer.parseInt(min));
                }
                cal.set(Calendar.MONTH, month);

                cal.set(Calendar.DATE, Integer.parseInt(da));
                f.attributes.setTimestamp(cal.getTime());
            }
            catch (NumberFormatException e)
            {
                // do nothing, date will be uninitialized
            }
            if (null == endtoken)
            {
                f.setPath(parent.getAbsolute(), name);
            }
            else
            {
                // oddball cases like symbolic links, file names
                // with spaces in them.
                name += endtoken;
                if (Path.SYMBOLIC_LINK_TYPE == type)
                {
                    int end = name.indexOf(" -> ");
                    // Give up if no link indicator is present
                    if (end == -1)
                    {
                        f.setPath(parent.getAbsolute(), name);
                    }
                    else
                    {
                        f.setPath(parent.getAbsolute(), name.substring(0, end));
//                        f.setPath(name.substring(end + 4));
                    }

                }
                else
                {
                    f.setPath(parent.getAbsolute(), name);
                }
            }
            return f;
        }
        return null;
    }
}
