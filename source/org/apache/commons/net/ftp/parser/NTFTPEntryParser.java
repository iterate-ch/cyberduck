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
//import org.apache.commons.net.ftp.FTPFile;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.ftp.FTPPath;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

/**
 * Implementation of FTPFileEntryParser and FTPFileListParser for NT Systems.
 * 
 * @author  <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id$
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class NTFTPEntryParser extends FTPFileEntryParserImpl
{
    /**
     * this is the regular expression used by this parser.
     */
    private static final String REGEX =
        "((?:0[1-9])|(?:1[0-2]))-" 
        + "((?:0[1-9])|(?:[1-2]\\d)|(?:3[0-1]))-" 
        + "(\\d\\d)\\s*" 
        + "((?:0[1-9])|(?:1[012])):" 
        + "([0-5]\\d)\\s*" 
        + "([AP])M\\s*" 
        + "(<DIR>)?\\s*" 
        + "([0-9]+)?\\s+" 
        + "(\\S.*)";

    /**
     * The sole constructor for an NTFTPEntryParser object.
     * 
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen 
     * under normal conditions.  It it is seen, this is a sign that 
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public NTFTPEntryParser()
    {
        super(REGEX);
    }


    /**
     * Parses a line of an NT FTP server file listing and converts it into a
     * usable format in the form of an <code> FTPFile </code> instance.  If the
     * file listing line doesn't describe a file, <code> null </code> is
     * returned, otherwise a <code> FTPFile </code> instance representing the
     * files in the directory is returned.
     * <p>
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public Path parseFTPEntry(Path parent, String entry)
    {
		Path f = PathFactory.createPath(parent.getSession());
        
        if (matches(entry))
        {
            String mo = group(1);
            String da = group(2);
            String yr = group(3);
            String hr = group(4);
            String min = group(5);
            String ampm = group(6);
            String dirString = group(7);
            String size = group(8);
            String name = group(9);
            if (null == name || name.equals(".") || name.equals(".."))
            {
                return (null);
            }
            f.setPath(parent.getAbsolute(), name);
            //convert all the calendar stuff to ints
            int month = new Integer(mo).intValue() - 1;
            int day = new Integer(da).intValue();
            int year = new Integer(yr).intValue() + 2000;
            int hour = new Integer(hr).intValue();
            int minutes = new Integer(min).intValue();

            // Y2K stuff? this will break again in 2080 but I will
            // be sooooo dead anyways who cares.
            // SMC - IS NT's directory date REALLY still not Y2K-compliant?
            if (year > 2080)
            {
                year -= 100;
            }

            Calendar cal = Calendar.getInstance();
            //set the calendar
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, minutes);
            cal.set(Calendar.HOUR, hour);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DATE, day);
            cal.set(Calendar.MONTH, month);
            int ap = Calendar.AM;
            if ("P".equals(ampm))
            {
                ap = Calendar.PM;
            }
            cal.set(Calendar.AM_PM, ap);
            f.attributes.setTimestamp(cal.getTime());

            if ("<DIR>".equals(dirString))
            {
                f.attributes.setType(Path.DIRECTORY_TYPE);
                f.status.setSize(0);
            }
            else
            {
				f.attributes.setType(Path.FILE_TYPE);
                if (null != size)
                {
					f.status.setSize(Long.parseLong(size));
                }
            }
            return (f);
        }
        return null;
    }
}
